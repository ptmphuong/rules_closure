package com.google.javascript.jscomp;

import static com.google.common.base.Preconditions.checkArgument;

import com.google.common.base.Optional;
import com.google.common.base.Strings;
import com.google.javascript.jscomp.CompilerOptions.LanguageMode;

import java.util.NavigableSet;

final class JsCheckerHelper {

  static boolean isEs6OrHigher(LanguageMode language) {
    return language == LanguageMode.ECMASCRIPT6_STRICT
        || language == LanguageMode.ECMASCRIPT6_TYPED;
  }

  static boolean isGeneratedPath(String path) {
    return path.contains("bazel-out/")
        || path.contains("bazel-genfiles/");
  }

  static Optional<String> convertPathToModuleName(String path, NavigableSet<String> roots) {
    checkArgument(!path.startsWith("/"));
    if (!path.endsWith(".js")) {
      return Optional.absent();
    }
    String module = path.substring(0, path.length() - 3);
    String root = roots.floor(module);
    if (!Strings.isNullOrEmpty(root)) {
      String prefix = root + "/";
      if (module.startsWith(prefix) && module.length() > prefix.length()) {
        module = module.substring(prefix.length());
      }
    }
    return Optional.of(module);
  }

  static String normalizeClosureNamespace(String namespace) {
    return "goog:" + namespace;
  }

  private JsCheckerHelper() {}
}
