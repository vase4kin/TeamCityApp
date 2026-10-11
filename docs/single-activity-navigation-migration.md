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

# Single activity navigation migration

TeamCityApp is migrating all 26 existing Compose features into one application-owned activity using Navigation 3. Home and Build Details become Compose shells with Hilt ViewModels. Existing account, network and cache behavior remains behind repository adapters. External browser, sharing, permission and OSS licenses activities remain platform integrations.

## Execution and completion gates

This is one continuous goal. Checkpoints are local savepoints, not scope reductions. The migration is complete only when all destinations and sheets use the root activity, required installed entry points dispatch correctly, old hosting infrastructure is removed, and the required checks pass.

| Phase | Completion evidence | Current state |
| --- | --- | --- |
| Contract and inventory | Each host, navigation contract, argument, result and alias maps to a destination or platform action | Initial host/API inventory below; outgoing adapter audit remains |
| Navigation foundation | Typed keys, registered entries, saved visits/results, Back/reset/replace, per-entry and child ViewModel ownership, and Compose sheets | Verified with 34 navigation tests, pilot feature tests, production compilation and 24 emulator cases |
| Startup and accounts | Splash/Login/Create Account/Manage Accounts routes; launcher compatibility; same-server account isolation | Implemented; coordinator tests and launcher/same-server emulator cases pass; remaining installed entry dispatch belongs to cleanup |
| Home | Compose shell/ViewModel/repository; five retained tabs; badges, filters, onboarding and drawer | Implemented and verified; native shell removal waits for the remaining caller migration |
| Project and build workflows | Recursive projects, History, Run Build, Filter Builds and details; origin-targeted results | Recursive projects verified; History, build actions and typed feature results pending |
| Build Details | Compose shell and seven existing child routes; conditional tabs, counts and actions; loaded/incoming snapshot separation | Pending |
| Cleanup | No superseded activity/fragment hosts or native shell code; installed aliases dispatch to root | Pending |
| Final verification | Local/screenshot tests, both-flavor lint, app/instrumentation builds, production compilation, formatting and emulator regressions | Pending |

## State and navigation contract

Feature APIs own serializable destination keys and plain argument models. Implementations register their entries and serializers through Hilt sets. The app combines registrations and owns incoming Intent compatibility. Feature implementations do not depend on one another.

Each visit has a unique ID even when its destination arguments equal those of another visit. Navigation 3 uses that ID for saveable UI state and ViewModel ownership. A popped/replaced visit loses its owner and pending results. Results target an existing originating visit, survive saved-state restoration, and are consumed once by the appropriate result type.

Account/session replacement creates fresh visit identities and discards previous-session results. The saved snapshot includes its session key; restoration compares it with the current account before composing any destination, because saveable input keys alone do not validate restored account state. Home tab and Build Details tab owners must remain scoped to their parent visit; they survive tab switching and recreation but are cleared when their parent/session is replaced. API session ownership uses account identity rather than server URL alone.

Build Details constructs available tabs from the incoming complete launch snapshot and derives current actions from loaded build data. Build Log's WebView and callbacks belong to visible composition resources; downloads retain destination ViewModel state and close blocked reads when cancelled.

The first foundation checkpoint routes About and Settings through MainActivity from existing callers. Their legacy hosts remain temporary test/compatibility boundaries until callers and integration tests migrate; they are not part of the final architecture.

## Acceptance cases

- Startup selects the correct authentication or Home flow, preserves the installed launcher alias, and does not return to completed login/startup screens on Back.
- Login/Create Account preserve Basic auth challenges, explicit HTTP/SSL consent, encrypted persistence, duplicate detection, pending submissions and in-memory password handling.
- Switching accounts, including accounts on the same server, cancels old work and clears old destination/tab owners, cache/cookie/consent behavior and results appropriately.
- Home preserves Projects/Favorites/Running/Queue/Agents selection, selected-tab request precedence, scroll and content retention, partial failures, badges, filters, drawer actions and onboarding.
- Recursive projects and repeated builds/projects have independent owners. Back returns to the previous visit with retained content.
- History keeps effective filters, favorite actions, paging and scroll state. Run Build/Filter Builds return typed results only to their originating visit; cancellation returns no result.
- Build Details preserves conditional tab availability, counts, selected tabs, optional failures, current menus, cancel/restart confirmations and queued-build replacement.
- Changes/Tests retain pages and filters; Properties, Snapshot Dependencies and Artifacts preserve complete arguments and navigation.
- Artifact folders, downloads, sharing, clipboard, browser and licenses actions work from the root host. Download cancellation does not overwrite completed files.
- Drawer, Action Bottom Sheet and Quick Filter use Compose sheets with correct dismissal/Back and action behavior.
- Recreation and process restoration preserve navigation keys, visit identities, applicable UI state and unconsumed results. Popping or replacing owners cancels their work.

## Verification

Use JDK 17 and narrow owning-module tests at each slice. Add behavioral Navigation 3 tests for results, visit identities, state serialization, actual ViewModel retention/disposal and restored Compose state. Add Home/Build Details Roborazzi baselines for each relevant state in both themes, compact/expanded layouts and enlarged text; inspect PNG changes before accepting them.

Final commands include `./gradlew testDebugUnitTest :app:testMockDebugUnitTest`, `./gradlew lintDebug :app:lintMockDebug :app:lintProdDebug --max-workers=1`, `./gradlew :app:assembleMockDebug :app:assembleMockDebugAndroidTest :app:compileProdDebugKotlin`, and `./gradlew spotlessApply spotlessCheck`. Use the relevant emulator/Marathon integration suite for startup, account switching, Home navigation, filters/results, Build Details actions, restoration and aliases. Record concrete results and any unavailable check here. Never infer broad completion from a narrow passing test.

Commit verified task-related checkpoints using GitButler on `codex/single-activity-navigation`. Behavior and its tests stay together. Do not push or open a PR.

## Existing feature host and argument inventory

The following inventory was read from the clean checkout at common base `dafbb39f1`. Activity/fragment names are migration inputs; every feature row must have an owning destination or Compose sheet before cleanup completes.

### about

Existing hosts: `AboutActivity`.

```kotlin
fun open(activity: Activity)
```

### agents

Existing hosts: `AgentsFragment`.

```kotlin
fun createFragment(): Fragment
```

### bottom-sheet

Existing hosts: `BottomSheetDialogFragment`.

```kotlin
fun createBottomSheetDialog(title: String, descriptions: Array<String>, menuType: SheetMenuType): DialogFragment
fun createBottomSheetDialog(title: String, description: String, menuType: SheetMenuType): DialogFragment = createBottomSheetDialog(title, arrayOf(description), menuType)
```

### build-artifacts

Existing hosts: `ArtifactsActivity`, `ArtifactsFragment`.

```kotlin
fun createFragment(build: BuildLaunchData, url: String): Fragment
fun open(activity: Activity, name: String, build: BuildLaunchData, url: String)
const val BUILD = "build"
const val URL = "url"
const val NAME = "name"
const val LEGACY_ACTIVITY = "com.github.vase4kin.teamcityapp.artifact.view.ArtifactListActivity"
```

Installed aliases: `com.github.vase4kin.teamcityapp.artifact.view.ArtifactListActivity`.

### build-history

Existing hosts: `BuildHistoryActivity`.

```kotlin
fun open(activity: Activity, configurationId: String, configurationName: String, locator: String? = null)
const val EXTRA_ID = "id"
const val EXTRA_NAME = "name"
const val EXTRA_LEGACY_FILTER = "filter"
const val EXTRA_LOCATOR = "buildHistoryLocator"
const val LEGACY_ACTIVITY = "com.github.vase4kin.teamcityapp.buildlist.view.BuildListActivity"
fun locator(filter: Serializable): String
fun openBuild(activity: Activity, build: BuildLaunchData, configurationName: String?)
fun openFavorites(activity: Activity)
```

Installed aliases: `com.github.vase4kin.teamcityapp.buildlist.view.BuildListActivity`.

### build-log

Existing hosts: `BuildLogFragment`.

```kotlin
fun create(buildId: String): Fragment
```

### build-overview

Existing hosts: `BuildOverviewFragment`.

```kotlin
fun createFragment(build: BuildLaunchData): Fragment
const val BUILD = "build"
```

### build-queue

Existing hosts: `BuildQueueFragment`.

```kotlin
fun createFragment(): Fragment
```

### change-details

Existing hosts: `ChangeDetailsActivity`.

```kotlin
fun open(activity: Activity, details: ChangeDetails)
```

### changes

Existing hosts: `ChangesFragment`.

```kotlin
fun createFragment(url: String): Fragment
```

### create-account

Existing hosts: `CreateAccountActivity`.

```kotlin
fun open(activity: Activity)
```

Installed aliases: `com.github.vase4kin.teamcityapp.account.create.view.CreateAccountActivity`.

### drawer

Existing hosts: `DrawerBottomSheetDialogFragment`.

```kotlin
fun open(fragmentManager: FragmentManager)
```

### favorites

Existing hosts: `FavoritesFragment`.

```kotlin
fun createFragment(): Fragment
```

### filter-bottom-sheet

Existing hosts: `FilterBottomSheetDialogFragment`.

```kotlin
fun createBottomSheetDialog(code: Int): DialogFragment
```

### filter-builds

Existing hosts: `FilterBuildsActivity`.

```kotlin
fun openForResult(activity: Activity, buildTypeId: String)
const val BUILD_TYPE_ID = "BuildTypeId"
const val REQUEST_CODE = 12921
const val EXTRA_FILTER = "filter"
```

Installed aliases: `com.github.vase4kin.teamcityapp.filter_builds.view.FilterBuildsActivity`.

### login

Existing hosts: `LoginActivity`.

```kotlin
fun open(activity: Activity)
fun openWithClearStack(activity: Activity)
```

Installed aliases: `com.github.vase4kin.teamcityapp.login.view.LoginActivity`.

### manage-accounts

Existing hosts: `ManageAccountsActivity`.

```kotlin
fun open(activity: Activity)
```

### navigation

Existing hosts: `NavigationActivity`, `NavigationFragment`.

```kotlin
fun createFragment(): Fragment
fun open(activity: Activity, project: ProjectReference)
const val PROJECT_ID = "id"
const val PROJECT_NAME = "name"
const val ROOT_PROJECT_ID = "_Root"
const val LEGACY_ACTIVITY = "com.github.vase4kin.teamcityapp.navigation.view.NavigationActivity"
```

Installed aliases: `com.github.vase4kin.teamcityapp.navigation.view.NavigationActivity`.

### properties

Existing hosts: `PropertiesFragment`.

```kotlin
fun create(properties: List<Property>): Fragment
```

### run-build

Existing hosts: `RunBuildActivity`.

```kotlin
fun openForResult(activity: Activity, buildTypeId: String)
const val REQUEST_CODE = 489
const val EXTRA_HREF = "href"
```

Installed aliases: `com.github.vase4kin.teamcityapp.runbuild.view.RunBuildActivity`.

### running-builds

Existing hosts: `RunningBuildsFragment`.

```kotlin
fun createFragment(): Fragment
```

### settings

Existing hosts: `SettingsActivity`.

```kotlin
fun open(activity: Activity)
```

### snapshot-dependencies

Existing hosts: `SnapshotDependenciesFragment`.

```kotlin
fun createFragment(buildId: String, buildTypeName: String? = null): Fragment
```

### splash

Existing hosts: `SplashActivity`.

Installed aliases: `teamcityapp.features.splash.view.SplashActivity`.

### test-details

Existing hosts: `TestDetailsActivity`.

```kotlin
fun open(activity: Activity, url: String)
```

### tests

Existing hosts: `TestsFragment`.

```kotlin
fun createFragment(url: String, passed: Int, failed: Int, ignored: Int): Fragment
```

## App owned shells

- `HomeActivity`: five-tab native navigation, selected-tab/reload Intent requests, account/session rebuild, badges, onboarding and filters. Replace HomePresenter/HomeView/AppNavigationInteractor/FragmentFactory with the Compose shell and repository state.
- `BuildDetailsActivity`: native tabs, complete incoming build/name extras, activity-scoped initial/current argument snapshots, new-intent replacement, toolbar/FAB/dialogs and cancel/restart actions. Replace the presenter/view/tab adapter while preserving the seven feature contents and current-action semantics.

## Checkpoint evidence

The foundation checkpoint `4f50a25` on `codex/single-activity-navigation` includes the navigation library, MainActivity, typed About/Settings destinations, Hilt registrations and legacy caller delegation. Existing Drawer integration tests assert the new root destination payloads.

Verification passed with JDK 17:

- 22 navigation tests, including real destination ViewModel lifetime/disposal, repeated visits, typed result delivery, saved Compose state and account-key restoration.
- 32 About and 101 Settings local cases, including existing golden verification; their stateless UI is unchanged.
- 371 app unit tests.
- 17 unique emulator integration cases on API 37: four MainActivity navigation cases and 13 Drawer cases. The full 17-case suite passed again after the session restoration fix.
- Navigation/About/Settings implementation lint; the navigation library lint passed again after the restoration fix.
- Mock app and instrumentation APK builds and production debug Kotlin/Hilt compilation.
- Repository `spotlessApply` and `spotlessCheck`; the final check passed.

Commands: `./gradlew :libraries:navigation:testDebugUnitTest :features:about:impl:testDebugUnitTest :features:settings:impl:testDebugUnitTest :app:testMockDebugUnitTest`, implementation `lintDebug` tasks, `./gradlew :app:assembleMockDebug :app:assembleMockDebugAndroidTest :app:compileProdDebugKotlin`, and repository formatting tasks. Emulator execution used the existing Hilt instrumentation runner with `MainActivityNavigationTest` and `DrawerTest`.

A regression test first proved that restoring after the active account changed incorrectly retained the previous account's project and pending result. The saved session key and pre-composition validation fixed that case; the complete navigation suite passes. The app's actual account session coordinator remains part of the startup/Home slice.

The next coherent slice connects startup/accounts and the Compose Home shell, so successful startup always targets an implemented Home destination. It must migrate the five Home feature entry points, account/session ownership, badges, filters, drawer and onboarding. Build workflows, Build Details, installed-entry compatibility cleanup and final full-scope verification remain pending. No final completion gate is claimed by this foundation checkpoint.

### Retained children and Compose sheets

Checkpoint `782a08e` adds the hosting needed by Home and Build Details. `RetainedNavigationPane` decorates all child visits but displays only the selected one: hidden children retain ViewModels and saveable state while releasing collectors and view resources. Tab siblings do not consume system Back; removing or resetting their parent clears every child owner. Callers must use stable child IDs scoped to the parent visit.

`DestinationArguments` translates feature-owned typed arguments into defaults for existing `SavedStateHandle` ViewModels without changing their entry store or factory. A fresh-store restoration test verifies that saved user state takes precedence over new defaults. `DestinationPresentation.BottomSheet` registers a Compose modal sheet on the root stack; its underlying screen remains composed at STARTED and returns to RESUMED on dismissal. Sheets cannot be root destinations.

Verification passed with JDK 17: all 33 navigation cases (11 new), 32 About cases, 101 Settings cases and 371 app unit cases; existing pilot golden verification; navigation lint and formatting; mock app/instrumentation APK builds and production compilation. All 17 MainActivity/Drawer emulator cases passed on API 37. Navigation lint retains one existing naming warning in the foundation test helper. The first emulator command used an outdated Drawer test package; the corrected complete suite passed.

Startup/account routing, the actual account session coordinator, five Home registrations and the production Drawer/Quick Filter registrations remain pending. The child and sheet infrastructure is verified independently; it does not establish completion of either shell migration.


### Startup, accounts and Compose Home

Checkpoint `0ef9dfe` saves this implementation on `codex/single-activity-navigation`.

The installed launcher alias now targets MainActivity. Splash, Login, Create Account and Manage Accounts register their existing Compose routes. Successful startup and account operations select a typed Home destination. Authentication, encrypted persistence, duplicate detection and consent behavior remain in the existing feature repositories and app adapters.

An application-scoped account coordinator prepares the API session without retaining an Activity or Navigator. Switching accounts cancels earlier preparation and resets the destination owners using the complete active-account identity, including distinct users on the same server. Preparation errors have a retryable destination; a restored unfinished activation resumes its saved reset policy. Ordinary startup preserves the saved account's Build Log consent. Explicit account activation clears cache, cookies and that shared consent through a dedicated storage contract. Logout clears the API session and replaces the stack with Login.

Home is split into API/implementation modules with a route, stateless screen, Hilt ViewModel and suspend/Flow repository. Its five child visits retain destination state. Phone navigation uses a bottom bar; wider windows use a rail. Badges load independently and retain successful counts when another section fails. Query changes and lost collectors cancel unfinished requests. Filter sheets directly update retained list queries and keep the existing analytics/events; UI feedback remains in the route. Existing onboarding preferences and copy back the Compose prompts.

Drawer and Quick Filter are Compose sheet destinations. Drawer account selection disables dismissal while busy; a focused sheet test verifies the scrim lock and re-enabling. Drawer actions navigate to registered account/About/Settings destinations. A shared list recreation effect preserves the legacy distinction between returning to a tab (cached reload allowed) and configuration recreation (completed content retained without a duplicate reload).

Verification with JDK 17:

- 1,041 passing local cases across navigation (34), shared list UI (65), Home (91), Agents (76), Projects (120), Favorites (92), Running Builds (90), Build Queue (90) and app (383).
- Home's 91 cases include seven ViewModel cases and 84 visually reviewed Roborazzi baselines: 14 states in light/dark phone, tablet and enlarged-text variants. Existing affected feature goldens also pass.
- 24 passing API 37 emulator cases: seven startup/Home integration cases, four root navigation cases and 13 existing Drawer cases. They cover the launcher alias, signed-out startup, consent preservation/reset, selected tabs and recreation, recursive project visits, Drawer-to-Settings Back behavior, filter-sheet recreation/application and same-server account replacement.
- Mock app and instrumentation APK builds, production debug Kotlin/Hilt compilation, affected implementation lint and formatting passed. Lint retains existing warnings for the navigation test helper, the shared list modifier parameter and Login’s legacy portrait-only host; these hosts are removed in cleanup.

The first emulator run exposed a duplicate visible-list reload during recreation; the shared recreation effect fixed it. A separate test lookup needed the toolbar's unmerged semantics. The corrected complete 24-case suite passed. Full process death, the remaining account submission paths in the root host, full app lint/Marathon and all-destination cleanup remain final acceptance work.

History and Build Details still use explicit legacy navigation adapters for this checkpoint. Other compatibility entry points and old hosts remain until their callers migrate. Next: typed History/Run Build/Filter Builds/Test Details/Change Details routes and results, then the Compose Build Details shell, installed-entry dispatch and removal of superseded infrastructure. This checkpoint does not claim the complete single-activity goal.
