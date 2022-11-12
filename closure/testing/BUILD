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

package(
    default_testonly = True,
    default_visibility = ["//visibility:public"],
)

licenses(["notice"])  # Apache 2.0

load("//closure:defs.bzl", "closure_js_binary", "closure_js_library")
load("@bazel_skylib//:bzl_library.bzl", "bzl_library")

# html place holder used by PhantomJS.
exports_files(["empty.html"])

closure_js_library(
    name = "phantomjs_jsunit_runner",
    srcs = ["phantomjs_jsunit_runner.js"],
    no_closure_library = True,
)

closure_js_library(
    name = "phantomjs_harness",
    srcs = ["phantomjs_harness.js"],
    no_closure_library = True,
    deps = ["//closure/testing/externs:phantomjs"],
)

closure_js_binary(
    name = "phantomjs_harness_bin",
    defs = ["--env=CUSTOM"],
    deps = [":phantomjs_harness"],
)

bzl_library(
    name = "phantomjs_test_bzl",
    srcs = ["phantomjs_test.bzl"],
    deps = [
        "//closure/compiler:compiler_bzl",
    ],
)

bzl_library(
    name = "closure_js_test_bzl",
    srcs = ["closure_js_test.bzl"],
    deps = [
        ":phantomjs_test_bzl",
        "//closure/compiler:compiler_bzl",
    ],
)

# html template used by webtest runner.
exports_files(["LICENSE", "gen_webtest_html.template"])

java_binary(
    name = "webtest",
    testonly = 1,
    srcs = ["WebTestRunner.java", "FileServerHandler.java"],
    main_class = "rules_closure.closure.testing.WebTestRunner",
    visibility = ["//visibility:public"],
    deps = [
        "@org_seleniumhq_selenium_selenium_api",
        "@org_seleniumhq_selenium_selenium_support",
        "@org_seleniumhq_selenium_selenium_remote_driver",
        "@io_bazel_rules_webtesting//java/com/google/testing/web:web",

        "//java/io/bazel/rules/closure:webpath",
        "//java/io/bazel/rules/closure/http",
        "//java/io/bazel/rules/closure/webfiles:build_info_java_proto",
        "//java/io/bazel/rules/closure/webfiles/server",
        "//java/io/bazel/rules/closure/webfiles/server:build_info_java_proto",
        # "@com_google_code_findbugs_jsr305",
        "@com_google_dagger",
        "@com_google_guava",
        # "@com_google_guava_testlib",
        "@com_google_jimfs",
        # "@com_google_truth",
        # "@junit",
        # "@org_mockito_core",
    ],
)

java_binary(
    name = "closureserver",
    testonly = 1,
    srcs = ["ClosureServer.java"],
    main_class = "rules_closure.closure.testing.ClosureServer",
    visibility = ["//visibility:public"],
    deps = [
        "@org_seleniumhq_selenium_selenium_api",
        "@org_seleniumhq_selenium_selenium_support",
        "@org_seleniumhq_selenium_selenium_remote_driver",
        "@io_bazel_rules_webtesting//java/com/google/testing/web:web",

        "//java/io/bazel/rules/closure:webpath",
        "//java/io/bazel/rules/closure/http",
        "//java/io/bazel/rules/closure/webfiles:build_info_java_proto",
        "//java/io/bazel/rules/closure/webfiles/server",
        "//java/io/bazel/rules/closure/webfiles/server:build_info_java_proto",
        # "@com_google_code_findbugs_jsr305",
        "@com_google_dagger",
        "@com_google_guava",
        # "@com_google_guava_testlib",
        "@com_google_jimfs",
        # "@com_google_truth",
        # "@junit",
        # "@org_mockito_core",
    ],
)

java_binary(
    name = "driver",
    testonly = 1,
    srcs = ["Driver.java"],
    main_class = "rules_closure.closure.testing.Driver",
    visibility = ["//visibility:public"],
    deps = [
        "@org_seleniumhq_selenium_selenium_api",
        "@org_seleniumhq_selenium_selenium_support",
        "@org_seleniumhq_selenium_selenium_remote_driver",
        "@io_bazel_rules_webtesting//java/com/google/testing/web:web",

        "//java/io/bazel/rules/closure:webpath",
        "//java/io/bazel/rules/closure/http",
        "//java/io/bazel/rules/closure/webfiles:build_info_java_proto",
        "//java/io/bazel/rules/closure/webfiles/server",
        "//java/io/bazel/rules/closure/webfiles/server:build_info_java_proto",
        # "@com_google_code_findbugs_jsr305",
        "@com_google_dagger",
        "@com_google_guava",
        # "@com_google_guava_testlib",
        "@com_google_jimfs",
        # "@com_google_truth",
        # "@junit",
        # "@org_mockito_core",
    ],
)
