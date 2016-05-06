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

load("//closure/compiler:closure_js_library.bzl", "closure_js_library")
load("//closure/private:file_test.bzl", "file_test")
load("//closure/private:rule_test.bzl", "rule_test")

closure_js_library(
    name = "a",
    srcs = ["a.js"],
)

closure_js_library(
    name = "b",
    srcs = ["b.js"],
    deps = [":a"],
)

closure_js_library(
    name = "c",
    srcs = ["c.js"],
    deps = [
        ":a",
        ":b",
    ],
)

closure_js_library(
    name = "t",
    testonly = True,
    srcs = ["t.js"],
)

rule_test(
    name = "a_rule_test",
    provides = {
        "js_language": "^\"ECMASCRIPT5_STRICT\"$",
        "js_exports": "^\\[\\]$",
        "transitive_js_srcs": "/a.js\\]",
        "transitive_js_externs": "",
    },
    rule = ":a",
)

rule_test(
    name = "b_rule_test",
    provides = {
        "js_language": "^\"ECMASCRIPT5_STRICT\"$",
        "js_exports": "^\\[\\]$",
        "transitive_js_srcs": "/a\\.js.*/b\\.js\\]",
        "transitive_js_externs": "",
    },
    rule = ":b",
)

rule_test(
    name = "c_rule_test",
    provides = {
        "js_language": "^\"ECMASCRIPT5_STRICT\"$",
        "js_exports": "^\\[\\]$",
        "transitive_js_srcs": "/a\\.js.*/b\\.js.*/c\\.js\\]",
        "transitive_js_externs": "",
    },
    rule = ":c",
)

rule_test(
    name = "t_rule_test",
    provides = {
        "js_language": "^\"ECMASCRIPT5_STRICT\"$",
        "js_exports": "^\\[\\]$",
        "transitive_js_srcs": "/t\\.js\\]",
        "transitive_js_externs": "",
    },
    rule = ":t",
)

file_test(
    name = "a_providesA",
    content = "a\n",
    file = "a-provided.txt",
)

file_test(
    name = "b_providesB",
    content = "b\n",
    file = "b-provided.txt",
)

file_test(
    name = "c_providesC",
    content = "c\n",
    file = "c-provided.txt",
)

file_test(
    name = "t_providesT",
    content = "t\n",
    file = "t-provided.txt",
)

file_test(
    name = "a_noErrors",
    content = "",
    file = "a-stderr.txt",
)

file_test(
    name = "b_noErrors",
    content = "",
    file = "b-stderr.txt",
)

file_test(
    name = "c_noErrors",
    content = "",
    file = "c-stderr.txt",
)

file_test(
    name = "t_noErrors",
    content = "",
    file = "t-stderr.txt",
)

closure_js_library(
    name = "didNotSpecifyTransitiveDep",
    srcs = ["c.js"],
    nofail = True,
    tags = ["manual"],
    deps = [":b"],  # oops I forgot :a, it doesn't matter if :b depends on :a
)

file_test(
    name = "didNotSpecifyTransitiveDep_causesMissingProvideError",
    file = "didNotSpecifyTransitiveDep-stderr.txt",
    regexp = "CR_NOT_PROVIDED",
)

closure_js_library(
    name = "duplicateProvidesWithinALibrary",
    srcs = [
        "a.js",
        "a2.js",
    ],
    nofail = True,
    tags = ["manual"],
)

file_test(
    name = "duplicateProvidesWithinALibrary_causesDuplicateProvideError",
    file = "duplicateProvidesWithinALibrary-stderr.txt",
    regexp = "CR_DUPLICATE_PROVIDES",
)

closure_js_library(
    name = "duplicateProvideFromDeps",
    srcs = ["a2.js"],
    nofail = True,
    tags = ["manual"],
    deps = [":a"],
)

file_test(
    name = "duplicateProvideFromDeps_causesDuplicateProvideError",
    file = "duplicateProvideFromDeps-stderr.txt",
    regexp = "CR_REDECLARED_PROVIDES",
)

closure_js_library(
    name = "setTestOnlyInLibraryWithoutTestOnly",
    # testonly = True,  # <-- oops I forgot this line
    srcs = ["t.js"],
    nofail = True,
    tags = ["manual"],
)

file_test(
    name = "setTestOnlyInLibraryWithoutTestOnly_causesDuplicateProvideError",
    file = "setTestOnlyInLibraryWithoutTestOnly-stderr.txt",
    regexp = "CR_INVALID_SETTESTONLY",
)
