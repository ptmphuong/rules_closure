"""Bazel rule for loading repository deps for web testing"""

load("@bazel_tools//tools/build_defs/repo:java.bzl", "java_import_external")
load("@io_bazel_rules_webtesting//web:repositories.bzl", "web_test_repositories")
load("@io_bazel_rules_webtesting//web/versioned:browsers-0.3.3.bzl", "browser_repositories")
load("@io_bazel_rules_webtesting//web:java_repositories.bzl", "java_repositories")

def setup_web_test_repositories():
    web_test_repositories()

    browser_repositories(
        chromium = True,
        firefox = True,
    )

    java_repositories()

