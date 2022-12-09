workspace(name = "io_bazel_rules_closure")

load("//closure:repositories.bzl", "closure_repositories")

closure_repositories()

maven_jar(
    name = "guava_testlib",
    artifact = "com.google.guava:guava-testlib:19.0",
    server = "closure_maven_server",
    sha1 = "ce5b880b206de3f76d364988a6308c68c726f74a",
)

maven_jar(
    name = "jimfs",
    artifact = "com.google.jimfs:jimfs:1.1",
    server = "closure_maven_server",
    sha1 = "8fbd0579dc68aba6186935cc1bee21d2f3e7ec1c",
)

maven_jar(
    name = "junit",
    artifact = "junit:junit:4.11",
    server = "closure_maven_server",
    sha1 = "4e031bb61df09069aeb2bffb4019e7a5034a4ee0",
)

maven_jar(
    name = "hamcrest_core",
    artifact = "org.hamcrest:hamcrest-core:1.3",
    server = "closure_maven_server",
    sha1 = "42a25dc3219429f0e5d060061f71acb49bf010a0",
)

maven_jar(
    name = "hamcrest_library",
    artifact = "org.hamcrest:hamcrest-library:1.3",
    server = "closure_maven_server",
    sha1 = "4785a3c21320980282f9f33d0d1264a69040538f",
)

maven_jar(
    name = "mockito",
    artifact = "org.mockito:mockito-all:1.10.19",
    server = "closure_maven_server",
    sha1 = "539df70269cc254a58cccc5d8e43286b4a73bf30",
)

maven_jar(
    name = "truth",
    artifact = "com.google.truth:truth:0.28",
    server = "closure_maven_server",
    sha1 = "0a388c7877c845ff4b8e19689dda5ac9d34622c4",
)
