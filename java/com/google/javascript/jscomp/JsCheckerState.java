/*
 * Copyright 2016 The Closure Rules Authors. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.google.javascript.jscomp;

import java.util.HashSet;
import java.util.Set;

final class JsCheckerState {

  final String label;
  final boolean testonly;

  // Assume we're processing //closure/library which has 4788 provides. HashMap has a default load
  // factor of 0.75. Therefore redimensioning would never occur for a map with the capacity of 6385
  // (4788/0.75+1). However we're going to pick 7000 to allow for growth in the Closure Library. No
  // other project should ever exist with such a large set of provides in one closure_js_library().
  //
  // There are actually cooler data structures we could be using here to save space. Like maybe a
  // graph of namespace labels represented as an IdentityHashMap of interned strings. But it'd take
  // too much braining for too little benefit.
  final Set<String> provides = new HashSet<>(7000);

  // In almost all circumstances, the user will be directly depending on the Closure Library, in
  // addition to a bunch of other things. So we're going to aim a bit higher.
  final Set<String> provided = new HashSet<>(9000);

  // These are used to avoid flooding the user with certain types of error messages.
  final Set<String> notProvidedNamespaces = new HashSet<>();
  final Set<String> redeclaredProvides = new HashSet<>();

  JsCheckerState(String label, boolean testonly) {
    this.label = label;
    this.testonly = testonly;
  }
}
