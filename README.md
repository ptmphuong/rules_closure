# Closure Rules for Bazel (αlpha) [![Build Status](https://travis-ci.org/bazelbuild/rules_closure.svg?branch=master)](https://travis-ci.org/bazelbuild/rules_closure)

JavaScript | Templating | Stylesheets
--- | --- | ---
[closure_js_library](#closure_js_library) | [closure_template_js_library](#closure_template_js_library) | [closure_css_library](#closure_css_library)
[closure_js_binary](#closure_js_binary) | [closure_template_java_library](#closure_template_java_library) | [closure_css_binary](#closure_css_binary)
[closure_js_deps](#closure_js_deps) | [closure_template_py_library](#closure_template_py_library) |
[closure_js_test](#closure_js_test) | |
[closure_js_lint_test](#closure_js_lint_test) | |
[closure_js_check_test](#closure_js_check_test) | |

## Overview

The Closure Rules provide a polished JavaScript build system for [Bazel][bazel]
that emphasizes type safety, strictness, testability, and optimization. These
rules are built with the [Closure Tools][closure-tools], which are what Google
used to create websites like Google.com and Gmail.

The mission of this project is to faithfully reproduce Google's internal
JavaScript development praxis. This is a very "ivory tower" approach to web
development. Our goal is to make it accessible to everyone—without sacrificing
any of its excellence.

### Caveat Emptor

This project is currently in an alpha state. It is not yet ready for general
consumption. Many of the features have not been implemented. The definitions of
these rules will change radically as we continue to collect feedback from
experienced engineers. There are also [launch blocking issues][blockers] that
cause this project to not work as advertised.

### What's Included

The Closure Rules bundle the following tools and makes them "just work."

- [Closure Compiler][closure-compiler]: Type-safe, null-safe, optimizing
  JavaScript compiler that transpiles [ECMASCRIPT6][es6] to minified ES3
  JavaScript that can run in any browser.
- [Closure Library][closure-library]: Google's core JavaScript libraries.
- [Closure Templates][closure-templates]: Type-safe HTML templating system that
  compiles to both JavaScript and Java. This is one of the most secure
  templating systems available. It's where Google has put the most thought into
  preventing things like XSS attacks. It also supports i18n and l10n.
- [Closure Stylesheets][closure-stylesheets]: CSS compiler supporting class name
  minification, variables, functions, conditionals, mixins, and bidirectional
  layout.
- [Closure Linter][closure-linter]: Automated style checker and fixer.
- [PhantomJS][phantomjs]: Headless web browser used for automating JavaScript
  unit tests in a command line environment.
- [Bazel][bazel]: The build system Google uses to manage a repository with
  petabytes of code.

## Setup

First you must [install][bazel-install] Bazel. Then you must add the following
to your `WORKSPACE` file:

```python
git_repository(
    name = "io_bazel_rules_closure",
    remote = "https://github.com/bazelbuild/rules_closure.git",
    tag = "0.0.1",
)

load("@io_bazel_rules_closure//closure:defs.bzl", "closure_repositories")
closure_repositories()
```

You are not required to install the Closure Tools or PhantomJS. They will be
fetched automatically.


## Examples

Please see the test directories within this project for concrete examples of usage:

- [//closure/testing/test](https://github.com/bazelbuild/rules_closure/tree/master/closure/testing/test)
- [//closure/compiler/test](https://github.com/bazelbuild/rules_closure/tree/master/closure/compiler/test)
- [//closure/library/test](https://github.com/bazelbuild/rules_closure/tree/master/closure/library/test)
- [//closure/stylesheets/test](https://github.com/bazelbuild/rules_closure/tree/master/closure/stylesheets/test)


# Reference


## closure\_js\_library

```python
load("@io_bazel_rules_closure//closure:defs.bzl", "closure_js_library")
closure_js_library(name, srcs, externs, deps, language, depmode, exports)
```

Defines a set of JavaScript sources or externs.

This rule does not incrementally compile sources. It must be used in conjunction
with a `closure_js_binary` target, which performs full program compilation.

Strict dependency checking is performed on the sources listed in each library
targets. See the documentation of the `deps` attribute for further information.

### Arguments

- **name:** ([Name][name]; required) A unique name for this rule. The standard
  convention is that this be the same name as the Bazel package, unless
  `depmode = "NONE"`, in which case this attribute should be the name of the
  `.js` source with a `_lib` suffix.

- **srcs:** (List of [labels][labels]; optional) The list of `.js` source files
  that represent this library. This attribute is required unless the `exports`
  attribute is being defined. Files listed under this attribute must not use the
  `@externs` annotation.

- **externs:** (List of [labels][labels]; optional) A list of `.js` files
  annotated `@externs` at the top of the file. If this attribute is specified,
  `srcs` must be empty. These files tell the Closure Compiler about the type
  signatures of external libraries. Please note that the externs for web
  browsers are enabled by default by the Closure Compiler.

- **deps:** (List of [labels][labels]; optional) Direct [dependency][dependency]
  list. These can point to `closure_js_library`, `closure_template_js_library`,
  and `closure_css_library` rules.

  JavaScript dependencies are checked by Bazel at compile-time. The transitive
  dependencies of these libraries are not taken into consideration when
  performing this strict dependency checking.

  This rule also checks CSS dependencies at compile-time. The build will fail if
  the class names referenced in sources using `goog.getCssName()` are not
  provided by the `closure_css_library` listed in `deps`.

- **language:** (String; optional; default is `"ECMASCRIPT5_STRICT"`) Variant of
  JavaScript in which `srcs` are written. The following are valid options:

  - `ECMASCRIPT6_TYPED`: (Experimental) Superset of ES6 which adds
    Typescript-style type declarations. Always strict.
  - `ECMASCRIPT6_STRICT`: Nitpicky, shiny new JavaScript.
  - `ECMASCRIPT5_STRICT`: Nitpicky, traditional JavaScript.
  - `ECMASCRIPT6`: Shiny new JavaScript.
  - `ECMASCRIPT5`: Traditional JavaScript.
  - `ECMASCRIPT3`: 90's JavaScript.
  - `ANY`: Indicates sources are compatible with any variant of JavaScript.

  Maintaining this attribute for your library rules is important because
  `closure_js_binary` checks the `language` attribute of dependencies to
  determine if it's a [legal combination](https://i.imgur.com/38lybNO.png)
  that's safe to compile. Combinations that traverse a red line cause strictness
  to decay and a warning will be emitted. For example, if just one library is
  unstrict, then strictness will be removed for your entire binary.  Therefore
  we *strongly* recommend that you use strict variants.

  **ProTip:** You are not required to put `"use strict"` at the tops of your
  files. The Closure Compiler generates that in the output for you.

  The default language is ECMASCRIPT5_STRICT for three reasons. First, we want
  to make the most conservative recommendation possible. Some ES6 features have
  not yet been implemented in the Closure Compiler. We're working on
  that. Secondly, it upgrades easily into ECMASCRIPT6\_STRICT and
  ECMASCRIPT6\_TYPED, should you choose to use them later. Thirdly, PhantomJS
  only supports ECMASCRIPT5\_STRICT, so your unit tests will be able to run
  lightning fast in raw sources mode if you write your code exclusively in that
  language. (XXX: Unfortunately a [bug][phantomjs-bug] in PhantomJS is blocking
  this at the moment.)

- **depmode:** (String; optional; default is `"CLOSURE"`) Indicates how
  dependencies work within a particular library. This flag is used to calculate
  which value is passed to the `--dependency_mode` flag of the Closure
  Compiler. The following are valid options:

  - `CLOSURE`: Indicates you are are using Closure Library `goog.provide` and
    `goog.require` or `goog.module` statements to manage your JavaScript
    dependencies. When using this option, each source file in a library must
    provide a symbol.

  - `ES6MODULES`: Indicates you're using the standard ECMASCRIPT6 module syntax.
    Strict dependency checking is not supported for this flag at this time.

  - `COMMONJS`: Indicates you're using the `require()` and `exports.foo` syntax
    frequently used by Node.js projects. Strict dependency checking is currently
    not supported for this attribute.

  - `NONE`: Indicates the sources in this library use no dependency directives
    at all. When this attribute is used, only a single file may be specified in
    `srcs`. This is because `closure_js_binary` will have to rely on the partial
    ordering of `closure_js_library` rules in order to determine declaration
    order. This attribute also affects the `--dependency_mode` flag passed to
    the Closure Compiler. If a binary references a single rule with
    `depmode=NONE` then `--dependency_mode` will decay from STRICT to LOOSE. If
    *all* JS libraries use `depmode="NONE"`, then `--dependency_mode` will be
    set to NONE.

- **exports:** (List of [labels][labels]; optional) Listing dependencies here
  will cause them to become *direct* dependencies in parent rules. This
  functions similarly to [java_library.exports][java-exports]. This can be used
  to create aliases or bundle libraries together. However this should be done
  sparingly. If this attribute is specified, then the `srcs`, `externs`, and
  `deps` attributes may not be used.

### Referencing the Closure Library

In order to use `goog.provide` and `goog.require` in your javascript code, make
sure to add the Closure Library as a dependency in `closure_js_library` rules:

```python
closure_js_library(
    name = "my_js_library",
    srcs = glob(["*.js"]),
    deps = [
        "@io_bazel_rules_closure//closure/library",
    ],
)
```


## closure\_js\_binary

```python
load("@io_bazel_rules_closure//closure:defs.bzl", "closure_js_binary")
closure_js_binary(name, deps, main, css, pedantic, debug, language,
                  compilation_level, formatting, defs)
```

Turns JavaScript libraries into a minified optimized blob of code.

This rule must be used in conjunction with `closure_js_library`.

#### Implicit Output Targets

- *name*.js: A minified JavaScript file containing all transitive sources.

- *name*.js.map: Sourcemap file mapping compiled sources to their raw
  sources. This file can be loaded into browsers such as Chrome and Firefox to
  view a stacktrace when an error is thrown by compiled sources.

### Arguments

- **name:** ([Name][name]; required) A unique name for this rule. Convention
  states that such rules be named `foo_bin` or `foo_dbg` if `debug = True`.

- **deps:** (List of [labels][labels]; required) Direct dependency list. This
  attribute has the same meaning as it does in `closure_js_library`. These can
  point to `closure_js_library` and `closure_template_js_library` rules.

- **css:** (List of [labels][labels]; optional) CSS class renaming target, which
  must point to a `closure_css_binary` rule. This causes the CSS name mapping
  file generated by the CSS compiler to be included in the compiled JavaScript.
  This tells Closure Compiler how to minify CSS class names.

  This attribute is required if any of JavaScript or template sources depend on
  a `closure_css_library`. This rule will check that all the referenced CSS
  libraries are present in the CSS binary.

- **pedantic:** (Boolean; optional; default is `0`) Setting this flag to `1`
  will turn on every single warning, and treat warnings as errors. Your reward
  is that type-based optimizations becomes enabled.

  This flag is recommended for greenfield projects, however *caveat emptor*
  applies. Some of the checks that get enabled aren't yet mature. The Closure
  Compiler might do something crazy like generate synthetic code that doesn't
  validate. If that happens, please file an [issue][compiler-issue].

  One benefit of pedantic mode is null safety. **ProTip:** The Closure Compiler
  will take into consideration `goog.asserts.assert` statements and conditionals
  like `if (foo != null)`.

- **debug:** (Boolean; optional; default is `0`) Enables debug mode. Many types
  of properties and variable names will be renamed to include `$` characters, to
  help you spot bugs when using `ADVANCED` compilation mode. Assert statements
  will not be stripped. Dependency directives will be removed.

- **language:** (String; optional; default is `"ECMASCRIPT3"`) Output language
  variant to which library sources are transpiled. The default is ES3 because it
  works in all browsers. The input language is calculated automatically based on
  the `language` attribute of `closure_js_library` dependencies.

- **compilation_level:** (String; optional; default is `"ADVANCED"`) Specifies
  how minified you want your JavaScript binary to be. Valid options are:

  - `ADVANCED`: Enables maximal minification and type checking. This is
    *strongly* recommended for production binaries. **Warning:** Properties that
    are accessed with dot notation will be renamed. Use quoted notation if this
    presents problems for you, e.g. `foo['bar']`, `{'bar': ...}`.

  - `SIMPLE`: Tells the Closure Compiler to function more like a traditional
    JavaScript minifier. Type checking becomes disabled. Local variable names
    will be minified, but object properties and global names will
    not. Namespaces will be managed. Code that will never execute will be
    removed. Local functions and variables can be inlined, but globals can not.

  - `WHITESPACE_ONLY`: Tells the Closure Compiler to strip whitespace and
    comments. Transpilation between languages will still work. Type checking
    becomes disabled. No symbols will not be renamed. Nothing will be inlined.
    Dependency statements will not be removed. **ProTip:** If you're using the
    Closure Library, you'll need to look into the `CLOSURE_NO_DEPS` and
    `goog.ENABLE_DEBUG_LOADER` options in order to execute the compiled output.)

- **formatting:** (String; optional) Specifies what is passed to the
  `--formatting` flag of the Closure Compiler. The following options are valid:

  - `PRETTY_PRINT`
  - `PRINT_INPUT_DELIMITER`
  - `SINGLE_QUOTES`

- **defs:** (List of strings; optional) Specifies additional flags to be passed
  to the Closure Compiler, e.g. `"--hide_warnings_for=some/path/"`. To see what
  flags are available, run:
  `bazel run @io_bazel_rules_closure//closure/compiler -- --help`

### Support for AngularJS

When compiling AngularJS applications these flags should be used for
compatibility:
- `defs=["--angular_pass", "--export_local_property_definitions"]`

## closure\_js\_test

```python
load("@io_bazel_rules_closure//closure:defs.bzl", "closure_js_test")
closure_js_test(name, srcs, deps, language)
```

Runs JavaScript unit tests inside a headless web browser.

PhantomJS (QtWebKit) is used to run tests. This program does not need to be
installed separately; it is fetched automatically by Bazel. In the future, other
testing environments may be supported, e.g. SlimerJS, Node.js, JSDom, etc.

#### Test Definitions

A test is defined as any function in the global namespace that begins with
`test`. If you don't have a global namespace, because you're either using
modules or `goog.scope`, then you must register your test functions with
`goog.testing.testSuite`.

Each `foo_test.js` file listed in `srcs` is tested individually. Each test file
is loaded into a separate global namespace; therefore, they may not reference
each other. Test helper functions must be defined in a dependent
`closure_js_library` with `testonly = True`.

Any JavaScript file related to testing is strongly recommended to contain a
`goog.setTestOnly()` statement in the file. However this is not required,
because some projects might not want to directly reference Closure Library
functions.

#### Assertions

You do not need to require `goog.testing.jsunit` and `goog.testing.asserts`
because they are loaded automatically by the test runner. These modules define
useful [testing functions][asserts] such as `assertEquals()`.

#### No Type Safety

Don't bother using type notation in your unit tests because it won't be checked.
Type safety is not feasible in unit tests because the mocking utilities in
Closure Library make the type checker angry.

This offers certain advantageous, such as the ability to override `const`
definitions and access private members; however, you should test by public APIs
whenever possible.

#### No Network Access

Your test will run within a hermetically sealed environment. You are not allowed
to send HTTP requests to any external servers. It is expected that you'll use
Closure Library mocks for things like XHR. However a local HTTP server is
started up on a random port that allows to request runfiles under the `/filez/`
path.

### Arguments

- **name:** ([Name][name]; required) A unique name for this rule.

- **srcs:** (List of [labels][labels]; required) A list of `_test.js` source
  files that represent this library.

- **deps:** (List of [labels][labels]; optional) Direct dependency list. This
  has the same meaning as it does in `closure_js_binary`.

- **language:** (String; optional; default is `"ECMASCRIPT5_STRICT"`) Variant of
  JavaScript in which `srcs` are written. See the `closure_js_library`
  documentation for more information.


## closure\_js\_lint\_test

```python
load("@io_bazel_rules_closure//closure:defs.bzl", "closure_js_lint_test")
closure_js_check_test(name, srcs, errors, defs)
```

Tests JavaScript sources for conformance to [Google JavaScript Style][jsstyle]
and Google Closure Style.

If the sources do not conform, then the error message will show you a command
you can run to fix your source code.

### Arguments

- **name:** ([Name][name]; required) A unique name for this rule.

- **srcs:** (List of [labels][labels]; required) JavaScript files to check.
  These can be normal sources or externs files.

- **errors:** (List of strings; optional; default is `["all"]`) Which errors to
  enable.

  - `all`: Enables all following errors.
  - `blank_lines_at_top_level`: Validates number of blank lines between blocks
    at top level.
  - `indentation`: Checks correct indentation of code.
  - `well_formed_author`: Validates the `@author` JsDoc tags.
  - `no_braces_around_inherit_doc`: Forbids braces around `@inheritdoc` JsDoc
    tags.
  - `braces_around_type`: Enforces braces around types in JsDoc tags.
  - `optional_type_marker`: Checks correct use of optional marker = in param
    types.
  - `unused_private_members`: Checks for unused private variables.
  - `unused_local_variables`: Checks for unused local variables.

- **defs:** (List of strings; optional) Additional flags to pass to the gjslint
  command. Some useful ones are: `--closurized_namespaces=ns1,ns2` and
  `--ignored_extra_namespaces=goog.testing.asserts`.


## closure\_js\_check\_test

```python
load("@io_bazel_rules_closure//closure:defs.bzl", "closure_js_check_test")
closure_js_check_test(name, deps, pedantic, defs)
```

Performs type checking on JavaScript libraries without producing a binary.

This rule is useful in certain circumstances, such as Node.js applications,
where the developer does not need minimization but still desires the language
safety benefits offered by the Closure Compiler in ADVANCED compilation mode.

### Arguments

- **name:** ([Name][name]; required) A unique name for this rule.

- **deps:** (List of [labels][labels]; optional) Direct dependency list. This
  has the same meaning as it does in `closure_js_binary`. If this attribute is
  empty, `srcs` must be specified.

- **pedantic** (Boolean; optional; default is `0`) See the documentation for
  `pedantic` under `closure_js_binary`.

- **defs:** (List of strings; optional) Specifies additional flags to be passed
  to the Closure Compiler, e.g. `"--hide_warnings_for=some/path/"`. To see what
  flags are available, run:
  `bazel run @io_bazel_rules_closure//closure/compiler -- --help`


## closure\_js\_deps

```python
load("@io_bazel_rules_closure//closure:defs.bzl", "closure_js_deps")
closure_js_deps(name, deps)
```

Generates a dependency file, for an application using the Closure Library.

Generating this file is necessary for running an application in raw sources
mode, because it tells the Closure Library how to load namespaces from the web
server that are requested by `goog.require()`.

For example, if you've made your source runfiles available under a protected
admin-only path named `/filez/`, then raw source mode could be used as follows:

```html
<script src="/filez/external/closure_library/closure/goog/base.js"></script>
<script src="/filez/myapp/deps-runfiles.js"></script>
<script>goog.require('myapp.main');</script>
<script>myapp.main();</script>
```

#### Implicit Output Targets

- *name*.js: A JavaScript source file containing `goog.addDependency()`
  statements which map Closure Library namespaces to JavaScript source paths.
  Each path is expressed relative to the location of the Closure Library
  `base.js` file. The paths in this file will contain direct references to the
  files in Bazel's output directories.

- *name*-runfiles.js: This file is the same as *name*.js except its paths will
  not contain any of the weird Bazel output directories. This is the file that
  you want to use when loading sources from a web server.

### Arguments

- **name:** ([Name][name]; required) A unique name for this rule. Convention
  states that this be `"deps"`.

- **deps:** (List of [labels][labels]; required) List of `closure_js_library`
  and `closure_template_js_library` targets which define all JavaScript sources
  in your application.


## closure\_template\_js\_library

```python
load("@io_bazel_rules_closure//closure:defs.bzl", "closure_template_js_library")
closure_template_js_library(name, srcs, deps, globals, plugin_modules,
                            should_generate_js_doc,
                            should_provide_require_soy_namespaces,
                            should_generate_soy_msg_defs,
                            soy_msgs_are_external)
```

Compiles Closure templates to JavaScript source files.

This rule is necessary in order to render Closure templates from within
JavaScript code.

This rule pulls in a transitive dependency on Closure Library.

The documentation on using Closure Templates can be found
[here][closure-templates].

For additional help on using some of these attributes, please see the output of
the following:

    bazel run @io_bazel_rules_closure//closure/templates:SoyToJsSrcCompiler -- --help

#### Implicit Output Targets

- *src*.js: A separate JavaScript source file is generated for each file listed
  under `srcs`. The filename will be the same as the template with a `.js`
  suffix. For example `foo.soy` would become `foo.soy.js`.

### Arguments

- **name:** ([Name][name]; required) A unique name for this rule.

- **srcs:** (List of [labels][labels]; required) A list of `.soy` source files
  that represent this library.

- **deps:** (List of [labels][labels]; optional) List of `closure_js_library`
  and `closure_template_js_library` targets which define symbols referenced by
  the template.

- **globals:** (List of [labels][labels]; optional) List of text files
  containing symbol definitions that are only considered at compile-time. For
  example, this file might look as follows:

      com.foo.bar.Debug.PRODUCTION = 0
      com.foo.bar.Debug.DEBUG = 1
      com.foo.bar.Debug.RAW = 2

- **plugin_modules:** (List of [labels][labels]; default is `[]`) Passed along
  verbatim to the SoyToJsSrcCompiler above.

- **should_generate_js_doc:** (List of [labels][labels]; default is `1`) Passed
  along verbatim to the SoyToJsSrcCompiler above.

- **should_provide_require_soy_namespaces:** (List of [labels][labels]; default
  is `1`) Passed along verbatim to the SoyToJsSrcCompiler above.

- **should_generate_soy_msg_defs:** (List of [labels][labels]; default is `0`)
  Passed along verbatim to the SoyToJsSrcCompiler above.

- **soy_msgs_are_external:** (List of [labels][labels]; default is `0`) Passed
  along verbatim to the SoyToJsSrcCompiler above.


## closure\_template\_java\_library

```python
load("@io_bazel_rules_closure//closure:defs.bzl", "closure_template_java_library")
closure_template_java_library(name, srcs, deps, java_package)
```

Compiles Closure templates to Java source files.

This rule is necessary in order to serve Closure templates from a Java backend.

Unlike `closure_template_js_library`, globals are not specified by this rule.
They get added at runtime by your Java code when serving templates.

This rule pulls in a transitive dependency on Guava, Guice, and ICU4J.

The documentation on using Closure Templates can be found
[here][closure-templates].

For additional help on using some of these attributes, please see the output of
the following:

    bazel run @io_bazel_rules_closure//closure/templates:SoyParseInfoGenerator -- --help

#### Implicit Output Targets

- SrcSoyInfo.java: A separate Java source file is generated for each file
  listed under `srcs`. The filename will be the same as the template, converted
  to upper camel case, with a `SoyInfo.java` suffix. For example `foo_bar.soy`
  would become `FooBarSoyInfo.java`.

### Arguments

- **name:** ([Name][name]; required) A unique name for this rule.

- **srcs:** (List of [labels][labels]; required) A list of `.soy` source files
  that represent this library.

- **deps:** (List of [labels][labels]; optional) Soy files to parse but not to
  generate outputs for.

- **java_package:** (List of [labels][labels]; required) The package for the
  Java files that are generated, e.g. `"com.foo.soy"`.


## closure\_template\_py\_library

TODO


## closure\_css\_library

```python
load("@io_bazel_rules_closure//closure:defs.bzl", "closure_css_library")
closure_css_library(name, srcs, deps)
```

Defines a set of CSS stylesheets.

This rule does not compile your stylesheets; it is used in conjunction with
`closure_css_binary` which produces the minified CSS file.

The documentation on using Closure Stylesheets can be found
[here][closure-stylesheets].

For additional help on using some of these attributes, please see the output of
the following:

    bazel run @io_bazel_rules_closure//closure/stylesheets -- --help

### Arguments

- **name:** ([Name][name]; required) A unique name for this rule. Convention
  states that this end with `_lib`.

- **srcs:** (List of [labels][labels]; required) A list of `.gss` or `.css`
  source files that represent this library.

  The order of stylsheets is `srcs` is undefined. If a CSS file overrides
  definitions in another CSS file, then each file must be specified in separate
  `closure_css_library` targets. That way Bazel can order your CSS definitions
  based on the partial ordering of build targets.

- **deps:** (List of [labels][labels]; optional) List of other
  `closure_css_library` targets on which the CSS files in `srcs` depend.


## closure\_css\_binary

```python
load("@io_bazel_rules_closure//closure:defs.bzl", "closure_css_binary")
closure_css_binary(name, deps, renaming, debug, defs)
```

Defines a set of CSS stylesheets.

The documentation on using Closure Stylesheets can be found
[here][closure-stylesheets].

#### Implicit Output Targets

- *name*.css: A minified CSS file containing all transitive sources.

- *name*.css.map: [CSS sourcemap file][css-sourcemap]. This tells browsers like
  Chrome and Firefox where your CSS definitions are located in their original
  source files.

- *name*.css.js: JavaScript file containing a `goog.setCssNameMapping()`
  statement which tells the Closure Compiler and Library how to minify CSS
  class names. See also the documentation for the `css` attribute of
  `closure_js_binary`.

### Arguments

- **name:** ([Name][name]; required) A unique name for this rule. Convention
  states that such rules be named `foo_bin` or `foo_dbg` if `debug = True`.

- **deps:** (List of [labels][labels]; required) List of `closure_css_library`
  targets to compile. All dependencies must have their `orientation` attribute
  set to the same value.

- **renaming:** (Boolean; optional; default is `1`) Enables CSS class name
  minification. This is one of the most powerful features of the Closure Tools.
  By default, this will turn class names like `.foo-bar` into things like
  `.a-b`. If `debug = True` then it will be renamed `.foo_-bar_`.

  In order for this to work, you must update your JavaScript code to use the
  `goog.getCssName("foo-bar")` when referencing class names. JavaScript
  library targets that reference CSS classes must add the appropriate CSS
  library to its `deps` attribute. The `css` attribute of the
  `closure_js_binary` also needs to be updated to point to this CSS binary
  target, so the build system can verify (at compile time) that your CSS and
  JS binaries are both being compiled in a harmonious way.

  You'll also need update your templates to say `{css foo-bar}` in place of
  class names. The `closure_template_js_library` must also depend on the
  appropriate CSS library.

- **debug:** (Boolean; optional; default is `0`) Enables debug mode, which
  causes the compiled stylesheet to be pretty printed. If `renaming = True` then
  class names will be renamed, but still readable to humans.

- **orientation:** (Boolean; optional; default is `NOCHANGE`) Specify this
  option to perform automatic right to left conversion of the input. You can
  choose between:

  - `NOCHANGE`: Uses same orientation as was specified in dependent libraries.
  - `LTR`: Outputs a sheet suitable for left to right display.
  - `RTL`: Outputs a sheet suitable for right to left display.

  The input orientation is calculated from the `orientation` flag of all
  `closure_css_library` targets listed in `deps`. If the input orientation is
  different than the requested output orientation, then 'left' and 'right'
  values in direction sensitive style rules are flipped. If the input already
  has the desired orientation, this option effectively does nothing except for
  defining `GSS_LTR` and `GSS_RTL`, respectively.

- **vendor:** (String; optional; default is `None`) Creates
  browser-vendor-specific output by stripping all proprietary browser-vendor
  properties from the output except for those associated with this vendor. Valid
  values are:

  - `WEBKIT`
  - `MOZILLA`
  - `MICROSOFT`
  - `OPERA`
  - `KONQUEROR`

  The default behavior is to not strip any browser-vendor properties.

- **defs:** (List of strings; optional) Specifies additional flags to be passed
  to the Closure Stylesheets compiler. To see what flags are available, run:
  `bazel run @io_bazel_rules_closure//closure/stylesheets -- --help`



[asserts]: https://github.com/google/closure-library/blob/master/closure/goog/testing/asserts.js#L1308
[bazel-install]: http://bazel.io/docs/install.html
[bazel]: http://bazel.io/
[blockers]: https://github.com/bazelbuild/rules_closure/labels/launch%20blocker
[closure-compiler]: https://developers.google.com/closure/compiler/
[closure-library]: https://developers.google.com/closure/library/
[closure-linter]: https://developers.google.com/closure/utilities/
[closure-stylesheets]: https://github.com/google/closure-stylesheets
[closure-templates]: https://developers.google.com/closure/templates/
[closure-tools]: https://developers.google.com/closure/
[coffeescript]: http://coffeescript.org/
[compiler-issue]: https://github.com/google/closure-compiler/issues/new
[css-sourcemap]: https://developer.chrome.com/devtools/docs/css-preprocessors
[dependency]: http://bazel.io/docs/build-ref.html#dependencies
[es6]: http://es6-features.org/
[java-exports]: http://bazel.io/docs/be/java.html#java_library.exports
[jsstyle]: https://google.github.io/styleguide/javascriptguide.xml
[jquery]: http://jquery.com/
[labels]: http://bazel.io/docs/build-ref.html#labels
[name]: http://bazel.io/docs/build-ref.html#name
[phantomjs-bug]: https://github.com/ariya/phantomjs/issues/14028
[phantomjs]: http://phantomjs.org/
