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

def _java_import_external(repository_ctx):
  """Downloads jar and creates a java_import rule."""
  urls = repository_ctx.attr.jar_urls
  sha256 = repository_ctx.attr.jar_sha256
  basename = urls[0][urls[0].rindex('/') + 1:]
  lines = [
      "# DO NOT EDIT: automatically generated BUILD file",
      "",
      "package(default_visibility=[\"//visibility:public\"])",
      "",
      "licenses(%s)" % repr(repository_ctx.attr.licenses),
      "",
      "java_import(",
      "    name = %s," % repr(repository_ctx.name),
      "    jars = [%s]," % repr(basename),
  ]
  for attr in ("deps",
               "runtime_deps",
               "exports",
               "neverlink",
               "visibility"):
    value = getattr(repository_ctx.attr, attr, None)
    if value:
      lines.append("    %s = %s," % (attr, repr(value)))
  if repository_ctx.attr.testonly_:
    lines.append("    testonly = 1,")
  lines.append(")")
  lines.append("")
  if repository_ctx.attr.extra:
    lines.append(repository_ctx.attr.extra)
  repository_ctx.download(urls, basename, sha256)
  repository_ctx.file("BUILD", "\n".join(lines))

java_import_external = repository_rule(
    implementation=_java_import_external,
    attrs={
        "jar_urls": attr.string_list(mandatory=True, allow_empty=False),
        "jar_sha256": attr.string(mandatory=True),
        "licenses": attr.string_list(mandatory=True),
        "deps": attr.string_list(),
        "runtime_deps": attr.string_list(),
        "exports": attr.string_list(),
        "neverlink": attr.bool(),
        "testonly_": attr.bool(),
        "extra": attr.string(),
    })
