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

# Phase 7 — Shared refinements and final acceptance

**Status, 2026-10-26:** all six reviewed refinements were authorized by the
owner and implemented. Implementation verification and report publication are
recorded below. Phase 7 remains open for the explicitly deferred final acceptance
checks: physical phone, the full manual VoiceOver walkthrough, actual iOS 17 and
owner acceptance. Simulator accessibility audits do not substitute for VoiceOver.

**Approved rating policy:** three distinct successfully loaded build Overviews,
then eligibility on a later process launch. Account → Rate remains immediate.

**Private report:** [Phase 7 changes, comparisons and evidence](https://teamcityapp-kmp-phase-seven-20261026.andrey-tolpeev.chatgpt.site/).
It includes code before/after explanations, actual Android/iOS captures, the
initial ten comparison pairs, all sixteen new artifact-state goldens, verification
scope and the remaining final gates. The original review data is retained in
`review-baseline.json`; it describes the pre-fix implementation, not current defects.

## Approved checklist and resulting behavior

| Done | ID | Priority | Result |
| --- | --- | --- | --- |
| [x] | P7-01 | P1 | Sections use the complete refreshed build shown in Overview and its exact live originating visit |
| [x] | P7-02 | P1 | Artifact browser links preserve the active account's Guest/private mode |
| [x] | P7-03 | P2 | Completed bytes remain reachable through Open or share; rejected export can retry the same file |
| [x] | P7-04 | P2 | Full first-use Home regression explicitly handles every Home coachmark |
| [x] | P7-05 | P3 | Account-scoped three-build milestone, later-process-launch eligibility and education gating |
| [x] | P7-06 | P3 | One shared root, explicit Shared orchestration names and preserved serialized compatibility |

P7-05 and P7-06 were optional at review time; “implement all the proposed fixes”
authorized both. The owner subsequently chose the rating timing explicitly.
Neither OS navigation redesign nor background downloads is part of this scope.

### P7-01 — Refreshed build sections

Previously, Overview displayed a freshly fetched build while section callbacks
used the older incoming destination snapshot. Newly available sections could
reject missing links, and Parameters could retain older properties.

`SharedManagement.kt` now passes `content.build` to the common
`Navigator.openBuildSection` helper in `SharedBuildNavigation.kt`. The helper
requires the same build ID, an available section and the exact current visit ID.
A delayed or duplicate callback from a departed Overview cannot push another
visit. Artifacts receives the refreshed href; other section destinations receive
the same complete snapshot. Count results still target and consume once at the
live originating Overview.

Common tests cover refreshed Changes/Tests/Parameters/Artifacts, missing or wrong
builds, duplicate taps and exact result ownership. Android and iOS journeys cover
an evolving running → finished fixture, refreshed properties, artifact navigation,
restoration and a single Back to History. Public TeamCity was not mutated.

### P7-02 — Account-aware artifact browser links

The common `artifactBrowserUrl` in `SharedWebLinks.kt` now requires the active
account's Guest flag; `SharedArtifacts.kt` supplies it. Guest links retain
`guest=1`; private-account links leave authentication to the browser's normal
session. API Basic credentials are never added to URLs or browser cookies.
Server contexts, archive paths and origin/traversal guards are retained.

The isolated read-only fixture now has a controlled private browser context that
rejects forced Guest and accepts its own independent browser cookie. Tests check
both account modes and safety guards. This is controlled transport verification,
not a claim of testing a real guest-disabled private TeamCity deployment. Legacy
Android routers retained for compatibility tests are not the production root.

### P7-03 — Completed-file export recovery

Previously, claiming a completed file moved download state to Idle before the OS
accepted export. Dismissal or a presentation failure left no action to reopen
those bytes, even though the cache file still existed.

`ArtifactsViewModel` now retains `Ready(file, token, name, exportRequested,
exportFailed)`. Claiming consumes only the pending export request. **Open or
share** creates a fresh one-shot token over the same file; it does not download
again. An OS rejection changes the card to **Retry export**. Recreation, resume,
double taps and stale tokens do not automatically replay the sheet.

`NativePlatform.openFile` now returns whether the OS accepted presentation.
Android's FileProvider/Intent adapter and iOS's UIKit adapter implement that small
contract; it does not report that the user actually sent or saved a file. iOS
export rejection is shown by the common inline card instead of a second generic
error dialog. The existing Android compatibility route also adapts to this state.

The common card includes completion/error copy, filename, retry/reopen and Dismiss.
Its spoken filename includes context, for example “Downloaded file, report.txt”.
The strict iOS audit initially rejected the bare filename as not human-readable;
that failure led to the shared semantic-label correction. The audit was retained.
Visible filename pixels are unchanged by that correction.

Completed bytes retain their existing unique cache directories. Screen departure,
backgrounding and account replacement still cancel unfinished transfers and remove
partials. Dismiss/new download/departure may forget the completed-file UI action;
this is not a persistent download manager or process-death restoration feature.
Previously completed files are not deleted or overwritten.

Tests cover same-file retry without another repository transfer, claim-once,
recreation, duplicate/stale tokens, failed presentation, cancellation and account
replacement. Actual Android/iOS tests dismiss native export and reopen the same
completed download without sending anything. Sixteen reviewed golden fixtures
cover completed/export-failed states in light/dark, phone/tablet and 1.5×/2× text.
All 3,498 previously tracked screenshot baselines remain byte-identical.

### P7-04 — First-use Home regression

The prior full iOS Home test waited for controls hidden by first-use coachmarks.
It now explicitly asserts and dismisses Drawer, Favorites, Running, Queue and
Agents through their public “Got it” control, recording first-use captures.
History/Overview education is drained before subsequent assertions. The full
retained panes, sections, account flow and relaunch assertions remain enabled.

An initial helper attempt called the generic tap helper, which already dismissed
education and then searched for its removed target. A later run exposed the
Agents prompt as well. Both harness mistakes were corrected; these failed runs
are preserved as diagnostic attempts, not counted as successful full journeys.

### P7-05 — Rating after useful activity

`AccountPreferences` adds default-compatible `ratingViewedBuildIds` and
`ratingQualifiedLaunch` fields. `TeamCityBrowsingRepositories` records at most
three distinct successfully loaded Overview IDs under the current account
generation. Repeated views do not advance the milestone. The third view records
the current process-launch identifier; eligibility requires a different launch.

The launch identifier lives once per process, not once per ViewModel, host
recreation or account switch. Existing handled state remains respected. Optional
preference-write failure cannot turn a successful build response into an error;
a later view can retry it. The common Home composition suppresses inline rating
while education is visible. Manual Account → Rate remains available immediately;
the OS still controls whether a native rating sheet appears.

Tests cover three distinct builds, duplicates, same/later launch, bounded progress,
handled state, failure retry, account isolation and stale-generation cancellation.
Encrypted account formats retain their existing fields/defaults, with compatibility
and Android session tests. No Android-only Firebase gate was reintroduced.

### P7-06 — Shared/native ownership

Both production hosts now call `TeamCityApp` directly; the redundant `PhaseTwoApp`
wrapper is removed. Common orchestration files/functions/classes use `Shared*`
names: accounts, repositories, browsing, build navigation, management, artifacts,
settings, licenses, trackers, web links and journey snapshots.

`NativePlatform` remains the intentional OS-service boundary. Persisted
`NativeLicense(s)Destination` identifiers and the legacy About migration string
remain unchanged. Snapshot tests verify compatibility. Installed Android aliases,
wire DTO class names, account codecs, old caches and referenced legacy regression
adapters remain intact. No speculative mass deletion or new cross-feature
implementation dependency was introduced.

## Shared code and intentional platform adapters

Business rules, account/session lifetime, navigation/results, Compose screen
layouts, ViewModels, rating/education policy, artifact transfer state, Ktor request
policy, serialization, cache and origin/authentication guards are common to both
production hosts. There is no independent SwiftUI implementation of these screens.

| Boundary | Android | iOS |
| --- | --- | --- |
| Framework host | MainActivity, installed Intent aliases, Hilt wiring | MainViewController, UIKit/scene lifecycle |
| HTTP engine | Ktor OkHttp engine | Ktor Darwin engine |
| Secure account storage | Existing encrypted account snapshot adapter | Keychain adapter |
| Theme preference | Existing DataStore adapter | UserDefaults adapter |
| File export | FileProvider + Intent chooser | UIKit activity/export sheet |
| Browser/share/clipboard/rating | Android OS integrations | UIKit/StoreKit integrations |
| Storage/date/accessibility/safe areas | Android implementations | Foundation/UIKit implementations |
| License inventory | Android dependency data | Bundled native dependency notices |

Common networking owns challenge-based Basic authentication, account-generation
cancellation, same-origin/context checks, bounded response caching and mutation
replay policy. Browser cookies/login remain browser-owned. Old Retrofit/RxCache
code stays for codecs and legacy harnesses; it does not drive this shared root.
OS sheets, keyboards, status bars and browser handoffs intentionally differ.

## Implementation verification

Final receipts are under `build/kmp-phase-7/implementation/`; generated logs,
xcresults, archives and raw images are not committed.

- **404 JVM tests passed:** shared-app 173, artifacts presentation 20, artifacts
  Android harness 146, shared-data 35, accounts 14 and Android session 16. This is
  the affected module scope, including shared screenshot checks, not every module
  in the repository. Formatting checks pass.
- **99 Kotlin/Native tests passed:** shared-app 30 (including five native storage/
  platform tests), artifacts presentation 20, shared-data 35 and accounts 14.
  A separate simulator is used so these do not contend with signed XCTest UI runs.
- **Android integration: 16/16**, MainActivityNavigationTest and
  BuildWorkflowsNavigationTest, API 34 emulator; scoped final export rerun also
  verifies the shared accessibility change. **Optimized Android R8: 6/6**.
- **iOS selection:** seven fixture cases cover complete first-use Home, account/
  single-build flow, refreshed sections, Settings/licenses, artifact folders/
  archive/re-export with strict accessibility audit, cancellation and download retry.
  The seven-case selection passed six cases and failed only the completed-file
  filename audit. After correcting the shared spoken label, the focused artifact
  case passed with the strict audit and re-export assertions retained. This is
  seven cases covered across a full selection plus one scoped rerun, not one clean
  seven-case pass. The other six journeys were not repeated for this
  filename-only semantic correction.
- **Twelve Python fixture tests passed**, including controlled Guest/private
  browser behavior, evolving snapshots and read-only defaults. This run used
  isolated port 18082/TLS 18445; the existing port-18081 fixture was preserved.
- **Optimized iOS unsigned device archive and release audit:** minimum deployment
  target 17.0, version 2.0.0/build 1, zero audit errors. This is packaging verification,
  not actual iOS 17 execution, signed distribution or upload authorization.

New golden states were recorded and inspected before verification. Actual Android
before/after captures follow the same Home → History → Artifacts path and real
chooser dismissal. The iOS before frame is an earlier artifact-list baseline,
**not a matched post-dismiss recording**; the after frame shows the new card after
native-sheet dismissal. The report labels this limit explicitly. The initial ten
side-by-side pairs remain historical review evidence.

No report counts the opt-in-disabled XCTest run (seven skipped), failed helper
attempts or the initial filename-audit failure as passing checks. The failed full selection remains recorded alongside
the successful focused artifact rerun.
No new full public-TeamCity/management-write qualification is claimed.

## Remaining final acceptance

- [ ] Automated final physical-phone pass.
- [ ] Owner's complete six-part manual VoiceOver walkthrough and disposition of findings.
- [ ] Actual iOS 17 runtime verification.
- [ ] Final-candidate regression/packaging qualification after those checks.
- [ ] Final evidence publication and explicit owner acceptance of the migration.

These are owner-deferred gates, not simulator passes. Phase 7 is not complete yet.
No application push, PR, upload, TestFlight distribution or public TeamCity write
is included. Only the separate owner-private Sites source is synchronized to
publish the requested report.

## Checkpoint and publication receipts

Application checkpoint: **`76c8e10984adff19ba966f38c47f1f8963dfafd3`**
(`fix: complete shared phase seven refinements`), saved locally on
`codex/kmp-phase-1` after the affected checks passed. It includes code, behavioral
tests and sixteen intentional goldens; report/checklist prose is a separate
checkpoint. The private publication receipt follows after deployment. The initial review is saved at `d08aa431f`;
its Site source `761f4886616259adb7e75ebfb5d0737ea028f38c` and deployment
`appgdep_6adf1506d0ec8191a9c17e4f65351431` are historical review receipts.

Reproduce the affected checks with JDK 17:

```sh
./gradlew :shared-app:testAndroidHostTest :shared-app:verifyRoborazziAndroidHostTest \
  :features:build-artifacts:presentation:testAndroidHostTest \
  :features:build-artifacts:impl:verifyRoborazziDebug \
  :libraries:shared-data:testAndroidHostTest :libraries:accounts:testAndroidHostTest \
  :libraries:session:testDebugUnitTest spotlessCheck

./gradlew :shared-app:iosSimulatorArm64Test \
  :features:build-artifacts:presentation:iosSimulatorArm64Test \
  :libraries:shared-data:iosSimulatorArm64Test :libraries:accounts:iosSimulatorArm64Test

python3 -m unittest discover -s scripts/tests -p test_kmp_platform_fixture.py
```

Native tests use the separate unit simulator; signed UI tests use the isolated
validation bundle on simulator `85F5F977-405C-4A78-9406-4A5E6A94B43E`. The
`Phase7-fixture.xctestrun` manifest is generated alongside the test products,
with `TEAMCITY_FIXTURE_UI=1`, `TEAMCITY_PLATFORM_UI=1`,
`TEAMCITY_FIXTURE_BASE_URL=http://127.0.0.1:18082/context` and Light evidence theme.
The test fixture runs with `--port 18082 --tls-port 18445`; mutations stay disabled.

Final implementation receipts:

| Check | Receipt under `build/kmp-phase-7/implementation/` |
| --- | --- |
| JVM/golden/format checks after semantic fix | `accessibility-unit-goldens.log` |
| Shared navigation/account-generation final guards, JVM + native | `owner-guards-final.log` |
| Earlier full affected native scope | Native test XML under the affected modules' `build/test-results/iosSimulatorArm64Test/` |
| Android final-source build and formatting | `android-accessibility-build.log` |
| Android 16-case final-source integration | `android-verified-instrumentation.log` |
| Android optimized build / six smoke cases | `android-verified-r8-build.log`, `android-verified-r8-smoke.log` |
| iOS validation app build | `ios-accessibility-build.log` |
| iOS seven-case selection: six pass, filename audit fails | `ios-final-selected.log`, `ios-final-selected.xcresult` |
| iOS fixed filename + strict audit + re-export scoped pass | `ios-accessibility-focused.log`, `ios-accessibility-focused.xcresult` |
| Representative images from those passing cases | `ios-final-attachments/manifest.json` (source bundle recorded per test) |
| Final-source optimized unsigned archive | `ios-release-archive-verified.log`, `TeamCityApp-verified.xcarchive` |
| Release metadata/privacy/notice audit | `ios-release-audit-verified.json` |
| Read-only fixture safeguards | `fixture-tests-final.log` |

The 404/99 counts describe the union of affected module results, not a claim that
all tests ran in one command. The filename-only final correction required renewed
artifact/shared UI verification and host/release builds; unaffected native
repository/account cases retain their earlier passing results from this implementation.

The updated owner-private Sites report was published successfully on 2026-10-26:

- Site project: `appgprj_6adef83dcd248191bf11c7147cdf489d`.
- Pushed report source: `74e9e706d6c65ad386db24dd0dc7eb56199c18c0`.
- Saved version: `appgprj_6adef83dcd248191bf11c7147cdf489d~appgver_2f868200433c8191a235a98106cd3c95`.
- Successful deployment: `appgdep_6adf30076c2c8191b032f327ca92bbd8`, 10:49 UTC.
- URL: https://teamcityapp-kmp-phase-seven-20261026.andrey-tolpeev.chatgpt.site/.
- Access remains owner-private. The published page may require ChatGPT sign-in;
  audience was not broadened for browser access.

Report verification: JavaScript syntax, 43 referenced image digests, comparison
switching, six-item priority filtering, expanded code/criteria and a 390px mobile
layout with no page overflow. Browser console contained no warnings/errors. The
actual Android comparison proof is saved at
`build/kmp-phase-7/implementation/report-android-before-after.png`.

This document, the migration checklist and the updated AGENTS contract are saved
in a separate local `chore(docs): record verified phase seven refinements` checkpoint.
The application branch remains local and no PR was created.
