# -*- mode: python; -*-
#
# Copyright 2016 The Bazel Authors. All rights reserved.
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

"""External dependencies for Closure Rules."""

def aopalliance():
  native.maven_jar(
      name = "aopalliance",
      artifact = "aopalliance:aopalliance:1.0",
      sha1 = "0235ba8b489512805ac13a8f9ea77a1ca5ebe3e8",
  )

def asm():
  native.maven_jar(
      name = "asm",
      artifact = "org.ow2.asm:asm:5.0.3",
      sha1 = "dcc2193db20e19e1feca8b1240dbbc4e190824fa",
  )

def asm_analysis():
  native.maven_jar(
      name = "asm_analysis",
      artifact = "org.ow2.asm:asm-analysis:5.0.3",
      sha1 = "c7126aded0e8e13fed5f913559a0dd7b770a10f3",
  )

def asm_commons():
  native.maven_jar(
      name = "asm_commons",
      artifact = "org.ow2.asm:asm-commons:5.0.3",
      sha1 = "a7111830132c7f87d08fe48cb0ca07630f8cb91c",
  )

def asm_util():
  native.maven_jar(
      name = "asm_util",
      artifact = "org.ow2.asm:asm-util:5.0.3",
      sha1 = "1512e5571325854b05fb1efce1db75fcced54389",
  )

def args4j():
  native.maven_jar(
      name = "args4j",
      artifact = "args4j:args4j:2.0.26",
      sha1 = "01ebb18ebb3b379a74207d5af4ea7c8338ebd78b",
  )

def closure_compiler():
  native.maven_jar(
      name = "closure_compiler",
      artifact = "com.google.javascript:closure-compiler:v20160315",
      sha1 = "f5b1a03f83a014e545db60a795fcf94db14a5ba2",
  )

def closure_library():
  # To update Closure Library, one needs to uncomment and run the js_files_maker
  # and js_testing_files_maker genrules in closure_library.BUILD.
  native.new_http_archive(
      name = "closure_library",
      url = "https://bazel-mirror.storage.googleapis.com/github.com/google/closure-library/archive/v20160315.zip",
      sha256 = "4327a27e040ccd36cb12dee2dc5aa6cff7bc614b09c0e45aab0a1ab9e750f542",
      strip_prefix = "closure-library-20160315",
      build_file = str(Label("//closure/library:closure_library.BUILD")),
  )

def closure_linter():
  native.new_http_archive(
      name = "closure_linter",
      url = "https://bazel-mirror.storage.googleapis.com/github.com/google/closure-linter/archive/v2.3.19.zip",
      sha256 = "ccb93b7327cd1e1520d0090c51f2f11d5174a34df24e1fa4d0114ffff28a7141",
      strip_prefix = "closure-linter-2.3.19",
      build_file = str(Label("//closure/linter:closure_linter.BUILD")),
  )

def closure_stylesheets():
  native.maven_jar(
      name = "closure_stylesheets",
      artifact = "com.google.closure-stylesheets:closure-stylesheets:20160212",
      sha1 = "f0e8625a2cfe0f501b28f5e6438b836358da8a97",
  )

def fonts_noto_hinted_deb():
  native.http_file(
      name = "fonts_noto_hinted_deb",
      url = "https://bazel-mirror.storage.googleapis.com/http.us.debian.org/debian/pool/main/f/fonts-noto/fonts-noto-hinted_20160116-1_all.deb",
      sha256 = "25b362c9437a7859ce034f22d94b698e8ed25007b443e5a26228ed5b3d2d32d4",
  )

def fonts_noto_mono_deb():
  native.http_file(
      name = "fonts_noto_mono_deb",
      url = "https://bazel-mirror.storage.googleapis.com/http.us.debian.org/debian/pool/main/f/fonts-noto/fonts-noto-mono_20160116-1_all.deb",
      sha256 = "74b457715f275ed893998a70d6bc955f67da6d36b36b422dbeeb045160edacb6",
  )

def guava():
  native.maven_jar(
      name = "guava",
      artifact = "com.google.guava:guava:19.0",
      sha1 = "6ce200f6b23222af3d8abb6b6459e6c44f4bb0e9",
  )

def gson():
  native.maven_jar(
      name = "gson",
      artifact = "com.google.code.gson:gson:2.4",
      sha1 = "0695b63d702f505b9b916e02272e3b6381bade7f",
  )

def guice():
  native.maven_jar(
      name = "guice",
      artifact = "com.google.inject:guice:3.0",
      sha1 = "9d84f15fe35e2c716a02979fb62f50a29f38aefa",
  )

def guice_assistedinject():
  native.maven_jar(
      name = "guice_assistedinject",
      artifact = "com.google.inject.extensions:guice-assistedinject:3.0",
      sha1 = "544449ddb19f088dcde44f055d30a08835a954a7",
  )

def guice_multibindings():
  native.maven_jar(
      name = "guice_multibindings",
      artifact = "com.google.inject.extensions:guice-multibindings:3.0",
      sha1 = "5e670615a927571234df68a8b1fe1a16272be555",
  )

def icu4j():
  native.maven_jar(
      name = "icu4j",
      artifact = "com.ibm.icu:icu4j:56.1",
      sha1 = "8dd6671f52165a0419e6de5e1016400875a90fa9",
  )

def jsr305():
  native.maven_jar(
      name = "jsr305",
      artifact = "com.google.code.findbugs:jsr305:1.3.9",
      sha1 = "40719ea6961c0cb6afaeb6a921eaa1f6afd4cfdf",
  )

def jsr330_inject():
  native.maven_jar(
      name = "jsr330_inject",
      artifact = "javax.inject:javax.inject:1",
      sha1 = "6975da39a7040257bd51d21a231b76c915872d38",
  )

def libexpat_amd64_deb():
  native.http_file(
      name = "libexpat_amd64_deb",
      url = "https://bazel-mirror.storage.googleapis.com/http.us.debian.org/debian/pool/main/e/expat/libexpat1_2.1.0-6+deb8u1_amd64.deb",
      sha256 = "1b006e659f383e09909595d8b84b79828debd7323d00e8a28b72ccd902273861",
  )

def libfontconfig_amd64_deb():
  native.http_file(
      name = "libfontconfig_amd64_deb",
      url = "https://bazel-mirror.storage.googleapis.com/http.us.debian.org/debian/pool/main/f/fontconfig/libfontconfig1_2.11.0-6.3_amd64.deb",
      sha256 = "2b21f91c8b46caba41221f1e45c5a37cac08ce1298dd7a28442f1b7332fa211b",
  )

def libfreetype_amd64_deb():
  native.http_file(
      name = "libfreetype_amd64_deb",
      url = "https://bazel-mirror.storage.googleapis.com/http.us.debian.org/debian/pool/main/f/freetype/libfreetype6_2.5.2-3+deb8u1_amd64.deb",
      sha256 = "80184d932f9b0acc130af081c60a2da114c7b1e7531c18c63174498fae47d862",
  )

def libpng_amd64_deb():
  native.http_file(
      name = "libpng_amd64_deb",
      url = "https://bazel-mirror.storage.googleapis.com/http.us.debian.org/debian/pool/main/libp/libpng/libpng12-0_1.2.50-2+deb8u2_amd64.deb",
      sha256 = "a57b6d53169c67a7754719f4b742c96554a18f931ca5b9e0408fb6502bb77e80",
  )

def phantomjs_linux_x86_64():
  native.http_file(
      name = "phantomjs_linux_x86_64",
      sha256 = "86dd9a4bf4aee45f1a84c9f61cf1947c1d6dce9b9e8d2a907105da7852460d2f",
      url = "https://bazel-mirror.storage.googleapis.com/bitbucket.org/ariya/phantomjs/downloads/phantomjs-2.1.1-linux-x86_64.tar.bz2",
  )

def phantomjs_macosx():
  native.http_file(
      name = "phantomjs_macosx",
      sha256 = "538cf488219ab27e309eafc629e2bcee9976990fe90b1ec334f541779150f8c1",
      url = "https://bazel-mirror.storage.googleapis.com/bitbucket.org/ariya/phantomjs/downloads/phantomjs-2.1.1-macosx.zip",
  )

def python_gflags():
  native.new_http_archive(
      name = "python_gflags",
      url = "https://bazel-mirror.storage.googleapis.com/github.com/google/python-gflags/archive/3.0.2.zip",
      sha256 = "8700f5b8d61f843425b090287874b4ff45510d858caa109847162dd98c7856f8",
      strip_prefix = "python-gflags-3.0.2",
      build_file = str(Label("//closure/linter:python_gflags.BUILD")),
  )

def soy():
  native.maven_jar(
      name = "soy",
      artifact = "com.google.template:soy:2016-01-12",
      sha1 = "adadc37aecf1042de7c9c6a6eb8f34719500ed69",
  )

def soyutils_usegoog():
  native.http_file(
      name = "soyutils_usegoog",
      sha256 = "fdb0e318949c1af668038df1d85d45353a00ff585f321c86efe91ac2a10cc91f",
      url = "https://bazel-mirror.storage.googleapis.com/repo1.maven.org/maven2/com/google/template/soy/2016-01-12/soy-2016-01-12-soyutils_usegoog.js",
  )

def closure_repositories():
  aopalliance()
  asm()
  asm_analysis()
  asm_commons()
  asm_util()
  args4j()
  closure_compiler()
  closure_library()
  closure_linter()
  closure_stylesheets()
  fonts_noto_hinted_deb()
  fonts_noto_mono_deb()
  guava()
  gson()
  guice()
  guice_assistedinject()
  guice_multibindings()
  icu4j()
  jsr305()
  jsr330_inject()
  libexpat_amd64_deb()
  libfontconfig_amd64_deb()
  libfreetype_amd64_deb()
  libpng_amd64_deb()
  phantomjs_linux_x86_64()
  phantomjs_macosx()
  python_gflags()
  soy()
  soyutils_usegoog()
