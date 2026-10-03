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
the same job uploads its XML directly to Codecov. Unit-test coverage runs on
every branch, and its upload does not wait for APK builds or instrumentation
tests. The report artifact includes XML and HTML.

Both mock debug and minified production verification applications are built on
every invocation. Same-repository PR validation runs changed-feature
instrumentation tests plus the five-test smoke suite when application, Android
test, or build inputs change. Documentation, CI-only, housekeeping, and empty
diffs skip UI tests. PR comments
do not trigger CI. Fork PRs cannot receive the Marathon secret and skip paid UI
tests. A push to the default branch (`dev`) runs both full UI suites when the push
changes application, Android test, or build inputs. Documentation and CI-only
merges skip both suites. This is not a pre-merge gate.

Manual dispatch defaults to changed + smoke. Check **Override changed + smoke
with the selected UI suite** to run one of the existing four manual choices.
Select the PR's head branch in the Run workflow branch dropdown. There are no
repository enable/disable variables.

The policy job compares the entire PR head with its base using a merge-base diff,
including both paths of renames and deleted files. Default-branch pushes compare
the event's `before` and `after` tips directly, covering every commit in a push
and changes removed by a force push. Manual changed runs compare with the default
branch. It discovers current non-ignored methods from changed
test classes and selects feature tests by source-folder/package conventions.
`scripts/marathon-changes.json` maps resource paths and cross-package tests, such
as build-log lifecycle tests under Hilt. Device validation is limited to
non-Markdown files under `app/`, `features/`, `libraries/`, `build-logic/`, and
`gradle/`, plus root Gradle build/settings/properties files, wrapper scripts, and
mock Google Services configurations. Changes only to documentation, workflows,
CI scripts and their Python tests, or other repository files skip instrumentation,
including smoke tests, and produce no selection artifact. Relevant changes
always include smoke tests, and selections are deduplicated. Explicit manual
suite overrides still run regardless of the changed files. Shared
or unmapped application inputs, or an unavailable PR/manual diff baseline, broaden
the selection to all eligible mock instrumentation tests. An unavailable push
baseline (including a newly created branch) preserves both full UI suites.
PR/manual fallback still uses the changed
suite, without R8 execution or device coverage. The selection artifact and job
summary record changed files, matched features, and fallback paths/reasons.

Manual overrides support four suite choices:

| Suite | Selection | Marathon jobs |
| --- | --- | --- |
| `full` (default) | All instrumentation tests and all R8 smoke tests | Two parallel jobs |
| `smoke` | Five explicit core-flow instrumentation tests | Mock instrumentation only |
| `single` | `test_name` in `package.TestClass#testMethod` form | Mock instrumentation only |
| `random` | One test from the maintained eight-test eligible pool | Mock instrumentation only |

`scripts/marathon-tests.json` defines the smoke and random catalogs. Smoke covers
login startup, guest-account creation, project loading, build loading, and
settings injection/recreation. Random also includes three other screen
injection/recreation tests. Change these catalogs to adjust the subsets; the
Python checks reject missing or ignored tests. Single mode supports the current
non-ignored Kotlin JUnit4 instrumentation tests, including tests outside these
catalogs. Its source preflight does not support inherited or parameterized test
selectors.

Random selection uses a SHA-256 seed and a sorted eligible list. By default the
seed contains the commit SHA, workflow run ID, and run attempt; supply
`random_seed` to repeat a selection on the same revision. The job summary and
`ui-test-selection` artifact record the suite, selected tests, and seed. A
Marathon YAML allowlist uses exact fully-qualified test names. JUnit verification
rejects zero successful tests, skipped selections, missing selected tests, and
unexpected tests. R8 verification requires at least five successful tests.

All Marathon jobs use Android OS version 17 with the `google_apis` image and CLI
1.0.64. Each job downloads its matching APK artifact and invokes Marathon once.
They wait for the APK builds and require `MARATHON_CLOUD_API_TOKEN`. Changed and
smoke runs build the mock application and test APK without device instrumentation,
coverage-input preparation, or coverage reporting/upload. Non-full runs omit the
R8 test APK and paid R8 execution. Superseded
PR validation is cancelled; manual and default-branch runs have unique
concurrency groups so a later run does not cancel their UI tests.

For full, single, or random runs, the mock APK build passes
`-PinstrumentationCoverage` and runs `:app:prepareInstrumentationCoverageInputs`.
Only the debug application is instrumented; minified R8 and release builds keep
their existing configuration. The task uses the tested app variant's scoped
class artifacts, including its transformed dependency JARs, so library class IDs
match the APK. It exports those uninstrumented classes, source files, and the
catalog-pinned JaCoCo CLI in
`app/build/coverage/instrumentation-inputs/instrumentation-coverage-inputs.zip`.
It does not run unit tests or generate a coverage report.

Full, single, and random mock runs collect device coverage and convert downloaded
`.ec`/`.exec` files (including device archives) into JaCoCo XML using
`scripts/generate-instrumentation-coverage.py`. Missing execution data or
mismatched classes fail the job rather than upload an empty or inaccurate
report. Report generation needs Java 17, without Gradle or compilation on the
Marathon runner. The minified R8 run disables coverage collection and skips the
coverage-input download and report generation.

Unit and full instrumentation reports upload independently with the `unit` and
`instrumentation` Codecov flags and the same PR head/commit SHA. Both flags join
the total and enable carryforward. When only unit coverage is uploaded, Codecov
updates it and retains the last available instrumentation baseline. Both suites
must have a complete initial upload before they can be carried forward; retained
coverage represents an earlier run, not proof that current code passed UI tests.
Carried-forward flags are shown in the PR comment. There is no fixed upload-count
requirement, and the comment updates as reports arrive.

Changed and smoke runs save raw Marathon results without collecting or uploading
instrumentation coverage. Single and random runs also save instrumentation XML
as artifacts but do not upload instrumentation coverage to Codecov. This avoids
replacing a full-suite baseline with a partial report: carryforward applies to
an absent flag, not missing tests within a newly uploaded report for that flag.
Instrumentation XML is saved as the `instrumentation-coverage` artifact.

Gradle task-output caching and parallel module execution are enabled in
`gradle.properties`. CI uses the enhanced Gradle cache provider for fallback
restoration and separate job caches. Default-branch builds and same-repository
PRs save caches; fork PRs only read them. The first run after switching providers
can start cold, while subsequent runs of the same PR can reuse compiled outputs.
