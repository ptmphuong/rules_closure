package(default_visibility = ["//visibility:public"])

licenses(["notice"])  # Apache License 2.0

java_library(
    name = "csscomp",
    exports = ["@closure_stylesheets//jar"],
    runtime_deps = [
        "//third_party/java/jscomp",
        "@args4j//jar",
        "@gson//jar",
        "@guava//jar",
        "@jsr305//jar",
    ],
)

java_binary(
    name = "ClosureCommandLineCompiler",
    jvm_flags = ["-client"],
    main_class = "com.google.common.css.compiler.commandline.ClosureCommandLineCompiler",
    runtime_deps = [":csscomp"],
)
