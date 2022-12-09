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

import com.google.javascript.jscomp.NodeTraversal.AbstractShallowCallback;
import com.google.javascript.rhino.Node;
import com.google.javascript.rhino.Token;

final class JsCheckerFirstPass extends AbstractShallowCallback implements HotSwapCompilerPass {

  public static final DiagnosticType INVALID_SETTESTONLY =
      DiagnosticType.error(
          "CR_INVALID_SETTESTONLY",
          "Not allowed here because {0} does not have testonly=1.");

  public static final DiagnosticType DUPLICATE_PROVIDES =
      DiagnosticType.error(
          "CR_DUPLICATE_PROVIDES", "Namespace provided multiple times by srcs of {0}.");

  public static final DiagnosticType REDECLARED_PROVIDES =
      DiagnosticType.error("CR_REDECLARED_PROVIDES", "Namespace already provided by deps of {0}.");

  private final JsCheckerState state;
  private final AbstractCompiler compiler;

  JsCheckerFirstPass(JsCheckerState state, AbstractCompiler compiler) {
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

  @Override
  public final void visit(NodeTraversal t, Node n, Node parent) {
    switch (n.getType()) {
      case Token.CALL:
        Node callee = n.getFirstChild();
        if (!state.testonly && callee.matchesQualifiedName("goog.setTestOnly")) {
          t.report(n, INVALID_SETTESTONLY, state.label);
          return;
        }
        Node namespace = n.getLastChild();
        if (namespace.isString()
            && (callee.matchesQualifiedName("goog.provide")
                || callee.matchesQualifiedName("goog.module"))) {
          if (!state.provides.add(namespace.getString())) {
            t.report(namespace, DUPLICATE_PROVIDES, state.label);
          }
          if (state.provided.contains(namespace.getString())
              && state.redeclaredProvides.add(namespace.getString())) {
            t.report(namespace, REDECLARED_PROVIDES, state.label);
          }
          return;
        }
        break;
      default:
        break;
    }
  }
}
