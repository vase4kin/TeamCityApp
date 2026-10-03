# Build conventions

The root and all Android module builds use Kotlin DSL. This included build owns
shared Android configuration and replaces the former buildSrc and root callbacks.

| Plugin | Configuration |
| --- | --- |
| `teamcityapp.android.application` | Android application and Kotlin plugins, shared SDK/Java/Kotlin targets, lint, and instrumentation defaults |
| `teamcityapp.android.library` | Android library and Kotlin plugins, the same shared defaults |
| `teamcityapp.android.library.java` | Android library defaults without Kotlin or kapt; use for Java/resource-only modules |
| `teamcityapp.android.hilt` | Hilt and kapt plugins, Hilt runtime and kapt compiler dependencies |
| `teamcityapp.android.data-binding` | Data Binding for Android application/library modules |
| `teamcityapp.android.coverage` | App aggregate debug coverage, JaCoCo agent configuration, and CI report locations |

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

`teamcityapp.android.coverage` replaces the applied JaCoCo script. It preserves
`generateCodeCoverageReport`, debug/mockDebug class inputs, generated-class
exclusions, and existing execution-data inputs. When compilation/tests and reporting
are requested together, the report runs after those producers. Reporting alone
can still use restored CI files without triggering builds or tests. XML is written to
`app/build/coverage/generateCodeCoverageReport/generateCodeCoverageReport.xml`;
HTML is written to `build/coverage-report`. The unused legacy PMD script has
been removed; CI's configured static check remains Android lint.

## Verification

Use JDK 17 and the repository wrapper:

```shell
./gradlew :build-logic:test
./gradlew assembleMockDebug assembleMockDebugAndroidTest testMockDebugUnitTest testDebugUnitTest lintMockDebug
./gradlew :app:generateCodeCoverageReport
python3 scripts/prepare-r8-verification.py
./gradlew -Pr8Verification :app:assembleProdR8Verification :app:assembleProdR8VerificationAndroidTest
```

Prepare debug Google Services files as described in AGENTS.md before app builds.
Functional tests use isolated Kotlin DSL builds to verify module overrides,
Hilt dependency registration, Data Binding, Java-only modules, and aggregate
coverage inputs/exclusions/output paths. Focused tests preserve the R8 report
count guard. CI runs these tests alongside the app and feature unit tests.
