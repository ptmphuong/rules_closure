package(default_visibility = ["//visibility:public"])

licenses([
    "notice",  # Apache License 2.0 (Closure Compiler)
    "reciprocal",  # Mozilla Public License Version 1.1 (Rhino AST)
])

java_library(
    name = "jscomp",
    exports = ["@closure_compiler//jar"],
    runtime_deps = [
        "@args4j//jar",
        "@gson//jar",
        "@guava//jar",
        "@jsr305//jar",
        "@protobuf_java//jar",
    ],
)
