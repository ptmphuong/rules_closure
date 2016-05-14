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

package(default_testonly = True)

load("//closure/compiler:closure_js_deps.bzl", "closure_js_deps")
load("//closure/compiler:closure_js_library.bzl", "closure_js_library")
load("//closure/private:file_test.bzl", "file_test")

closure_js_library(
    name = "hyperion",
    srcs = ["hyperion.js"],
)

closure_js_deps(
    name = "hyperion_deps",
    deps = [":hyperion"],
)

file_test(
    name = "noTransitiveDeps_containsSingleLineRelativeToClosureLibraryBaseJs",
    file = "hyperion_deps.js",
    regexp = "^goog.addDependency('../../../../closure/compiler/test/closure_js_deps/hyperion.js', \\['hyperion'\\], \\[\\], false);$",
)

closure_js_library(
    name = "goblin",
    srcs = ["goblin.js"],
    deps = ["//closure/library"],
)

closure_js_deps(
    name = "goblin_deps",
    deps = [":goblin"],
)

file_test(
    name = "module_usesTrueArgument",
    file = "goblin_deps.js",
    regexp = "^goog.addDependency('../../../../closure/compiler/test/closure_js_deps/goblin.js', \\['goblin'\\], \\['goog.dom'\\], true);$",
)

file_test(
    name = "googDomDependency_includesClosureLibraryStuffInDepsFile",
    file = "goblin_deps.js",
    regexp = "^goog.addDependency('\\./dom/dom\\.js', \\['goog\\.dom'",
)
