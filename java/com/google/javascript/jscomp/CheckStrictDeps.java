/*
 * Copyright 2016 The Closure Rules Authors. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.google.javascript.jscomp;

import static com.google.javascript.jscomp.JsCheckerHelper.convertPathToModuleName;

import com.google.javascript.jscomp.NodeTraversal.AbstractShallowCallback;
import com.google.javascript.rhino.Node;
import com.google.javascript.rhino.Token;

abstract class CheckStrictDeps
    extends AbstractShallowCallback implements HotSwapCompilerPass {

  public static final DiagnosticType DUPLICATE_PROVIDES =
      DiagnosticType.error(
          "CR_DUPLICATE_PROVIDES", "Namespace provided multiple times by srcs of {0}.");

  public static final DiagnosticType NOT_PROVIDED =
      DiagnosticType.error(
          "CR_NOT_PROVIDED", "Namespace not provided by any srcs or direct deps of {0}.");

  public static final DiagnosticType REDECLARED_PROVIDES =
      DiagnosticType.error("CR_REDECLARED_PROVIDES", "Namespace already provided by deps of {0}.");

  final JsCheckerState state;
  final AbstractCompiler compiler;

  private CheckStrictDeps(JsCheckerState state, AbstractCompiler compiler) {
    this.state = state;
    this.compiler = compiler;
  }

  @Override
  public final void process(Node externs, Node root) {
    NodeTraversal.traverseEs6(compiler, root, this);
  }

  @Override
  public final void hotSwapScript(Node scriptRoot, Node originalRoot) {
    NodeTraversal.traverseEs6(compiler, scriptRoot, this);
  }

  static final class FirstPass extends CheckStrictDeps {

    FirstPass(JsCheckerState state, AbstractCompiler compiler) {
      super(state, compiler);
    }

    @Override
    public final void visit(NodeTraversal t, Node n, Node parent) {
      if (n.getType() != Token.CALL) {
        return;
      }
      Node callee = n.getFirstChild();
      Node parameter = n.getLastChild();
      if (parameter.isString()
          && (callee.matchesQualifiedName("goog.provide")
              || callee.matchesQualifiedName("goog.module"))) {
        String namespace = JsCheckerHelper.normalizeClosureNamespace(parameter.getString());
        if (!state.provides.add(namespace)) {
          t.report(parameter, DUPLICATE_PROVIDES, state.label);
        }
        if (state.provided.contains(namespace)
            && state.redeclaredProvides.add(namespace)) {
          t.report(parameter, REDECLARED_PROVIDES, state.label);
        }
        state.provides.removeAll(convertPathToModuleName(t.getSourceName(), state.roots).asSet());
      }
    }
  }

  static final class SecondPass extends CheckStrictDeps {

    SecondPass(JsCheckerState state, AbstractCompiler compiler) {
      super(state, compiler);
    }

    @Override
    public void visit(NodeTraversal t, Node n, Node parent) {
      switch (n.getType()) {
        case CALL:
          visitFunctionCall(t, n);
          break;
        case IMPORT:
          visitEs6Import(t, n);
          break;
        default:
          break;
      }
    }

    private void visitFunctionCall(NodeTraversal t, Node n) {
      Node callee = n.getFirstChild();
      Node parameter = n.getLastChild();
      if (!parameter.isString()) {
        return;
      }
      if (callee.matchesQualifiedName("goog.require")) {
        checkNamespaceIsProvided(t, parameter,
            JsCheckerHelper.normalizeClosureNamespace(parameter.getString()));
      }
    }

    private void visitEs6Import(NodeTraversal t, Node n) {
      Node namespace = n.getChildAtIndex(2);
      if (!namespace.isString()) {
        return;
      }
      checkNamespaceIsProvided(t, namespace, namespace.getString());
    }

    private void checkNamespaceIsProvided(NodeTraversal t, Node n, String namespace) {
      if (!state.provided.contains(namespace)
          && !state.provides.contains(namespace)
          && state.notProvidedNamespaces.add(namespace)) {
        t.report(n, NOT_PROVIDED, state.label);
      }
    }
  }
}
