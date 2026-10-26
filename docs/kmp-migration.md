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

# Kotlin Multiplatform migration checklist

TeamCityApp will share Kotlin business logic, lifecycle-owned ViewModels, and
Compose UI between Android and iOS. Android remains usable throughout the
migration. Feature APIs stay small; platform APIs remain behind explicit adapters.
The former toolchain phase and shared-foundation phase are combined into Phase 1.

## Execution contract

- Start only the phase explicitly requested by the user, with one goal scoped to
  that phase's implementation, verification, evidence, and report.
- Capture the starting commit and preserve unrelated existing work. Implement
  small coherent slices, then verify and checkpoint using GitButler.
- Update task checkboxes only when evidence supports completion. A failed or
  unavailable required check leaves its task and phase incomplete. Explicit owner
  scope changes may move checks to final acceptance; record the decision and keep
  the moved checks unchecked rather than claiming they passed.
- Each completed phase needs a dedicated private Sites report linked here. Show
  outcomes, actual UI evidence, code/architecture changes, compatibility effects,
  verification results, outstanding work, and starting/final commit references.
- For UI changes, compare actual Android before/after images and iOS before/after
  images in equivalent states, themes, text scales, and layouts. The first iOS
  screen is an initial baseline, never a fabricated before image. Distinguish
  Robolectric renders, simulator captures, and physical-device captures.
- Store generated build logs and raw captures under `build/kmp-phase-N`. Commit
  intentional golden fixtures, reproducible source/configuration, and durable
  reports. Do not commit build products or unrelated store screenshots.
- Mark a phase complete only after its implementation, required checks, report,
  and checkpoint commits are complete. Completing one phase does not authorize
  starting the next. Do not push or create an application PR unless requested.

## Progress

P0 establishes feasibility and the first usable iOS app. P1 delivers parity and
release readiness. P2 covers the explicitly scoped final platform refinements.

- [x] Phase 1 — Toolchain and shared foundation (P0; completed 2026-10-22)
- [x] Phase 2 — Accounts and first real workflow (P0; completed 2026-10-22)
- [x] Phase 3 — Home and build browsing (P1; completed 2026-10-22)
- [x] Phase 4 — Build management (P1; completed 2026-10-22)
- [x] Phase 5 — Downloads and platform integrations (P1; completed 2026-10-23)
- [x] Phase 6 — Release validation and cleanup (P1; completed 2026-10-26; final qualification deferred by owner)
- [ ] Phase 7 — Platform review and refinements (P2; all six refinements implemented; final acceptance open)

## Phase 1 — Toolchain and shared foundation

**Outcome:** the same About screen runs on Android and iOS with deterministic
data, shared state/theme/resources, automated checks, and an evidence report.
Real iOS accounts, network access, and outgoing platform actions are later phases.

### Implementation

- [x] Record the seven-phase checklist and phase/report workflow.
- [x] Capture Android About test/golden baseline before edits (27 images).
- [x] Pin and verify compatible Kotlin, Compose, Material, Gradle, AGP, and Xcode.
- [x] Define scope: Android API 24+, iOS 17+ (owner changed the original iOS 16 minimum on 2026-10-25), arm64 devices and Apple Silicon
  simulators; compact and expanded phone/tablet layouts.
- [x] Inventory all modules and classify shared/platform/retained ownership.
- [x] Add KMP architecture, resources, DI, source-root, and test rules to AGENTS.md.
- [x] Add reusable KMP library and Compose conventions alongside Android conventions.
- [x] Preserve the Android app/flavors and add a small iOS host and framework.
- [x] Share About repository/model contracts; keep Android destination keys scoped.
- [x] Share portable theme tokens, components, resources, and typed UI copy.
- [x] Share About UI/state/ViewModel, retaining Android Hilt/router adapters.
- [x] Provide deterministic available/unavailable/loading fixtures for the iOS host.
- [x] Preserve Android screen actions, application version, and real repository.
- [x] Verify lifecycle-owned construction, retention, cancellation and resubscription.
- [x] Add macOS CI, iOS compilation, common tests, and simulator UI smoke checks.
- [x] Preserve Android screenshot verification of the actual shared composables.

### Completion evidence

- [x] Convention-plugin tests and affected shared/Android behavioral tests pass.
- [x] About goldens inspected and verified for loading/content/optional failure,
  both themes, compact/expanded layouts, and enlarged text, including lower actions.
- [x] Android application and instrumentation integration compile; production
  About wiring compiles and affected lint/formatting checks pass.
- [x] iOS arm64 and simulator frameworks compile and the simulator app runs.
- [x] Common behavioral tests and iOS UI smoke tests pass on a simulator.
- [x] Actual Android after and iOS initial images captured and inspected.
- [x] Module inventory, toolchain decisions, limitations, and verification recorded
  in `docs/kmp-phase-1.md`.
- [x] Dedicated Sites report published privately and verified; link recorded below.
- [x] Task-only checkpoint commits saved and references recorded below.

**Baseline:** Android source base `49a6c4bd7`; GitButler workspace HEAD at start
`550745178ac91b65c18f3eabf53e89a953b31e6d`. Existing README/store-image edits
belong to other work and are excluded. Baseline About tests passed on JDK 17.

**Report:** [Private Phase 1 review](https://teamcityapp-kmp-phase-one-20261022.andrey-tolpeev.chatgpt.site).
**Evidence:** [Implementation and verification record](kmp-phase-1.md).
**Checkpoint commits:** implementation `0cf1fe021fa313629b2a04c43e5a664e768745d1`; documentation and checklist saved
in the accompanying `chore(docs): record KMP phase one evidence` checkpoint on
`codex/kmp-phase-1`. Application changes remain local; only the separate Sites
report source was synchronized for private publication.

## Phase 2 — Accounts and first real workflow

**Depends on:** Phase 1. **Outcome:** Login → Projects → Configurations → History
→ Build Overview works on Android and iOS against a real server.

- [x] Introduce shared coroutine networking behind feature repository contracts;
  evaluate Ktor/Kotlin serialization and keep legacy Android adapters as needed.
- [x] Preserve Basic challenges, guest paths, URL normalization, duplicate checks,
  HTTP/SSL consent, error handling, and cancellation.
- [x] Implement platform secure credential storage; preserve Android persisted accounts.
- [x] Share account identity, session initialization/lifetime, logout and switching.
- [x] Reject work/results/restoration belonging to another account, including
  different users on the same server. Never persist passwords in screen state.
- [x] Port typed destinations, unique visits, saved state, ViewModel ownership,
  result serialization, back/reset/replace, and account restoration validation.
- [x] Migrate the first full journey's UI and repositories to shared implementations.
- [x] Preserve complete build snapshots, optional data, opaque pagination URLs,
  cache/refresh distinctions, retries, and cancellation semantics.
- [x] Verify HTTPS, guest/private servers, selected HTTP/SSL consent behavior,
  account switching, relaunch, offline failures, and pagination on both platforms.
- [x] Publish the detailed Sites report, save checkpoints, and record evidence.

**Gate passed:** the journey works against JetBrains' approved public guest
server on Android and iOS. Common/Android tests, real-engine local private-auth
and HTTP/TLS fixtures, secure relaunch, pagination, failure/retry coverage and
actual UI evidence are recorded in [the Phase 2 evidence](kmp-phase-2.md).
That record includes measured scope and revised remaining-phase estimates.

**Report:** [Private Phase 2 review](https://teamcityapp-kmp-phase-two-20261022.andrey-tolpeev.chatgpt.site),
published and verified on 2026-10-22. **Checkpoint commits:** implementation
`44676c6cedd34c7471086f6570aee8cfdcda9176`; CI
`7175484662fef26af594bbabc88bae26beefe357`; this checklist, inventory, agent
contract and evidence are saved in the accompanying
`chore(docs): record KMP phase two evidence` checkpoint on `codex/kmp-phase-1`.
Application commits remain local. Phase 3 requires a new user instruction.

## Phase 3 — Home and build browsing

**Depends on:** Phase 2. **Outcome:** complete everyday monitoring and read-only sections.

- [x] Migrate Favorites, Running Builds, Queue, and Agents.
- [x] Preserve Home's independent retained panes, selected Activity view, saved
  content/scroll, polling/visibility gates, refresh, query changes, and filters.
- [x] Preserve saved Favorites IDs during partial/all-failed loads.
- [x] Migrate Changes, Tests, Test Details, Parameters, and Snapshot Dependencies.
- [x] Preserve Paging retention, opaque continuation URLs, Tests status filters,
  optional counts, independent retries, and optional-section failures.
- [x] Share portable row models and date/status formatting.
- [x] Verify loading/empty/error/content, account isolation, cancellation, background
  behavior, compact/expanded layouts, dark mode, and enlarged text.
- [x] Publish the detailed Sites report, save checkpoints, and record evidence.

**Gate passed:** shared monitoring and read-only browsing work on both platforms.
Retained panes, polling, account-bound favorites/filters, paging/count failures,
restoration and platform differences are recorded in [the Phase 3 evidence](kmp-phase-3.md).
Verification includes 394 Native common tests, 98 Android integration cases,
13 signed iPhone UI cases, dark/expanded/accessibility-large journeys and 1,318
unchanged Android golden hashes. Native runs use simulators; physical-device and
release validation retain their later phase ownership.

**Report:** [Private Phase 3 review](https://teamcityapp-kmp-phase-three-20261022.andrey-tolpeev.chatgpt.site),
published and verified on 2026-10-22. **Checkpoint commits:** implementation
`0617fe939b972707ef865099358d54e17acf52ef`; CI
`5702c076a39c8ee53cb5d7bbbc3db96ca353daa2`; checklist, inventory, agent contract
and evidence saved in the accompanying `chore(docs): record KMP phase three evidence`
checkpoint on `codex/kmp-phase-1`. Application commits remain local.
Phase 4 requires a new user instruction.

## Phase 4 — Build management

**Depends on:** Phase 3. **Outcome:** build actions and origin-targeted results work.

- [x] Migrate Run Build, Filter Builds, Quick Filter, and Action Bottom Sheet.
- [x] Preserve locator/legacy filter semantics and return results once to the live
  originating visit, including restoration and stale-recipient rejection.
- [x] Implement Stop, Remove, Restart, and queue actions with confirmations,
  progress, disabled interactions, errors, and retry behavior.
- [x] Preserve queued-build reference loading before normal Build Details entry.
- [x] Reject obsolete screen/account action results and cancel unfinished work.
- [x] Verify successful, failed, cancelled, restored, and repeated actions on both platforms.
- [x] Publish the detailed Sites report, save checkpoints, and record evidence.

**Gate passed:** shared management workflows, exact option/parameter payloads,
confirmations, queued references, failure/retry, account cancellation, retained
filter arguments and one-time results are verified on both platforms. Evidence
includes 443 common Native cases, 37 Android integration cases plus a final 10-case
Run smoke, the 17-case signed iPhone regression suite, final exact-input journeys
on phone/iPad/accessibility-largest dark layouts, and 1,120 unchanged Android
baseline hashes. Physical-device validation retains Phase 6 ownership.

**Report:** [Private Phase 4 review](https://teamcityapp-kmp-phase-four-20261022.andrey-tolpeev.chatgpt.site),
published and verified on 2026-10-22. **Evidence:** [Implementation and verification](kmp-phase-4.md).
**Checkpoint commits:** implementation `1be3c3f552a384a22735f966fd9516c2d6c984eb`;
CI `12502dce705d1ab53f8ef844cf06ff0b2c449119`; this checklist, inventory, agent
contract and evidence saved in the accompanying
`chore(docs): record KMP phase four evidence` checkpoint on `codex/kmp-phase-1`.
Application commits remain local. Phase 5 requires a new user instruction.

## Phase 5 — Downloads and platform integrations

**Depends on:** Phase 4. **Outcome:** platform-dependent capabilities are complete.

- [x] Complete artifact browsing, archive folders, and live-origin navigation results.
- [x] Implement downloads and inline progress/retry/cancel UI.
- [x] Preserve screen-owned lifetime through recreation/sheets; cancel on departure
  or backgrounding, close blocked reads, remove partial files, protect completed files.
- [x] Complete file open/export, browser/log URLs, sharing, clipboard, and permissions.
- [x] Keep browser cookies/authentication separate from stored API credentials.
- [x] Complete Settings/theme migration, account management, rating and licenses.
- [x] Complete selected analytics and crash-reporting integrations behind platform contracts.
- [x] Verify success/failure/unavailable handlers and lifecycle/resource cleanup.
- [x] Publish the detailed Sites report, save checkpoints, and record evidence.

**Gate:** every supported platform action has implementation, error handling, and
verification on both platforms. **Gate passed on 2026-10-23.**

**Report:** [Private Phase 5 Site](https://teamcityapp-kmp-phase-five-20261022.andrey-tolpeev.chatgpt.site)
and [detailed implementation/evidence record](kmp-phase-5.md).

**Checkpoints:** runtime/tests `b31c1ecdb133a044e338f7ff756a4dfaa5aa537b`;
CI `a333e3f7b4b47b7a251deb20f92cdf6fb731a413`. This checklist, the detailed
record and agent contract form the documentation checkpoint.

Evidence includes 491 Native cases, 450 affected JVM cases (one opt-in skip),
231 app unit tests, 35 Android integration tests plus a final progress check,
14 signed iOS journeys plus a final-source artifact smoke, 69 Python tests and
330 scoped renders. All 3,381 existing repository PNG baseline hashes are unchanged.
The report labels 92 new render baselines and actual Android/iOS evidence.
The selected native diagnostics boundary uses OS events and Apple crash reports;
hosted iOS telemetry upload and the full release license inventory remain explicitly
unconfigured/deferred as described in the record.

Application commits remain local. Phase 6 requires a new user instruction.

## Phase 6 — Release validation and cleanup

**Depends on:** Phase 5. **Status:** completed 2026-10-26 under the owner's
revised scope. Implementation, recorded automated verification, cleanup,
checkpoints and the private report are complete. This is phase completion,
not final release qualification.

**Owner decision (2026-10-26):** move the current-candidate phone pass, complete
VoiceOver walkthrough and actual iOS 17 runtime verification to the very end,
after Phase 7 refinements. Those checks remain unpassed and are tracked in
Phase 7's final acceptance checklist below.

- [x] Implement common production business/UI behavior and record the verified
  regression scope, including the account-flow, repeated build transition,
  shared repositories, navigation, prompts, Settings and license-reader changes.
  Earlier full-suite results and current scoped runs remain distinct in the
  Phase 6 record; final-candidate regression belongs to final acceptance.
- [x] Record automated accessibility, large-text, theme, safe-area, keyboard,
  orientation and phone/tablet evidence with its source/candidate limitations.
  Manual VoiceOver and final device/minimum-runtime qualification are deferred.
- [x] Verify termination/relaunch, backgrounding, network failures, account changes,
  stale restoration/results, and interrupted downloads.
- [x] Assess startup, scrolling, memory, and optimized release builds.
  Earlier optimized iPhone simulator: 11/11 tests pass; the final iOS 17-minimum
  archive and Android R8 checks pass.
- [x] Prepare signing, distribution/TestFlight, privacy declarations, and store materials.
- [x] Audit superseded code/dependencies and remove only proven unused paths. The
  original 111-module reachability audit is retained. Three newly wired shared
  account presentation modules bring the current inventory to 114; the retired
  native-only account prototype is removed. Retain the Android compatibility stack.
- [x] Preserve Android account/cache compatibility and installed Intent aliases.
- [x] Publish the detailed Sites report, save checkpoints, and record evidence.

**Revised completion gate:** implementation, scoped automated verification,
compatibility records, local checkpoints and the private Sites report are complete.
Final physical/manual/minimum-runtime checks remain required before migration
acceptance. Distribution still follows a separate shipping instruction.

**Historical qualification record (before the 26 October rescope):**
**Resumed on 2026-10-25:** the owner provided the paired phone and
authorized iOS 17.0 as the supported minimum. Physical About orientation/accessibility
and HTTP-consent checks pass. Optimized hardware coverage is a nine-pass run plus
two corrected Login-precondition reruns. Native journeys are a five-pass run plus
two affected-case reruns, including strict clipped-row audits and artifact export.
The earlier optimized physical pass passed 11/11, with zero skips. The owner
then reported duplicate build transitions and account UI differences during a
partial walkthrough. Those fixes are saved locally and pass simulator/regression
checks; the updated optimized validation build and audited archive are ready.
The owner chose simulator-only follow-up for now, so the updated app has not
been installed on the phone. A new physical pass, complete manual VoiceOver
results and actual iOS 17 runtime execution remain open. Android API 24 retains six
optimized smoke checks and 59 selected integration cases covered by a full run
plus scoped reruns. Simulator automation, compilation and physical/manual results
are recorded separately; these remaining gates are not checked off.

**Evidence:** [Phase 6 implementation and verification](kmp-phase-6.md) and
[iOS release preparation](ios-release.md).
**Report:** [Private Phase 6 review](https://teamcityapp-kmp-phase-six-20261023.andrey-tolpeev.chatgpt.site),
refreshed and verified on 2026-10-25 with actual hardware captures, accessibility
findings, corrected-run scopes and the open manual/iOS 17 runtime gates. The
latest owner-private account-flow report update is verified succeeded, with
actual compact/expanded/enlarged-text screens and a deferred new phone pass. The
earlier simulator/build slice remains recorded. Runtime `08586ec15fe1c34308627539f8fe7da4cf90eb8f`, harness
`2e8716b2c015a084ea48a3cd618ce29fb079f3d4`, CI
`e1b7f0b866f5f97c15a09bcbe89e9d6cfc2ffea1` are local checkpoints.
The 25 October follow-up checkpoints are iOS 17 minimum `9ffb4ca54`, clipped-row
accessibility `2b664e589` and physical qualification `8aa8c4977`. The documentation
checkpoint accompanies these commits. Owner-review fixes are saved at
`3153fb505` (one live History visit) and `44835c390` (shared account flows).
The current-OS follow-up verifies the optimized account journey on iOS 27.1,
with an independent native-inset fixture; older-target regression passes 3/3.
Duo landscape remains unqualified: SDK-matched UIKit, SwiftUI and Compose
controls all retain portrait geometry there. This diagnostic does not replace
physical/manual or iOS 17 qualification. Harness checkpoint: `cca5585cb`. The private report refresh is verified succeeded
on 25 October; its source/version/deployment receipts are in the Phase 6 record.
Phase 6 is closed by the owner; Phase 7 refinements are implemented and final acceptance remains open.

## Phase 7 — Platform review and refinements

**Depends on:** Phase 6. **Status:** all six reviewed refinements authorized and
implemented on 2026-10-26. Physical phone, full manual VoiceOver, actual iOS 17 and
final owner acceptance remain open.
**Outcome:** agreed refinements, final qualification and migration acceptance.

- [x] Review Android/iOS side by side: Account/Manage/Add/sign-out, Home and
  retained tabs, History/Overview/sections, Run/Filter, artifacts and Settings/About.
  Distinguish product defects from intentional OS integrations and visual differences.
  Review and implementation recorded in [Phase 7 evidence](kmp-phase-7.md);
  the first-use Home test now explicitly verifies every Home coachmark.
- [x] Agree a bounded punch list with priority, expected behavior and testable
  completion criteria for each item before implementing refinements. All six
  approved; rating selected as three distinct successful build views, later launch.
- [x] Fix the selected defects and polish shared UI/behavior in common code.
  Change native adapters only where an OS service requires it. Native navigation/
  appearance changes require an explicit selection; background downloads remain
  a separate product decision.
- [x] Audit remaining shared/platform boundaries and proven-unused migration
  scaffolding. Preserve installed accounts, aliases, wire/cache codecs and supported
  behavior; avoid speculative deletion or a new broad architecture migration.
- [x] Repeat affected behavioral, UI, accessibility and optimized-release checks;
  capture final actual Android/iOS screenshots and document intentional differences.
- [x] Update migration documentation and publish the detailed private Phase 7 Sites
  report with before/after evidence, checkpoint references and remaining findings.

### Final acceptance — performed after the refinements

These checks were moved from Phase 6 by the owner; none is marked passed by
closing Phase 6.

- [ ] Run the final candidate's automated physical-phone pass.
- [ ] Complete the owner's full six-part manual VoiceOver walkthrough and resolve
  or explicitly accept its findings.
- [ ] Run the actual iOS 17 minimum runtime and resolve any compatibility findings.
- [ ] Run the final candidate regression/packaging checks appropriate to the agreed
  refinements, keeping full runs, scoped reruns and historical results distinct.
- [ ] Publish final evidence/results, save local checkpoints and record explicit
  owner acceptance of the migration. App upload/distribution needs separate authorization.

**Gate:** the agreed refinement list and every final acceptance item are complete.
**Refinement report:** [Private Phase 7 changes, comparisons and evidence](https://teamcityapp-kmp-phase-seven-20261026.andrey-tolpeev.chatgpt.site/).
**Evidence:** [Review findings, acceptance criteria and measured verification](kmp-phase-7.md).
All six approved items are implemented. Business/UI policy remains common; native
changes are limited to OS export acceptance. See the phase evidence for verification
scope, before/after provenance, checkpoints and private publication receipts.
Application checkpoint: `76c8e10984adff19ba966f38c47f1f8963dfafd3`, saved locally
on `codex/kmp-phase-1`. The owner-private refinement report was republished on
2026-10-26 (deployment `appgdep_6adf30076c2c8191b032f327ca92bbd8`); the phase
evidence records the exact source/version and distinguishes the six-pass iOS
selection from the successful focused artifact/audit rerun.
The overall phase stays unchecked until every final acceptance gate is complete.

## Follow-up — simpler feature module ownership

The owner authorized an About pilot followed by the same api/impl structure for
all other features. The 26 former presentation modules are consolidated into KMP
implementations: common UI/state/resources/tests stay portable, Android adapters
and screenshot harnesses use Android source sets inside the same implementation.
The feature inventory becomes 54 modules (27 api/impl pairs), including Splash's
existing Android launch-compatibility pair. Shared list/build component libraries
remain reusable libraries, not third modules belonging to a feature.

`AGENTS.md` and the build-logic guide define the new convention;
`python3 scripts/check-feature-modules.py` enforces it in CI. The detailed pilot,
rollout, verification and private report receipts are in
[kmp-phase-7.md](kmp-phase-7.md). This structural follow-up does not close the
physical-phone, complete manual VoiceOver or actual iOS 17 final acceptance gates.
Local implementation checkpoint: `f555e32319f426448debeadefc324b9b9b66bbf7`.
All affected tests/builds and the scoped native UI selection pass; the phase evidence
records full selections, focused fixture corrections and actual captures separately.
The owner-private report was updated on 2026-10-26 at 20:39 Asia/Bangkok
(deployment `appgdep_6adf57fe03ac8191ab294dfe65aaa92f`).
Historical phase commands above retain the module names used when they ran;
current feature tests run under `impl` with KMP host/native task names.

## Follow-up — phone-reported onboarding and build navigation

- [x] Correct History's Run/Filter/Favorite spotlights inside retained Home panes.
- [x] Open complete build snapshots directly from Dependencies, Running and Queue,
  with the same exact-origin repeated-tap guard used by History.
- [x] Verify common JVM/native behavior, review 18 new nested-layout golden
  fixtures, and exercise iOS source-list/Overview/one-Back journeys.
- [ ] Install the fixed optimized iPhone candidate — deferred by the owner.

The [Phase 7 evidence](kmp-phase-7.md) records the checks and screenshots. The
[private report](https://teamcityapp-kmp-phase-seven-20261026.andrey-tolpeev.chatgpt.site/#phone-feedback)
compares the supplied phone captures with current iOS evidence and includes
Android controlled renders. The fix is saved locally as `fed0c68d1dd200acdeb8205194b3b57e8e7f7327`;
the report was republished on 2026-10-26 at 22:24 Asia/Bangkok
(deployment `appgdep_6adf7093fd80819186020979376464de`).
Final phone/VoiceOver/iOS 17 acceptance stays open.

## References

- [JetBrains Android Compose migration guide](https://kotlinlang.org/docs/multiplatform/migrate-from-android.html)
- [Kotlin toolchain compatibility](https://kotlinlang.org/docs/multiplatform/multiplatform-compatibility-guide.html)
- [Android KMP library plugin](https://developer.android.com/kotlin/multiplatform/plugin)
- [Compose platform differences](https://kotlinlang.org/docs/multiplatform/compose-platform-specifics.html)
