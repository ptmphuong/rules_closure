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

"""Web component validation, packaging, and development web server."""

load(
    "//closure/private:defs.bzl",
    "create_argfile",
    "difference",
    "long_path",
    "unfurl",
)

def _impl(ctx):
    if not ctx.attr.srcs:
        if ctx.attr.deps:
            fail("deps can not be set when srcs is not")
        if not ctx.attr.exports:
            fail("exports must be set if srcs is not")
    if ctx.attr.path:
        if not ctx.attr.path.startswith("/"):
            fail("webpath must start with /")
        if ctx.attr.path != "/" and ctx.attr.path.endswith("/"):
            fail("webpath must not end with / unless it is /")
        if "//" in ctx.attr.path:
            fail("webpath must not have //")
    elif ctx.attr.srcs:
        fail("path must be set when srcs is set")
    if "*" in ctx.attr.suppress and len(ctx.attr.suppress) != 1:
        fail("when \"*\" is suppressed no other items should be present")

    # process what came before
    deps = unfurl(ctx.attr.deps, provider = "webfiles")
    webpaths = []
    manifests = []
    for dep in deps:
        webpaths.append(dep.webfiles.webpaths)
        manifests += [dep.webfiles.manifests]

    # process what comes now
    new_webpaths = []
    manifest_srcs = []
    path = ctx.attr.path
    strip = _get_strip(ctx)
    for src in ctx.files.srcs:
        suffix = _get_path_relative_to_package(src)
        if strip:
            if not suffix.startswith(strip):
                fail("Relative src path not start with '%s': %s" % (strip, suffix))
            suffix = suffix[len(strip):]
        webpath = "%s/%s" % ("" if path == "/" else path, suffix)
        if webpath in new_webpaths:
            _fail(ctx, "multiple srcs within %s define the webpath %s " % (
                ctx.label,
                webpath,
            ))
        if webpath in webpaths:
            _fail(ctx, "webpath %s was defined by %s when already defined by deps" % (
                webpath,
                ctx.label,
            ))
        new_webpaths.append(webpath)
        manifest_srcs.append(struct(
            path = src.path,
            longpath = long_path(ctx, src),
            webpath = webpath,
        ))

    webpaths += [depset(new_webpaths)]
    manifest = ctx.actions.declare_file("%s.pbtxt" % ctx.label.name)
    ctx.actions.write(
        output = manifest,
        content = struct(
            label = str(ctx.label),
            src = manifest_srcs,
        ).to_proto(),
    )
    manifests = depset([manifest], transitive = manifests, order = "postorder")

    params = struct(
        label = str(ctx.label),
        bind = "%s:%s" % (str(ctx.attr.host), str(ctx.attr.port)),
        manifest = [long_path(ctx, man) for man in manifests.to_list()],
        external_asset = [
            struct(webpath = k, path = v)
            for k, v in ctx.attr.external_assets.items()
        ],
    )
    params_file = ctx.actions.declare_file("%s_server_params.pbtxt" % ctx.label.name)
    ctx.actions.write(output = params_file, content = params.to_proto())

    runfiles = ctx.runfiles(
    files = [ctx.outputs.config_file, manifest], collect_default = True)
    return [DefaultInfo(runfiles = runfiles)]

def _fail(ctx, message):
    if ctx.attr.suppress == ["*"]:
        print(message)
    else:
        fail(message)

def _get_path_relative_to_package(artifact):
    """Returns file path relative to the package that declared it."""
    path = artifact.path
    for prefix in (
        artifact.root.path,
        artifact.owner.workspace_root if artifact.owner else "",
        artifact.owner.package if artifact.owner else "",
    ):
        if prefix:
            prefix = prefix + "/"
            if not path.startswith(prefix):
                fail("Path %s doesn't start with %s" % (path, prefix))
            path = path[len(prefix):]
    return path

def _get_strip(ctx):
    strip = ctx.attr.strip_prefix
    if strip:
        if strip.startswith("/"):
            _fail(ctx, "strip_prefix should not end with /")
            strip = strip[1:]
        if strip.endswith("/"):
            _fail(ctx, "strip_prefix should not end with /")
        else:
            strip += "/"
    return strip

gen_web_config = rule(
    implementation = _impl,
    attrs = {
        "path": attr.string(),
        "host": attr.string(default = "0.0.0.0"),
        "port": attr.string(default = "6006"),
        "srcs": attr.label_list(allow_files = True),
        "deps": attr.label_list(providers = ["webfiles"]),
        "exports": attr.label_list(),
        "data": attr.label_list(allow_files = True),
        "suppress": attr.string_list(),
        "strip_prefix": attr.string(),
        "external_assets": attr.string_dict(default = {"/_/runfiles": "."}),
    },
    outputs = {
        "config_file": "%{name}_server_params.pbtxt",
    },
)
