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

# Phase 3 — Home and build browsing

Baseline: `0169a6f72283c2855d3249036268b6561593892b`, the completed Phase 2
checkpoint on `codex/kmp-phase-1`. The user authorized continuing with Phase 3.
The existing approval for read-only verification against JetBrains' public guest
server (`https://teamcity.jetbrains.com`) remains in effect. Phase 4 is not started.

Completed 2026-10-22. [Private Phase 3 report](https://teamcityapp-kmp-phase-three-20261022.andrey-tolpeev.chatgpt.site)
is published and verified. It contains the actual Android comparisons, native
screenshots, code changes and verification details.

- Site: `appgprj_6ada017e380c8191870af728f1ea35e7`
- Source: `49397535be239bb6ad6fbaa4c509c2224404bc04`
- Saved version: `appgprj_6ada017e380c8191870af728f1ea35e7~appgver_e444926cd6708191af0e84fc421dad4a`
- Successful private deployment: `appgdep_6ada120089d48191b834a5e401671f10`

## Result and ownership

The normal iOS app now starts at Home after authentication. Home contains Projects,
Favorites and Activity. Activity groups Running, Queue and Agents. Projects and
Favorites open configuration History; build rows open a complete Build Overview.
Overview opens Changes, Change Details, Tests, Test Details, Parameters and Snapshot
Dependencies as separate visits. Dependency and configuration rows continue browsing
through the same registered navigation stack.

Eleven presentation modules are added and twelve existing API modules are converted
to the KMP conventions. The repository now has 103 included modules, 47 using KMP,
and 16 shared feature presentations. The KMP graph contains 177 common production
Kotlin files and 47 common test files. Module counts describe ownership; they are
not code-sharing percentages or a claim of complete iOS product parity.

| Slice | Shared implementation | Android adapter |
| --- | --- | --- |
| Home | `features/home/presentation`: Home state/ViewModel, navigation layout, Activity selection, badges, filter chips and tracker contract | Hilt wrapper, root destination registration, account router, Firebase tracker |
| Favorites | `features/favorites/presentation`: immutable failure envelope, grouped rows, loader, lifecycle retention and retry | Existing installed favorites storage and repository |
| Running, Queue, Agents | Their `presentation` modules: real composables, polling ViewModels, queries, filters and update affordances | Thin Hilt factories, platform routers and coachmark resource adapters |
| Changes, Tests | Their `presentation` modules: screens, paging sources, generation retention, lazy row mapping, optional counts and shared collection routes | Existing wire DTO hydration, Retrofit/RxCache policy and count-result bridges |
| Change Details | `features/change-details/presentation`: saved plain arguments, file rows, comment expansion and tracker contract | Bundle compatibility, browser/diff routers and Firebase tracker |
| Test Details | `features/test-details/presentation`: literal output, loading/error/empty/content, query matching, wrapping and active match | Clipboard/platform feedback adapter and tracker implementation |
| Parameters | `features/properties/presentation`: immutable rows, expansion, selection and portable empty-value text | Installed argument adapter and clipboard action |
| Snapshot Dependencies | `features/snapshot-dependencies/presentation`: lifecycle loader and full build rows in server order | Legacy hydration/cache adapter and platform routing |

The existing Kotlin package names are retained to reduce caller churn. Package
segments named `impl` do not imply Android ownership: the source-set/module path
is authoritative. Cross-feature implementation dependencies remain prohibited;
`shared-app` is the native composition root and may wire shared presentations.

Common ViewModels use constructor injection and lifecycle-owned factories. Thin
Android subclasses carry `@HiltViewModel` and receive the existing bindings.
Common UI resolves Compose resources and typed StringResource/SharedUiText values
in composition. It contains no Hilt, Android Context/Bundle/resource IDs, Retrofit,
RxJava, platform storage or Java reflection dependency.

## Retained Home and lifecycle contract

The native root uses the existing common `RetainedNavigationStacks` with five
independent child stacks. Each visit owns its ViewModel store and saveable
composition. Selecting another pane releases collectors/platform resources while
retaining completed data, nested visits and in-session scroll. Activity remembers
its last Running/Queue/Agents selection. Reselecting the selected tab returns its
stack to the root.

Running, Queue and Agents preserve the existing polling interval, refresh/retry
behavior and explicit updates affordance. Home counts remain independent from
list content. Query generations copy account and favorite/filter inputs. Hidden
or stopped content cancels unfinished loads; cancellation is not presented as a
failure. Favorites and Snapshot Dependencies retain their resume/pause behavior.
Android keeps its configuration-recreation adapters; iOS uses the retained native
host/visit lifecycle.

The native root owns top/horizontal safe-area padding and the background surface.
Nested section headers are checked below the system status area. Compact Home uses
bottom navigation; expanded Home uses the same shared rail. Large text moves the
Activity count below its label instead of clipping it.

The saved native public-navigation envelope records Home selection, last Activity
view and all five registered stacks. Restoration validates account identity, live
Home ownership, the expected child root and entry/encoded-size limits. Malformed,
stale-owner, other-account and incompatible Phase 2 navigation reset safely.
The Keychain account is retained. Network content is reloaded in a new activation;
a complete durable offline cache and process-persisted scroll/reader state are
not introduced. In-session state/scroll retention and saved destination restoration
are verified separately rather than conflated.

## Account preferences and data boundary

`AccountPreferences` and `AccountPreferencesStorage` add native, account-bound
favorite IDs and Running/Queue/Agents filter preferences. `VaultAccountStorage`
adds an optional field with defaults, so Phase 2 vaults continue to decode without
credential or active-selection changes. Replacing credentials preserves the same
account's preferences. Signing out retains them; removing the account removes them.
Writes atomically replace the secure payload before publishing the new query.
A failed write leaves the old durable preferences and UI query intact.

`TeamCityBrowsingRepositories` exposes separate adapters for Home, Favorites,
Running, Queue, Agents, Changes, Tests, Test Details, metadata and dependencies.
Every request runs inside one activation scope. A different user on the same
server cancels old requests and rejects late results. Favorites capture one ID
snapshot per batch, preserve IDs during partial/all failures and never turn a
cancellation into an unavailable-ID result. The account activation owns the engine
and in-memory document cache.

Android continues to use its installed encrypted account/favorite snapshot,
Retrofit/Gson DTOs, RxCache keys, challenge authentication and hydration/cancellation
adapters. Their storage formats and serialized legacy class names are preserved.
The phase does not replace Android's complete networking/cache stack.

## REST and paging semantics

- Running retains `running:true,branch:default:any,personal:false,pinned:false`,
  with a configuration locator for each saved favorite when that filter is active.
  An empty favorite selection makes no collection/count request.
- Queue uses `buildQueue`. The All query omits the locator parameter completely;
  favorite queries use `buildType:<id>`. Live verification found that an empty
  `locator=` returns HTTP 400 on TeamCity. The adapter now omits it, matching
  Android, with a regression test for both list and count requests.
- Running/Queue request full build projections in bulk rather than detail-per-row.
  Continuations retain server order and pass the same transport guards.
- Agents retain the installed `includeDisconnected` semantics. Missing server IDs
  use name/occurrence fallback keys so duplicate names remain distinct.
- Changes and Tests amend only the initial locator for page size and test status.
  Server nextHref values remain opaque and are never reconstructed. Origin,
  context-path and authentication guards still apply to every link.
- Changes hydrate file details before publishing each page. Empty pages end
  continuation; IDs fall back to the server href or opaque page/index identity.
- Tests retain Failed/Passed/Ignored generation state and ViewModel-scoped pages.
  Optional counts have independent failure/retry paths. Errors or counts do not
  replace usable list rows. Test output remains literal and searchable.
- Native dependencies read a fresh full collection projection using
  `snapshotDependency:(to:(id:<id>),includeInitial:true),defaultFilter:false`.
  Android retains its legacy hydration order and running/finished cache checks.
- Date/status/row mapping remains portable. Change dates preserve the installed
  local-wall-time formatting contract rather than silently changing time zones.

The common Build Reference ViewModel can load optional Changes/Tests counts
independently and retain completed results. Section counts return once to the
exact live originating build visit. Stale result owners are rejected by the
common navigator. Configuration/project rows navigate on iOS; unsupported explicit
copy and branch-action affordances are capability-gated.

## Actual UI evidence

The Site contains a curated 46-state Android before/after gallery in light/dark,
compact/expanded and enlarged-text configurations. Its full hash index compares
1,318 baseline images with the current fixtures: all hashes match. No Android
golden was rerecorded to make an unexpected difference pass.

Before images were captured at the Phase 2 checkpoint. The adjacent Change Details
and Overview baseline files were read from that immutable commit. After views are
the current fixtures verified by Roborazzi; identical image bytes are deduplicated
in the report. Full-device Android images from the 98-case integration run provide
additional runtime evidence, with a focused 10-case smoke run of the final APK.

Native evidence is captured from the normal app using a deterministic read-only
HTTP fixture and, separately, the approved public guest server. iOS views absent
in Phase 2 are explicitly labeled first baselines. The prior Phase 2 Projects-only
root is shown beside the new Home root with its different data source identified;
it is a workflow comparison, not a pixel-difference test.

Intentional differences include iOS system bars/safe areas, keyboard/accessibility
and platform font/monospace rendering. Different widths can change wrapping. The
iOS History now exposes Favorites, while Run/Filter controls remain phase-scoped.
Native explicit browser/diff/copy buttons and artifacts remain later work; Android
retains its existing actions. Native text selection and parameter/test readers are
shared. The screenshots do not imply that all app features are available on iOS.

Resource extraction exposed literal vector-color and positional-format requirements.
Fixing those resources restored the original Android pixels. UI tests also wait
for Compose scroll deceleration before tapping; UIKit's idle check alone could
tap a moving row at accessibility text sizes. Final native capture uses full-screen
screenshots where system context is required.

## Verification

| Check | Result |
| --- | --- |
| All `iosSimulatorArm64Test` common suites | 394 tests, no failures |
| Android common/host test results | 488 cases, no failures; one existing skipped host case |
| Phase 3 Android screen/interaction/Roborazzi suites and shared host harness | Passed; 1,318 scoped before/current baseline hashes unchanged |
| Android app unit tests, mock/prod debug apps, test APK, app lint | Passed with JDK 17, `--max-workers=2`, local heap `-Xmx6g` |
| Android browsing instrumentation | 98/98 passed; final rebuilt APK smoke 10/10 passed |
| Signed iPhone UI suite | 13/13 passed, including approved public guest journey, deterministic failure/count states and About regression modes |
| iPhone dark + iPad expanded browsing | Passed with actual screenshot attachments |
| iPhone accessibility-large browsing | Passed after waiting for scroll settlement before taps |
| iOS arm64 device and Apple Silicon simulator frameworks | Both compile |
| Build conventions, CI-script unit tests and formatting | Passed; 62 CI-script tests |
| Private report | Published privately; deployment succeeded on 2026-10-22 |

The initial local parallel Android dex transform exceeded the default 2 GiB heap.
A bounded worker count and 6 GiB local heap completed the build; the generated
heap dump was removed. No runtime memory change or toolchain version bump was
needed. Live Queue verification found the locator bug described above and passed
after its fix. The first enlarged-text failure was a moving-row tap; the test
helper now waits for frame stability before interacting.

### Reproduction and artifacts

Use JDK 17. Representative commands (individual module tasks are listed in the
build logs and report record):

```sh
./gradlew --max-workers=2 iosSimulatorArm64Test \
  :shared-app:linkDebugFrameworkIosArm64 \
  :shared-app:linkDebugFrameworkIosSimulatorArm64
./gradlew :features:home:impl:verifyRoborazziDebug \
  :features:changes:impl:verifyRoborazziDebug \
  :features:tests:impl:verifyRoborazziDebug \
  :shared-app:verifyRoborazziAndroidHostTest
./gradlew -Dorg.gradle.jvmargs=-Xmx6g --max-workers=2 \
  :app:assembleMockDebug :app:assembleProdDebug \
  :app:assembleMockDebugAndroidTest :app:testMockDebugUnitTest \
  :app:lintMockDebug spotlessCheck
python3 scripts/kmp-platform-fixture.py --output build/kmp-phase-3/platform-fixture
```

Start the local fixture before the normal Xcode UI suite. Set
`TEST_RUNNER_TEAMCITY_FIXTURE_UI=1` for deterministic browsing tests and opt in
with `TEST_RUNNER_TEAMCITY_LIVE_UI=1` only when a read-only public run is wanted.
Signed simulator runs use `CODE_SIGN_IDENTITY=- CODE_SIGNING_ALLOWED=YES
CODE_SIGNING_REQUIRED=YES`; this is needed for Keychain verification. Use an
isolated test vault and keep native account tests and XCTest from concurrently
controlling the same simulator. GitHub Actions remains the CI platform, now
starting/stopping the local fixture for its native browsing smoke suite.

Local logs, result bundles, screenshots and hash verification are under
`build/kmp-phase-3/`. They are disposable output and are not committed.
Intentional Android golden fixtures remain at their owning module paths.
The Site stores the curated images and provenance with its own source version.

Local toolchain: Kotlin 2.4.20, Gradle 9.6, AGP 9.3.2, Compose Multiplatform
1.12.2, JDK 17 and Xcode 27.1. UI runs use iOS 26.4 Apple Silicon simulators and
an API 34 Android emulator; Android goldens retain their pinned API 35 setup.
CI remains pinned to Xcode 26.4. Both iOS 16 minimum-target frameworks compile;
no iOS 16 runtime or physical Apple device run is claimed. This phase introduces
no durable offline database or release readiness claim.

## Checkpoints and next boundary

- Implementation and tests: `0617fe939b972707ef865099358d54e17acf52ef`
  (`feat(kmp): share Home monitoring and build browsing`).
- CI verification: `5702c076a39c8ee53cb5d7bbbc3db96ca353daa2`
  (`chore(ci): exercise shared Home and browsing on iOS`).
- Documentation/report evidence: saved with this record after private publication.

The implementation checkpoint tree was compared with all 301 selected code/test
files: zero content mismatches and zero unrelated committed paths. Unrelated
README and Google Play artwork/rendering work is preserved. Checkpoints remain
local; no application branch push or pull request was requested.

Phase 4 build management is not started. Browser/diff/download/explicit clipboard
integrations, artifacts and broader native parity retain their phase ownership
in `docs/kmp-migration.md`.
