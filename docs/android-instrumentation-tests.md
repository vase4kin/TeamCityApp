<!-- Copyright 2026 Andrey Tolpeev. Licensed under the Apache License, Version 2.0. -->

# Android instrumentation test ownership

Feature instrumentation coverage lives beside the feature implementation:

```text
features/about/impl/src/
  commonTest/kotlin/       # portable behavior
  androidHostTest/kotlin/  # JVM/Robolectric and screenshots
  androidAppTest/kotlin/   # device tests using the application test host
```

Reusable component and storage coverage uses the corresponding
`libraries/<owner>/src/androidAppTest/{kotlin,java}` roots. Java Hilt test modules
move with their owning feature suites. Packages, class names, method selectors,
assertions and ignored tests are retained.

## Source ownership and execution

`androidAppTest` is an explicit repository convention, not a new Kotlin target or
an independently runnable feature-module instrumentation source set. The
`teamcityapp.android.app-tests` convention on `app` adds these feature/library
Kotlin and Java roots to the application Android test compilation. The tests run
against the actual `MainActivity`, installed aliases, application Hilt graph and
mock fixtures, using the existing application test APK and Marathon setup.
Feature production dependencies remain unchanged: no feature depends on `app`
or another feature implementation. App-owned test helpers and the Hilt runner
stay in `app/src/androidTest`. All instrumentation-only dependencies and Hilt
processing for these suites remain on the app's test configurations.

Run an individual feature suite through its application host:

```sh
./gradlew :app:connectedMockDebugAndroidTest \
  -Pandroid.testInstrumentationRunnerArguments.class=teamcityapp.features.about.impl.AboutScreenTest
```

Build all of these suites with `:app:assembleMockDebugAndroidTest`. Running
`:features:about:impl:...` does not execute `androidAppTest`. Isolated device suites
can use a module's own Android instrumentation target when it is configured;
`libraries/security/src/androidTest/kotlin` is an existing example and still runs
with `:libraries:security:connectedDebugAndroidTest`.

The R8 verification property selects the existing independent
`app/src/r8VerificationTest` root and skips all `androidAppTest` aggregation.
Marathon discovery uses the same explicit source-root pattern, excluding local
JVM tests, independent library device suites, API modules and generated output.
Changed-feature selection follows source-folder ownership, including classes
whose historical package names differ from the owning module.

## Remaining application suites

The following coverage stays under `app/src/androidTest` because it verifies the
application composition root or contracts spanning multiple features:

- `HiltMigrationSmokeTest`: application injection and recreation across features.
- `MainActivityNavigationTest`, `MainActivityRestorationTest`,
  `BuildDetailsNavigationTest`, `BuildWorkflowsNavigationTest`,
  `HomeStartupNavigationTest`: root navigation, owners, results and restoration.
- `FormActivityAliasesTest`, `FeatureActivityAliasesTest`: installed component
  aliases in the app manifest.
- `HomeInitialTabTest`: installed launch arguments and initial root selection.
- `RootSheetHostTest`: the shared root sheet host and its cross-feature dismissal
  and navigation contract.

The two `teamcityapp.features.*` package names among these app suites are retained
for existing selectors; their assertions concern the root host/manifest, not a
single feature implementation.

## Moved coverage

44 app-hosted source files moved into 25 feature implementations and four
libraries. The existing security test also moved from its library's `java` root
to `kotlin`. All 404 runnable application test selectors were preserved.

| Owner | Files |
| --- | --- |
| `features/about/impl` | `AboutActivityTest.kt`, `AboutScreenTest.kt` |
| `features/agents/impl` | `AgentListFragmentTest.kt` |
| `features/build-artifacts/impl` | `ArtifactListFragmentTest.kt` |
| `features/build-details/impl` | `BuildLogCustomTabTest.kt`, `CancelBuildTest.kt`, `RestartBuildTest.kt`, `BuildActionsTest.kt` |
| `features/build-history/impl` | `BuildHistoryActivityTest.kt` |
| `features/build-overview/impl` | `OverviewFragmentTest.kt` |
| `features/build-queue/impl` | `BuildQueueFragmentTest.kt` |
| `features/change-details/impl` | `ChangeDetailsActivityTest.kt` |
| `features/changes/impl` | `ChangesFragmentTest.kt` |
| `features/create-account/impl` | `CreateAccountActivityTestAppModule.java`, `CreateAccountActivityValidationTestAppModule.java`, `CreateAccountActivityTest.kt`, `CreateAccountActivityValidationTest.kt` |
| `features/drawer/impl` | `DrawerTest.kt` |
| `features/favorites/impl` | `FavoritesFragmentTest.kt` |
| `features/filter-builds/impl` | `FilterBuildsActivityTest.kt`, `FilterBuildsBranchesAutoCompleteTest.kt` |
| `features/home/impl` | `RateTheAppTestAppModule.java`, `HomeActivityTest.kt`, `RateTheAppTest.kt`, `AccessibilityControlsTest.kt` |
| `features/login/impl` | `LoginActivityTestAppModule.java`, `LoginActivityTest.kt`, `LoginActivityValidationTest.kt` |
| `features/manage-accounts/impl` | `ManageAccountsActivityTest.kt` |
| `features/navigation/impl` | `NavigationActivityTest.kt` |
| `features/properties/impl` | `PropertiesFragmentTest.kt` |
| `features/run-build/impl` | `RunBuildActivityMockTest.kt`, `RunBuildActivityTest.kt` |
| `features/running-builds/impl` | `RunningBuildsFragmentTest.kt` |
| `features/settings/impl` | `SettingsActivityTest.kt` |
| `features/snapshot-dependencies/impl` | `SnapshotDependenciesFragmentTest.kt` |
| `features/splash/impl` | `SplashActivityTest.kt`, `SplashStartupTest.kt` |
| `features/test-details/impl` | `TestDetailsActivityTest.kt` |
| `features/tests/impl` | `TestOccurrencesFragmentTest.kt` |
| `libraries/app-rating` | `AppRatingTest.kt` |
| `libraries/authentication-shared` | `AuthenticationSwitchRippleTest.kt` |
| `libraries/design-system` | `BottomActionElevationTest.kt` |
| `libraries/security` | `CryptoManagerImplTest.kt` |
| `libraries/session` | `SharedUserStorageEncryptionTest.kt` |

## Verification of the move (26–27 October 2026)

- The final `mockDebug` application and test APKs build successfully, including
  relocated Kotlin suites and Java Hilt modules. Formatting and feature boundary
  checks pass.
- APK runner discovery reports the same 404 active methods as the pre-move source
  inventory, with no missing or extra selectors. This is discovery evidence, not
  a claim that the complete 404-test suite ran during this refactor.
- Twelve focused application-hosted device checks pass on API 34: six About
  screen checks, About server-error integration, Projects retry, storage
  migration, two rating adapter checks, and the Android OSS-plugin integration.
- Both independent security-library device checks pass from the Kotlin root.
- The convention TestKit regression passes for Kotlin/Java ownership roots and
  R8 exclusion. All 95 Python script tests pass, including source discovery,
  changed-feature selection and existing CI/coverage guards.
- Of the 45 relocated files, 43 remain byte-identical; the two older storage and
  security tests only gain the standard copyright/license header. Assertions,
  test identities and ignored cases are unchanged.

During setup, an attempted precompiled script could not resolve a binary
convention from the same build; the final implementation uses a registered
binary convention plugin. A TestKit fixture initially treated AGP's source-root
strings as File objects; its path assertions were corrected and passed. Neither
issue required changing application behavior or test expectations.

Build logs and discovery/comparison evidence are under
`build/instrumentation-ownership/`. No UI, screenshot-baseline or iOS application
changes are involved. Physical iPhone installation and previously deferred final
iOS qualification are outside this test-location change.

Code checkpoint: `82c34ad6d5ca6a5c54e6b8a5111f9e3ebe9690cf` (`refactor: colocate Android instrumentation tests with owners`).
