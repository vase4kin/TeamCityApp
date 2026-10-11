<!--
Copyright 2026 Andrey Tolpeev

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
-->

# Compose list migration plan

The legacy list foundation serves ten list destinations, plus part of Build Overview and Build Details argument handling. Replace its presenter/view inheritance with shared stateless Compose components and reusable loading behavior. Each feature should own its ViewModel, models, repository contract, rows, and actions. This preserves common behavior without making every screen implement irrelevant callbacks.

The inventory below describes the legacy implementation before migration. Every consumer now has a verified Compose replacement wired into the app. The migration is complete, and the superseded list foundation, native list layouts, sectioned adapters, Mugen, and shimmer dependencies have been removed.

## Foundation and consumers

`BaseListPresenterImpl` coordinates loading, refresh, retry, view callbacks, and lifecycle cleanup. `BaseListViewImpl` coordinates RecyclerView, skeleton, empty/error views, and swipe refresh. Their surrounding foundation includes `BaseAdapter`, `BaseViewHolder`, `ViewHolderFactory`, `BaseDataModel`, `BaseListRxDataManagerImpl`, `BaseValueExtractorImpl`, and load-more contracts. Sectioned adapters and Mugen add grouping and pagination outside the base classes.

| Destination | Presenter and view consumers | Hosts | Behavior that must survive |
| --- | --- | --- | --- |
| Agents | `AgentPresenterImpl`, `AgentViewImpl` | `AgentListFragment` in Home | Connected/disconnected filter, distinct empty messages, drawer action, cached reload on return, Home count |
| Changes | `ChangesPresenterImpl`, `ChangesViewImpl` | `ChangesFragment` in Build Details | Hydrated change rows, ten-item pages, opaque continuation URLs, independent tab count, Change Details navigation |
| Test Occurrences | `TestsPresenterImpl`, `TestsViewImpl` | `TestOccurrencesFragment` in Build Details | Failed/passed/ignored selection, available filter counts, status sections, pagination, failed-only Test Details navigation |
| Projects and configurations | `NavigationPresenterImpl`, `NavigationViewImpl` | `NavigationActivity`, `NavigationListFragment` in Home | Recursive project navigation, projects before configurations, descriptions, Build History navigation, rating prompt and analytics |
| Favorites | `FavoritesPresenterImpl`, `FavoritesViewImpl` | `FavoritesFragment` in Home | Active-account favorites, project sections with clickable headers, resume reload, configuration navigation and analytics |
| Build History | `BuildListPresenterImpl`, `BuildListViewImpl` | `BuildListActivity` | Paging, queued/date sections, build filters, favorite toggle, Run Build results, onboarding, Build Details navigation |
| Running Builds | `RunningBuildsListPresenterImpl` extends Build History presenter; `RunningBuildsListViewImpl` reuses its view | `RunningBuildsFragment` in Home | All/favorites filter, hydrated status/progress, configuration sections and clickable headers, forced fresh running data |
| Build Queue | `QueueBuildsListPresenterImpl` extends Build History presenter; `BuildQueueViewImpl` extends running view | `BuildQueueFragment` in Home | All/favorites filter, queue order, configuration sections, fresh queue state, counts and build navigation |
| Snapshot Dependencies | `SnapshotDependenciesPresenterImpl`, anonymous `RunningBuildsListViewImpl` | `SnapshotDependenciesFragment` in Build Details | Build rows and configuration headers, dependency request, no active paging; several inherited actions are currently disabled |
| Artifacts | `ArtifactPresenterImpl`, `ArtifactViewImpl` | `ArtifactListActivity`, `ArtifactListFragment` in Build Details | Folder/archive navigation, action sheet, downloads, permission handling, file browser and error recovery |
| Build Overview | Own presenter/view, but `OverviewAdapter`, model, holders/factories, data manager and extractor use foundation contracts | `OverviewFragment` in Build Details | Build facts, actions, project navigation and optional sections; migrate as its own screen |
| Build Details arguments | `BaseValueExtractor` in view, interactor and DI | `BuildDetailsActivity` | Existing build payload and extra keys; replace decoding before deleting the extractor foundation |

Existing Compose lists in Properties, Manage Accounts, Drawer, and Change Details do not consume this legacy foundation. Shared list components can be adopted when they improve those screens, without coupling their business behavior to the migration.

## Shared Compose architecture

Use three layers of reuse:

1. **List presentation:** `libraries/list-ui` supplies `TeamCityListContainer`, loading/empty/error surfaces, pull refresh, retained-content refresh failure, section headers, and append loading/retry. Features supply their own lazy layout and row composables. Stable row keys, scroll state, and content types belong with those rows.
2. **Finite loading:** `libraries/list-state` supplies immutable `ListUiState` and `RefreshableListLoader<Query, Item>`. It handles query changes, cancellation, initial load, forced refresh/retry, retained completed results, and failure while existing content remains visible. A feature ViewModel owns the single shared StateFlow subscription and its lifecycle policy.
3. **Domain reuse:** `libraries/build-configurations` shares plain project/configuration models between Navigation and Favorites. `libraries/builds` supplies immutable complete build launch snapshots and an explicit compatibility codec contract. `libraries/build-ui` supplies build rows, configuration headers, status/date presentation, and duplicate-safe row keys for History, Running, Queue, and Snapshot Dependencies.

Use Paging for server-paged Changes, Tests, and Build History. Map refresh/append states into the same presentation components while keeping indexed item access so Paging can prefetch. Preserve opaque TeamCity continuation URLs. A full-list generic loader should not emulate paging or require every destination to implement load-more callbacks.

The feature pattern remains consistent:

```text
Activity or Fragment compatibility host
  -> Route: Hilt ViewModel, lifecycle collection, UI effects and router
  -> Screen: immutable state, stateless rows, callbacks, shared list components

ViewModel
  -> finite loader or Paging plus feature-specific state
  -> feature repository API
  -> app adapter: account session, legacy Rx API/cache and DTO mapping
```

Feature `api` modules expose navigation and repository contracts and immutable models. `impl` modules own hosts, ViewModels, screen resources, UI-scoped routers, trackers, behavioral tests, and screenshot baselines. Other features depend on APIs. Keep Activities, Fragments, callbacks, adapters, and platform launches out of retained ViewModels.

## Expressive presentation and shared errors

All migrated lists use grouped Material 3 surfaces through `TeamCityListRow`. The owning feature supplies `ListRowPosition` from adjacent rows in the same logical section; paged features inspect neighbors with `peek` so grouping does not trigger extra requests. Outer corners are larger than inner corners, and interactive rows animate their corners when pressed. Passive agent and test rows retain their non-interactive semantics. Artifact long presses retain their existing actions.

Shared leading surfaces, section headings, and loading placeholders use the same geometry. Build rows expose a readable status label alongside their status icon, with wrapping titles, details, and branches. History uses a wrapping screen title and a labeled Run build action. Its list viewport reserves the action's measured height so a scrolled append retry cannot sit beneath the floating button.

`libraries/theme` owns both error presentations. `ErrorContent` supplies the full-screen icon, headline, message, and primary recovery action. `ErrorNotice` supplies a compact notice beside retained content for refresh, append, optional-section, save, and download failures. Notices stack their actions in narrow layouts or with enlarged text. Bounded feedback areas scroll and preserve space for the main content; full-screen errors also scroll when the window is too short.

The same error components are used by Settings, Manage Accounts, Splash, Build Log, Drawer, About, authentication forms, Run Build, and Quick Filter. Feature-specific explanations and recovery callbacks remain with their owners: an authentication failure offers Sign in, and download/count failures retry their own operation. Favorites presents one all-failed explanation rather than duplicating a generic failure. Field validation remains attached to its field, and consent/permission dialogs retain their existing purpose.

## Compose text ownership

Compose screens resolve localized text against the current composition, while
ViewModel-produced UI models select state-dependent resource IDs. Use `@StringRes`
for a simple message or action label and `UiText` from `libraries/theme` for text
that can contain formatting arguments or server-provided content. The descriptor
survives configuration changes without retaining a localized string or Android
resources. Feature resource IDs stay in feature implementations, outside domain
models and feature API contracts.

Paged Tests, Changes, and Build History map presentation rows before rendering;
finite build lists expose presentation rows alongside their complete launch
snapshots. Rendering still uses lazy Paging item access and preserves list refresh
flags, navigation payloads, and page caching. Navigation host mode is an explicit
ViewModel input so recursive screens keep Back even when opened for the root
project. The action sheet and Overview expose labeled action items in their state.

The audit covers every current Compose feature and the shared text-bearing
components:

| Features/components | Text ownership |
| --- | --- |
| Build History | Notice messages/actions, favorite labels/failures, coachmarks, row status and section text in UI models |
| Tests and Changes | Filter/empty/section/status labels and file-count badges selected before rendering paged rows |
| Running Builds, Build Queue, Snapshot Dependencies | Empty-filter messages and shared build-row presentation selected in UI state |
| Navigation and Favorites | Host title/navigation labels and failure feedback in UI state |
| Build Overview and Action Bottom Sheet | Row labels/fallback values and action labels in UI models |
| Run Build, Filter Builds, Quick Filter Bottom Sheet | Submission/error text, option summaries, branch fallback text and filter labels in UI state |
| Login and Create Account | Shared authentication error descriptors; duplicate-account text belongs to Create Account |
| Build Log and Drawer | Recovery messages/actions and account status messages in UI state |
| Settings and Properties | Theme names/availability and empty property-value descriptors in UI state |
| Artifacts and Manage Accounts | Local platform-error/consent/removal dialog models carry resource descriptors |
| About, Splash, Change Details, Test Details | Fixed screen/section/state copy has no alternative text-selection mapping; test detail formatting is produced with content state |
| Shared theme, list, authentication and build components | Resolve descriptors supplied by state; retain fixed component copy and visual/layout decisions |

Static titles, fixed section copy, plurals resolved by Android, and theme/layout
choices remain in composables. Expansion labels remain with the local expansion
state, as do UI-scoped consent dialogs and platform launch errors. Do not move UI
lifecycle or platform action execution into a retained ViewModel to relocate text.
Existing screenshot baselines verify that the ownership change preserves wording
and layout; resource assertions exercise state transitions, and the shared text
resolver has a locale-change test using a retained descriptor.

## Lifecycle and failure policy

Home hides and shows fragments without pausing them. Its list routes therefore need explicit visibility gates alongside lifecycle collection. Hidden finite lists should cancel unfinished requests. Navigation retains completed content; Agents and Favorites reload with normal cache policy after a genuine return. Configuration recreation retains completed content without duplicating requests.

Paged features can deliberately retain pages and in-flight requests through `cachedIn(viewModelScope)`. Destroying their owning Fragment cancels that scope. Filter changes must replace the paging generation and clear old rows atomically; old responses must never appear under the newly selected filter. Test cancellation and restoration explicitly.

Model initial error separately from failed refresh with retained content or retained empty state. Optional tab counts, rating eligibility, Overview sections, and partial favorite fetches need their own failure states. Cancellation must not become an error. Initial retry and pull refresh must preserve the legacy forced-cache policy; append retry must preserve its current continuation URL and request policy.

Legacy Favorites dropped individual fetch errors and could report an empty list when all saved entries failed. Its Compose replacement exposes partial/all-failed states while retaining saved IDs, and groups by project identity so equally named projects remain separate. Focused tests cover both behavior improvements.

## Migration sequence

| Slice | Deliverable | Reason for order |
| --- | --- | --- |
| 1 | Shared list state/UI and Agents pilot | Proves finite loading, filter changes, Home visibility, cache adapters, and shared screen states with a small list |
| 2 | Changes, then Test Occurrences | Proves paging, append retry, optional tab counts, status filters, and existing Build Details hosts |
| 3 | Navigation, then Favorites | Removes their shared legacy row/model/adapter dependency together; preserves recursive navigation, rating and account favorites |
| 4 | Shared build rows through Snapshot Dependencies, then Running and Queue, then Build History | Proves simple build rows before adding Home freshness/filter behavior and History paging, favorite/run actions, and onboarding |
| 5 | Artifacts | Handles folder/archive navigation and downloads as an explicit feature slice, with platform permission and file-browser behavior in the UI layer |
| 6 | Overview and Build Details argument decoding | Removes the remaining adapter/model/extractor consumers so the old list foundation can be retired |
| 7 | Foundation cleanup | Deletes unused presenter/view/adapter/data-manager/extractor/load-more code, layouts, DI, and dependencies after a reference audit |

Each slice replaces a complete screen, integrates existing callers through its public API, migrates its tests, and removes its superseded implementation. Preserve installed Activity names through aliases and retain incoming argument/result keys. Do not combine this work with single-activity Navigation 3 conversion; existing hosts provide the compatibility boundary.

Keep serialized legacy DTO class names and RxCache keys until their data/storage migration is separately addressed. Adapt Rx subscriptions with cancellation-aware `await`; resolve the current account session for each request, and snapshot one session for a multi-request page or batch. Favorites storage is also read by Home badges and Running/Queue filters and written by Build History, so preserve that account-storage boundary behind adapters.

Build row models and navigation payloads need distinct contracts. Build Details constructs its tabs from the incoming build's change/test URLs and counts, properties, artifacts, dependency flags, and state before Overview loads. Preserve a complete immutable launch snapshot, or fetch it behind an explicit app adapter with loading/error state; an ID-only build reconstructed from a row would lose tabs. Preserve each caller's existing Activity flags as well.

Build History's default locator permits any personal/pinned value, while a filter selection with no such choices explicitly selects false. Represent the default query separately from an empty selection. Preserve existing list/detail cache expiry, forced detail refresh for running summaries, and invalidation when a summary's finished-state disagrees with cached detail. Preserve server order when asynchronous hydration completes out of order, and clear continuation state on an empty first page. Keep the effective filter in immutable ViewModel state so refresh uses the current selection rather than the original Intent filter.

## Verification and completion

For each feature, verify repository mapping/cache policy, query replacement, cancellation, resubscription, configuration retention, and feature actions. Test the stateless screen independently from the ViewModel. Add Roborazzi baselines for every state in light/dark themes, compact/expanded sizes, and enlarged text, including scrolled actions. Update app-level Compose/Espresso integration tests for existing Activity/Fragment navigation.

The Agents pilot has 17 loader tests, 64 shared UI tests, 76 feature tests, six app unit tests, and six passing emulator integration tests. Its 126 PNG baselines were visually reviewed. App and instrumentation APKs build, and app/feature/shared UI lint passes. The local checkpoint is `de390ac` on `codex/compose-lists`.

The Changes pilot has 28 behavioral tests and 66 screenshot cases, with 78 visually reviewed PNG baselines including scrolled enlarged-text content. Its seven app adapter tests and three existing mapper tests pass. All four emulator integration tests pass, covering row navigation, initial retry, empty content, and optional count retry. App and instrumentation APKs build; app/Changes lint and repository formatting checks pass. The local checkpoint is `a4f0be2` on the same branch. These checkpoints remain local.

The Test Occurrences slice has 153 feature tests, seven app adapter tests, and 126 visually reviewed PNG baselines. All ten emulator integration cases pass, including filter-specific empty messages, count retry without page reload, and selected-filter/page retention through Activity recreation. Four Changes integration cases also pass after fixing Build Details to reconnect restored tab fragments instead of discarding their ViewModels. Opening a different build still creates fresh tabs. App and instrumentation APKs build; app/feature lint and formatting pass. The local checkpoint is `af3818c`; no checkpoint has been pushed.

Navigation and Favorites share immutable project/configuration models in `libraries/build-configurations`. Their 210 feature tests, 18 app adapter tests, and 162 visually reviewed PNG baselines pass, along with app/feature lint, formatting, and both APK builds. All 34 Navigation, Favorites, Home, rating, and Splash integration cases pass, covering the installed Navigation alias, recursion, drawer actions, rating persistence, partial/all-failed favorites, configuration retention, same-server account switching, and Splash startup. The two formerly gated favorite-action cases now pass through Compose Build History, updating the same active-account storage used by Favorites and Home badges. The local checkpoint is `a8ca0ce`.

The shared build models/UI, Running Builds, Build Queue, and Snapshot Dependencies have 357 passing local tests and 282 visually reviewed PNG baselines, including 18 scrolled captures. All eight owning library/API/implementation lint tasks pass. History also passes 231 local tests with 200 reviewed PNG baselines and clean API/implementation lint. Its route presents Paging content and modal actions only while resumed, retaining cached pages, pending requests, the effective filter, and scroll state across pause and recreation. Overview and Artifacts pass 312 tests with 258 reviewed PNG baselines and clean API/implementation lint. The combined app integration is verified. Home count badges use a narrow compatibility adapter rather than depending on the removed list data managers. Build Details shares one activity-scoped argument snapshot, retaining loaded host data while constructing tabs from the incoming payload; app tests verify recreation, new-intent reset, and current actions.

All consumers in the inventory use their Compose replacements, existing entry points and cache/account behavior are verified, and no production references remain to `base/list`, sectioned RecyclerView adapters, or Mugen.

Final verification covers 1,514 shared-library and feature tests, 371 app unit tests, 1,232 visually reviewed PNG baselines, and 196 unique passing emulator integration cases. The comprehensive 196-case emulator run was followed by a passing 61-case targeted rerun after correcting test fixtures and synchronization; every case has a passing result. The native pager test helper pumps pending offscreen Compose layout while retaining actual TabLayout taps, and filter tests drive Compose frames before checking returned requests. Four Home regression cases verify incoming tab selection, restored-tab precedence, and invalid tab indices. Both mock debug APKs build; production debug Kotlin/Hilt compilation, lint for both flavors, and repository formatting pass. Lint retains existing warnings and reports no errors.

The initial migration implementation checkpoint is `818cd42`. All checkpoints remain local on `codex/compose-lists`; nothing has been pushed. Native Home and Build Details hosts, Rx APIs/cache DTOs, and account storage remain explicit compatibility boundaries for later migrations.

The expressive presentation follow-up passes 3,288 local test cases, including 371 app tests and verification of the refreshed screenshot baselines, plus 35 unique emulator integration cases. Added behavior coverage checks default and custom error actions, disabled retries, passive notices, long build labels, retained-content recovery, and short windows with enlarged text. History append retry and account-load retry stay above their measured floating action lanes. Screenshot review covers both themes, compact/expanded layouts, enlarged text, and scrolled recovery actions, including authentication failures. Drawer integration uses the accessible Compose navigation button rather than the superseded native toolbar selector.

Verification commands use JDK 17: `./gradlew testDebugUnitTest :app:testMockDebugUnitTest`, `./gradlew lintDebug :app:lintMockDebug :app:lintProdDebug --max-workers=1`, and `./gradlew :app:assembleMockDebug :app:assembleMockDebugAndroidTest :app:compileProdDebugKotlin`. Formatting passes with `spotlessApply` and `spotlessCheck`. Lint reports existing warnings and no errors. Production lint passed when retried with one worker after an internal Kotlin analyzer crash.

Compose list and paging behavior follow the official [Compose lists guidance](https://developer.android.com/develop/ui/compose/lists) and [Paging overview](https://developer.android.com/topic/libraries/architecture/paging/v3-overview). Feature ownership follows the repository's `AGENTS.md` contract.
