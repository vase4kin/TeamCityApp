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

# Phase 7 — Android/iOS review and proposed refinements

**Status:** review checkpoint, 2026-10-26. No refinement fixes implemented.
The owner requested the side-by-side review and prioritized punch list before
implementation. Phase 7 remains incomplete and the proposed list is not approved.

**Review baseline:** `db0592d18` (Phase 6 closure and final acceptance rescope).
Application implementation remains `304067c36` plus `373552201`; the later
`a6dc6b39c` and `db0592d18` checkpoints document that implementation and scope.
Tracked application source and screenshot golden bytes were unchanged during this
review. Temporary observation probes and raw captures are under `build/kmp-phase-7`.

**Private report:** [Phase 7 side-by-side review](https://teamcityapp-kmp-phase-seven-20261026.andrey-tolpeev.chatgpt.site/).
The report includes ten screen comparisons, evidence labels, a priority filter,
expandable acceptance criteria, the shared/native boundary and verification limits.
It is a review baseline, not fabricated before/after evidence of fixes.

## Recommended scope

Start with P7-01 through P7-04: two shared correctness paths, export recovery and
the missing fresh-install regression coverage. P7-05 and P7-06 are optional choices.
All six remain unchecked. This recommendation does not authorize implementing them.

| Open | ID | Priority | Proposed outcome | Evidence |
| --- | --- | --- | --- | --- |
| [ ] | P7-01 | P1 | Refreshed Overview and section launch use a consistent complete build snapshot | Source + component probe |
| [ ] | P7-02 | P1 | Artifact browser URLs respect Guest/private mode | URL probe; private-server impact not exercised |
| [ ] | P7-03 | P2 | Re-export completed bytes without downloading again | State probe + adapter audit |
| [ ] | P7-04 | P2 | Full first-use Home journey passes with coachmarks enabled | Fresh XCTest failure and hierarchy |
| [ ] | P7-05 | P3, optional | Agree first-use prompt/rating timing | Shared policy source audit |
| [ ] | P7-06 | P3, optional | Clarify ownership; remove only proven-unused scaffolding | Production entry-point/reference audit |

P1 means correctness work to prioritize in the agreed refinement scope. P2 covers
recovery and verification. P3 is optional product/maintenance work, not a proven
runtime defect. None of the source/probe findings is represented as a reproduced
physical-device crash.

## P7-01 — Open refreshed build sections with consistent data

**P1 · Defect · source + component probe.** Shared app orchestration · Android and iOS.

Overview resolves a fresh build, but its section callbacks still pass destination.build, the older incoming snapshot. A new Artifacts row can do nothing; a newly available Tests/Changes destination can reject the old snapshot. Parameters can show older values.

NativeManagement.kt:227–233 uses destination.build. BuildReferenceViewModel builds visible sections from the fetched build. ReviewProbeTest confirms new Tests/Artifacts availability while the old snapshot has no artifact link and rejects a Tests destination. This is a component reproduction, not an observed on-device crash.

Primary source: `shared-app/src/commonMain/kotlin/teamcityapp/shared/NativeManagement.kt`.

Completion criteria if selected:

- [ ] After an incoming/running build gains sections, every visible section opens valid data for that same build.
- [ ] Use one complete, validated section snapshot; retain exact originating visit/result ownership and account generation.
- [ ] Cover running → finished, added Tests/Changes/artifacts, refreshed parameters, restoration and one-Back behavior on both hosts.

## P7-02 — Preserve account mode in artifact browser links

**P1 · Defect · URL probe; private-server impact pending.** Shared URL policy · Android and iOS.

artifactBrowserUrl always appends guest=1, including for authenticated accounts. This explicitly chooses guest access when a private account may need the browser’s normal login session. The legacy Android router also did this, so this is inherited behavior rather than an iOS-only regression.

NativeWebLinks.kt:41–48 and NativeArtifacts.kt:75. The probe confirms forced guest=1 and contrasts the already account-aware build-log route. JetBrains documents guest=1 as guest access. No live guest-disabled/private server failure was reproduced in this review.

Primary source: `shared-app/src/commonMain/kotlin/teamcityapp/shared/NativeWebLinks.kt`.

Completion criteria if selected:

- [ ] Guest artifact links retain guest access; private-account links use normal browser authentication.
- [ ] Keep server context, archive paths, same-origin guards and credential-free URLs.
- [ ] Test both modes against controlled guest-enabled/disabled cases. API credentials never go to browser URLs or cookies.

## P7-03 — Retry exporting an already downloaded file

**P2 · Recovery gap · state probe.** Shared download state + small native outcome contracts.

The completed file is claimed once and download state becomes Idle before openFile returns any OS outcome. If presenting the viewer/export sheet fails, the generic error cannot reopen those bytes; the user must download again. Dismissing a successful export also leaves no re-export action.

ArtifactsViewModel.claimDownloadedFile clears Ready; NativePlatform.openFile returns Unit; NativeArtifacts immediately hands it off. The probe verifies Ready → claim → Idle. The current iOS successful export path passed. No file loss is alleged; the completed cache file remains.

Primary source: `features/build-artifacts/presentation/src/commonMain/kotlin/teamcityapp/features/artifacts/impl/ArtifactsViewModel.kt`.

Completion criteria if selected:

- [ ] Keep a reachable completed-file action to Open/Share again without a network transfer.
- [ ] Failed native launch offers retry of the same existing file, and cancel/dismiss does not cause an automatic duplicate sheet.
- [ ] Keep cancellation, account replacement, recreation and unique-directory guarantees; background downloads remain out of scope.

## P7-04 — Restore fresh-install Home regression coverage

**P2 · Confirmed test gap · 1 of 4 iOS review cases failed.** iOS XCTest harness · application behavior unchanged.

The retained Home journey taps Favorites and immediately waits for favorites:add. A new account shows the legitimate Favorites coachmark first, which hides that target. The test stops before it verifies the remaining retained panes/sections/relaunch.

PhaseTwoLiveSmokeTests.swift:415 assertion failed. Exported accessibility hierarchy shows home:coachmark:Favorites, selected Favorites tab and a reachable home:dismiss-prompt (“Got it”). This is not evidence that tab switching itself is broken.

Primary source: `iosApp/TeamCityAppUITests/PhaseTwoLiveSmokeTests.swift`.

Completion criteria if selected:

- [ ] Exercise and explicitly dismiss each first-use prompt at the appropriate transition; do not disable onboarding globally or remove assertions.
- [ ] Rerun the whole failed journey on an isolated new account and preserve later retention/sections/relaunch checks.
- [ ] Keep first-use prompt behavior independently tested; report fresh full passes separately from scoped reruns.

## P7-05 — Choose a less intrusive first-use prompt policy

**P3 · Optional product decision · source finding.** Shared onboarding/rating policy · Android and iOS.

Rating eligibility is currently just !ratingHandled for that account, and the first non-empty project list can trigger it. Coachmarks also appear as each new pane is first visited. This can interrupt the first useful browsing task; there is no success/usage milestone in the eligibility rule.

TeamCityBrowsingRepositories.kt:95–98 and NavigationViewModel.kt:59–86. The failed Home review demonstrates a first-use coachmark, not a rating dialog. The About “Rate” capture is a deliberate manual action and is not used as proof of an automatic prompt.

Primary source: `libraries/shared-data/src/commonMain/kotlin/teamcityapp/libraries/shared_data/TeamCityBrowsingRepositories.kt`.

Completion criteria if selected:

- [ ] Agree an explicit milestone/timing rule before implementation; no threshold has been chosen yet.
- [ ] Coordinate prompts so only one appears at a time; keep dismissals account-scoped and retryable.
- [ ] Cover first login, returning user, account switching and manual Rate action without changing native store behavior.

## P7-06 — Make shared ownership easier to recognize

**P3 · Optional maintenance · no demonstrated runtime defect.** Names/docs and proven-unused adapters only.

PhaseTwoApp and Native-prefixed files in commonMain now serve both production hosts. Those historical names obscure that fact. Legacy Android adapters and harnesses still exist alongside the new root, but their presence alone does not prove duplicated production behavior.

Both production hosts execute the common root and repositories. Some legacy code remains for installed entry points, encrypted-account compatibility, serialized DTO/cache formats and regression harnesses. Deletion needs reachability evidence.

Primary source: `shared-app/src/commonMain/kotlin/teamcityapp/shared/PhaseTwoApp.kt`.

Completion criteria if selected:

- [ ] Inventory references before any removal or rename; remove only proven-unused scaffolding.
- [ ] Preserve installed aliases, account/ciphertext formats, DTO/cache class names and existing user files.
- [ ] Update architecture documentation; verify both hosts and compatibility tests. Do not start another architecture migration.

## Side-by-side conclusions

| Area | Android implementation | iOS implementation | Review conclusion |
| --- | --- | --- | --- |
| Account / Manage / Add / sign-out | Shared presentation, ViewModels, AccountRecords and session policy | Same shared implementation | Dedicated screens and Sign out are present. Fresh account/navigation checks pass; the earlier capped sign-out behavior is not the current implementation. |
| Home / Favorites / Activity | Common root and retained child navigation | Same common root and retained child navigation | Shared policy; fresh iOS full journey stops at an unhandled coachmark in the test. Later steps are not claimed passed. |
| History / Overview / read-only sections | Common presentation, result orchestration, repositories | Same common implementation | Single-build-opening scenario passes; refreshed section arguments have P7-01's separate mismatch. |
| Run / Filter / management | Common ViewModels, Ktor writes, typed live-origin results | Same common implementation | Fresh Android workflow coverage passes. Visual iOS review uses retained evidence; no new management-write qualification this turn. |
| Artifacts | Common list, menu, download lifetime/state; Android file/export port | Same common policy; native file/export port | Successful iOS export passes. URL mode and export recovery are P7-02/P7-03. |
| Settings / About / licenses | Shared UI and four theme choices, Android preference/notice data | Same UI, iOS preference/notice data | Fresh Settings/licenses check passes. Different dependency inventories are expected. |

The Site's five fresh/fresh pairs are Account, Manage Accounts, Add user account,
Settings and licenses. History, Overview and Artifacts pair retained Android
Roborazzi baselines with fresh iOS screenshots. Run and Filter use retained
Android baselines and historical Phase 6 iOS captures. These are comparative visual
reviews, not a newly passing cross-platform suite or a pixel-diff claim.

Fresh Android captures use the isolated mock app, API 34 Pixel emulator, 1344×2992,
and synthetic wire data. Fresh iOS captures use an iPhone 17 Pro simulator,
iOS 26.4.1, 1206×2622, and the read-only local fixture. Baseline renders are 360×800.
Data, selected theme, insets and native viewport differ; the gallery labels those
conditions. Android Settings was explicitly Light, while iOS started Follow system
with a light OS. Both share the System default; the capture does not show a
platform-default discrepancy.

No new dark/expanded/large-text golden matrix was recorded. Existing Phase 6
coverage remains historical evidence; affected matrices must be rerun after
selected refinements. A fresh iOS Dark-preference/relaunch frame is included.

## Shared and native ownership

Both production hosts execute the common app root, SharedAppViewModel, account
session policy, repositories, destination/results and common Compose presentation.
Files named `Native*` inside commonMain serve both platforms. Names alone do not
imply that a feature was independently rebuilt for iOS.

Networking policy is common: Ktor transport, Kotlin serialization, challenge-based
Basic authentication, account-generation cancellation, origin/context guards,
mutation replay policy and the bounded response cache. The native HTTP engines
are OkHttp on Android and Darwin on iOS. Browser cookies/login remain owned by the
browser; API credentials never enter outgoing URLs.

The OS-specific boundaries are deliberate:

- Android `MainActivity` and `AndroidAppPlatform` handle installed Intents,
  Hilt/framework wiring, encrypted-account compatibility, file storage/FileProvider,
  browser/share/clipboard/rating launches, lifecycle and power state. Theme
  persistence uses the existing DataStore adapter.
- iOS `MainViewController` and `IosNativePlatform` supply UIKit/scene lifecycle,
  Keychain, native file storage and export/share/browser/clipboard/StoreKit calls.
  Theme persistence uses UserDefaults.
- Date/locale rendering, actual accessibility hooks and safe-area integration have
  small platform implementations. Public UI state and app rules remain common.
- License-reader UI is common; dependency inventories differ by runtime. The iOS
  collector bundles transitive native notices into the Skia reader. Its shorter
  primary catalog is not evidence that those notices are absent.
- Legacy Android codecs/aliases/accounts/cache formats and regression adapters
  remain compatibility constraints. Old Retrofit/RxCache code does not drive the
  common production root. Do not remove it merely because a name looks old.

Status bars, keyboards, OS export/share sheets and browser handoffs are expected
platform differences. A new SwiftUI/UIKit navigation redesign, background
artifact downloads or broader architecture migration is not proposed by this review.

## Verification receipts

- **Android: 15/15 passed**, existing `MainActivityNavigationTest` and
  `BuildWorkflowsNavigationTest`, API 34 emulator. Account lifecycle, host recreation,
  shared root/wire, Settings/license packaging and Run/Filter typed results are
  covered. Mock writes use synthetic responses, not public-server mutations.
- **iOS: 3/4 passed**, signed existing XCTest UI cases on the dedicated iOS 26.4.1
  simulator. Account/single-build-opening, Settings/licenses and artifact
  folders/archive/export passed. The full retained Home case failed at
  `favorites:add` because the Favorites coachmark was present. The hierarchy shows
  selected Favorites and `home:dismiss-prompt` with a reachable “Got it” label.
  No production fix or test-source edit was made; later journey steps did not run.
- **Shared observation probes: 3/3 passed** in `:shared-app:testAndroidHostTest`.
  They reproduce current component/URL/state behavior rather than future desired
  behavior. The probes are temporary review artifacts, not accepted production tests.
  Initial temporary init-path and dispatcher-cleanup setup failures were corrected
  only under `build/`; the final probe run has zero failures.
- **Report:** JavaScript syntax, 23 referenced image files/digests, responsive
  preview, gallery selection, priority filtering and expandable criteria checked.
  Private publication receipt is recorded below after deployment.
- **Not rerun:** full native/common suite, complete screenshot matrix, fresh iOS
  management writes, optimized Android/iOS packaging and actual iOS 17. No new
  physical phone or manual VoiceOver result is claimed.

Commands and raw artifacts:

```sh
adb -s emulator-5554 shell am instrument -w -r \
  -e class com.github.vase4kin.teamcityapp.navigation.MainActivityNavigationTest,com.github.vase4kin.teamcityapp.navigation.BuildWorkflowsNavigationTest \
  com.github.vase4kin.teamcityapp.mock.debug.test/com.github.vase4kin.teamcityapp.helper.HiltTestRunner

xcodebuild \
  -xctestrun build/kmp-phase-6/SharedProductionDerivedData/Build/Products/SharedProduction-fixture.xctestrun \
  -destination 'platform=iOS Simulator,id=85F5F977-405C-4A78-9406-4A5E6A94B43E' \
  -resultBundlePath build/kmp-phase-7/ios-side-by-side-review.xcresult \
  -parallel-testing-enabled NO -collect-test-diagnostics never \
  -only-testing:TeamCityAppUITests/PhaseThreeBrowsingSmokeTests/testHomeRetainedPanesFavoritesSectionsAndRelaunch \
  -only-testing:TeamCityAppUITests/PhaseThreeBrowsingSmokeTests/testAccountFlowMatchesAndroidAndHistoryHasOneBuildVisit \
  -only-testing:TeamCityAppUITests/PhaseFivePlatformSmokeTests/testSettingsThemePersistenceAndAboutLicenses \
  -only-testing:TeamCityAppUITests/PhaseFivePlatformSmokeTests/testArtifactFoldersArchivesAndNativeExport \
  test-without-building

# Temporary init script includes build/kmp-phase-7/probes in commonTest for this invocation only.
# Run Gradle with JDK 17.
./gradlew -I build/kmp-phase-7/review.init.gradle \
  :shared-app:testAndroidHostTest --tests '*ReviewProbeTest*'
```

Logs: `build/kmp-phase-7/android-side-by-side-review.log`,
`ios-side-by-side-review.log`, `review-probes.log`. Exported iOS captures and failure
hierarchy: `build/kmp-phase-7/ios-review-attachments/manifest.json` and its siblings.
Fresh Android captures: `build/kmp-phase-7/android-review-captures/files/`.
Screenshot provenance/digests and proposed criteria:
`build/kmp-phase-7/review-evidence.json` and the private Site's `review.json`.
The post-test simulator SpringBoard screenshot is not used as application evidence.

The private-server browser finding uses JetBrains' official
[artifact URL documentation](https://www.jetbrains.com/help/teamcity/patterns-for-accessing-build-artifacts.html)
and [Guest access documentation](https://www.jetbrains.com/help/teamcity/guest-user.html)
for the meaning of `guest=1`. A private guest-disabled deployment was not exercised.

## Phase status and final acceptance

The side-by-side review is recorded; agreement on the punch list is pending.
Implementation, affected verification and final before/after evidence remain open.
The initial private review report does not complete Phase 7's final-report gate.

At the very end, retain the owner-deferred final gates from the migration checklist:
physical-phone automation, the complete six-part manual VoiceOver walkthrough,
actual iOS 17 execution, final candidate regression/packaging and explicit acceptance.
No upload, TestFlight distribution, public TeamCity writes, push or application PR
was authorized by this review.

## Publication and local checkpoint

The owner-private publication succeeded on 2026-10-26 at 08:53 UTC:

- Sites project: `appgprj_6adef83dcd248191bf11c7147cdf489d`.
- Pushed Site source: `761f4886616259adb7e75ebfb5d0737ea028f38c`.
- Saved version: `appgprj_6adef83dcd248191bf11c7147cdf489d~appgver_6166a02158b08191b17f225ebac19683`.
- Successful deployment: `appgdep_6adf1506d0ec8191a9c17e4f65351431`.
- URL: https://teamcityapp-kmp-phase-seven-20261026.andrey-tolpeev.chatgpt.site/.

This document and the checklist are saved together in the local
`chore(docs): record phase seven review and proposed refinements` checkpoint on
`codex/kmp-phase-1`. No application source, golden or runtime fix is in that
checkpoint. The application repository stays local; only the separate private
Sites source is synchronized for report publication.
