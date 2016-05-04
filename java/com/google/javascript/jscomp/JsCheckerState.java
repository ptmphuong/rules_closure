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
