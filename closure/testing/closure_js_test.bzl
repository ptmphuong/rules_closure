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

"""Build macro for running JavaScript unit tests in PhantomJS."""

load("//closure/compiler:closure_js_binary.bzl", "closure_js_binary")
load("//closure/compiler:closure_js_library.bzl", "closure_js_library")
load("//closure/testing:phantomjs_test.bzl", "phantomjs_test")
load("//closure:webfiles/web_library.bzl", "web_library")
load("//closure:webfiles/web_library_test.bzl", "web_library_test")
load("@io_bazel_rules_webtesting//web:web.bzl", "web_test_suite")

def closure_js_test(
        name,
        srcs,
        data = None,
        deps = None,
        compilation_level = None,
        css = None,
        defs = None,
        entry_points = None,
        html = None,
        language = None,
        lenient = False,
        suppress = None,
        visibility = None,
        tags = [],
        debug = False,
        browsers = None,
        webtest = False,
        **kwargs):
    if not srcs:
        fail("closure_js_test rules can not have an empty 'srcs' list")
    if language:
        print("closure_js_test 'language' is removed and now always ES6 strict")
    for src in srcs:
        if not src.endswith("_test.js"):
            fail("closure_js_test srcs must be files ending with _test.js")
    if len(srcs) == 1:
        work = [(name, srcs)]
    else:
        work = [(name + _make_suffix(src), [src]) for src in srcs]
    for shard, sauce in work:

        closure_js_library(
            name = "%s_lib" % shard,
            srcs = sauce,
            data = data,
            deps = deps,
            lenient = lenient,
            suppress = suppress,
            visibility = visibility,
            testonly = True,
            tags = tags,
        )

        if type(entry_points) == type({}):
            ep = entry_points.get(sauce[0])
        else:
            ep = entry_points



        closure_js_binary(
            name = "%s_bin" % shard,
            deps = [":%s_lib" % shard],
            compilation_level = compilation_level,
            css = css,
            debug = True,
            defs = defs,
            entry_points = ep,
            formatting = "PRETTY_PRINT",
            visibility = visibility,
            testonly = True,
            tags = tags,
        )

        if webtest:
            gen_test_html(
                name = "gen_%s" % shard,
                test_file_js = "%s_bin.js" % shard,
            )
            html = "gen_%s" % shard

            if not browsers:
                browsers = ["@io_bazel_rules_webtesting//browsers:chromium-local"]

            # web_library(
            #     name = "%s_debug_lib" % shard,
            #     srcs = ["%s_bin" % shard],
            #     path = "/",
            # )

            # web_library(
            #     name = "%s_debug" % shard,
            #     srcs = [html],
            #     deps = [":%s_debug_lib" % shard,],
            #     port = "8080",
            #     host = "localhost",
            #     path = "/",
            # )

            web_library_test(
                name = "%s_debug" % shard,
                srcs = [
                    html,
                    ":%s_bin.js" % shard,
                ],
                port = "8080",
                host = "localhost",
                path = "/",
            )

            web_test_suite(
                name = shard,
                data = [":%s_bin" % shard, html],
                # test = "//closure/testing:webtest",
                test = "//closure/testing:driver",
                args = ["%s_bin.js" % shard, "$(location %s)" % html, html],
                browsers = browsers,
                tags = ["no-sandbox", "native"],
                visibility = visibility,
                **kwargs
            )
        else :
            phantomjs_test(
                name = shard,
                runner = str(Label("//closure/testing:phantomjs_jsunit_runner")),
                deps = [":%s_bin" % shard],
                debug = debug,
                html = html,
                visibility = visibility,
                tags = tags,
                **kwargs
            )

    if len(srcs) > 1:
        native.test_suite(
            name = name,
            tests = [":" + shard for shard, _ in work],
            tags = tags,
        )

def _make_suffix(path):
    return "_" + path.replace("_test.js", "").replace("-", "_").replace("/", "_")

def _gen_test_html_impl(ctx):
    """Implementation of the gen_test_html rule."""
    ctx.actions.expand_template(
        template = ctx.file._template,
        output = ctx.outputs.html_file,
        substitutions = {
            "{{TEST_FILE_JS}}": ctx.attr.test_file_js,
        },
    )
    runfiles = ctx.runfiles(files = [ctx.outputs.html_file], collect_default = True)
    return [DefaultInfo(runfiles = runfiles)]

# Used to generate default test.html file for running Closure-based JS tests.
# The test_file_js argument specifies the name of the JS file containing tests,
# typically created with closure_js_binary.
# The output is created from gen_test_html.template file.
gen_test_html = rule(
    implementation = _gen_test_html_impl,
    attrs = {
        "test_file_js": attr.string(mandatory = True),
        "_template": attr.label(
            default = Label("//closure/testing:gen_webtest_html.template"),
            allow_single_file = True,
        ),
    },
    outputs = {"html_file": "%{name}.html"},
)
