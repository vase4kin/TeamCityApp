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

# KMP Phase 6 — Release validation and cleanup

Status: in progress. Physical-device qualification is deferred at the user's
request ("Use simulator for now"); the Phase 6 gate remains open. Phase 7 has
not started. No application push, upload or distribution has occurred.

Starting checkpoint: `5c778a1f53351bcaec544463f61c5af81d87ccf7`.

The complete Android/JVM test plan ran 142 configured tasks: 4,435 cases,
zero failures/errors and one existing opt-in live-server skip. The isolated
optimized Android app built successfully and its six R8 compatibility smoke
tests passed. Its APK retains all 23 installed activity aliases after resolving
relative component names. Normal mock/prod debug builds and lint also passed.

The initial unsigned iOS device archive built with the release-scoped 8 GiB
compiler heap and passed packaging audits (2.0.0, iOS 16 minimum, icon, privacy,
and current native license inventory). Its executable measured 39,568,000 bytes;
this is an initial archive measurement, preceding the accessibility fixes. The
final-source optimized archive has now passed the same source/dependency/package
audits. Its executable is 39,420,328 bytes (SHA-256
`b1957a33759f354ad8413b4d99ae14f14747f971f8db1119f95e845c6000b933`),
with no embedded provisioning profile. The final isolated optimized iPhone run
passed all 11 cases with zero failures/skips, including strict About/Login audits,
orientation/keyboard checks and five-iteration performance measurements.

The first full signed iPhone simulator run executed 27 cases: 25 passed, one
optional public-server journey skipped, and the newly added About accessibility
audit failed. Existing account, Home, browsing, management and platform journeys
passed against the isolated local fixture. Screenshots and metrics are retained
as before-fix evidence. Diagnostic iPad runs confirmed the findings below.

## Changes under verification

- Bundle an app-level privacy manifest with app-only UserDefaults, file-metadata
  and elapsed-time reasons; no tracking or hosted iOS collection is declared.
- Render the original Android vector into a reproducible opaque 1024px iOS icon.
- Move the iOS marketing version to Release Please's `Version.xcconfig` and
  support an optional app/UI-runner bundle-ID suffix for isolated validation.
- Exclude Intel simulator slices from Xcode to match the agreed Apple Silicon
  KMP target; Release otherwise requested an unconfigured `ios_x64` target.
- Give optimized Xcode framework builds an 8 GiB heap; keep normal Android/debug
  settings. The first default-heap native optimization failed with Java heap space.
- Export actual iOS arm64 artifacts and audit POM/license metadata and hashes.
  The offline license readers now include 78 external artifacts and 29 native/
  Kotlin-runtime notices, pinned to Skiko 0.150.2 and Skia m150-a3f509bf52.
- Normalize combined native-notice text to LF before hashing; preserve raw
  upstream notice hashes in the inventory. A clean-checkout audit checks the
  committed Git blobs against every bundled provenance hash.
- Reject stale versions, missing privacy/icon/license resources, missing license
  metadata and drift between resolved dependencies and the reviewed inventory.
- Add strict signed accessibility, rotation/keyboard and performance tests.
  Rotation captures wait for the native app window to reach the requested
  orientation; lower-action checks scroll until the control is hittable.
  Full-display captures avoid an offset app-window crop in landscape keyboard
  screenshots on the current toolchain; diagnostic captures remain under build/. The
  audit callback records failures without suppressing them.
- The current iOS Material port exposes an unlabeled tooltip long-press wrapper
  alongside the labeled Back button. Apple controls now render directly in the
  accessibility tree; Android retains long-press, hover and keyboard tooltips.
- Give clickable About rows a complete descriptive headline/detail label. A
  final optimized phone audit caught a bare server URL after scrolling; the
  action payload and visible rows stay unchanged. Android tests verify both.
- Add an accessible "Application version …" description to the visually unchanged
  version string. Apple's audit flagged the bare numeric version as not human-readable.
- Correct the Create Account validation mock to return the installed storage's
  empty signed-out account contract. Its previous null caused 12 test failures;
  production account/storage behavior is unchanged. The corrected 19-case account/About rerun passed, as did the explicit About capture test.
- Allow a dedicated simulator for common Native tests through
  `-PteamcityIosTestDevice=<UDID>`, avoiding signed UI-test device ownership.
- Extend GitHub Actions with optimized native archive/packaging checks and retain
  the evidence artifacts. CI remains GitHub Actions and pins Xcode 26.4.

## Qualification evidence and remaining gates

The initial Debug phone reference measurements used five iterations: responsive
launch mean 1.408 seconds; About scroll-pair mean 0.920 seconds; peak physical
memory mean 54,348 kB. They were collected under concurrent validation load,
without a historical performance threshold. They are not optimized-device results
or representative of every production list/server size. Final optimized iPhone
measurements averaged 1.302 seconds responsive launch, 0.909 seconds per About
swipe-pair, 54,296 kB peak physical memory and 53,617 kB absolute physical memory.
Gesture wall time includes test-tool overhead; no FPS or leak-free claim is made.
Physical-device performance and manual VoiceOver qualification remain open.

The final minified Android smoke suite passed all six checks. Five force-stopped
ActivityManager launches on the API 34 emulator averaged 611 ms TotalTime
(573–676 ms); one resident snapshot reported PSS 37,688 kB and RSS 158,736 kB.
Those are different metrics from XCTest and are not a cross-platform ranking.
All 23 installed aliases were retained in the actual final optimized APK.
Final Spotless verification against the starting checkpoint passed.

A paired iPhone 15 Pro running iOS 27.0.1 was detected, but was locked and Xcode
could not mount its developer image. The user chose simulator-only work for now.
No attempt was made to change its lock, pairing or developer settings. Simulator
runtimes available locally begin at iOS 26.4; minimum-runtime execution is not
claimed by an iOS 16 deployment-target check.

Strict app-content audits now pass on the iPad for About, Login, Projects,
Favorites, Activity, History and Overview. Keyboard display/rotation/background
checks are separate. An Apple prediction cell in the system keyboard had no label;
this OS-owned finding is retained as a limitation, and app-content audits run
before opening the keyboard. The original toolbar finding came from another
Material parent-semantics wrapper; iOS now uses an equivalent fixed surface for
its always-expanded actions, with independent controls and a group label.

The full Android instrumentation run executed 400 cases: 388 passed and 12
failed in the Create Account mock. After correcting the signed-out sentinel, all
19 account/About cases passed on the rebuilt APK, plus an explicit About capture
case. There are no unresolved failures; this is a full run plus scoped rerun, not
a claim of a second clean 400-case run. The final-source signed dark iPad suite passed: 27 tests passed, one optional
public-server journey skipped, and zero failures across 28 cases.
All 491 shared Native cases passed across 31 modules on a separate simulator.
A final signed iPhone run passed About/Login audits, orientation/keyboard and
background checks at the largest system accessibility text size; the clickable
About labels include their descriptive headline and detail.
The full JVM run covers 69 modules with test XML, zero failures/errors and one
existing opt-in skip. The final About semantic change then passed focused
behavioral/action tests, golden verification and all seven Android integration
cases. The focused final UI verification ran 25 About cases and 151 shared-host
cases, including their existing golden checks, with zero failures/errors/skips.
All 3,482 starting PNG fixtures remain byte-identical.
 One Native test run was cancelled to serialize shared compilation; it
is not counted as a passing regression run. Cache-lock failures from completed
Gradle daemons are retained as diagnostics and do not count as test results.

A static module reachability audit found all 111 included application modules
reachable from the Android or shared host (separate from the root and included build-logic build). This is module reachability, not proof of every symbol being live.
Android Retrofit/RxCache, encrypted accounts, compatibility adapters and installed
aliases retain callers and remain intentional. No unproven dependency removal
or broad cache/account migration is included in this release-validation phase.

See [iOS release preparation](ios-release.md) for reproducible archive/audit commands,
signing/TestFlight inputs, proposed store copy and privacy/export-compliance review.
Those are prepared materials, not submitted declarations, accepted agreements or
distributed builds. The full release gate will only be checked after required
Android, simulator and physical-device evidence is available.

## Minimum-runtime follow-up

A continuation audit found installed API 24 ARM images, correcting the earlier
blanket minimum-runtime limitation. A new isolated Pixel AVD under ignored build
output booted Android API 24. Its final optimized APK passed all six compatibility
checks. The selected 59-case instrumentation run then passed 57 cases; the two
Settings capture failures were resolved and all seven Settings cases passed on
a scoped rerun. Every failed case is covered; this is a full selected run plus
reruns, not a claim of a second clean 59-case run or a complete API 24 400-case run.
The explicit About capture/recreation case also passed, producing an actual API 24
image for the private report.

The initial default-harness attempt had 49 failures across 59 cases. Mockito's
inline Android engine requires API 28, and window PixelCopy capture requires API
26. The explicit `-PminimumAndroidInstrumentation` profile uses the last subclass
engine (Mockito Android/Core 5.22.0), while the normal graph remains 5.24.0.
Dependency insight verified both resolutions. Kotlin Mockito's transitive Core
is excluded only in the minimum profile to keep that graph aligned. This changes
test dependencies only; application runtime dependencies are unchanged.

The older capture path reads the actual display, selects the application window
rather than a tooltip root, recycles each bitmap and requires the exact theme
background color within five seconds. One-shot captures caught intermediate
colors; unsuccessful diagnostic runs included a background-service ANR. The final
seven-case Settings run passed without temporary tracing or exclusions. Light
navigation-bar assertions now reflect the documented API 26 capability boundary.
A mistyped runner that executed zero tests is excluded from results.

Reproduce with JDK 17 and an isolated API 24 ARM emulator:

```sh
./gradlew -PminimumAndroidInstrumentation \
  :app:assembleMockDebug :app:assembleMockDebugAndroidTest
./gradlew -PminimumAndroidInstrumentation :app:dependencyInsight \
  --configuration mockDebugAndroidTestRuntimeClasspath --dependency mockito-core
```

Install those matching APKs and run the existing Hilt runner/orchestrator with an
explicit device serial. R8 checks use the separate documented `-Pr8Verification`
profile. Default CI/JVM Mockito selection is unchanged. The resolved iOS production
inventory was exported again and still matches the bundled 78-artifact inventory.

Only Xcode 27.1 is installed here; Apple lists iOS 17+ simulator support for it.
iOS 16 qualification needs a compatible test environment or device. The declared
iOS 16 deployment minimum is unchanged. Physical-device/manual VoiceOver work
remains deferred by the owner's instruction; neither missing gate is checked off.

Sources: [Mockito Android API boundary](https://github.com/mockito/mockito/releases/tag/v5.23.0),
[Android navigation-bar capability](https://developer.android.com/reference/androidx/core/view/WindowInsetsControllerCompat#isAppearanceLightNavigationBars()),
[Apple Xcode simulator support](https://developer.apple.com/xcode/system-requirements).

## Resumed physical and minimum-runtime qualification — 2026-10-25

The owner authorized a change from iOS 16 to iOS 17.0 as the supported minimum,
and made the paired iPhone 15 Pro (iOS 27.0.1) available for testing. Shared Kotlin
compiler settings, every Xcode configuration, convention coverage and release
metadata checks now use the new minimum. The source audit rejects inconsistent
Xcode/shared targets; the package audit compares the built minimum to source.
Earlier iOS 16 archives and simulator results above remain historical evidence.

The initial physical run passed all seven About state cases and the explicit
SSL-consent/validation case. The orientation case initially failed while Portrait
Orientation Lock was enabled; after the owner disabled it, physical landscape and
About accessibility passed. HTTP consent/unreachable-server behavior also passed
on the scoped rerun. The remaining keyboard assertion assumed a UIKit Keyboard
container, but the phone's Gboard exposed actual Key elements without that
container. The harness now accepts visible keyboard keys and verifies an actual
edit plus retention after backgrounding; this does not suppress app audit issues.
The first typing diagnostic was unsuccessful and is retained in the raw results.

Release tooling tests pass 81 cases, including the new deployment-consistency
and stale-package checks. Convention-plugin tests pass. The development-signed
isolated Debug candidate passes signature, privacy/icon/notices and minimum-OS
audits. Optimized device tests, manual VoiceOver and actual iOS 17 runtime
qualification remain in progress; none is marked passed by these build checks.
The new unsigned optimized archive is
`build/kmp-phase-6/TeamCityApp-ios17-qualified.xcarchive`. Its source/dependency/
package audits pass: version 2.0.0/build 1, minimum iOS 17.0, 39,420,328-byte
executable with SHA-256 `fd22648c68064be1c2b9e5d91531df5bbb606b10b8553ce3e46db6264045bb0a`.
It includes the local-network purpose text and the unchanged reviewed native
license inventory. It supersedes the earlier iOS 16 package for current release
preparation; it remains unsigned and is not an uploaded or distributed build.
The interrupted first optimized command is excluded from test totals. The later optimized hardware command waited for the phone to unlock, then lost its runner connection before launch; it executed no cases and is also excluded. A fresh hardware run is required. Account
journeys use Debug-only test namespaces; Release measurements use the separate
validation app identity without enabling test vault behavior in production.

The physical fixture adds an explicit RFC1918/loopback interface option. Its
normal default remains loopback, and the CLI refuses mutation mode on a LAN
interface. Read-only XCTest URLs are restricted to a canonical private IPv4
address, fixed fixture port and context; management tests retain their original
loopback URLs. Browser links preserve the same fixture origin and context.
The app's native local-network purpose string explains user-selected TeamCity
connections. The isolated Debug test bundle compiles with these adapters, and
its package audit confirms that purpose text and the iOS 17 minimum.
All shared simulator test executables and the simulator framework were rebuilt
successfully for the new minimum before running runtime qualification.

Apple's catalog lists the final iOS 17.0 simulator (21A328). Xcode's current
filtered downloader did not offer it; its official DMG requires developer sign-in.
The owner is obtaining that runtime from Apple. No unsupported runtime, beta or
minimum-version declaration is substituted for execution evidence.

### Hardware findings and focused fixes

The available optimized phone run passed nine of eleven cases. Both failures
expected a signed-out Login screen but found a legitimately restored guest Home
session in the validation installation. The harness now prepares signed-out
state through the normal account UI, outside the measured launch block, retaining
saved accounts. Both scoped optimized reruns passed, including five launch
iterations and the keyboard/rotation/background case. Release continues to ignore
the Debug-only test-vault override.

The selected seven-case Debug hardware run passed browser/sharing/clipboard,
download cancellation on background/departure, Settings/theme/offline licenses,
keyboard/rotation/background and the approved real public guest journey, including
Keychain-backed relaunch and account reselection. The first artifact case failed
around first-use Local Network consent; the permission was subsequently granted.
Its focused rerun passed download/export, folder and archive behavior.

The strict hardware Overview audit found a real clipped action: Agent was exposed
as a 361×6-point button at the viewport edge. The iOS design-system modifier now
excludes interactive row fragments below the existing 48dp touch minimum until
scrolling reveals them. It runs before clickable semantics and clears the hidden
action's descendants, without changing layout or pointer interaction. Android's
platform flag is a no-op. Passive rows keep their semantics.

The regression audits the initial viewport without ignoring any issue, reveals
Agent through scrolling, checks its Button role, full hit area and accessibility label,
and audits again. Both focused physical cases pass. Android list/Overview renders
and host checks pass 289 cases; all prior PNG assets remain unchanged. Actual
same-phone before/after Overview frames were inspected: layout is unchanged apart
from the clock; the accessibility frame/exposure is the fix.

The broad resumed simulator run executed 28 cases: 27 passed and one live guest
pagination case failed while tracking a row by a changing accessibility-query
index. The harness now resolves the selected build by its stable build identifier.
Three final-source simulator reruns passed: strict Home/Overview and revealed-row
audits, retained browsing/sections/relaunch, and the real public guest journey.
This is a full run plus scoped reruns, not a claim of a second clean 28-case run.

The final optimized archive containing the clipped-row fix is
`build/kmp-phase-6/TeamCityApp-candidate-20261025.xcarchive`. Source, dependency
and package audits pass for version 2.0.0/build 1 and minimum iOS 17.0. Its
39,420,336-byte executable has SHA-256
`29f52f202b4dd2138364a947f8d5c5443478ad748450ee8f819ffa77ced92772`;
the bundled inventory hash is
`c583b4acd742ae9e3911b291e0f2ab8b3f5a49235f0b6ea66ea022d32765907b`.
This unsigned local package supersedes the pre-fix iOS 17 archive above. Nothing
has been uploaded or distributed. The final-source optimized eleven-case phone
pass is prepared with `test-without-building`. Its command remained at the locked
phone and was stopped before launch; it executed no cases. It is ready to rerun
after unlock, without another compilation.
Manual VoiceOver and actual iOS 17.0 runtime execution remain required. Hardware
captures and precise raw results remain under ignored build output; no device
identifier or signing profile is copied into the report.

### Final optimized phone pass and manual handoff

After the owner unlocked the phone, the final-source optimized validation run
passed all 11 selected tests, with zero failures and skips. Its result is
`build/kmp-phase-6/physical-final-unlocked.xcresult`: seven About states plus
About orientation/actions/audit, Login editing/rotation/background retention,
responsive launch and About scrolling/memory. The fixture-backed Home/Overview
check is excluded from this run and remains covered by the earlier final-source
two-case physical rerun, including artifact export and strict clipped-row audits.

The current device metadata reports iPhone 15 Pro, iOS 27.0.1 (24A446); the test
SDK is 27.1. The owner's mention of 27.1 does not substitute for either this
recorded OS version or the still-required iOS 17 minimum-runtime check.
Five optimized iterations averaged 0.572 seconds responsive launch, 1.037 seconds
for the About swipe pair, 144,492 kB peak physical memory and 127,912 kB absolute
physical memory. These are local measurements without a historical threshold;
they do not establish FPS, battery behavior or absence of leaks.

Actual final About portrait/landscape and keyboard captures were inspected. The
landscape keyboard frame shows Gboard and the retained edited URL; the harness
verifies actual entry rather than assuming a UIKit Keyboard container. No layout
or golden changes were made for this verification.

All automated phone control ended after the successful run. The normal Release
validation app was opened without preview or test-vault overrides, and a
read-only private-interface fixture was started for the owner's manual VoiceOver
walkthrough. The six walkthrough criteria are in `docs/ios-release.md`; the manual
gate remains unchecked until the owner reports results. Do not control the phone
while that walkthrough is in progress. The fixture has mutations disabled.

### Owner walkthrough findings and account parity follow-up

The owner did not claim completion of the six-part VoiceOver walkthrough. They
reported a build appearing to open twice and a mismatch between the expected
Android account flow and the simplified native Accounts/Login flow. Investigation
confirmed that History routed complete row snapshots through a reference-loading
visit before replacing it with managed Build Details. History now opens its
complete snapshot directly and rejects another callback from the outgoing visit;
queued-result Show actions keep reference resolution. A local navigation
checkpoint records the initial correction, with focused ownership/payload tests.

The owner explicitly selected the existing Android Account → Manage Accounts
flow with a separate Add user account screen. The former combined native Accounts
screen is replaced by shared `drawer`, `manage-accounts` and `create-account`
presentation modules, retaining Android Hilt/storage adapters and native
Keychain/session adapters. Initial Login remains separate. The native sign-out
action is capability-gated; Android's current menu is unchanged. Native account
IDs retain their full opaque public identity to distinguish guest and named users
on the same server. No credential or persisted account format is changed.

The port uses three KMP API modules and three new presentation modules; the
current inventory is 114 application modules, 66 KMP and 26 shared presentations.
Common ViewModels use constructor injection. Existing Android entry points now
wrap shared routes/screens with thin Hilt ViewModels and platform routers. Native
repositories map public identity keys onto the existing generation-scoped session
manager. A mismatched/missing key cannot select or remove another guest/member
identity with the same server and username. Sign-out remains native capability;
Android's current menu and persisted account/cache formats are unchanged.

The signed simulator scenarios pass on the compact phone (two cases), expanded
dark iPad (one), and phone at maximum accessibility text (one). Two subsequent
normal-text phone regressions also pass: retained Home/Favorites/sections/relaunch
and Account → Settings theme persistence/relaunch → About/offline licenses.
All six selected current-source scenarios pass with zero skips. They verify the
separate Add header/form/button and discard confirmation, separate Manage list,
current-account state, duplicate rejection, explicit HTTP consent, cancel/confirm
inactive removal, sign-out, final saved-account removal while signed out, and
History's single Back return. Strict Account/Add/validation/Manage audits pass.
The first Manage audit caught a bare URL label; its localized server context is
now exposed without changing pixels. The first lifecycle attempt stopped in the
capture helper under the HTTP modal; the final two-case run passes after using
the visible modal frame as the settling anchor.

Fresh checks: 45 common Android-host feature behavior cases; 63 Kotlin/Native
cases across the three features and shared root; 121 shared-host checks including
reviewed account renders; 231 app unit tests and the MockDebug application build.
Existing Android feature suites pass (Drawer 183, Manage 122, Create 129); their
intentional screenshot baselines are unchanged. The initial Android package
attempt exhausted the default 2 GiB D8 heap; the scoped 8 GiB retry passed.
Spotless apply/check passed. Native unit and signed UI tests used different
owned simulators.

The retired native-only prototype's 64 Android-host PNG fixtures are deliberately
removed, replacing them with 32 reviewed/verified shared-account host renders
covering light/dark, compact/expanded and enlarged text, content, busy, explicit
sign-out retry, and scrolled states. These are intentional host fixtures, not iOS
captures. Actual simulator screenshots are exported under
`account-parity-complete-attachments`, `account-parity-ipad-final-attachments` and
`account-parity-largest-text-attachments`. At maximum text, standard app bars
visually ellipsize long titles while retaining their full accessible labels;
controls remain reachable. New separate Manage/Add views are labeled as their
first native baseline, not given a fabricated before screenshot.

The updated optimized validation app and runner built successfully with the
existing development signing setup. The new unsigned normal-identity archive is
`build/kmp-phase-6/TeamCityApp-accounts-20261025.xcarchive`; its source, resolved
dependency and package audits pass. It declares 2.0.0/build 1/minimum iOS 17.0.
The executable is 39,961,688 bytes, SHA-256
`f2ceaa6456f472b88dbcc94d5885599375311e49694f1846e817f62c8ba70725`.
The 78-artifact and bundled notice inventory remain consistent with source.
No upload/distribution occurred. When the updated phone candidate was ready,
the owner chose to continue with simulators for now; no new phone installation
or automated control occurred. Physical/manual qualification remains deferred.

This follow-up changes the candidate after the 11-case physical run above. That
run remains valid historical evidence, and the updated account/navigation slice
requires its own UI evidence, regression checks and renewed physical/manual
qualification. Phase 6 stays open; iOS 17 runtime execution is also still required.

The earlier final-phone report publication initially returned an MCP timeout.
Reconciliation found its matching source `23831ff98d22ef7a01e064d1a09bfa937cad0c71`
as saved private version 5, deployment
`appgdep_6add90b2f6b08191be92366f72a7b89c`, confirmed succeeded. No duplicate
publication was created to recover that unknown outcome. The account-flow report
refresh follows this verified existing Site and preserves the owner-only audience.

The account/navigation checkpoints are `3153fb505` and `44835c390`, both local
and unpublished. The final account checkpoint contains only source, configuration,
behavioral tests and intentional golden fixtures. Generated diagnostics remain
under ignored `build/`; no heap dump is reachable from that branch.

## Report and checkpoints

Private Site published and verified on 2026-10-23 for the final simulator/build
verification slice:
[Phase 6 release validation](https://teamcityapp-kmp-phase-six-20261023.andrey-tolpeev.chatgpt.site).

Site project: `appgprj_6adaac3919c48191b8c7e45d35e27ebc`.

Local checkpoints on `codex/kmp-phase-1`:

- Runtime and behavioral tests: `08586ec15fe1c34308627539f8fe7da4cf90eb8f`.
- Qualification harness: `2e8716b2c015a084ea48a3cd618ce29fb079f3d4`.
- CI archive/audit gate: `e1b7f0b866f5f97c15a09bcbe89e9d6cfc2ffea1`.
- Owner-approved iOS 17 minimum and deployment audit: `9ffb4ca54`.
- Clipped-row accessibility behavior and regression: `2b664e589`.
- Isolated physical qualification fixtures and harness: `8aa8c4977`.

Initial Site source commit: `67d50a3342543b02a40a7795e729d6c420e74a15`.
Initial saved version: `appgprj_6adaac3919c48191b8c7e45d35e27ebc~appgver_bc9a0a0a00888191ae93b735b501ca58`.
Initial verified deployment: `appgdep_6adabdf5a6848191bc307e7da0e7807f`.

Earlier source/notice refresh: `71106cb95c7ee17db5cb7efaeedcc599b506d054`.
Earlier saved version: `appgprj_6adaac3919c48191b8c7e45d35e27ebc~appgver_beeae575c7c48191b0caaf07ef48d42c`.
Earlier verified deployment: `appgdep_6adac06711d4819195d4e09605dc2a73`.
Both optimized packages contain notice resources byte-identical to source; all
76 tooling tests passed again after normalization.

API 24 report update source: `4b08957f7f59b3796ecfcfc6ad13d3f1d76c7abe`.
API 24 saved version: `appgprj_6adaac3919c48191b8c7e45d35e27ebc~appgver_fe2f919ac5448191b9fbdbeda2479d0d`.
API 24 verified deployment: `appgdep_6adad32e11b48191b4b98567916654e7`, succeeded
on 2026-10-23. The private report includes the actual API 24 capture, selected-run
and scoped-rerun reconciliation, and separate physical/manual and iOS 16 gates.

This record, the checklist, release runbook and agent contract form the
accompanying `chore(docs): record KMP phase six simulator qualification`
checkpoint. At that earlier checkpoint, hardware/manual and iOS 16 qualification
remained open. The owner subsequently selected iOS 17 as recorded above; the
current remaining gates are recorded in the latest account follow-up below. Raw logs, captures, archives and inventories
remain ignored under `build/kmp-phase-6`; only intentional source/resources,
verification tooling and durable documentation belong in local checkpoints.


### 25 October report refresh

The existing owner-private report was updated and verified on 2026-10-25. It
contains 353 actual native frames across the retained evidence collections,
Android renders/captures and ten before/after comparisons, including equivalent
physical Overview frames and the revealed Agent action. It retains diagnostic
failures, focused rerun scopes, package hashes and the open manual/runtime gates.

Site source: `e777d52215637913a3e6f68b4d32391e3a01afd0`.
Saved version: `appgprj_6adaac3919c48191b8c7e45d35e27ebc~appgver_b93508f966048191a730d819e750b3b0`.
Verified deployment: `appgdep_6add8d3e4250819194696b95b2a70176`, succeeded
on 2026-10-25. Earlier receipts above remain historical. At that publication, the final optimized phone pass, manual VoiceOver and actual
iOS 17 minimum-runtime execution were open. The successful final phone result
above covered the earlier candidate; the subsequent owner account/navigation
findings reopened physical qualification. Phase 7 has not started.


### Account-flow report refresh and current handoff

The owner-private report update is verified succeeded. It includes 430 evidence
frames: 416 actual iOS device/simulator captures, 2 Android emulator captures and
12 deterministic Android renders, plus 17 before/after comparisons, the account flow,
platform adapter differences and current versus historical verification scopes.
All referenced native frame hashes were checked. The new common account host
fixtures are labeled separately from iOS captures. Browser QA checked the
responsive flow and current evidence/status controls.

Site source: `bce188aafa9984ece98f0eacd82abb81aa829845`.
Saved version: `appgprj_6adaac3919c48191b8c7e45d35e27ebc~appgver_7d1a80c942ec8191b3688b60edffabc0`.
Verified deployment: `appgdep_6adda2214b3c81919e9c49c1fc407f45`, succeeded
on 2026-10-25. Owner-only access was preserved.

The updated app is built and simulator-verified, with an audited optimized
archive and local source checkpoints. At the owner's request, no updated phone
installation or automated phone pass was attempted. Its installed validation app
remains the earlier candidate. A new physical pass, complete manual VoiceOver
results and actual iOS 17 runtime execution remain required. Phase 6 remains
unchecked; Phase 7 has not started. No app-code push, PR, upload or distribution.

### Current-OS simulator continuation

The owner kept phone testing deferred. The installed iOS 27.1 runtime
(build 24A94425) supports the iPhone Duo model in this local inventory. A new
owned simulator runs the updated optimized validation app, version 2.0.0/build 1,
compiled with Simulator SDK 27.1 and still declaring minimum iOS 17.0. No physical
installation or automated phone control occurred. The physical Release test
runner was rebuilt offline after the harness changes and is ready for a later
owner-authorized phone pass.

The initial ten-case Release run (`ios271-account-release.xcresult`) passed the
seven deterministic About checks and failed two landscape checks plus the old
fixed 20-point header assertion. This is a diagnostic result, not ten passing
cases. The header actually began at 17.33 points on this model, whose status
controls occupy a right-side region.

The independent control consists of `scripts/ios-geometry-control.swift`, built
with `python3 scripts/build-ios-geometry-control.py` into ignored build output.
It has no account storage or network behavior. Its minimum comes from the shared
Config, and its simulator bundle and linked SDK metadata match the app. The
opt-in `testSimulatorRotationControl` checks plain UIKit, SwiftUI hosting a UIKit
controller, and the shared Compose app; it captures all active screens and
requires the control's native metrics label before interpreting its measurements.
It is simulator-only and skipped without `TEAMCITY_GEOMETRY_CONTROL=1`.

Earlier control attempts are retained: the first did not render its metrics;
explicit scene/window setup corrected that. The next linked SDK 27.0 and entered
legacy window compatibility. The definitive matched-SDK run is
`ios271-rotation-control-matched.xcresult`; both binaries declare SDK 27.1 and
minimum 17.0, and all three hosts use 466×678-point portrait windows. After the
landscape request all three still report 466×678; UIKit/SwiftUI report interface
orientation portrait despite the device request. All three landscape assertions
remain failed, so the Duo environment is not accepted as rotation evidence.
The same final control test passes on the iPhone 17 Pro / iOS 26.4.1 simulator:
all hosts change from 402×874 to 874×402. This is evidence of a limitation in the
new simulator/display setup, not proof of a Compose-only rotation regression or
of universal compatibility with every Duo configuration.

SDK-matched UIKit/SwiftUI report insets top 0, left 0, bottom 34, right 84 on the
Duo fixture. Header verification now checks its full rectangle against the
independently specified native window/insets, including the side status area.
`TEAMCITY_UI_EXPECTED_SAFE_AREA=0,0,34,84` supplies this measured fixture. Existing
targets retain the default 20-point top minimum; malformed/non-finite/negative
fixture input fails. No production inset/layout or accessibility issue filtering
changed. The normal strict accessibility audits remain enabled.

The first focused rerun stopped because Release restored Account while the old
setup assumed Home. Shared validation setup now reaches Login through normal
Back/account/sign-out controls, including Add discard, without deleting saved
accounts or changing production persistence. Fixture journeys call this setup
independently rather than relying on a previous test to leave Login open.

`ios271-account-ready.xcresult` passes launch and the complete account/navigation
scenario (2/2). The final harness independently reruns the scenario from its
retained visit in `ios271-account-final.xcresult` (1/1), including strict
Account/Add/validation/Manage audits, distinct Add/Manage routes, discard, and a
single build visit with one Back to History. `ios264-harness-regression.xcresult`
passes 3/3: launch, all three independent rotation hosts, and the account journey
using unchanged default header bounds. These are scoped reruns plus retained
initial failures, not a second clean ten-case run on iOS 27.1.

The final test bundle is produced by `xcodebuild ... build-for-testing`; its
explicit `.xctestrun` manifest is reused with `test-without-building`. Current
and older UI runs control separate owned simulators. Native unit tests do not
share either device during these runs. The diagnostic builder, Python syntax,
Simulator/physical Release runner builds and Spotless checks pass. Application
UI/data code and Android golden fixtures are unchanged by this follow-up.
The local harness checkpoint is `cca5585cb`.

The iOS 17 runtime remains absent from the installed inventory. Its official DMG
path has been requested; compilation and iOS 27.1 execution do not close the
minimum-runtime gate. The updated physical pass and full owner VoiceOver
walkthrough also remain deferred. Phase 6 remains open, with Phase 7 untouched.


### Current-OS private report receipt

The owner-private report refresh succeeded on 2026-10-25. It contains 470
frames: 456 actual iOS device/simulator captures, 2 Android emulator captures
and 12 deterministic Android renders, plus 17 before/after comparisons.
The new section compares SDK-matched UIKit, SwiftUI and Compose geometry and
separates failed diagnostics from the focused account pass. All 458 actual
platform-capture hashes and referenced HTML files were checked; browser QA
verified the new comparison table and iOS 27.1 screenshot selector. No audience
change or application distribution occurred.

Site source: `714e707f37a839827faf782c0a802d232566b433`.
Saved version: `appgprj_6adaac3919c48191b8c7e45d35e27ebc~appgver_b2c80117906c8191ba7dba9bc2a402ca`.
Verified deployment: `appgdep_6addb20d47bc8191bdcf08858bd52521`, succeeded.
The two task-owned UI simulators are shut down with their data preserved.
The phone remains untouched. Physical, full manual VoiceOver and iOS 17 gates
remain open; Phase 7 has not started.

### Historical sharing audit after owner review

This subsection records the pre-consolidation checkpoint `40040d804`. The
owner subsequently authorized replacing the listed runtime splits; see the
current implementation below. Its statements describe that earlier source tree.

The owner clarified the intended result: common business rules and app UI
behavior on Android and iOS, with explicit disclosure of native implementations.
The current migration does not yet satisfy that full requirement. `commonMain`
means portable source; it does not establish that both installed hosts execute
that implementation. The 26 feature presentation modules are referenced by both
Android feature adapters and the native root, but their data and route adapters
still differ. This is a source/dependency audit, not a runtime parity pass.

The shared presentations cover Login, Account, Manage Accounts, Add Account,
Home, Projects/configurations, Favorites, Running Builds, Queue, Agents, History,
Overview, Build Details, Changes/Change Details, Tests/Test Details, Parameters,
Snapshot Dependencies, Artifacts, Run Build, Filter Builds, Quick Filter,
Action Bottom Sheet, Settings and About. Their common screens/ViewModels do not
by themselves prove equal host configuration, repository policy or navigation.

| Area | Android production implementation | iOS production implementation | Unresolved sharing or behavior boundary |
| --- | --- | --- | --- |
| App root/navigation | `app/.../navigation/MainActivity.kt`, registrations, `AppAccountFlowNavigation`, Android saved-state/Intent adapters | `shared-app/.../PhaseTwoApp.kt`, `NativeBrowsing`, `NativeManagement`, `NativeJourneySnapshot` | Common navigation machinery, separate route coordination, restoration orchestration and callbacks. |
| Accounts | `DefaultDrawerRepository`, `DefaultManageAccountsRepository`, installed encrypted storage/cache cleanup | `NativeAccountSwitcherRepository`, `NativeManageAccountsRepository` over portable sessions/storage | Common Account/Manage ViewModels; repository orchestration and identity lookup differ. |
| Session lifetime | Common `AccountSessionManager` via `SessionModule`/`LegacyAndroidAccountStorage`, plus retained Android API graph | Same common manager, `SharedAppViewModel` and `TeamCityAccountGraph` | Session primitives are genuinely shared; API initialization/reset wiring is separate. |
| Sign out | Account does not expose the optional action | `supportsSignOut = true`; shared Account supplies the action while active | Real product difference, not a KMP constraint. Sign out clears active selection while preserving saved accounts. |
| Authentication | `AppAuthenticationRepository`, OkHttp validation, installed storage/API initialization | `TeamCityAuthentication`, Ktor validation and portable session persistence | Common form/presentation contract; separate URL/error/authentication orchestration. |
| Add HTTP consent | `AndroidCreateAccountViewModel` keeps shared default `checkSecureConnection = false` | `NativeAccounts` explicitly supplies `true` | Same ViewModel configured for different policy. |
| Browsing/read data | `libraries/data`, Android feature repository adapters | `TeamCityAccountGraph`, `TeamCityBrowsingRepositories` | Separate request/mapping/cache/preference implementations behind common feature contracts. |
| Build management | `DefaultRunBuildRepository`, `DefaultBuildDetailsRepository`, existing service | `TeamCityManagementRepositories` | Shared UI/state, separate payload/repository execution logic. |
| Favorites/filters | Installed account/preferences and filter adapters | Portable account preferences and browsing repositories | Persistence and query-building paths differ. |
| Downloads | `DefaultArtifactsRepository`, Android file/permission adapters | `TeamCityArtifactsRepository`, `IosArtifactFileStore` | Shared presentation; separate transfer repositories and native file handling. |
| Onboarding/rating prompts | Persisted Android prompt flags and rating eligibility | Home/History/Details prompt providers return empty; project rating eligibility is false | Behavioral parity is incomplete. Manual account/About rating action remains available through Apple. |
| Login surrounding UI | Android feature route | Native Login adds an Accounts entry | Host-owned UI/navigation difference around the common screen. |
| Settings | Common Settings over DataStore; Auto Battery option where supported | Same presentation over UserDefaults; Light/Dark/System | Native storage boundary plus differing available options. |
| Licenses | Google's OSS Licenses activity | `NativeLicenses` common-source list/readers used by iOS | Separately implemented UI. |
| Diagnostics | Existing Firebase analytics/crash integrations | Fixed OS events, Apple crash reports and some no-op trackers | Instrumentation differs; no hosted iOS upload configured. |

The new network stack is a client of the same TeamCity REST server, not a new
backend/proxy. Android production still executes its Retrofit/OkHttp/Gson/
RxJava/RxCache adapters. iOS executes common `TeamCityTransport` (HTTP/authentication,
origin/context guards, streaming and no automatic uncertain-write retry),
`TeamCityApi` (REST/JSON mapping and account-activation memory caches), and
`libraries/shared-data` feature repositories over the Darwin engine. An Android
Ktor/OkHttp engine exists, but Android production is not wired to `shared-data`.
Its existing RxCache disk policy and the new memory caches are not equivalent.

Concrete source locations:

- [Shared-root dependencies](../shared-app/build.gradle.kts).
- [Native account routing/policy](../shared-app/src/commonMain/kotlin/teamcityapp/shared/NativeAccounts.kt) and [repositories](../shared-app/src/commonMain/kotlin/teamcityapp/shared/NativeAccountRepositories.kt).
- [Android authentication](../app/src/main/kotlin/com/github/vase4kin/teamcityapp/integration/authentication/AppAuthenticationRepository.kt) and [portable authentication](../libraries/shared-data/src/commonMain/kotlin/teamcityapp/libraries/shared_data/TeamCityAuthentication.kt).
- [Android common-session wiring](../app/src/main/kotlin/com/github/vase4kin/teamcityapp/di/modules/SessionModule.kt).
- [Portable network](../libraries/network/src/commonMain/kotlin/teamcityapp/libraries/network/TeamCityTransport.kt) and [native graph](../libraries/shared-data/src/commonMain/kotlin/teamcityapp/libraries/shared_data/TeamCityAccountGraph.kt).
- [Native root and disabled prompts](../shared-app/src/commonMain/kotlin/teamcityapp/shared/PhaseTwoApp.kt), [native browsing repositories](../libraries/shared-data/src/commonMain/kotlin/teamcityapp/libraries/shared_data/TeamCityBrowsingRepositories.kt) and [native license UI](../shared-app/src/commonMain/kotlin/teamcityapp/shared/NativeLicenses.kt).

Native boundaries remain necessary for Keychain versus Android secure storage,
Darwin versus OkHttp engines, UserDefaults versus DataStore, browser/share/export/
clipboard APIs, application lifecycle/hosting, system date services and VoiceOver
semantics. Those boundaries do not require separate product rules. The target
is common decisions/contracts with small platform implementations. Remaining
consolidation concerns production data/network wiring, account policies,
route orchestration, prompt eligibility and the listed UI differences.

This audit changes documentation only. It neither implements that consolidation
nor starts Phase 7. The feature-parity checklist remains unchecked, alongside
physical/manual/iOS 17 qualification. The installed runtime inventory was checked
again after the audit: 26.4.1, 26.5, 27.0 and 27.1 are available; 17 is absent.
The owner still defers phone control. No newer-runtime or source-audit result
replaces the missing qualification evidence.


The sharing-audit report refresh is verified succeeded, owner-private, on
2026-10-25. Source: `a1aa37f5693499d0dbe6eb78b1256f306f05a10e`.
Saved version: `appgprj_6adaac3919c48191b8c7e45d35e27ebc~appgver_66b2a394247c8191af125a97c1ebd3db`.
Deployment: `appgdep_6addb46031708191a934a98538fbd048`.
Browser QA checked the new sharing section; all eleven repository source links
and HTML asset references resolve locally. Spotless passes. The 470 existing
frames and 17 comparisons are unchanged. No app/runtime or phone action occurred.

### Authorized production consolidation

The owner explicitly authorized implementing the sharing target after the audit.
This is the Phase 6 parity follow-up, not an implicit start of Phase 7. The
remaining physical/VoiceOver/minimum-runtime qualification stays separate.

- [x] Both production hosts execute the common root, route/result/restoration logic.
- [x] Both execute the same authentication, account and feature repositories.
- [x] One account/consent/sign-out policy; preserve installed Android encrypted accounts and favorites.
- [x] Common cache policies with durable platform storage; preserve installed legacy cache files.
- [x] Common prompt/rating eligibility and persisted onboarding behavior.
- [x] Common Settings options/theme resolution and license-reader UI, with native content/storage adapters.
- [x] Native adapters own only OS services and data-format migration boundaries.
- [x] Exercise production networking, alias compatibility, stale-account isolation and both host journeys.
- [x] Verify common behavior on Android/iOS, affected Android goldens and actual host UI evidence.
- [x] Update the private report with implemented versus pending scope and save local checkpoints.

The initial compile-only foundation has been superseded by the verified source
and host journeys recorded below. Qualification for device/manual/minimum-runtime
coverage remains separate.



### Production-core implementation (owner-authorized follow-up)

Both shipped source entry points now execute the same common application:
Android `MainActivity` → `TeamCityApp` → `PhaseTwoApp`, and iOS
`MainViewController` → `PhaseTwoApp`. `Native*` file names inside commonMain
are historical names, not iOS-only implementations. The local foundation
checkpoint is `304067c36` (`feat(kmp): share the production app core across platforms`).

| Feature/rule | Shared implementation now used by both | Android boundary | iOS boundary |
|---|---|---|---|
| App root, retained Home stacks, destination visits/results, account restoration | `shared-app`, `libraries/navigation`; exact account ownership validated before content restoration | Installed alias/Intent decoding and framework Bundle bridge | UIKit scene/controller lifecycle and public snapshot byte storage |
| Login/Add Account validation, HTTP/TLS consent, duplicates and request cancellation | Common authentication presentation and `TeamCityAuthentication` | Ktor OkHttp engine | Ktor Darwin/URLSession engine |
| Account, Manage Accounts, Add user account, switching/removal/sign-out | Common screens/ViewModels, `AccountSessionManager`, `AccountRecords`, account repositories | Existing ciphertext/JSON mapping through `LegacyAndroidAccountStorage`; DataStore preference migration | Common vault envelope through Keychain |
| REST queries, JSON/domain mapping, management safety, cancellation | `TeamCityTransport`, `TeamCityApi`, `TeamCityAccountGraph`, `TeamCityBrowsingRepositories`, `TeamCityManagementRepositories` | OS HTTP engine only in production | OS HTTP engine only in production |
| Cache format, TTLs, bounds, invalidation and account guards | `TeamCityResponseCache` | AtomicFile bytes | Foundation atomic file bytes |
| Projects/Favorites/Activity/History/Overview and sections, Run/Filter/quick actions | Existing common feature presentations and the same common route registrations | Hilt root construction only | Lifecycle-owned root construction |
| Education and project rating eligibility | Common account-scoped prompt flags and local rating rule; migrated Details metadata VM and History coachmark | Migrates installed prompt/rated flags | Same common policy, persisted with account metadata |
| Theme choices and theme resolution | All four choices: Light, Dark, System, Auto Battery; common root computes the effective theme | Existing DataStore preference file; PowerManager power-saving notifications | UserDefaults preference; NSProcessInfo low-power notifications |
| About/license list/text/loading/error/retry/navigation | Common presentation and `AppLicenseRepository` contract | Reads actual packaged OSS plugin catalog and byte offsets | Reads bundled native dependency notices |
| Artifact naming/progress/cancellation/completion rules | Common `ArtifactTarget` and repository/ViewModel; unique per-transfer target on both | FileOutputStream/atomic rename/FileProvider | POSIX bytes/rename/system activity sheet |
| Browser/log/diff/share/copy/rating | Common decisions, safe links and error UI | Custom Tabs/Intents/clipboard/installed rating integration | UIKit browser/activity sheet/pasteboard/StoreKit |
| Diagnostics | Shared tracker event decisions with fixed labels | Existing Firebase delivery and crash integration | Local OS labels/Apple crash reports; no hosted upload configured |

Behavior changes are deliberate and visible: Android now exposes Sign out while
signed in; both hosts have the same separate Account/Manage/Add journey. Sign out
keeps saved accounts and removes only the active selection. Add Account requires
explicit HTTP consent on both. Native Settings gains Auto Battery, and Android
license navigation uses the same list/reader as iOS. Inventories differ because
platform dependencies differ; an Android notice without an upstream URL omits
that optional source action. Common account rules also choose the same fallback
account after removing the active record, without decoding unrelated secrets.

The previous Android Remote Config rating gate is replaced by the common local
eligibility rule (nonempty project content plus the shared persisted handled flag).
Acknowledging or dismissing education/rating persists per account. Migrated flags
prevent already-dismissed Android education from reappearing. No hosted iOS
analytics service was added.

The new network layer calls the user's existing TeamCity REST server directly.
It is not a proxy/backend. Queries, Basic challenge handling, HTTP/TLS consent,
same-origin/context checks, byte streaming, cache policy and no automatic replay
of uncertain writes are common code. Only the operating-system HTTP engines differ.
The Android mock flavor uses an injectable wire factory over its existing fake
service, so the production common parser/repositories/UI still execute. Only explicit
loopback/emulator-host fixtures use the actual engine; external mock origins
remain deterministic and offline.
The mock bridge exists only in the mock source set; it is not in production iOS
or the Android prod flavor.

Preservation and migration limits:

- Existing Android encrypted account JSON/ciphertext, favorite IDs and installed
  component aliases are preserved. DataStore metadata mirrors favorite updates
  back to the legacy snapshot; interrupted mirrors repair on read.
- Old RxCache files remain intact, but entries are not imported into the new
  bounded response cache. A first request after migration fetches the server.
  The new disposable envelope is limited to one active account, 128 entries,
  1M characters/document and 4M total; it rejects corrupt/stale/cross-account data.
  Optional cache I/O failure cannot undo a successful server operation.
- Previous Android downloads remain intact. New same-named downloads each get
  their own cache directory on both platforms, instead of replacing Android's
  previous same-name file. Unfinished directories are removed on cancellation.
- Old Android root Bundle serializer state is incompatible with the common
  registry and is intentionally discarded once. Persisted accounts/preferences
  remain. New common-root state and pending launches restore with exact account
  validation, and a consumed launch is not replayed after recreation.
- Navigation read/write failures offer common retry UI; cancellation propagates.
  Destination factories seed arguments into owner-provided SavedStateHandles,
  preserving restored user selections rather than creating unattached handles.
- Retained legacy Android feature adapters, DTOs and regression harnesses still
  compile. Their existence is not an active second production UI/data path.
  Removing all compatibility/test infrastructure is outside this parity slice.

Verification receipts and current-candidate runtime evidence follow below.
Physical/manual VoiceOver/iOS 17 qualification remains open. The phone is untouched;
Phase 7 and app distribution have not started.


### Consolidation verification receipts

The final scoped JVM regression has **957 cases**, **956 passed**, **one opt-in
live-server skip**, zero failures/errors: app 234, shared-app 169, Account 191,
History 245, network 25, shared-data 33, accounts 14, Android session 16, Details
metadata 30. This covers the affected common root, repositories, migration,
metadata and existing/new screenshot fixtures; it is not a second complete
4,435-case run or a complete 400-case device suite. Existing golden fixtures
were not rewritten. Forty new Settings/license/navigation-save-error fixtures and
eight new sign-out fixtures were inspected and verified, including both themes,
compact/expanded sizes and enlarged text. The 2x Settings capture also verifies
scrolling the final choice into view.

Dedicated iOS simulator unit results total **127 passed** across accounts 14,
network 24, shared-data 33, Details metadata 30, shared-app/native storage 26.
The Native unit device and UI device were separate throughout.

Android API 34 emulator `shared-production-android-14-isolated-final.log` passes
**14/14** root/account/restoration/Favorites/Run/Filter scenarios. This includes
Account → Manage → Add/discard, sign-out/recreation, aliases, retained stack,
filter/run results, one queued result through recreation, cancellation after a
forced shared account-generation replacement, and Open favorites returning to
the live Home owner while retaining the Projects History visit.

A separate focused Android packaged-notice/Settings integration check passes
**1/1** (`shared-production-android-license-final.log`). It loads the actual
packaged Android catalog, opens a nonempty common license reader, verifies all
four theme choices and captures the rendered list/text/Settings screens.
This is 14 cases plus one scoped check, not a claimed clean full device suite.

The expanded journeys caught a real missing common snackbar action: FavoriteAdded
now selects the live parent Home/Favorites owner, with a common parent/child
regression test. Earlier harness attempts and their logs are retained. Tests now
reset only the mock app's public navigation/preference sidecars between independent
cases, so previously saved favorites cannot override each case's installed legacy
fixture. This reset occurs before test setup and does not erase account credentials
or affect production persistence. Screenshot captures wait for the settled route
and two rendered display frames; the first premature Add capture was rejected.

Signed iPhone 17 Pro / iOS 26.4.1 simulator checks pass in scoped runs:

- `shared-production-ios-journeys.xcresult`: Account/Manage/Add validation and
  strict accessibility audits, one build visit and one Back to History passed.
  Two platform scenarios initially skipped because their separate opt-in flag
  was missing; they are not counted as passes in that run.
- `shared-production-ios-platform.xcresult`: Settings choices, Dark persistence
  after relaunch, About, rating return and offline licenses passed. Artifact
  navigation paused on the newly enabled History education in that harness.
- `shared-production-ios-artifacts-final.xcresult`: folders, archive actions,
  download completion and native export passed after the harness completed the
  common education using its normal public dismiss control. This is a focused
  rerun, not a second clean full platform suite.

The common coachmarks are exercised rather than disabled in simulator journeys.
No accessibility issue type was suppressed. The account audit remains strict.
The subsequent Favorites action has common Android/iOS unit coverage and its
actual Android retained-pane integration pass.

ProdDebug and the final optimized Android R8 verification package build. The
unsigned optimized `SharedProductionFinal.xcarchive` passes the current source
packaging audit: version **2.0.0**, build **1**, minimum **17.0**, no embedded
provisioning, bundled inventory hash matching source, and zero audit errors.
The executable SHA-256 is
`e4fe0850df6069113e07dda44b1e086ac6dcc0b27ce219f4516986ee896f98d6`.
The exported native dependency inventory still matches the bundled notices.
The earlier `SharedProduction.xcarchive` predates the final Favorites fix and is
historical. No archive was uploaded or distributed.

Build logs and actual captures are under `build/kmp-phase-6/shared-production-*`.
Formatting passes. Final source/checkpoint/report receipts are recorded after
publication. Current-candidate physical/manual VoiceOver and actual iOS 17 runtime
execution remain open, so Phase 6 is not complete and Phase 7 has not started.


The second local source checkpoint is `373552201`
(`feat(kmp): unify account policy and native service boundaries`). Its exact
native/compatibility adapters are:

- `app/.../navigation/MainActivity.kt` and `RootActivitySavedState.kt`: Android
  framework/installed Intent compatibility and host restoration only.
- `app/.../navigation/AndroidAppPlatform.kt`: Android browser/share/clipboard/
  rating/power/lifecycle/file I/O and FileProvider export.
- `AndroidSharedAppViewModel.kt`: Hilt construction and DataStore snapshot bytes.
- `AndroidResponseCacheStore.kt`: atomic file bytes; common code owns cache rules.
- `AndroidLicenseRepository.kt`: the actual Android OSS resource catalog/offsets.
- `libraries/session/.../LegacyAndroidAccountStorage.kt` and
  `AndroidAccountPreferencesStorage.kt`: preserve/migrate installed encrypted
  records, favorite mirror and public metadata. Account mutation policy is common.
- `libraries/network/src/androidMain/.../PlatformClient.kt` versus
  `src/iosMain/.../PlatformClient.kt`: OkHttp versus Darwin HTTP engines.
- `shared-app/src/iosMain/.../IosNativePlatform.kt`: UIKit/StoreKit/Foundation/
  POSIX services, UserDefaults and atomic cache/snapshot file bytes.
- `shared-app/src/iosMain/.../MainViewController.kt` and the Xcode/Swift shell:
  controller/scene entry, Keychain namespace, icon/privacy/signing/packaging.
- Native secure-vault, date/locale and clipped-action accessibility actuals remain
  OS adapters. Android tooltip semantics and iOS clipped-fragment behavior remain
  intentionally platform-appropriate; no business/navigation policy is forked there.
- Android legacy feature registrations/Retrofit/RxCache/Gson DTO codecs still
  support compatibility and historical regression harnesses. Android mock-only
  `SharedClientModule`/`SharedMockClient` adapt deterministic test wire data;
  only explicit loopback/emulator-host fixtures can use the real mock engine path.

Existing explicit themes are retained. Fresh installs use the common System
default and all four choices on both platforms. The final normal ProdDebug APK
was installed in place on the task emulator: the existing approved public Guest
account restored and actual JetBrains projects loaded through the common Ktor/
OkHttp graph. No account data was cleared and public TeamCity verification stayed
read-only. Its local rating prompt was dismissed normally; no store review was sent.
The production Debug startup/display capture is labeled separately from mock
instrumentation captures. Physical-device control remains deferred.


The final optimized Android package also passes **6/6** isolated R8 smoke checks
(`shared-production-r8-smoke-final.log`): JSON/serialized models, encrypted-account
compatibility, retained Retrofit/disk-cache codecs, event/date resources, common
Login startup and packaged OSS resources/external-menu compatibility. These
smoke tests do not imply that the retired Retrofit graph drives the common root.
The current production Debug public-Guest startup also passed separately.


### Shared-core private report receipt

The owner-private report update succeeded on 2026-10-25 at 16:12 UTC.

- URL: https://teamcityapp-kmp-phase-six-20261023.andrey-tolpeev.chatgpt.site/#consolidation
- Site source: `e997a9d38dbc069b1493a38cd102ae1ee96539c6`.
- Saved version: `appgprj_6adaac3919c48191b8c7e45d35e27ebc~appgver_72ed94654d9881919d53c29748e8780d`.
- Deployment: `appgdep_6ade2a36c0ec8191979de2a0db0351aa`, succeeded.
- Audience verified immediately before publication: one owner, zero editors,
  groups or external visitors; no sharing change.
- 548 frames and 20 comparisons: 478 actual iOS captures, 10 actual Android
  captures and 60 deterministic renders. The new collection adds 22 iOS,
  eight Android and 48 reviewed render fixtures. Existing historical evidence
  remains labeled by source/candidate; three new comparisons show Android sign-out
  and iOS theme-option changes.
- All frame hashes and local HTML references were checked. Browser QA verified
  the common-core/native-boundary diagram, current readiness data, actual
  Android/iOS Account pair, iOS Settings gallery and Android sign-out comparison.
  Versioned JSON/JS references refresh the prior audit in existing browser tabs.

The report's attached implementation record is the packaging-time snapshot;
this successful publication receipt is recorded in the repository afterward.
Source checkpoints remain local and no application package was distributed.
The implementation follow-up checklist is complete. The wider Phase 6 gates for
current physical-device, complete manual VoiceOver and iOS 17 runtime execution
remain open, and Phase 7 has not started.
