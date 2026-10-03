# Build conventions

The root and all Android module builds use Kotlin DSL. This included build owns
shared Android configuration and replaces the former buildSrc and root callbacks.

| Plugin | Configuration |
| --- | --- |
| `teamcityapp.android.application` | Android application and Kotlin plugins, shared SDK/Java/Kotlin targets, lint, unit-test coverage, and instrumentation defaults |
| `teamcityapp.android.library` | Android library and Kotlin plugins, the same shared defaults |
| `teamcityapp.android.library.java` | Android library defaults without Kotlin or kapt; use for Java/resource-only modules |
| `teamcityapp.android.hilt` | Hilt and kapt plugins, Hilt runtime and kapt compiler dependencies |
| `teamcityapp.android.data-binding` | Data Binding for Android application/library modules |
| `teamcityapp.android.coverage` | App aggregate debug coverage and CI report locations |

Apply an application or library convention before the optional Hilt convention:

```kotlin
plugins {
    id("teamcityapp.android.library")
    id("teamcityapp.android.hilt")
    id("teamcityapp.android.data-binding")
}

android {
    namespace = "teamcityapp.features.example"
}
```

Keep feature dependencies, namespaces, flavors, signing, packaging, and any
module-specific overrides in the module script. Modules that use plain Dagger
in addition to Hilt still declare their Dagger runtime/compiler dependencies.
The convention does not add `annotationProcessor` dependencies. Modules needing
Parcelize apply `org.jetbrains.kotlin.plugin.parcelize`; the utils module applies
kapt for Data Binding without Hilt.

`teamcityapp.android.base` is an internal plugin that registers shared defaults
before Android is applied. SDK/JVM defaults and app version settings live in
`src/main/kotlin/Config.kt`. Both the app and About feature use that same app
version. The root build declares plugins without applying them and has no
cross-project Android configuration.

Dependency/plugin versions, including JaCoCo, come from `gradle/libs.versions.toml`.
The included build imports that catalog explicitly. Main build repositories are
centralized in `settings.gradle.kts`; project-level repositories are rejected.

The conventions preserve the current external Kotlin plugin, kapt, and legacy
Android DSL setup. They require the existing `android.builtInKotlin=false` and
`android.newDsl=false` properties; switching to AGP's built-in Kotlin/new DSL is
a separate migration.

## App variants and coverage

The app keeps mock/prod flavors, debug/release builds, release signing, and disabled
mockRelease. `-Pr8Verification` adds the debug-signed minified verification build,
uses its isolated instrumentation sources, disables mapping uploads, and checks
that connected runs execute at least five smoke tests.

`teamcityapp.android.base` enables JaCoCo on unit-test tasks in every Android
module. `teamcityapp.android.coverage` aggregates their execution data using
`generateCodeCoverageReport`. It uses transformed debug/mockDebug classes when
Hilt or Firebase modifies bytecode, and compiler outputs otherwise, preserving
generated-class exclusions. It reads current AGP unit-test coverage outputs
and legacy JaCoCo unit-test execution files. Device coverage stays in a separate
report so the unit upload never includes instrumentation data. When compilation,
tests and reporting are requested together, the report runs after those producers. Reporting alone
can still use restored CI files without triggering builds or tests. XML is written to
`app/build/coverage/generateCodeCoverageReport/generateCodeCoverageReport.xml`;
HTML is written to `build/coverage-report`. The unused legacy PMD script has
been removed; CI's configured static check remains Android lint.

## Verification

Use JDK 17 and the repository wrapper:

```shell
python3 -m unittest discover -s scripts/tests
./gradlew :build-logic:test
./gradlew assembleMockDebug assembleMockDebugAndroidTest testMockDebugUnitTest testDebugUnitTest lintMockDebug
./gradlew testMockDebugUnitTest testDebugUnitTest :app:generateCodeCoverageReport
python3 scripts/prepare-r8-verification.py
./gradlew -Pr8Verification :app:assembleProdR8Verification :app:assembleProdR8VerificationAndroidTest
```

Prepare debug Google Services files as described in AGENTS.md before app builds.
Functional tests use isolated Kotlin DSL builds to verify module overrides,
Hilt dependency registration, Data Binding, Java-only modules, and aggregate
coverage inputs/exclusions/output paths. Focused tests preserve the R8 report
count guard. CI runs these tests alongside the app and feature unit tests.

## CI execution and caching

CI starts `Build` (mock debug), `Build minified` (production R8 verification),
`Checks` (lint), and `Unit tests` independently. The unit-test invocation also
generates aggregate coverage after the test and compilation tasks;
the Coverage job downloads that report and uploads it to Codecov without starting
Gradle or rebuilding classes. Unit-test coverage runs on every branch, even
when Marathon is paused, and its upload does not wait for APK builds or
instrumentation tests. The report artifact includes XML and HTML.

Both mock debug and minified production verification APKs are always built. Test
APKs and their upload run only when `ENABLE_MARATHON_TESTS` is `true`, the branch
does not start with `chore-`, and a pull request originates in this repository.
The Marathon job waits for both APK jobs and downloads their separate mock
and R8 test artifacts, using the mock build's policy output so preparation
and execution stay aligned. Pausing Marathon therefore skips instrumentation
compilation too.

When Marathon is enabled, the mock APK build also passes
`-PinstrumentationCoverage` and runs `:app:prepareInstrumentationCoverageInputs`.
Only the debug application is instrumented; minified R8 and release builds keep
their existing configuration. The task exports matching uninstrumented classes,
source files, and the catalog-pinned JaCoCo CLI in
`app/build/coverage/instrumentation-inputs/instrumentation-coverage-inputs.zip`.
It does not run unit tests or generate a coverage report.

The mock Marathon run uses CLI 1.0.64 with `--code-coverage true`. Each active
matrix job converts its downloaded `.ec`/`.exec` files (including device
archives) into JaCoCo XML using `scripts/generate-instrumentation-coverage.py`.
Missing execution data or mismatched classes fail the job rather than upload
an empty or inaccurate report. Report generation needs Java 17, without Gradle
or compilation on the Marathon runner. R8 smoke tests retain their existing action.

Unit and instrumentation reports upload independently with the `unit` and
`instrumentation` Codecov flags and the same PR head/commit SHA. Codecov merges
all uploads into the commit's overall coverage, with separate flag views.
Both flags join the total and disable carryforward; when Marathon is skipped,
only that commit's unit coverage is reported. There is no fixed upload-count
requirement, and the PR comment updates as reports arrive. Instrumentation XML
artifacts are named `instrumentation-coverage-<android-version>`.

Gradle task-output caching and parallel module execution are enabled in
`gradle.properties`. CI uses the enhanced Gradle cache provider for fallback
restoration and separate job caches. Default-branch builds and same-repository
PRs save caches; fork PRs only read them. The first run after switching providers
can start cold, while subsequent runs of the same PR can reuse compiled outputs.
