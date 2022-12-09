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

load(
    "//closure/private:defs.bzl",
    "collect_css",
    "collect_runfiles",
    "unfurl",
)

def _closure_css_binary(ctx):
    if not ctx.attr.deps:
        fail("closure_css_binary rules can not have an empty 'deps' list")
    deps = unfurl(ctx.attr.deps, provider = "closure_css_library")
    css = collect_css(deps)
    if not css.srcs:
        fail("There are no CSS source files in the transitive closure")
    inputs = []
    files = [ctx.outputs.bin, ctx.outputs.map]
    outputs = files[:]
    args = [
        "--output-file",
        ctx.outputs.bin.path,
        "--output-source-map",
        ctx.outputs.map.path,
        "--input-orientation",
        css.orientation,
        "--output-orientation",
        ctx.attr.orientation,
    ]
    if ctx.attr.renaming:
        outputs += [ctx.outputs.js]
        args += [
            "--output-renaming-map",
            ctx.outputs.js.path,
            "--output-renaming-map-format",
            "CLOSURE_COMPILED_SPLIT_HYPHENS",
        ]
        if ctx.attr.debug:
            args += ["--rename", "DEBUG"]
        else:
            args += ["--rename", "CLOSURE"]
    else:
        ctx.file_action(
            output = ctx.outputs.js,
            content = "// closure_css_binary target had renaming = false\n",
        )
    if ctx.attr.debug:
        args += ["--pretty-print"]
    if ctx.attr.vendor:
        args += ["--vendor", ctx.attr.vendor]
    args += ctx.attr.defs
    for f in css.srcs:
        args.append(f.path)
        inputs.append(f)
    ctx.actions.run(
        inputs = inputs,
        outputs = outputs,
        arguments = args,
        executable = ctx.executable._compiler,
        progress_message = "Compiling %d stylesheets to %s" % (
            len(css.srcs),
            ctx.outputs.bin.short_path,
        ),
    )
    return struct(
        files = depset(files),
        closure_css_binary = struct(
            bin = ctx.outputs.bin,
            map = ctx.outputs.map,
            renaming_map = ctx.outputs.js,
            labels = css.labels,
        ),
        closure_css_library = struct(
            srcs = depset([ctx.outputs.bin]),
            orientation = (css.orientation if ctx.attr.orientation == "NOCHANGE" else ctx.attr.orientation),
        ),
        runfiles = ctx.runfiles(
            files = files + ctx.files.data,
            transitive_files = (collect_runfiles(deps) |
                                collect_runfiles(ctx.attr.data)),
        ),
    )

closure_css_binary = rule(
    implementation = _closure_css_binary,
    attrs = {
        "debug": attr.bool(),
        "defs": attr.string_list(),
        "deps": attr.label_list(providers = ["closure_css_library"]),
        "orientation": attr.string(default = "NOCHANGE"),
        "renaming": attr.bool(default = True),
        "vendor": attr.string(),
        "data": attr.label_list(allow_files = True),
        "_compiler": attr.label(
            default = Label(
                "@com_google_closure_stylesheets//:ClosureCommandLineCompiler",
            ),
            executable = True,
            cfg = "host",
        ),
    },
    outputs = {
        "bin": "%{name}.css",
        "map": "%{name}.css.map",
        "js": "%{name}.css.js",
    },
)
