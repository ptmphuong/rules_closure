#!/bin/bash
#
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

# ProTip: You can uncomment these lines to debug loading:
#         export LD_DEBUG="files"
#         export FC_DEBUG="1024"

shopt -s nullglob

if [[ "${RUNFILES}" == "" ]]; then
  if [[ "$(pwd)" =~ ^(.*\.runfiles)(/.*)?$ ]]; then
    RUNFILES="${BASH_REMATCH[1]}"
  else
    RUNFILES="${0}.runfiles"
  fi
fi
if [[ -d "${RUNFILES}/io_bazel_rules_closure" ]]; then
  RUNFILES="${RUNFILES}/io_bazel_rules_closure"
fi
if [[ -d "${RUNFILES}/external/io_bazel_rules_closure" ]]; then
  RUNFILES="${RUNFILES}/external/io_bazel_rules_closure"
fi
OMFG=(${RUNFILES}/*/external/io_bazel_rules_closure)
if [[ ${#OMFG[@]} > 0 ]]; then
  RUNFILES="${OMFG[0]}"
fi

export LD_LIBRARY_PATH="${RUNFILES}/third_party/fontconfig/k8:${LD_LIBRARY_PATH}"
export LD_LIBRARY_PATH="${RUNFILES}/third_party/freetype/k8:${LD_LIBRARY_PATH}"
export LD_LIBRARY_PATH="${RUNFILES}/third_party/expat/k8:${LD_LIBRARY_PATH}"
export LD_LIBRARY_PATH="${RUNFILES}/third_party/png/k8:${LD_LIBRARY_PATH}"

export FONTCONFIG_PATH="${RUNFILES}/third_party/fontconfig"
export XDG_DATA_HOME="${RUNFILES}"
export XDG_CACHE_HOME="$(mktemp -d "${TMPDIR:-/tmp}/fontcache.XXXXXXXXXX")"

"${RUNFILES}/third_party/phantomjs/bin/phantomjs" "$@"
rc="$?"
rm -rf "${XDG_CACHE_HOME}"
exit "${rc}"
