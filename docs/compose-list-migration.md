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

The inventory below describes the legacy implementation before migration. Agents, Changes, and Test Occurrences have verified Compose replacements, proving finite loading and server paging against the shared foundation. Navigation, Favorites, and the build list family have feature drafts awaiting integration and verification; their legacy screens remain active.

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
3. **Domain reuse:** share plain build-configuration/project models between Navigation and Favorites, and build row models/components between History, Running, Queue, and Snapshot Dependencies. Extract components after a second real consumer demonstrates the common contract.

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

## Lifecycle and failure policy

Home hides and shows fragments without pausing them. Its list routes therefore need explicit visibility gates alongside lifecycle collection. Hidden finite lists should cancel unfinished requests. Navigation retains completed content; Agents and Favorites reload with normal cache policy after a genuine return. Configuration recreation retains completed content without duplicating requests.

Paged features can deliberately retain pages and in-flight requests through `cachedIn(viewModelScope)`. Destroying their owning Fragment cancels that scope. Filter changes must replace the paging generation and clear old rows atomically; old responses must never appear under the newly selected filter. Test cancellation and restoration explicitly.

Model initial error separately from failed refresh with retained content or retained empty state. Optional tab counts, rating eligibility, Overview sections, and partial favorite fetches need their own failure states. Cancellation must not become an error. Initial retry and pull refresh must preserve the legacy forced-cache policy; append retry must preserve its current continuation URL and request policy.

Favorites currently drops individual fetch errors and can report an empty favorites list when all saved entries failed. Prefer explicit partial/all-failed states, retaining saved IDs. Group by project identity rather than project name so equally named projects remain separate. These are intentional behavior improvements and need focused tests.

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

Migration is complete when all consumers in the inventory use their Compose replacements, existing entry points and cache/account behavior still work, and no production references remain to `base/list`, sectioned RecyclerView adapters, or Mugen. Remove each dependency only after checking whether another untouched screen still uses it.

Compose list and paging behavior follow the official [Compose lists guidance](https://developer.android.com/develop/ui/compose/lists) and [Paging overview](https://developer.android.com/topic/libraries/architecture/paging/v3-overview). Feature ownership follows the repository's `AGENTS.md` contract.
