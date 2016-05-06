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

final class JsCheckerSecondPass extends AbstractShallowCallback implements HotSwapCompilerPass {

  public static final DiagnosticType NOT_PROVIDED =
      DiagnosticType.error(
          "CR_NOT_PROVIDED", "Namespace not provided by any srcs or direct deps of {0}");

  private final JsCheckerState state;
  private final AbstractCompiler compiler;

  JsCheckerSecondPass(JsCheckerState state, AbstractCompiler compiler) {
    this.state = state;
    this.compiler = compiler;
  }

  @Override
  public void process(Node externs, Node root) {
    NodeTraversal.traverseEs6(compiler, root, this);
  }

  @Override
  public void hotSwapScript(Node scriptRoot, Node originalRoot) {
    NodeTraversal.traverseEs6(compiler, scriptRoot, this);
  }

  @Override
  public void visit(NodeTraversal t, Node n, Node parent) {
    switch (n.getType()) {
      case Token.CALL:
        Node callee = n.getFirstChild();
        Node namespace = n.getLastChild();
        if (!namespace.isString()) {
          return;
        }
        if (callee.matchesQualifiedName("goog.require")) {
          if (!state.provided.contains(namespace.getString())
              && !state.provides.contains(namespace.getString())
              && state.notProvidedNamespaces.add(namespace.getString())) {
            t.report(namespace, NOT_PROVIDED, state.label);
          }
        }
        break;
      default:
        break;
    }
  }
}
