# java-learning

Java features by versions

A multi-module Gradle project, with one subproject per JDK version. Each
subproject is pinned to a Gradle Java toolchain for that version, so the code
in it actually compiles/runs against that JDK — not just whatever JDK happens
to be on your PATH.

## Modules

- [`jdk8/`](jdk8) — lambdas & functional interfaces, method references, default/static interface methods, Streams + `Collectors`, `Optional`, `java.time`, `CompletableFuture`. Each class has a `main` you can run directly.
- [`jdk11/`](jdk11) — new `String` methods (`isBlank`/`strip`/`lines`/`repeat`), `Files.readString`/`writeString`, `var` in lambda parameters, the `java.net.http.HttpClient` (sync/async/timeouts, tested against a local `HttpServer`), and small API additions (`Predicate.not`, `Optional.isEmpty`, `Pattern.asMatchPredicate`). Each class has a `main` you can run directly.
- [`jdk17/`](jdk17) — sealed classes/interfaces (all three subclass modifiers: `final`/`sealed`/`non-sealed`), records (compact constructors, validation, defensive accessors), pattern matching for `instanceof` (incl. flow scoping), switch expressions (`->`/`yield`, exhaustive enum switches), text blocks, enhanced random generators (`RandomGenerator`, `RandomGeneratorFactory`, jumpable streams), and helpful NullPointerExceptions. Each class has a `main` you can run directly.
- [`jdk21/`](jdk21) — pattern matching for `switch` (guards, `null` cases, exhaustive sealed switches) + nested record patterns, virtual threads (incl. 10,000 concurrently blocking), sequenced collections/sets/maps, plus JDK 18's UTF-8-by-default and Simple Web Server API. Each class has a `main` you can run directly. (Deliberately skips structured concurrency, string templates, and scoped values — all preview-only in 21, which would force `--enable-preview` on the whole module; scoped values are covered finalized in [`jdk25/`](jdk25) instead.)
- [`jdk25/`](jdk25) — scoped values (nested rebinding, virtual-thread inheritance), module import declarations, compact source files + instance main methods (JEP 512 — note `CompactSourceMainDemo.java` deliberately lives in the default package at the source root, since compact source files can't have a package declaration), the Key Derivation Function API (`javax.crypto.KDF`/HKDF), Stream Gatherers (built-in + a custom one), unnamed variables/patterns (`_`), flexible constructor bodies, and the Class-File API. Each class has a `main` you can run directly.

## Usage

Build everything (Gradle will auto-download any JDK toolchain it needs, via
the foojay resolver, the first time you build):

```bash
./gradlew build
```

Run tests for one module:

```bash
./gradlew :jdk21:test
```

Add a new module for a future JDK version by creating a directory with a
`build.gradle` (see any existing module for the toolchain block) and adding
`include 'jdkXX'` to [`settings.gradle`](settings.gradle).
