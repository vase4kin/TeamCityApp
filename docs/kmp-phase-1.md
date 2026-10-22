<!--
 * Copyright 2026 Andrey Tolpeev
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 -->

# KMP Phase 1 evidence

The About screen now uses the same Kotlin contracts, ViewModel, UI state,
Compose layout, resources, and branded design-system definitions on Android and
iOS. Android keeps its real repository, Hilt construction, navigation and outgoing
actions. The iOS host is a deterministic preview with available, unavailable and
loading server fixtures; its actions display explicit preview feedback.

The [migration checklist](kmp-migration.md) controls progress. Phase 2 is not
started. The [module inventory](kmp-module-inventory.md) classifies all 81 modules.

**Private visual report:** [Phase 1 review](https://teamcityapp-kmp-phase-one-20261022.andrey-tolpeev.chatgpt.site) — owner-private deployment verified.
**Report source checkpoint:** `96e569c6ffd8d9b696a752bdc08793a0750d1def` (separate Sites repository).
**Implementation checkpoint:** `0cf1fe021fa313629b2a04c43e5a664e768745d1` (`feat(kmp): share About presentation with an iOS foundation`).

## Ownership and code changes

| Area | Before | After |
| --- | --- | --- |
| About API | Android library | KMP repository/model in commonMain; Android destination key/manifest in androidMain |
| About presentation | Layout, state and Hilt ViewModel in Android impl | One common presentation module; plain lifecycle ViewModel and shared stateless screen |
| Android About impl | All presentation plus Android integrations | Thin screen/version wrapper, Hilt ViewModel subclass, registered route and existing platform router |
| Design system | Android theme library | Portable definitions/components in design-system; Android theme re-exports them and retains platform components/resources |
| iOS entry | None | SwiftUI host embeds ComposeUIViewController from a static Kotlin framework |
| Common tests | Android-only ViewModel tests | Same seven behavioral tests run on Android and the iOS simulator; two portable color/contrast tests run on both |
| Build conventions | Android/JVM conventions | Additional KMP library/Compose conventions; Android conventions retained |
| CI/coverage | Android source/task assumptions | KMP host-test scheduling, common sources, shared JVM bytecode transport, macOS native/UI workflow |

The Android route obtains `AndroidAboutViewModel` through Hilt. That class only
calls the shared `AboutViewModel` constructor. The iOS route constructs the shared
ViewModel through the lifecycle owner's `viewModel` factory. Both routes collect
state with lifecycle awareness. No Activity, Context, router or Hilt dependency
enters common code.

Collection-driven loading retains completed content, shares pending requests and
cancels unfinished repository work when the last collector leaves or its owner
is cleared. Common tests cover those contracts, optional failure and mapping.
Kotlin packages and public Android screen signatures are preserved where useful.

Common UI uses typed Compose resource accessors and `SharedUiText`, rather than
Android integer resource IDs. Existing Android `UiText`, wire DTO packages,
account persistence, navigation aliases and other feature implementations remain
in their current owners. `TeamCityScreenFrame` contains one scaffold implementation;
the Android wrapper resolves existing resources synchronously, while shared About
uses common resources. This avoids altering resource loading in unmigrated screens.

## Toolchain decisions

| Tool/target | Selection | Reason / evidence boundary |
| --- | --- | --- |
| Kotlin / JDK / Gradle | 2.4.20 / 17 / 9.6.0 | Existing Kotlin/JVM baseline, verified with native targets added |
| AGP | 9.3.1, previously 9.4.1 | Within the published Kotlin 2.4.20 KMP compatibility range |
| Compose Multiplatform | 1.12.2 | Common UI, runtime, resources and previews |
| Multiplatform Material | 1.12.0-alpha03 | Required for existing Expressive components; alpha dependency remains a tracked risk |
| Android Material | Existing 1.5.0-beta01 with existing BOM | Android rendering preserved; no About golden refresh |
| Lifecycle | 2.11.0 | Common ViewModel and lifecycle-aware Compose collection |
| Xcode | CI 26.4; local 27.1 | Native builds and simulator tests succeeded locally on newer installed Xcode |
| Android | API 24+ | Existing flavors/application retained |
| iOS | Minimum 16.0; arm64 device/simulator | Device framework compilation and modern simulator runtime verification; older/physical-device validation is later |

The multiplatform Material Android bridge initially brought graphics-shapes 1.1.0
into Android, changing small edges of the animated loading indicator. The stable
multiplatform Material port lacks the Expressive APIs already used by this app.
The KMP Compose convention therefore excludes that bridge's Android graphics-shapes
dependency on the common edge, allowing Android's explicit Material dependency
to supply its existing renderer. A separate nativeMain dependency on the full
multiplatform Material port retains the native graphics implementation. This
separation is covered by a convention test and the iOS loading smoke tests. All 27 original About PNGs and existing theme goldens verify unchanged.

Dependencies remain in the version catalog, target settings in `Config.kt`, and
reusable setup in conventions. The framework host generates its visible version
from the same application version definition. Xcode project marketing metadata
will be integrated with release automation during the release phase.

## Verification record

Logs and raw captures live under ignored `build/kmp-phase-1/`. Required commands
use JDK 17 and explicit Android variants. No APK, framework, report output or
DerivedData is committed.

| Verification | Result / evidence |
| --- | --- |
| Original About baseline | Passed before edits; 27 original PNGs saved for comparison |
| About Android tests | 24 passed: 18 screenshot cases producing 27 PNGs, two version tests, four router tests |
| Shared About behavior | Seven tests on each platform |
| Shared color/contrast | Two tests on each platform |
| Android theme | 91 tests passed, including existing loading/error goldens and component behavior |
| Additional Android features | Home 254, Login 166 and Build Details 312 tests passed |
| Build conventions | 17 tests, including KMP targets/resources/host tests and coverage source/class inclusion |
| CI Python helpers | 61 tests passed, including KMP task scheduling and coverage transport |
| App integration | Mock app and instrumentation APK assembly, production Kotlin compile, 231 app unit tests, About lint; all passed |
| Android emulator | 12 About and root-navigation integration tests passed on Pixel 8 Pro API 34 |
| Native frameworks | Debug iosArm64 and iosSimulatorArm64 frameworks compiled |
| iOS simulator | Seven XCTest UI tests passed: available/unavailable/loading in both themes, lower actions and preview feedback |
| Formatting | spotlessApply, diff inspection and spotlessCheck passed |

The repository's new `.github/workflows/kmp.yml` pins an Apple Silicon macOS runner,
JDK 17, Xcode 26.4 and iPhone 17/iOS 26.4. It builds both frameworks, runs common
simulator tests and XCTest UI checks, and uploads their result bundle. The
constituent checks run locally. Hosted CI has not run because this task does not
authorize pushing application code or opening a PR.

## Screenshot provenance and UI differences

- Android before/after: matched original Roborazzi fixtures, 360 × 800 dp compact,
  1000 × 700 dp expanded and compact 1.5× text, both themes, loading/available/
  optional failure, scrolled lower content and tooltip. Fixed version is 1.52.8.
  Every final About PNG is byte-identical to its original baseline.
- Android runtime: actual mock APK About screen on the Pixel 8 Pro emulator,
  application version 2.0.0. This is labeled separately from fixed golden fixtures.
- iPhone: actual iPhone 17/iOS 26.4 simulator captures, application version 2.0.0,
  deterministic server 2026.1 at `https://teamcity.example`. This is the first iOS
  baseline; there is no fabricated pre-migration iOS screenshot.
- iPad: actual 13-inch iPad Pro/iOS 26.5 simulator, expanded two-column layout.
- Enlarged iOS text: actual system Accessibility Medium content-size category,
  with wrapping and scrolling. It is not presented as equal to Android's 1.5× scale.

The branded Compose layout is shared. System bars, safe areas, typography rasterization,
viewport and scrolling differ between platforms. The Android fixed fixture and
running iOS preview also have different version labels. Cross-platform screenshots
are visual evidence of the same design, not a promise of identical pixels.

## Remaining limits and next phase

Phase 1 establishes compilation, shared presentation and representative behavior.
It does not provide real iOS accounts/networking, root navigation parity, secure
iOS persistence or outgoing platform actions. Those begin with the explicitly
requested Phase 2. Physical-device testing, oldest-supported-iOS validation,
release optimization, signing and distribution remain later gates.

The Material Expressive port is alpha and should be revisited with dependency
updates. Local Xcode 27.1 success is measured evidence, not an extension of the
vendor's published compatibility guarantee. The local linker also warns that a
bundled ICU data object was built for iOS 18.5. The declared minimum remains 16.0;
minimum-OS runtime compatibility requires validation before release. The module inventory is a direct
source/import audit; transitive platform dependencies need review as each feature
moves.

Starting source base: `49a6c4bd7`. Starting GitButler workspace:
`550745178ac91b65c18f3eabf53e89a953b31e6d`. Existing README and Google Play image
work is excluded. Application changes are saved locally using GitButler; Sites
publishing synchronizes only the separate report source repository.

## Reproduce the focused checks

```sh
./gradlew :build-logic:test
python3 -m unittest discover -s scripts/tests
./gradlew :features:about:impl:verifyRoborazziDebug :libraries:theme:testDebugUnitTest
./gradlew :features:about:presentation:testAndroidHostTest :libraries:design-system:testAndroidHostTest
./gradlew :features:home:impl:testDebugUnitTest :features:login:impl:testDebugUnitTest :features:build-details:impl:testDebugUnitTest
./gradlew :app:assembleMockDebug :app:assembleMockDebugAndroidTest :app:compileProdDebugKotlin :app:testMockDebugUnitTest :features:about:impl:lintDebug
./gradlew :shared-app:linkDebugFrameworkIosArm64 :shared-app:linkDebugFrameworkIosSimulatorArm64 :features:about:presentation:iosSimulatorArm64Test :libraries:design-system:iosSimulatorArm64Test
./gradlew spotlessCheck
xcodebuild -project iosApp/TeamCityApp.xcodeproj -scheme TeamCityApp -configuration Debug -destination 'platform=iOS Simulator,name=iPhone 17,OS=26.4' -derivedDataPath build/kmp-phase-1/DerivedData -resultBundlePath build/kmp-phase-1/recheck.xcresult -parallel-testing-enabled NO -collect-test-diagnostics never CODE_SIGNING_ALLOWED=NO test
```

Use a fresh result-bundle path for each Xcode run. App builds need the CI debug
Google Services fixtures described in AGENTS.md. For emulator integration, install
the app/test APKs and run the HiltTestRunner with the AboutScreenTest and
MainActivityNavigationTest classes selected.
