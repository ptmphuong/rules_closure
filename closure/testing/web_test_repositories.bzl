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


    # java_import_external(
    #     name = "org_seleniumhq_selenium_selenium_support",
    #     jar_sha256 = "2c74196d15277ce6003454d72fc3434091dbf3ba65060942719ba551509404d8",
    #     jar_urls = [
    #         "https://repo1.maven.org/maven2/org/seleniumhq/selenium/selenium-support/3.141.59/selenium-support-3.141.59.jar",
    #     ],
    #     licenses = ["notice"],  # The Apache Software License, Version 2.0
    #     testonly_ = 1,
    #     deps = [
    #         "@com_google_guava_guava",
    #         "@net_bytebuddy_byte_buddy",
    #         "@com_squareup_okhttp3_okhttp",
    #         "@com_squareup_okio_okio",
    #         "@org_apache_commons_commons_exec",
    #         "@org_seleniumhq_selenium_selenium_api",
    #         "@org_seleniumhq_selenium_selenium_remote_driver",
    #     ],
    # )

