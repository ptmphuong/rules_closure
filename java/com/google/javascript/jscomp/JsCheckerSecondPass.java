package com.google.javascript.jscomp;

import com.google.javascript.jscomp.NodeTraversal.AbstractShallowCallback;
import com.google.javascript.rhino.Node;
import com.google.javascript.rhino.Token;

final class JsCheckerSecondPass extends AbstractShallowCallback implements HotSwapCompilerPass {

  public static final DiagnosticType NOT_PROVIDED =
      DiagnosticType.error(
          "CR_NOT_PROVIDED", "Namespace not provided by any srcs or deps of {0}");

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
          state.provides.add(namespace.getString());
        }
        break;
      default:
        break;
    }
  }
}
