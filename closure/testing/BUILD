package(
    default_testonly = True,
    default_visibility = ["//visibility:public"],
)

load("//closure/compiler:closure_js_library.bzl", "closure_js_library")

exports_files(["phantomjs_runner.js"])

closure_js_library(
    name = "phantomjs_jsunit_runner_lib",
    srcs = ["phantomjs_jsunit_runner.js"],
    language = "ECMASCRIPT5_STRICT",
    deps = [
        "//closure/library",
        "//closure/library:testing",
    ],
)

closure_js_library(
    name = "phantomjs_runner_lib",
    srcs = ["phantomjs_runner.js"],
    language = "ECMASCRIPT5_STRICT",
    visibility = ["//visibility:private"],
    deps = ["//closure/testing/externs:phantom_lib"],
)
