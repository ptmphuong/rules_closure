# Copyright 2022 The Closure Rules Authors. All rights reserved.
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

"""Build rule for running a webtest."""

load("//closure:webfiles/web_library.bzl", "web_library", "get_web_library_config")
load("@io_bazel_rules_webtesting//web:web.bzl", "web_test_suite")

def webdriver_test(
    name,
    browsers,
    test_file_js,
    tags = [],
    host = None,
    port = None,
    visibility = None,
    **kwargs):

    html = "gen_html_%s" % name
    gen_test_html(
        name = html,
        test_file_js = test_file_js,
    )

    path = "/"
    html_webpath = "%s%s.html" % (path, html)

    # set up a development web server that links to the test for debugging purposes.
    web_library(
        name = "%s_debug" % name,
        srcs = [html, test_file_js],
        path = path,
    )

    web_config = "%s_server_config" % name
    get_web_library_config(
        name = web_config,
        srcs = [html, test_file_js],
        path = path,
    )

    native.java_binary(
        name = "%s_webtest_runner" % name,
        data = [test_file_js, html, web_config],
        main_class = "rules_closure.closure.testing.WebtestRunner",
        jvm_flags = [
            "-Dserver_config_path=$(location :%s)" % web_config,
            "-Dhtml_webpath=%s" % html_webpath,
        ],
        runtime_deps = [Label("//closure/testing:test_runner_lib")],
        testonly = 1,
    )

    web_test_suite(
        name = name,
        data = [test_file_js, html, web_config],
        test = ":%s_webtest_runner" % name,
        browsers = browsers,
        tags = ["no-sandbox", "native"],
        visibility = visibility,
        **kwargs
    )

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
