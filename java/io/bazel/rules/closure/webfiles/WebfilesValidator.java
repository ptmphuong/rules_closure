// Copyright 2016 The Closure Rules Authors. All Rights Reserved.
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//     http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package io.bazel.rules.closure.webfiles;

import static com.google.common.base.Preconditions.checkNotNull;
import static java.lang.String.format;
import static java.nio.charset.StandardCharsets.UTF_8;

import com.google.common.base.Joiner;
import com.google.common.base.Optional;
import com.google.common.base.Supplier;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Multimap;
import com.google.common.collect.Ordering;
import io.bazel.rules.closure.Tarjan;
import io.bazel.rules.closure.webfiles.BuildInfo.Webfiles;
import io.bazel.rules.closure.webfiles.BuildInfo.WebfilesSource;
import java.io.IOException;
import java.nio.file.FileSystem;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Strict dependency checker for HTML and CSS files.
 *
 * <p>This checks that all the href, src, etc. attributes in the HTML and CSS point to srcs defined
 * by the current rule, or direct children rules. It also checks for cycles.
 */
public class WebfilesValidator {

  // TODO(jart): Use jsoup and csscomp to get AST.
  private static final Pattern HTML_COMMENT = Pattern.compile("<!--.*?-->", Pattern.DOTALL);
  private static final Pattern CSS_COMMENT = Pattern.compile("/\\*.*?\\*/", Pattern.DOTALL);
  private static final Pattern HREF_SRC_ATTRIBUTE =
      Pattern.compile(" (?:href|src)=(\"[^\"]+\"|'[^']+'|[^'\" >]+)");
  private static final Pattern URL_ATTRIBUTE =
      Pattern.compile(" url\\(\\s*('[^']+'|\"[^\"]+\"|[^)]+)\\)");

  private final FileSystem fs;

  public WebfilesValidator(FileSystem fs) {
    this.fs = checkNotNull(fs, "fs");
  }

  /** Validates {@code srcs} in {@code manifest} and returns error messages. */
  ImmutableList<String> validate(
      Webfiles target,
      Iterable<Webfiles> directDeps,
      Supplier<? extends Iterable<Webfiles>> transitiveDeps)
          throws IOException {
    Impl impl = new Impl(target, directDeps, transitiveDeps);
    impl.validate();
    return impl.errors.build();
  }

  private final class Impl {
    private final Webfiles target;
    private final Iterable<Webfiles> directDeps;
    private final Supplier<? extends Iterable<Webfiles>> transitiveDeps;
    private final Set<Webpath> accessibleAssets = new HashSet<>();
    private final Multimap<Webpath, Webpath> relationships = HashMultimap.create();
    final ImmutableList.Builder<String> errors = new ImmutableList.Builder<>();

    Impl(
        Webfiles target,
        Iterable<Webfiles> directDeps,
        Supplier<? extends Iterable<Webfiles>> transitiveDeps) {
      this.target = target;
      this.directDeps = directDeps;
      this.transitiveDeps = transitiveDeps;
    }

    void validate() throws IOException {
      for (WebfilesSource src : target.getSrcList()) {
        accessibleAssets.add(Webpath.get(src.getWebpath()));
      }
      for (Webfiles dep : directDeps) {
        for (WebfilesSource src : dep.getSrcList()) {
          accessibleAssets.add(Webpath.get(src.getWebpath()));
        }
      }
      for (WebfilesSource src : target.getSrcList()) {
        Path path;
        String contents;
        Pattern pattern;
        if (src.getPath().endsWith(".html")) {
          path = fs.getPath(src.getPath());
          contents = new String(Files.readAllBytes(path), UTF_8);
          contents = HTML_COMMENT.matcher(contents).replaceAll("");
          pattern = HREF_SRC_ATTRIBUTE;
        } else if (src.getPath().endsWith(".css")) {
          path = fs.getPath(src.getPath());
          contents = new String(Files.readAllBytes(path), UTF_8);
          contents = CSS_COMMENT.matcher(contents).replaceAll("");
          pattern = URL_ATTRIBUTE;
        } else {
          continue;
        }
        Webpath webpath = Webpath.get(src.getWebpath());
        Matcher matcher = pattern.matcher(contents);
        while (matcher.find()) {
          String url = stripQuotes(matcher.group(1));
          if (shouldSkipUrl(url)) {
            continue;
          }
          addRelationship(path, webpath, Webpath.get(url));
        }
      }
      for (ImmutableSet<Webpath> scc : Tarjan.findStronglyConnectedComponents(relationships)) {
        errors.add(format(
            "These webpaths are strongly connected; please make your html acyclic\n\n  - %s\n",
            Joiner.on("\n  - ").join(Ordering.natural().sortedCopy(scc))));
      }
    }

    private void addRelationship(Path path, Webpath src, Webpath relativeDest) {
      if (relativeDest.isAbsolute()) {
        // Even though this code supports absolute paths, we're going to forbid them anyway, because
        // we might want to write a rule in the future that allows the user to reposition a
        // transitive closure of webfiles into a subdirectory on the web server.
        errors.add(format("%s: Please use relative path for asset: %s", path, relativeDest));
        return;
      }
      Webpath dest = src.lookup(relativeDest);
      if (dest == null) {
        errors.add(format("%s: Could not normalize %s against %s", path, relativeDest, src));
        return;
      }
      if (relationships.put(src, dest) && !accessibleAssets.contains(dest)) {
        Optional<String> label = tryToFindLabelOfTargetProvidingAsset(dest);
        errors.add(format(
            "%s: Referenced %s (%s) without depending on %s",
            path, relativeDest, dest, label.or("a webfiles() rule providing it")));
        return;
      }
    }

    private Optional<String> tryToFindLabelOfTargetProvidingAsset(Webpath webpath) {
      String path = webpath.toString();
      for (Webfiles dep : transitiveDeps.get()) {
        for (WebfilesSource src : dep.getSrcList()) {
          if (path.equals(src.getWebpath())) {
            return Optional.of(dep.getLabel());
          }
        }
      }
      return Optional.absent();
    }
  }

  private static boolean shouldSkipUrl(String uri) {
    return uri.endsWith("/")
        || uri.contains("//")
        || uri.startsWith("data:")
        || uri.startsWith("[")
        || uri.startsWith("{");
  }

  private static String stripQuotes(String value) {
    if (value.charAt(0) == '\'' || value.charAt(0) == '"') {
      return value.substring(1, value.length() - 1);
    }
    return value;
  }
}
