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

# KMP Phase 5 — Downloads and platform integrations

Status: complete on 2026-10-23. Implementation, verification and private report
publication are complete; this record and the checklist form the documentation
checkpoint. Phase 6 has not started.

Starting checkpoint: `065c645253a657510e07b03f36d67596cde8ccf8`.

Private report: published privately at
[the Phase 5 Site](https://teamcityapp-kmp-phase-five-20261022.andrey-tolpeev.chatgpt.site).

## Result and code ownership

Artifacts and Settings now have shared `presentation` modules. Their API modules
use common Kotlin roots; Android artifact platform interfaces stay in `androidMain`.
The Android implementations retain Hilt factories, platform routers, existing
resources used by adapters/tests, and screenshot harnesses. Shared screens are not
duplicated in platform adapters.

- `features/build-artifacts/presentation`: artifact list, rows, download card,
  immutable state, constructor-injected ViewModel, lifetime hook, tracker and menu
  contracts; portable resources and behavioral tests.
- `features/settings/presentation`: theme screen/state/ViewModel/tracker and
  common resources; Android DataStore behavior remains behind its installed adapter.
- `libraries/theme-preferences`: portable theme modes, SDK option policy and
  preference repository contract, retaining packages and stored strings.
- `libraries/network`: bounded artifact streaming, challenge-based Basic auth,
  guarded origin/context/REST paths, explicit stream deadline policy, advertised
  identity-body length validation and decoded-compression handling.
- `libraries/shared-data`: account-scoped artifact metadata/download repository
  and platform file-store contract; the graph also supplies real About information.
- `shared-app`: native registrations for artifact folders/actions, Settings,
  About and offline licenses; build/browser/log/diff/share/copy actions and common
  URL policies. The UIKit adapter owns cache files, the activity sheet, browser,
  clipboard, Apple review requests, UserDefaults and app/scene notifications.

The project contains 111 included modules, 60 KMP modules and 23 shared
presentation modules. The inventory contains 236 common production Kotlin files
and 57 common test files, plus platform-specific adapters/tests.

## Downloads and navigation

A download belongs to the visible artifact visit. It survives configuration
recreation and temporary action sheets, and cancels on departure, app/scene
backgrounding, ViewModel destruction or account replacement. Cancellation closes
blocked reads and removes partial files. Returning does not resume a cancelled
transfer automatically.

An atomic transfer record prevents late progress/completion from replacing a
new transfer. Ready-file claim tokens prevent duplicate viewer launches after
recreation. Folder/action results target a live exact originating visit/consumer;
account generations prevent an old request from entering a new account's UI.

Native files use unique cache directories and the original filename. Completed
same-name files have different paths. Android retains its installed successful
replacement policy and preserves existing output on cancellation or failure.
Both reject unsafe filenames and incomplete identity-length bodies. Streams use
8 KiB buffers; active transfers may exceed the JSON request deadline while
socket inactivity and explicit cancellation remain bounded. HTTP-compressed
responses may have an unknown decoded total rather than comparing decoded
bytes with their encoded wire length.

## Platform behavior and differences

- Android opens FileProvider URIs with MIME handling and temporary grants. iOS
  offers compatible apps and Save to Files through UIKit's activity sheet. At the
  largest Dynamic Type size iOS groups file actions under View More.
- Build/browser/log/diff links stay on the active server/context. Stored API
  credentials never enter browser URLs. Browser cookies/sign-in remain separate
  from the Ktor account graph. Explicit project/contact links use platform handlers.
- Native clipboard actions are enabled for Overview/branches, parameters and test
  details. Platform failures have dismissible shared messages.
- Android theme preferences/migration and SDK-specific options are preserved.
  iOS stores Light/Dark/System independently of credentials; Auto Battery remains
  an Android option. Theme persistence is verified through termination/relaunch.
- Keychain account add/select/remove/sign-out and generation isolation remain.
  Native Accounts now offers Settings/About. About reads real account server info;
  deterministic About remains an explicit preview mode.
- Apple controls rating-prompt display. Simulator tests show its native prompt,
  dismiss Not Now and never select stars or submit a rating.
- Native licenses offer nine bundled primary runtime texts and upstream links.
  Standard Apache text comes from repository `LICENSE.txt`; Skia's BSD text keeps
  upstream notices. SHA-256 provenance is bundled. Full release-binary notices and
  distribution compliance remain part of Phase 6 release preparation.
- Existing Android Firebase integrations remain behind bindings/trackers. Native
  tracking uses fixed OS diagnostic labels without account/server/request/file/
  clipboard payloads. Apple's crash diagnostics remain the native crash-reporting
  boundary; hosted iOS analytics/crash upload is unconfigured.

## Verification

| Check | Result | Scope |
| --- | --- | --- |
| Native suite | 491 passed, zero failures | Common logic plus native adapter tests across 31 modules |
| Affected JVM suites | 450 cases, zero failures; one opt-in live-host skip | Artifact/Settings implementations and shared presentations, network, shared data and native-root harness |
| App unit tests | 231 passed | Mock debug application integration/state |
| Android instrumentation | 35/35 passed | Artifacts, byte progress/blocked-read cancellation, Settings and account management |
| Final byte-progress evidence | 1/1 passed | Matching 16 KiB fixture metadata; refreshed captures |
| Signed native platform UI | 6/6 iPhone + 6/6 iPad passed | Archive/export, share/copy/browser, background/departure cancel, retry, >30-second transfer, Settings/rating/offline licenses |
| Final-source native smoke | 1/1 passed | Artifact/archive/export after final transport hardening; equivalent light iPhone comparison |
| Largest native text | 2/2 passed | Artifact folders/export and Settings/rating/offline licenses |
| Existing repository baselines | 3,381 hashes unchanged | Features, libraries and shared-app PNG audit; affected tasks verified |
| Affected/shared-root renders | 330 images | 238 existing images unchanged, 92 reviewed first baselines |
| Python fixture/CI tests | 69 passed | Read-only artifact fixtures, synthetic mutation guard, context-preserving links, CI script checks |
| Builds/tooling | Passed | Mock/prod debug, instrumentation APK, lint, iOS arm64 device/simulator frameworks, build-logic tests and formatting |

Final UI evidence uses task-owned iOS 26.4 simulators and API 34 Android emulation.
Android screenshot fixtures pin API 35 and cover light/dark, compact/expanded and
larger text; shared native-root fixtures additionally cover double text. Native
exports include 75 actual phone, iPad and largest-text frames; nine captures show
the installed Android application. The report
compares equivalent light iPhone Overview states from Phases 4 and 5 and labels
new iOS destinations as first baselines. Native-root renders on Android are
labeled harness evidence, separate from the installed Android application.

Generated logs, xcresults, screenshots and audit files remain under
`build/kmp-phase-5`. Intentional golden PNGs and reproducible source/configuration
are committed. The report links its screenshot, module and verification manifests.

Checks found and corrected a JVM-only map call, resource/type portability issues,
stale progress after cancellation, unsafe/truncated writes, SwiftUI scene
background handling, native share-sheet accessibility/animation assumptions,
late rating-prompt presentation, and context/length errors in synthetic fixtures.
Rejected fixture POSTs drain bounded request bodies before closing, avoiding
connection resets while preserving opt-in mutation guards.

## Limits and next phase

iOS 16 remains the deployment minimum; the local toolchain is Xcode 27.1 and CI
remains pinned to Xcode 26.4. Simulator evidence does not claim physical-device
or minimum-runtime validation. Full accessibility/orientation/parity, optimized
builds, performance, signing/distribution, store branding and the release license
inventory remain Phase 6. Downloads remain screen-owned and foreground-only;
cache files may be reclaimed by iOS, while exported Files copies are user-owned.

No application push, PR, distribution or Phase 6 work is authorized by completing
this phase. The private report uses a separate Sites source repository.

## Checkpoints and report

Runtime/tests: `b31c1ecdb133a044e338f7ff756a4dfaa5aa537b` (GitButler `sxq`).

CI: `a333e3f7b4b47b7a251deb20f92cdf6fb731a413` (GitButler `vwv`).

Documentation/evidence: this record, the migration checklist and the updated
`AGENTS.md` contract are saved together in the Phase 5 documentation checkpoint.

Site project: `appgprj_6ada3e91a7d88191b2a107e36ca2c3a3`.

Site source: `8b32cd3d4c3b271d895e6c7553e218001569f5fe`.

Saved version: `appgprj_6ada3e91a7d88191b2a107e36ca2c3a3~appgver_b7941a81a29881918433f9e84f8e3078`.

Deployment: `appgdep_6ada44ee45808191918df6fe98f63dee` — succeeded, owner-private.
