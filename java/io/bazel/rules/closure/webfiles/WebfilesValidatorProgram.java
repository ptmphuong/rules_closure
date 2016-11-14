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
import static com.google.common.base.Suppliers.memoize;
import static java.nio.charset.StandardCharsets.UTF_8;

import com.google.common.base.Supplier;
import com.google.common.collect.ImmutableList;
import com.google.protobuf.TextFormat;
import io.bazel.rules.closure.program.CommandLineProgram;
import io.bazel.rules.closure.webfiles.BuildInfo.Webfiles;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.file.FileSystem;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/** CLI for {@link WebfilesValidator}. */
public final class WebfilesValidatorProgram implements CommandLineProgram {

  private static final String RESET = "\u001b[0m";
  private static final String BOLD = "\u001b[1m";
  private static final String RED = "\u001b[31m";
  private static final String ERROR_PREFIX = String.format("%s%sERROR:%s ", BOLD, RED, RESET);

  private final PrintStream output;
  private final FileSystem fs;
  private final WebfilesValidator validator;

  public WebfilesValidatorProgram(
      PrintStream output,
      FileSystem fs,
      WebfilesValidator validator) {
    this.output = checkNotNull(output, "output");
    this.fs = checkNotNull(fs, "fs");
    this.validator = checkNotNull(validator, "validator");
  }

  @Override
  public Integer apply(Iterable<String> args) {
    try {
      return run(args);
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  private int run(Iterable<String> args) throws IOException {
    Webfiles target = null;
    List<Webfiles> directDeps = new ArrayList<>();
    final List<Path> transitiveDeps = new ArrayList<>();
    Iterator<String> flags = args.iterator();
    while (flags.hasNext()) {
      String flag = flags.next();
      switch (flag) {
        case "--dummy":
          Files.write(fs.getPath(flags.next()), new byte[0]);
          break;
        case "--target":
          target = loadWebfilesPbtxt(fs.getPath(flags.next()));
          break;
        case "--direct_dep":
          directDeps.add(loadWebfilesPbtxt(fs.getPath(flags.next())));
          break;
        case "--transitive_dep":
          transitiveDeps.add(fs.getPath(flags.next()));
          break;
        default:
          throw new RuntimeException("Unexpected flag: " + flag);
      }
    }
    if (target == null) {
      output.println(ERROR_PREFIX + "Missing --target flag");
      return 1;
    }
    ImmutableList<String> errors =
        validator.validate(
            target,
            directDeps,
            memoize(
                new Supplier<ImmutableList<Webfiles>>() {
                  @Override
                  public ImmutableList<Webfiles> get() {
                    ImmutableList.Builder<Webfiles> builder = new ImmutableList.Builder<>();
                    for (Path path : transitiveDeps) {
                      try {
                        builder.add(loadWebfilesPbtxt(path));
                      } catch (IOException e) {
                        throw new RuntimeException(e);
                      }
                    }
                    return builder.build();
                  }
                }));
    if (errors.isEmpty()) {
      return 0;
    }
    for (String error : errors) {
      output.println(ERROR_PREFIX + error);
    }
    return 1;
  }

  private static Webfiles loadWebfilesPbtxt(Path path) throws IOException {
    Webfiles.Builder build = Webfiles.newBuilder();
    TextFormat.getParser().merge(new String(Files.readAllBytes(path), UTF_8), build);
    return build.build();
  }
}
