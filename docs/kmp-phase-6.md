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
Latest saved version: `appgprj_6adaac3919c48191b8c7e45d35e27ebc~appgver_fe2f919ac5448191b9fbdbeda2479d0d`.
Latest verified deployment: `appgdep_6adad32e11b48191b4b98567916654e7`, succeeded
on 2026-10-23. The private report includes the actual API 24 capture, selected-run
and scoped-rerun reconciliation, and separate physical/manual and iOS 16 gates.

This record, the checklist, release runbook and agent contract form the
accompanying `chore(docs): record KMP phase six simulator qualification`
checkpoint. Phase 6 remains in progress for deferred hardware/manual and
iOS 16 runtime qualification; Phase 7 has not started. Raw logs, captures, archives and inventories
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
on 2026-10-25. Earlier receipts above remain historical. The current minimum is
iOS 17.0; Phase 6 remains open for its final optimized phone pass, manual VoiceOver
and actual minimum-runtime execution. Phase 7 has not started.
