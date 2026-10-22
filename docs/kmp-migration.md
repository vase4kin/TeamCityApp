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
  unavailable required check leaves its task and phase incomplete.
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
- [ ] Phase 3 — Home and build browsing (P1)
- [ ] Phase 4 — Build management (P1)
- [ ] Phase 5 — Downloads and platform integrations (P1)
- [ ] Phase 6 — Release validation and cleanup (P1)
- [ ] Phase 7 — Platform review and refinements (P2)

## Phase 1 — Toolchain and shared foundation

**Outcome:** the same About screen runs on Android and iOS with deterministic
data, shared state/theme/resources, automated checks, and an evidence report.
Real iOS accounts, network access, and outgoing platform actions are later phases.

### Implementation

- [x] Record the seven-phase checklist and phase/report workflow.
- [x] Capture Android About test/golden baseline before edits (27 images).
- [x] Pin and verify compatible Kotlin, Compose, Material, Gradle, AGP, and Xcode.
- [x] Define scope: Android API 24+, iOS 16+, arm64 devices and Apple Silicon
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

- [ ] Migrate Favorites, Running Builds, Queue, and Agents.
- [ ] Preserve Home's independent retained panes, selected Activity view, saved
  content/scroll, polling/visibility gates, refresh, query changes, and filters.
- [ ] Preserve saved Favorites IDs during partial/all-failed loads.
- [ ] Migrate Changes, Tests, Test Details, Parameters, and Snapshot Dependencies.
- [ ] Preserve Paging retention, opaque continuation URLs, Tests status filters,
  optional counts, independent retries, and optional-section failures.
- [ ] Share portable row models and date/status formatting.
- [ ] Verify loading/empty/error/content, account isolation, cancellation, background
  behavior, compact/expanded layouts, dark mode, and enlarged text.
- [ ] Publish the detailed Sites report, save checkpoints, and record evidence.

**Gate:** monitoring/browsing works on both platforms with matching behavior and
documented intentional platform differences. **Report/commits:** pending.

## Phase 4 — Build management

**Depends on:** Phase 3. **Outcome:** build actions and origin-targeted results work.

- [ ] Migrate Run Build, Filter Builds, Quick Filter, and Action Bottom Sheet.
- [ ] Preserve locator/legacy filter semantics and return results once to the live
  originating visit, including restoration and stale-recipient rejection.
- [ ] Implement Stop, Remove, Restart, and queue actions with confirmations,
  progress, disabled interactions, errors, and retry behavior.
- [ ] Preserve queued-build reference loading before normal Build Details entry.
- [ ] Reject obsolete screen/account action results and cancel unfinished work.
- [ ] Verify successful, failed, cancelled, restored, and repeated actions on both platforms.
- [ ] Publish the detailed Sites report, save checkpoints, and record evidence.

**Gate:** management workflows pass behavioral and integration checks on both
platforms with actual before/after evidence. **Report/commits:** pending.

## Phase 5 — Downloads and platform integrations

**Depends on:** Phase 4. **Outcome:** platform-dependent capabilities are complete.

- [ ] Complete artifact browsing, archive folders, and live-origin navigation results.
- [ ] Implement downloads and inline progress/retry/cancel UI.
- [ ] Preserve screen-owned lifetime through recreation/sheets; cancel on departure
  or backgrounding, close blocked reads, remove partial files, protect completed files.
- [ ] Complete file open/export, browser/log URLs, sharing, clipboard, and permissions.
- [ ] Keep browser cookies/authentication separate from stored API credentials.
- [ ] Complete Settings/theme migration, account management, rating and licenses.
- [ ] Complete selected analytics and crash-reporting integrations behind platform contracts.
- [ ] Verify success/failure/unavailable handlers and lifecycle/resource cleanup.
- [ ] Publish the detailed Sites report, save checkpoints, and record evidence.

**Gate:** every supported platform action has implementation, error handling, and
verification on both platforms. **Report/commits:** pending.

## Phase 6 — Release validation and cleanup

**Depends on:** Phase 5. **Outcome:** a verified Android/iOS release candidate.

- [ ] Run the complete feature-parity and regression checklist.
- [ ] Verify accessibility, large text, themes, safe areas, keyboard, gestures,
  supported sizes/orientations, and iPad layouts.
- [ ] Verify termination/relaunch, backgrounding, network failures, account changes,
  stale restoration/results, and interrupted downloads.
- [ ] Assess startup, scrolling, memory, and optimized release builds.
- [ ] Prepare signing, distribution/TestFlight, privacy declarations, and store materials.
- [ ] Remove superseded implementations/dependencies after their final callers migrate.
- [ ] Preserve Android account/cache compatibility and installed Intent aliases.
- [ ] Publish the detailed Sites report, save checkpoints, and record evidence.

**Gate:** Android regression checks pass and the iOS candidate passes simulator
and physical-device validation. Distribution actions follow the user's shipping
instructions. **Report/commits:** pending.

## Phase 7 — Platform review and refinements

**Depends on:** Phase 6. **Outcome:** agreed refinements and final migration acceptance.

- [ ] Review remaining Android/iOS differences with the user.
- [ ] Agree a bounded refinement list and explicit completion criteria before implementation.
- [ ] Implement the selected refinements; evaluate native navigation/appearance only
  where requested. Treat background downloads as a separate product decision.
- [ ] Repeat affected behavioral, UI, accessibility, and release checks.
- [ ] Capture final actual screenshots and document intentional differences.
- [ ] Audit shared/platform boundaries and complete migration documentation.
- [ ] Publish the final detailed Sites report, save checkpoints, and record acceptance.

**Gate:** the agreed refinement list and final acceptance checklist are complete.
**Report/commits:** pending.

## References

- [JetBrains Android Compose migration guide](https://kotlinlang.org/docs/multiplatform/migrate-from-android.html)
- [Kotlin toolchain compatibility](https://kotlinlang.org/docs/multiplatform/multiplatform-compatibility-guide.html)
- [Android KMP library plugin](https://developer.android.com/kotlin/multiplatform/plugin)
- [Compose platform differences](https://kotlinlang.org/docs/multiplatform/compose-platform-specifics.html)
