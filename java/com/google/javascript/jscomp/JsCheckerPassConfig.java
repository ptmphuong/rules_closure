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

import com.google.common.collect.ImmutableList;
import com.google.javascript.jscomp.NodeTraversal.Callback;
import com.google.javascript.jscomp.lint.CheckDuplicateCase;
import com.google.javascript.jscomp.lint.CheckEmptyStatements;
import com.google.javascript.jscomp.lint.CheckEnums;
import com.google.javascript.jscomp.lint.CheckInterfaces;
import com.google.javascript.jscomp.lint.CheckJSDocStyle;
import com.google.javascript.jscomp.lint.CheckPrototypeProperties;
import com.google.javascript.jscomp.lint.CheckRequiresAndProvidesSorted;
import com.google.javascript.jscomp.lint.CheckUselessBlocks;

import java.util.List;

final class JsCheckerPassConfig extends PassConfig.PassConfigDelegate {

  private final JsCheckerState state;

  JsCheckerPassConfig(JsCheckerState state, CompilerOptions options) {
    super(new DefaultPassConfig(options));
    this.state = state;
  }

  @Override
  protected List<PassFactory> getChecks() {
    return ImmutableList.of(
        earlyLintChecks,
        closureGoogScopeAliases,
        closureRewriteClass,
        lateLintChecks,
        checkRequires);
  }

  @Override
  protected List<PassFactory> getOptimizations() {
    return ImmutableList.of();
  }

  private final PassFactory earlyLintChecks =
      new PassFactory("earlyLintChecks", true) {
        @Override
        protected CompilerPass create(AbstractCompiler compiler) {
          return new CombinedCompilerPass(
              compiler,
              ImmutableList.<Callback>of(
                  new CheckDuplicateCase(compiler),
                  new CheckEmptyStatements(compiler),
                  new CheckEnums(compiler),
                  new CheckJSDocStyle(compiler),
                  new CheckJSDoc(compiler),
                  new CheckRequiresAndProvidesSorted(compiler),
                  new CheckUselessBlocks(compiler),
                  new ClosureCheckModule(compiler),
                  new JsCheckerFirstPass(state, compiler)));
        }
      };

  private final PassFactory closureGoogScopeAliases =
      new PassFactory("closureGoogScopeAliases", true) {
        @Override
        protected HotSwapCompilerPass create(AbstractCompiler compiler) {
          return new ScopedAliases(compiler, null, options.getAliasTransformationHandler());
        }
      };

  private final PassFactory closureRewriteClass =
      new PassFactory("closureRewriteClass", true) {
        @Override
        protected HotSwapCompilerPass create(AbstractCompiler compiler) {
          return new ClosureRewriteClass(compiler);
        }
      };

  private final PassFactory lateLintChecks =
      new PassFactory("lateLintChecks", true) {
        @Override
        protected CompilerPass create(AbstractCompiler compiler) {
          return new CombinedCompilerPass(
              compiler,
              ImmutableList.<Callback>of(
                  new CheckInterfaces(compiler),
                  new CheckPrototypeProperties(compiler),
                  new JsCheckerSecondPass(state, compiler)));
        }
      };

  // This cannot be part of lintChecks because the callbacks in the CombinedCompilerPass don't
  // get access to the externs.
  private final PassFactory checkRequires =
      new PassFactory("checkRequires", true) {
        @Override
        protected CompilerPass create(AbstractCompiler compiler) {
          return new CheckRequiresForConstructors(
              compiler, CheckRequiresForConstructors.Mode.SINGLE_FILE);
        }
      };
}
