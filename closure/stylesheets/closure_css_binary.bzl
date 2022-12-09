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

"""Build definitions for CSS compiled by the Closure Stylesheets.
"""

load("//closure/private:defs.bzl",
     "CSS_DEPS_ATTR",
     "collect_transitive_css_labels")

def _impl(ctx):
  if not ctx.attr.deps:
    fail("closure_css_binary rules can not have an empty 'deps' list")
  srcs = set()
  tdata = set()
  for dep in ctx.attr.deps:
    srcs += dep.transitive_css_srcs
    tdata += dep.transitive_data
  files = [ctx.outputs.out, ctx.outputs.map]
  input_orientation = _get_input_orientation(ctx.attr.deps)
  args = ["--output-file", ctx.outputs.out.path,
          "--output-source-map", ctx.outputs.map.path,
          "--input-orientation", input_orientation,
          "--output-orientation", ctx.attr.orientation]
  if ctx.attr.renaming:
    files += [ctx.outputs.js]
    args += ["--output-renaming-map", ctx.outputs.js.path,
             "--output-renaming-map-format", "CLOSURE_COMPILED_SPLIT_HYPHENS"]
    if ctx.attr.debug:
      args += ["--rename", "DEBUG"]
    else:
      args += ["--rename", "CLOSURE"]
  else:
    ctx.file_action(
        output=ctx.outputs.js,
        content="// closure_css_binary target had renaming = false\n")
  if ctx.attr.debug:
    args += ["--pretty-print"]
  if ctx.attr.vendor:
    args += ["--vendor", ctx.attr.vendor]
  args += ctx.attr.defs
  args += [src.path for src in srcs]
  ctx.action(
      inputs=list(srcs),
      outputs=files,
      arguments=args,
      executable=ctx.executable._compiler,
      progress_message="Compiling %d stylesheets to %s" % (
          len(srcs), ctx.outputs.out.short_path))
  return struct(files=set(files),
                transitive_css_labels=collect_transitive_css_labels(ctx),
                css_orientation=(input_orientation
                                 if ctx.attr.orientation == "NOCHANGE" else
                                 ctx.attr.orientation),
                js_css_renaming_map=ctx.outputs.js,
                compiled_css_labels=set(),
                transitive_css_srcs=files,
                transitive_data=tdata + ctx.files.data,
                runfiles=ctx.runfiles(files=files + ctx.files.data,
                                      transitive_files=srcs + tdata))

def _get_input_orientation(deps):
  orientation = None
  for dep in deps:
    if not orientation:
      orientation = dep.css_orientation
    elif orientation != dep.css_orientation:
      fail("Not all deps have the same orientation")
  return orientation

closure_css_binary = rule(
    implementation=_impl,
    attrs={
        "debug": attr.bool(default=False),
        "defs": attr.string_list(),
        "deps": CSS_DEPS_ATTR,
        "orientation": attr.string(default="NOCHANGE"),
        "renaming": attr.bool(default=True),
        "vendor": attr.string(),
        "data": attr.label_list(cfg=DATA_CFG, allow_files=True),
        "_compiler": attr.label(
            default=Label("//third_party/java/csscomp:ClosureCommandLineCompiler"),
            executable=True),
    },
    outputs={"out": "%{name}.css",
             "map": "%{name}.css.map",
             "js": "%{name}.css.js"})
