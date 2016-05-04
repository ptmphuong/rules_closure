package com.google.javascript.jscomp;

import static java.nio.charset.StandardCharsets.UTF_8;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.common.collect.Ordering;
import com.google.javascript.jscomp.CompilerOptions.LanguageMode;
import com.google.javascript.jscomp.lint.CheckEnums;
import com.google.javascript.jscomp.lint.CheckJSDocStyle;
import com.google.javascript.jscomp.lint.CheckPrototypeProperties;

import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;
import org.kohsuke.args4j.Option;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

/**
 * Program for incrementally checking JavaScript code.
 *
 * <p>This program is invoked once for each {@code closure_js_library} rule. It validates JS files
 * for most of the really bad syntax errors, but doesn't do robust type checking because it can't
 * take the full program into consideration. This program also performs linting, which is something
 * that does not happen on {@code closure_js_binary} rules.
 *
 * <p>But most importantly, this program does strict dependency checking. It is able to verify that
 * required namespaces are provided by direct dependencies rather than transitive dependencies. It
 * does this in an incremental fashion by producing a txt file containing a sorted list of all
 * namespaces provided by the srcs listed in a {@code closure_js_library}. These files are then
 * accessed via the {@code --deps} flag on subsequent invocations for parent rules.
 */
public final class JsChecker {

  static {
    DiagnosticGroups.registerGroup("duplicateEnumValue", CheckEnums.DUPLICATE_ENUM_VALUE);
    DiagnosticGroups.registerGroup("illegalPrototypeMember",
        CheckPrototypeProperties.ILLEGAL_PROTOTYPE_MEMBER);
    DiagnosticGroups.registerGroup("invalidSuppress", CheckJSDocStyle.INVALID_SUPPRESS);
    DiagnosticGroups.registerGroup("missingJsDoc", CheckJSDocStyle.MISSING_JSDOC);
    DiagnosticGroups.registerGroup("missingReturnJsDoc", CheckJSDocStyle.MISSING_RETURN_JSDOC);
    DiagnosticGroups.registerGroup("mustBePrivate",
        CheckJSDocStyle.MUST_BE_PRIVATE,
        CheckJSDocStyle.MUST_HAVE_TRAILING_UNDERSCORE);
    DiagnosticGroups.registerGroup("optionalParams",
        CheckJSDocStyle.OPTIONAL_PARAM_NOT_MARKED_OPTIONAL,
        CheckJSDocStyle.OPTIONAL_TYPE_NOT_USING_OPTIONAL_NAME);
    DiagnosticGroups.registerGroup("strictDependencies",
        JsCheckerFirstPass.DUPLICATE_PROVIDES,
        JsCheckerFirstPass.REDECLARED_PROVIDES,
        JsCheckerSecondPass.NOT_PROVIDED);
    DiagnosticGroups.registerGroup("strictSetTestOnly", JsCheckerFirstPass.INVALID_SETTESTONLY);
  }

  private enum Convention {
    CLOSURE(new ClosureCodingConvention()),
    GOOGLE(new GoogleCodingConvention()),
    JQUERY(new JqueryCodingConvention());

    final CodingConventions.Proxy convention;

    private Convention(CodingConventions.Proxy convention) {
      this.convention = convention;
    }
  }

  private static final String USAGE =
      String.format("Usage:\n  java %s [FLAGS]\n", JsChecker.class.getName());

  @Option(
      name = "--label",
      usage = "Name of rule being compiled.")
  private String label = "//ohmygoth";

  @Option(
      name = "--src",
      usage = "JavaScript source files, sans externs.")
  private List<String> sources = new ArrayList<>();

  @Option(
      name = "--extern",
      usage = "JavaScript @externs source files.")
  private List<String> externs = new ArrayList<>();

  @Option(
      name = "--dep",
      usage = "foo-provides.txt files from deps targets.")
  private List<String> deps = new ArrayList<>();

  @Option(
      name = "--convention",
      usage = "Coding convention for linting.")
  private Convention convention = Convention.GOOGLE;

  @Option(
      name = "--language",
      usage = "Language spec of input sources.")
  private LanguageMode language = LanguageMode.ECMASCRIPT5_STRICT;

  @Option(
      name = "--jscomp_off",
      usage = "Means same thing as JSCompiler equivalent.")
  private List<String> offs = Lists.newArrayList();

  @Option(
      name = "--jscomp_warning",
      usage = "Means same thing as JSCompiler equivalent.")
  private List<String> warnings =
      Lists.newArrayList(
          "checkTypes",
          "extraRequire",
          "lintChecks");

  @Option(
      name = "--jscomp_error",
      usage = "Means same thing as JSCompiler equivalent.")
  private List<String> errors =
      Lists.newArrayList(
          "checkRegExp",
          "missingRequire",
          "extraRequire",
          "strictDependencies");

  @Option(
      name = "--testonly",
      usage = "Indicates a testonly rule is being compiled.")
  private boolean testonly;

  @Option(
      name = "--output",
      usage = "Incremental -provides.txt report output filename.")
  private String output = "";

  @Option(
      name = "--help",
      usage = "Displays this message on stdout and exit")
  private boolean help;

  private boolean run() throws IOException {
    JsCheckerState state = new JsCheckerState(label, testonly);

    // read provided files created by this program on deps
    for (String dep : deps) {
      state.provided.addAll(Files.readAllLines(Paths.get(dep), UTF_8));
    }

    // check syntax and collect state data
    Compiler compiler = new Compiler(System.out);
    CompilerOptions options = new CompilerOptions();
    options.setLanguage(language);
    options.setCodingConvention(convention.convention);
    options.setSkipTranspilationAndCrash(true);
    options.setIdeMode(true);
    options.setWarningsGuard(
        new ComposeWarningsGuard(ImmutableList.of(suppressPath("bazel-out/"))));
    DiagnosticGroups groups = new DiagnosticGroups();
    for (String error : errors) {
      options.setWarningLevel(groups.forName(error), CheckLevel.ERROR);
    }
    for (String warning : warnings) {
      options.setWarningLevel(groups.forName(warning), CheckLevel.WARNING);
    }
    for (String off : offs) {
      options.setWarningLevel(groups.forName(off), CheckLevel.OFF);
    }

    if (language == LanguageMode.ECMASCRIPT6_STRICT
        || language == LanguageMode.ECMASCRIPT6_TYPED) {
      options.setWarningLevel(
          DiagnosticGroups.registerGroup("doodle",
              CheckJSDocStyle.MISSING_PARAMETER_JSDOC,
              CheckJSDocStyle.MISSING_RETURN_JSDOC,
              CheckJSDocStyle.OPTIONAL_PARAM_NOT_MARKED_OPTIONAL,
              CheckJSDocStyle.OPTIONAL_TYPE_NOT_USING_OPTIONAL_NAME),
          CheckLevel.OFF);
    }

    compiler.setPassConfig(new JsCheckerPassConfig(state, options));
    compiler.disableThreads();
    Result result = compiler.compile(getSourceFiles(externs), getSourceFiles(sources), options);
    if (!result.success) {
      return false;
    }

    // write provided file
    if (!output.isEmpty()) {
      Files.write(Paths.get(output), Ordering.natural().immutableSortedCopy(state.provides), UTF_8);
    }

    return true;
  }

  private static WarningsGuard suppressPath(String path) {
    return new ShowByPathWarningsGuard(path, ShowByPathWarningsGuard.ShowType.EXCLUDE);
  }

  private static ImmutableList<SourceFile> getSourceFiles(Iterable<String> filenames) {
    ImmutableList.Builder<SourceFile> result = new ImmutableList.Builder<>();
    for (String filename : filenames) {
      result.add(SourceFile.fromFile(filename));
    }
    return result.build();
  }

  public static void main(String[] args) throws IOException {
    JsChecker checker = new JsChecker();
    CmdLineParser parser = new CmdLineParser(checker);
    parser.setUsageWidth(80);
    try {
      parser.parseArgument(args);
    } catch (CmdLineException e) {
      System.err.println(e.getMessage());
      System.err.println(USAGE);
      parser.printUsage(System.err);
      System.err.println();
      System.exit(1);
    }
    if (checker.help) {
      System.err.println(USAGE);
      parser.printUsage(System.out);
      System.out.println();
    } else {
      if (!checker.run()) {
        System.exit(1);
      }
    }
  }
}
