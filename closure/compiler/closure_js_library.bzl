# -*- mode: python; -*-
#
# Copyright 2016 The Closure Rules Authors. All rights reserved.
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
#    http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.

"""Build definitions for Closure JavaScript libraries.
"""

load("//closure/private:defs.bzl",
     "JS_LANGUAGE_DEFAULT",
     "JS_DEPS_ATTR",
     "JS_FILE_TYPE",
     "collect_js_srcs",
     "determine_js_language")

def _determine_check_language(language):
  if language == "ANY":
    return "ECMASCRIPT3"
  return language

def _impl(ctx):
  srcs, externs = collect_js_srcs(ctx)
  if ctx.files.exports:
    for forbid in ['srcs', 'externs', 'deps']:
      if getattr(ctx.files, forbid):
        fail("'exports' may not be specified when '%s' is set" % forbid)
  else:
    if not ctx.files.srcs and not ctx.files.externs:
      fail("Either 'srcs' or 'externs' must be specified")
    if ctx.files.srcs and ctx.files.externs:
      fail("'srcs' may not be specified when 'externs' is set")
  inputs = []
  args = ["--output=%s" % ctx.outputs.provided.path,
          "--label=%s" % ctx.label,
          "--language=%s" % _determine_check_language(ctx.attr.language)]
  if ctx.attr.testonly:
    args += ["--testonly"]
  for direct_src in ctx.files.srcs:
    args += ["--src=%s" % direct_src.path]
    inputs.append(direct_src)
  for direct_extern in ctx.files.externs:
    args += ["--extern=%s" % direct_extern.path]
    inputs.append(direct_extern)
  for direct_dep in ctx.attr.deps:
    args += ["--dep=%s" % direct_dep.js_provided.path]
    inputs.append(direct_dep.js_provided)
    for edep in direct_dep.js_exports:
      args += ["--dep=%s" % edep.js_provided.path]
      inputs.append(edep.js_provided)
  args += ["--jscomp_off=%s" % s for s in ctx.attr.suppress]
  ctx.action(
      inputs=inputs,
      outputs=[ctx.outputs.provided],
      executable=ctx.executable._jschecker,
      arguments=args,
      mnemonic="JSChecker",
      progress_message="Checking %d JS files in %s" % (
          len(ctx.files.srcs) + len(ctx.files.externs), ctx.label))
  return struct(files=set(ctx.files.srcs),
                js_language=determine_js_language(ctx),
                js_exports=ctx.attr.exports,
                js_provided=ctx.outputs.provided,
                transitive_js_srcs=srcs,
                transitive_js_externs=externs,
                runfiles=ctx.runfiles(files=[ctx.outputs.provided]))

closure_js_library = rule(
    implementation=_impl,
    attrs={
        "srcs": attr.label_list(allow_files=JS_FILE_TYPE),
        "externs": attr.label_list(allow_files=JS_FILE_TYPE),
        "deps": JS_DEPS_ATTR,
        "exports": JS_DEPS_ATTR,
        "language": attr.string(default=JS_LANGUAGE_DEFAULT),
        "suppress": attr.string_list(),
        "_jschecker": attr.label(
            default=Label("//java/com/google/javascript/jscomp:jschecker"),
            executable=True),
    },
    outputs={"provided": "%{name}-provided.txt"})
