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

TeamCityApp runs its Compose features in one application-owned activity using Navigation 3. Home and Build Details are Compose shells with Hilt ViewModels, alongside the feature API/implementation pairs. Existing account, network and cache behavior remains behind repository adapters. External browser, sharing, permission and OSS licenses activities remain platform integrations.

## Execution and completion gates

This is one continuous goal. Checkpoints are local savepoints, not scope reductions. The migration is complete only when all destinations and sheets use the root activity, required installed entry points dispatch correctly, old hosting infrastructure is removed, and the required checks pass.

| Phase | Completion evidence | Current state |
| --- | --- | --- |
| Contract and inventory | Each host, navigation contract, argument, result and alias maps to a destination or platform action | Inventory and outgoing production-reference audit complete; external platform launches retained |
| Navigation foundation | Typed keys, registered entries, saved visits/results, Back/reset/replace, per-entry and child ViewModel ownership, and Compose sheets | Verified with 38 navigation tests, feature tests, production compilation and root-host emulator cases |
| Startup and accounts | Splash/Login/Create Account/Manage Accounts routes; launcher compatibility; same-server account isolation | Verified; coordinator, installed launcher/alias and same-server account-isolation cases pass |
| Home | Compose shell/ViewModel/repository; three navigation destinations with five retained panes; badges, inline filters, onboarding and Account | Implemented and verified; native Home shell and its seven fragment/sheet hosts removed |
| Project and build workflows | Recursive projects, History, Run Build, Filter Builds and details; origin-targeted results | Typed recursive projects, History, Run Build, Filter Builds, detail/result destinations and Build Details callers verified |
| Build Details | Overview hub, floating actions, five conditional standalone section destinations, optional counts, and loaded/incoming snapshot separation | Grouped Overview, section headers/visits, artifact result ownership, and external build-log Custom Tabs replace the tabbed shell |
| Cleanup | No superseded activity/fragment hosts or native shell code; installed aliases dispatch to root | All native application/feature hosts removed; aliases dispatch to MainActivity; production references audited |
| Final verification | Local/screenshot tests, both-flavor lint, app/instrumentation builds, production compilation, formatting and emulator regressions | Complete; all-module tests/goldens, both-flavor lint, builds, production compilation, formatting, 388 emulator cases and real process restoration pass |

## State and navigation contract

Feature APIs own serializable destination keys and plain argument models. Implementations register their entries and serializers through Hilt sets. The app combines registrations and owns incoming Intent compatibility. Feature implementations do not depend on one another.

Each visit has a unique ID even when its destination arguments equal those of another visit. Navigation 3 uses that ID for saveable UI state and ViewModel ownership. A popped/replaced visit loses its owner and pending results. Results target an existing originating visit, survive saved-state restoration, and are consumed once by the appropriate result type.

Account/session replacement creates fresh visit identities and discards previous-session results. The saved snapshot includes its session key; restoration compares it with the current account before composing any destination, because saveable input keys alone do not validate restored account state. Home tab owners remain scoped to their parent visit. Build Details Overview and separate section visits retain their own state through recreation and Back; their owners are cleared when their visit/session is removed. API session ownership uses account identity rather than server URL alone.

Build Details constructs available grouped section links from the complete incoming launch snapshot and derives status/actions from loaded build data. Section keys preserve that snapshot, including initial parameters, opaque URLs and test filters. Each section visit owns a retained content pane; its live parent receives artifact-sheet results. View Log opens the active account's server URL in a Chrome Custom Tab, preserving guest URLs. Browser authentication stays in the browser. The embedded log and its consent storage are removed. History and restart snackbars navigate their Show actions immediately to a reference-loading visit. That visit owns loading and Retry, resolves the opaque API URL, and replaces itself with a normal Build Details destination carrying the complete snapshot. Back returns to the source visit. Login, Add account, Run Build and Build Details mutations show progress in their originating shared button/FAB, while preserving confirmations and existing success/error handling.

Artifact downloads show a pinned inline filename/progress card with Cancel and a screen-lifetime warning; failures retain Retry/Dismiss in that card. Downloads survive configuration changes and transient sheets, but cancel when navigating away or backgrounding the app. Cancellation closes blocked reads and deletes temporary files without overwriting completed downloads. Background scheduling and cross-screen download tracking remain outside this implementation.

The foundation checkpoint initially kept About and Settings hosts as temporary compatibility boundaries. Final cleanup removed those hosts and migrated their callers and integration fixtures to MainActivity. The checkpoint sections below record verification at each stage; the completion table and final verification section describe the current architecture.

## Home navigation and account access

Home has three primary destinations: Projects, Favorites and Activity. Activity contains
Running, Queue and Agents, and restores its last view after visiting another destination
or recreating the app. The five existing `HomeTab` values and their ordinals remain
unchanged for installed Intent compatibility and saved navigation. Each content pane
keeps its original retained owner.

Home owns a shared pinned header with the selected destination title and account avatar.
The compact bottom navigation uses icons with accessible destination descriptions. Hosted
root screens suppress their own headers through the scoped `LocalTeamCityHeaderProvided`
contract and bind list scrolling to the host's app-bar state. Nested project visits
retain their own Back header. Activity's native Expressive connected toggle buttons
show separate Running/Queue counts; larger text puts counts below their labels. The
primary Activity badge sums Running and Queue only when both counts are known;
loading and failures never produce a misleading partial total. Agents and Favorites
have no notification-style badge in primary navigation.

Filters are labeled inline controls with a visible current-query summary. Favorites
embeds its add action in the existing shared empty-state message card for an empty
saved-favorites list. The action selects the retained Projects pane directly. Existing onboarding keys remain
compatible, with the former drawer tip targeting the avatar and action tips targeting
the filters or the empty-state action. Optional count failures use a Snackbar with a
Retry counts action rather than reserving permanent space above navigation.

Activity panes retain completed content through selection, backgrounding and recreation.
While visible and resumed, they check fresh repository snapshots every 30 seconds.
Changed snapshots produce a floating Updates available card; Update applies the latest
successful check. Returning to an opened pane waits for the next interval instead of
reloading immediately. Polling stops when hidden and rejects canceled or superseded
results. Manual refresh, query changes and unfinished initial-load resumption keep
their existing behavior. Refresh failures float above retained list content, with
Retry and Close actions; the list geometry and scroll position do not move. Update
and refresh-failure notices share the same primary-container tonal surface, shape,
spacing and action layout, with distinct refresh/error icons.

The avatar opens the full-screen Account destination. Its serialized
`DrawerDestination` key remains compatible, and the existing account ViewModel,
repository, analytics and switching behavior remain in the owning feature. Account
child destinations push onto the stack so Back returns to Account, then Home. Manage
accounts and Add another account form one connected tonal row group, without a
second accounts heading. Back and other interactions remain disabled while an account switch is pending. Quick
Filter and Action Bottom Sheet keep their existing sheet presentation.

Verification for this layout passed the owning feature and shared-list consumer unit
and screenshot suites, Home repository adapter tests, both debug app builds and the
mock instrumentation APK, app/feature lint and formatting. All 88 API 37 emulator
cases were verified across Home startup/restoration, navigation, Favorites, Activity
and Account. They include retained scroll/content on return, cancellation, same-server
account isolation, Snackbar count retry, the empty-state Projects action, and a real
30-second poll that keeps a new build pending until Update is tapped.

The follow-up styling verification passed the shared-list/UI behavioral and golden
suites and 28 native Favorites, Account, refresh-retry and polling-update cases. It
covers the original empty-state card with an embedded action, one connected account
action group, and matching tonal floating notices in both themes and larger text.

## Acceptance cases

- Startup selects the correct authentication or Home flow, preserves the installed launcher alias, and does not return to completed login/startup screens on Back.
- Login/Create Account preserve Basic auth challenges, explicit HTTP/SSL consent, encrypted persistence, duplicate detection, pending submissions and in-memory password handling.
- Switching accounts, including accounts on the same server, cancels old work and clears old destination/tab owners, cache/API resources and results appropriately. External browser sessions remain browser-owned.
- Home exposes Projects/Favorites/Activity navigation. Activity preserves Running/Queue/Agents selection, incoming-tab request precedence, independent scroll/content retention, partial failures, counts and filters. The avatar opens Account with existing account/app actions and onboarding.
- Recursive projects and repeated builds/projects have independent owners. Back returns to the previous visit with retained content.
- History keeps effective filters, favorite actions, paging and scroll state. Run Build/Filter Builds return typed results only to their originating visit; cancellation returns no result.
- Build Details preserves conditional tab availability, counts, selected tabs, optional failures, current menus, cancel/restart confirmations and immediate reference navigation from Show actions.
- Changes/Tests retain pages and filters; Properties, Snapshot Dependencies and Artifacts preserve complete arguments and navigation.
- Artifact folders, downloads, sharing, clipboard, browser and licenses actions work from the root host. Download cancellation does not overwrite completed files.
- Account is a full-screen destination; its child screens return to Account before Home. Action Bottom Sheet and Quick Filter use Compose sheets with correct dismissal/Back and action behavior.
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

MainActivity installs the AndroidX launch splash before `super.onCreate`, with a
day/night background and a padded vector icon. It stays visible through the saved
account check and initial account preparation until Login/Home is ready. Storage
or preparation errors release it to show the existing retry UI. A one-second
maximum gives slow preparation ordinary progress UI instead of an indefinite
launch splash; this is a maximum wait, not a minimum display duration. Release is
one-way for each activity, so retries and account switches cannot bring the
launch splash back. The retained Compose startup/account-activation destinations
keep their cancellation, recreation, initialization and retry behavior and render
progress/errors on the app surface without a second logo or branded background.

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


### Typed build workflows and origin-targeted results

Checkpoint `89ec4b0` registers History, Run Build, Filter Builds, Test Details and Change Details in Navigation 3. Projects, Favorites, Running Builds and Build Queue navigate directly to typed History keys. The incoming History adapter now dispatches through MainActivity.

Run Build and Filter Builds carry the opening History visit ID. Their serializable results retain the complete queued URL or immutable filter selection and are consumed once by that specific visit while resumed. Equal History payloads can have separate owners and results. Existing app adapters preserve filter-to-locator semantics, favorite storage, paging and queued-build feedback. History's selection and pages survive recreation; cancelled forms emit no result. The Test/Change detail registrations reuse their existing argument mappings and UI-scoped platform routers.

The Favorites notice action returns to the existing Home visit, selects its Favorites tab and removes intermediate screens without replacing Home's owners. `Navigator.popTo` removes results owned by discarded entries and can deliver a typed result to the retained target. If no Home exists, the action creates a Home root as the legacy clear-stack launch did.

Verification passed with JDK 17:

- 1,086 local cases: navigation (36), Home (91), History (238), Run Build (147), Filter Builds (76), Test Details (62), Change Details (53) and app (383). The new cases cover all filter status payloads, repeated History visits, single-use restoration, expired targets, cancellation, account reset and owner-preserving return navigation. Existing affected goldens pass; stateless feature visuals are unchanged.
- 29 API 37 emulator cases: five new workflow cases plus the prior 24 startup/Home/root/Drawer regressions. They verify filter selection and form recreation, effective History queries, cancelled/queued Run Build results, no queue resubmission after recreation, Test/Change arguments and Back, and the Favorites action's cleared back stack.
- Mock app/instrumentation APKs, production debug Kotlin/Hilt compilation, affected feature/navigation/Home lint and formatting passed.

The first emulator run passed 28/29 cases: the Favorites test used the Espresso Back helper that reports closing the final activity as an error. Its unconditional-Back assertion now verifies the intended root exit; the complete corrected suite passes.

Build Details still opens through the explicit legacy adapter. Its native tab callers and the remaining installed component/result compatibility entry points migrate in the next two phases. The Compose Build Details shell, artifact folders and action sheets, old-host removal, full process-restoration acceptance and final all-module/Marathon checks remain pending. The goal stays active; no push or PR has been created.


### Build Details shell and retained operations

Checkpoint `b2cf0e3` adds the owning `features/build-details/impl` module, a stateless Material 3 shell, Hilt ViewModel, private resources/tracker and a suspend repository contract with an app adapter. `BuildDetailsDestination` carries the complete immutable launch snapshot. Its serializable nested models preserve raw server values, opaque URLs, nullable section presence, test counts and parameter metadata; the legacy wire/cache DTOs remain unchanged.

The ViewModel constructs conditional tabs from the incoming snapshot and saves refreshed action data separately. Selected tabs and counts survive restoration; equal build visits have independent owners. Cancel confirmations retain the exact snapshot the user confirms, select own/other-user messages, and allow re-add only when stopping a running build. Restart uses the refreshed configuration, branch and the complete parameter payload with the installed minimal request shape. Pending mutations survive collector loss during recreation, reject duplicate submissions and cancel with their owner. Process restoration retains confirmations, queued feedback and pending replacements without automatically repeating a mutation. Queued reads support retry and produce one replacement.

The app adapter resolves one account repository per operation, disposes Rx subscriptions on coroutine cancellation and checks account identity before and after both success and failure. Same-server user changes cannot publish old results. Forbidden responses remain distinct from ordinary server failures; failed cancellation requests an Overview refresh.

Verification passed with JDK 17:

- 519 local cases: Build Details (120), shared builds (8) and app (391). Build Details includes 17 ViewModel, four destination/conditional-tab and three Compose interaction cases, plus 96 Roborazzi cases. Eight new app adapter cases verify request shape, parameter presence/metadata, forbidden responses, cancellation, account isolation and queued reads.
- All 96 shell baselines were visually reviewed: seven selected tabs, five confirmation variants and four progress variants, each in light/dark phone, tablet and enlarged-text layouts. The shell uses deterministic child-content fixtures; child-feature visuals remain owned by their existing modules. Normal golden verification and `verifyRoborazziDebug` pass.
- Build Details implementation lint, mock app/instrumentation APK builds, production debug Kotlin/Hilt compilation and formatting pass.

This checkpoint prepares the shell without changing the existing Build Details entry point. Connecting its seven retained child destinations, action sheets/artifact folders, counts, platform actions and onboarding is the next phase-six slice. Build Details still opens through the legacy adapter until that integration is verified. Old-host removal and the final all-destination/process-restoration/Marathon checks remain pending. The goal stays active; no push or PR has been created.


### Build Details routes, folders and scoped sheet results

Checkpoint `95b12b307` saves this integration on `codex/single-activity-navigation`.

Build Details now registers its Compose shell in the root activity. History, Running Builds, Build Queue and Snapshot Dependencies open its typed destination directly. Each of its seven conditional tabs owns a stable child visit, scoped to the particular parent visit. Existing feature routes retain their ViewModels and saveable state across selection and recreation while hidden tabs release their collectors and view resources.

The shell constructs tabs and parameter content from the complete incoming snapshot. Loaded Overview data updates its title and action arguments without rebuilding those child owners. Cancel/restart confirmation and submitted operations survive recreation. Successful mutations replace the visit and all its old tab owners; the replacement forces an initial fresh Overview read to avoid reloading a stale cached pre-mutation build. Failed cancellation refresh tokens also survive saved-state restoration.

Artifact folders are separate root destinations. Action sheets return serializable results to a live parent visit and name the exact retained child consumer. A consumer leaves another child's results untouched. Artifact actions queue until the resumed route can receive them, preserving download delivery during sheet dismissal and recreation. Platform routers are created for the destination UI and release Activity-bound resources with that UI. Clipboard, sharing and browser launches remain UI actions.

Verification with JDK 17 passed:

- 1,439 local cases: navigation (37), Build Details (123), Overview (198), Artifacts (117), Changes (94), Tests (153), Build Log (65), Properties (53), Snapshot Dependencies (100), Action Sheet (81), History (238), Running Builds (90) and Build Queue (90). These include existing screenshot verification and the 96 reviewed shell baselines. The final saved refresh-token assertion also passed in the 17-case Build Details ViewModel suite.
- All 35 API 37 emulator regression cases pass: six new Build Details cases plus the existing workflow, startup/Home, root and Drawer suites. The new cases cover all seven tabs and initial parameters across recreation; exact loaded configuration/branch routing from a restored sheet; artifact folder and real download delivery without duplicate reopening; stop/re-add submitted once with replacement; restart submitted once and queued-build replacement; and independent tab selection for repeated visits to the same build. These root flows assert that no feature Fragments were created.
- Focused Build Details/Overview/Artifacts/navigation lint, mock app and instrumentation APK builds, production Kotlin/Hilt compilation, and repository formatting passed. Lint retains the existing navigation test-helper naming warning; all three feature implementations report no issues. A parallel broad check exhausted the default 2 GB heap; the complete affected-module run passed with one worker, no parallel execution and a 4 GB heap.

The first emulator run exposed Kotlin-null-unsafe test matchers and incomplete fixtures. A subsequent test waited for a lazy Branch row below the viewport; the corrected tests wait for loaded Overview content. Product behavior was preserved while fixing those test assumptions.

Remaining Build Details acceptance work: preserve menu onboarding preferences/copy; settle initial badge loading and optional count failures without reporting a failed count as zero; verify view/download cancellation when visits are removed and during account replacement. Then remove the legacy Activity/Fragment hosts, their outgoing APIs, native shell infrastructure and unused dependencies, preserve installed component dispatch, and run the full final acceptance gate. This checkpoint does not complete the single-activity migration.


### Build Details counts, menu education and resource disposal

Checkpoint `ae76513ae` loads Changes and Tests totals independently from the parent build visit, before either child tab is opened. Loading, available and unavailable totals stay distinct. A failed total displays `?` and has an explicit retry; successful totals survive recreation and process restoration without duplicate requests. Child count notices share this parent state and retry only their failed section. Losing the parent collector cancels unfinished reads; removing or replacing its owner cancels its work. Account checks reject old successes and failures even when two users share the same server.

Stop, remove-from-queue and restart education uses the existing global preferences and exact copy. Its presentation follows the loaded action snapshot, appears only on the resumed Overview destination, survives recreation, and records only the dismissed prompt. Preference failures are explicitly retryable. Pending education does not block independent count publication.

Verification passed with JDK 17:

- 819 affected local cases: Build Details (177), Changes (94), Tests (153) and app (395). Build Details includes 138 verified screenshot cases: the previous 96 plus 42 visually reviewed baselines for three prompts, loading totals, partial/all count failures and failed education, in both themes, phone/tablet and enlarged text. The final ViewModel cleanup and unknown-build-state assertion passed again in the full 177-case Build Details suite.
- All 39 API 37 emulator cases passed: ten Build Details cases and the existing workflow, startup/Home, root and Drawer suites. The four new cases prove initial independent totals and targeted retry across recreation; all three education preferences/copy and persisted dismissal; blocked artifact reads surviving recreation but closing on destination/account replacement without overwriting a completed file; and detached Build Log views being replaced by fresh WebViews on return and released on Back.
- Mock app/instrumentation APK builds, production debug Kotlin/Hilt compilation, Build Details/Changes/Tests lint and formatting passed. Changes and Tests retain their existing modifier-parameter lint warnings. The final builds, Build Details lint and formatting passed again after cleanup of the obsolete shell count setter.

Installed component dispatch, removal of superseded hosts/navigation/DI/layouts/dependencies, complete process-restoration and root authentication/platform acceptance, and the final all-module/both-flavor verification remain pending. The goal stays active; no push or PR was created.


### Installed entry dispatch and removal of About/Settings hosts

Checkpoint `74c8f87` removes the About and Settings activity classes, their obsolete activity DI/close router and unused host dependencies. Their installed component names are private aliases owned by the app manifest and target MainActivity. `IncomingDestinationResolver` maps cold alias launches to typed keys; explicit typed payloads take precedence. Both features continue to reuse their existing route, ViewModel and stateless screen. Their incoming Activity adapters remain only while native app callers still need them. About retains its required AppCompat dependency for the external OSS licenses screen.

The root uses `singleTask` because Android treats alias names as separate component identities under `singleTop`. Incoming requests retain the host's original launch component and save a normalized typed payload. Alias-to-root and root-to-alias requests reuse the same activity and preserve its destination owners, saved stack and Back behavior across recreation.

Verification passed with JDK 17:

- 531 local cases: About (32), Settings (101) and app (398), including three incoming dispatch cases and existing screenshot verification.
- All 58 API 37 emulator cases: the complete previous navigation/Drawer regression suites, six root cases, seven migrated About cases, five Settings cases and five Hilt smoke cases. About links, email chooser, app rating and external OSS licenses Intent contracts pass from the root. Both directions of alias/typed request reuse preserve the previous About owner without another server request. Cold alias launches, themes, cancellation, recreation and Back pass.
- Mock app/instrumentation APK builds, production Kotlin/Hilt compilation, affected lint and formatting pass. Settings lint reports no issues; About retains its existing window-size and URI-extension warnings.

The first cleanup compile showed that About still needs AppCompat for the external licenses superclass; that dependency was retained. The first emulator run passed 56/57 cases and exposed duplicate alias-launched roots under `singleTop`. The single-task root and stable incoming component fixed the issue; the complete expanded 58-case suite passed. Remaining hosts, aliases, native shells and full final verification are still pending.


### Startup host removal

Checkpoint `e6ede1b` removes the standalone Splash activity, its activity-scoped router/DI, the app's outgoing Splash-to-Home/Login adapter and its obsolete Android API contract. Unused Splash host dependencies are removed. The previously installed private implementation component is an app-owned root alias; the existing exported launcher component remains unchanged and still targets MainActivity. The app resolver maps the private startup component to the registered Splash destination.

Splash integration tests now launch MainActivity and verify destination replacement instead of expecting another activity launch. Empty/account startup replaces Splash with Login/Home, recreation does not repeat the account read, and Back exits the root. Pending reads cancel on stop and restart on resume; background completion waits for resume before account routing; storage errors retain their retry state; both installed components resolve to the root with their required exported/private flags. Real storage-adapter startup tests also retain their selected Projects content and Login screen after recreation.

Verification passed with JDK 17:

- 446 local cases: Splash (48), including existing screenshot verification, and app (398).
- All ten startup/launcher emulator cases passed on API 37, followed by all 13 Home/root regressions. The earlier 58-case host-removal regression checkpoint remains verified; these additional ten cases bring the distinct migrated acceptance inventory to 68.
- Mock app/instrumentation APK builds, production Kotlin/Hilt compilation, Splash lint and formatting passed. Splash lint reports no issues.

The first startup emulator run exposed two obsolete test assumptions: paused Android frames need not redraw the ready UI, and Projects text appears in both the toolbar and Home navigation. The tests now observe completed reads and the account coordinator while paused, and use unique content/tab tags. The corrected complete startup and related regression suites pass.

Cleanup remains active. Next remove the remaining authentication/account/detail/build activities and their compatibility DI/routers, migrate their app integration coverage, and replace every legacy fragment/sheet and native Home/Build Details dependency. Complete installed argument/result dispatch and legacy saved-state compatibility during that removal. Full all-module/both-flavor checks and final process-restoration/Marathon acceptance remain required; the goal is not complete. No push or PR was created.


### Detail and account-management host removal

Checkpoint `135849324` removes Test Details, Change Details and Manage Accounts activity classes, their activity-scoped DI, the obsolete Manage Accounts outgoing app router/API, its unused native theme, and unused host dependencies. The app owns private aliases for their installed component names. Incoming legacy list/Drawer callers now request typed root destinations; their Activity adapters remain only until those native callers are removed.

Installed test-occurrence URLs and complete change snapshots dispatch through the app resolver. Change file names/types must both be present and have matching lengths; missing or mismatched legacy arguments remain invalid input and close their owning destination. Valid snapshots retain all browser/diff arguments. Test Details retains its invalid-input message while delegating close to the destination owner, so nested routes pop and a root route exits.

Manage Accounts integration coverage now verifies actual destination outcomes. Removing the active account replaces the stack with Home; removing the final account replaces it with Login; Back exits both roots. Adding an account pushes Create Account in the same activity, preserves the form and discard dialog through recreation, and returns to the retained account list after confirmation. The existing removal-dialog, storage, cancellation, native light/dark ripple and recreation cases continue to pass.

Verification passed with JDK 17:

- 657 local cases: Test Details (63), Change Details (54), Manage Accounts (139) and app (401). The total includes 168 existing screenshot cases, unchanged by host removal, and new root-adapter, legacy argument-validation and navigation-owned invalid-input feedback cases.
- All 84 API 37 emulator cases passed: the three migrated host suites, Hilt smoke tests, legacy Drawer/Tests/Changes callers, and root, workflow, Home and Build Details regressions. Installed aliases resolve privately to MainActivity. Complete change arguments, browser/diff links, missing input, occurrence cancellation/retry/recreation, account removal and retained Back behavior pass.
- Mock app/instrumentation APK builds, production Kotlin/Hilt compilation, affected lint and formatting passed. All three affected feature lint reports contain no issues.

The first instrumentation compile caught duplicate imports in the migrated Hilt smoke test; they were corrected. The first emulator run passed 83/84 cases: the new Create Account Back test omitted its existing discard confirmation because the initial URL contains `https://`. The corrected test verifies that confirmation across recreation and then the retained account list; the complete 84-case rerun passes.

Nine legacy activity hosts remain, along with feature fragment/sheet hosts and native Home/Build Details infrastructure. Login/Create Account host removal and root authentication integration coverage are next, followed by the remaining build/project hosts and all native shell callers. Installed argument/result compatibility, legacy saved-state compatibility, complete process restoration and final all-module/both-flavor verification remain required. The goal stays active; no push or PR was created.


### Authentication host removal and root lifecycle coverage

Checkpoint `f83c2b381` removes Login/Create Account activity classes, activity-scoped DI/routers, obsolete app outgoing routers and their API contracts, unused AppCompat dependencies, and Login's native theme. Existing Compose routes, Hilt ViewModels, authentication repositories and analytics remain the owners of behavior. Incoming legacy callers use the root destination launcher; Login's clear-task compatibility policy is preserved.

The app manifest owns private root aliases for both the implementation component names and the older Login/Create Account names. The resolver maps all four names to their destination keys. Authentication/validation instrumentation now launches MainActivity through these aliases and verifies the actual Home destination and persisted account properties. Successful authentication replaces its stack; Back exits the root instead of returning to the form. The shared deprecated test rules accept an optional installed component, preserve their normal behavior for remaining legacy callers, and handle nullable default Intents explicitly. Their Kotlin files move to the Kotlin source root with the original license notices preserved.

The existing Login pending-request recreation test runs through Navigation 3. New Create Account integration cases prove that a pending guest request retains its owner and submits only once across root recreation, and that replacing its account stack cancels the network call. A late success after cancellation cannot persist a new account or replace the selected account. Native light/dark authentication switch feedback and bottom-action/keyboard/rotation coverage now use the same root routes.

Verification passed with JDK 17:

- 675 local cases: Login (151), Create Account (122) and app (402), including 236 existing screenshot cases and the new four-name alias dispatch case. Stateless screen baselines are unchanged.
- All 108 API 37 emulator cases passed: authentication and validation, native switch/bottom-action UI, installed aliases, root/Home/account/Drawer/workflow/Hilt/startup regressions. All 19 Login/Create Account validation cases then passed through the older installed aliases; the authentication and native UI suites already exercise the implementation aliases. Pending requests, cancellation, late responses, HTTP/SSL consent, persistence failure, duplicate validation, demo login, passwords leaving the UI, recreation and Back behavior pass.
- Mock app/instrumentation APK builds, production Kotlin/Hilt compilation, affected lint and formatting passed. Both feature lint reports contain no issues. The final validation-fixture build and formatting checks passed again before the older-alias emulator run.

The first emulator run exposed a nullable initial Intent in the deprecated custom rules and two stale startup test package names. The rules now provide an explicit default Intent before setting their alias component, and the suite uses the actual startup packages. The corrected complete 108-case run and older-alias checks pass.

Seven legacy activity hosts remain: Navigation, Build History, Run Build, Filter Builds, Artifacts, Home and Build Details. Remove them and their native fragment/sheet callers, migrate the remaining integration fixtures, finish installed argument/result and legacy saved-state compatibility, and run complete process-restoration/all-module/both-flavor verification. The goal remains active; no push or PR was created.

### Project, History and artifact activity host removal

Navigation, Build History and Artifacts now use the root activity. Their standalone activity classes, activity routers, manifests and twelve private transition resources are removed. Build History's obsolete outgoing app router/API/binding and the unused Navigation/History AppCompat and History utility dependencies are removed. Fragment entry points and their scoped routers remain until the native Home and Build Details callers are migrated.

The app owns six private compatibility aliases covering both the implementation and older installed component names. Its resolver preserves nested project IDs/names and non-root Up behavior, translates legacy History filters to their effective locator with explicit-locator precedence, and decodes complete artifact launch snapshots with opaque folder/archive URLs and names. MainActivity normalizes incoming requests to typed, plain destination arguments before recreation, removing the consumed legacy filter/build payloads. The host exposes a read-only snapshot of its UI-owned navigator for integration assertions; it releases that reference with its composition.

History instrumentation now exercises the actual Run Build and Filter Builds forms and typed result delivery. It verifies refresh/cancellation, retained effective filters, reset, pending queued hydration through recreation, cancellation after owner removal, complete Build Details arguments and one destination push. Its Favorites action replaces a standalone History root with Home/Favorites, and Back exits. Canonical and older project/History/artifact aliases retain content without duplicate requests across recreation. Remaining native callers assert typed root keys, complete build snapshots and branch locators; recursive child navigation is checked directly in the root stack.

Verification for this checkpoint:

- 881 passing local cases: Navigation (120), Build History (238), Artifacts (119), and app (404). This includes 336 unchanged screenshot cases and focused launcher/resolver/normalization tests.
- All 170 distinct API 37 integration cases are covered successfully: installed project/History/artifact aliases, actual History forms/results, folder/archive navigation and download lifecycle, affected native Home/Favorites/rating/Running/Queue/Overview/Snapshot callers, and root navigation/Home/build workflow/Build Details regressions. The broader batch passed 169 cases; after correcting its test assertion to include the preserved branch locator, all 31 Overview cases passed in the focused rerun.
- Mock app and instrumentation APKs, production-debug Kotlin/Hilt compilation, Navigation/History/Artifacts lint, formatting and whitespace checks passed. The app's generated Kotlin compiler cache was cleared after an incremental-cache failure; subsequent verification passed.

Four legacy activities remain: Run Build, Filter Builds, Home and Build Details. Fifteen feature fragment/sheet hosts and the native shell infrastructure also remain. Next remove the two form hosts and unused activity-result entry adapters, then migrate native shell integration fixtures and remove Home/Build Details, fragment hosts and obsolete dependencies. Installed argument compatibility, legacy FragmentManager saved-state compatibility, account validation of cold restored entry arguments, complete process restoration and final all-module/both-flavor verification remain required. The goal stays active; no push or PR was created.

### Run Build and Filter Builds host removal

Run Build and Filter Builds now use MainActivity through private aliases for both generations of their installed component names. Incoming requests preserve the existing `BuildTypeId` argument and become typed destinations. Their standalone activities, activity routers/DI, navigation implementations and unused activity-result API contracts are removed. No production caller used those old result launchers after the History migration. The feature implementations no longer depend directly on AppCompat, the legacy screen-owner utilities or shared resources used only by their retired transitions. History retains the narrow legacy filter-serialization adapter at its data boundary; its screen results target the originating Navigation 3 visit.

Form rendering/queue/autocomplete instrumentation now launches the root, and existing History filter cases execute their real typed-result flow. Both canonical and older aliases preserve the configuration ID, editable branch draft and destination identity across recreation, then close the standalone root. New root integration proves that a pending Run Build submission keeps one request through recreation, returns exactly one result to the retained History visit, and does not resubmit. Replacing its account navigation stack cancels the pending subscription; a late success cannot add a result or queued-build notice to the new Home visit.

The shared deprecated activity/Intents test rules now apply their optional installed component to explicitly supplied Intents as well as default launches, copying explicit Intents before changing their component. The initial emulator run exposed that missing behavior because form fixtures supplied arguments explicitly. The corrected run also verifies authentication and Hilt regressions for those helpers.

Verification:

- 859 local cases passed: Run Build (144), Filter Builds (73), Build History (237), and app (405), including 352 unchanged screenshot cases and incoming alias tests.
- All 144 API 37 integration cases passed: form rendering/queue/options/autocomplete, effective filters/results, both alias generations and draft retention, pending submission recreation/cancellation, History, root navigation/Home/Build Details, light/dark feedback, keyboard/rotation/action surfaces, authentication/validation and Hilt smoke coverage.
- Mock app/instrumentation builds, production-debug Kotlin/Hilt compilation, both form modules' lint, formatting and whitespace checks passed.

Only Home and Build Details remain as application-owned legacy activities. Their fifteen feature fragment/sheet hosts, native shell layouts/presenters/DI, obsolete APIs/adapters and dependencies must be removed after migrating their integration fixtures. Cold restoration must validate entry arguments against the active account and discard legacy FragmentManager state safely. Complete process restoration and final all-module/both-flavor checks remain required. The goal remains active; no push or PR was created.


## Root restoration checkpoint

MainActivity now records the account owning its saved UI and validates that marker before decoding restored launch arguments. A different user on the same server replaces the old arguments and destination owners, prepares the new account with cache/cookie/Build Log consent reset, and opens Home. A removed account returns to Login. Replacing rejected extras prevents a second recreation from replaying the old alias payload; missing old markers also reject launch arguments without assuming a known account change.

The root strips retired FragmentManager state from a copied platform/SavedStateRegistry bundle while preserving Compose navigation and all other saved state. The framework regression test saves a real fragment, replaces its recorded class with a removed class, proves normal restoration fails, and verifies the cleaned bundle restores safely. Runtime coverage preserves same-account artifact content and visit identity, verifies two recreations after a same-server account change, covers logout during saving, and retains the Compose stack while discarding a real saved fragment.

Validation:

- All 409 app local tests passed, including four new restoration tests.
- Both app lint variants, mock app/instrumentation APKs, production Kotlin compilation, and formatting passed.
- All 56 API 37 integration cases passed: restoration, root navigation, Home/startup/account switching, Build Details, workflows, installed form/component aliases, Login and Create Account.

The fix is saved in local checkpoint `e7bc0e046`. Home and Build Details remain the only native application activities; fifteen feature fragment/sheet hosts and their superseded infrastructure still need removal. Complete process restoration and final all-module/both-flavor verification remain required. The goal stays active; no push or PR was created.


## Home host removal checkpoint

Local checkpoint `68e273b75` removes the native Home activity, presenter/view/router/count wiring, FragNav shell, XML bottom navigation and seven fragment/sheet hosts: Projects, Favorites, Running Builds, Build Queue, Agents, Drawer and Quick Filter. Their obsolete navigation APIs, app adapters, screen DI and unused dependencies are removed. Drawer repository bindings and the existing immutable filter-event boundary remain. The installed Home component is a private alias to MainActivity.

Incoming Home requests preserve the requested tab, return to an existing Home visit when appropriate, and apply legacy reload/new-account policy once. Account preparation keeps its requested destination through failure, retry and recreation. Account request ownership includes the user/session key; navigation pauses old destination collectors before Android resumes the root for an incoming request. The cap applies only to destination content. Navigation 3's root and retained-tab decorators keep their original lifecycles so configuration changes retain completed ViewModels. Runtime tests verify cancellation and exact request counts for different users sharing a server.

All Home and sheet integration fixtures now exercise the root Compose shell. Drawer/Quick Filter use Navigation 3 sheets with full initial expansion, keeping footer actions reachable. The sheet suite covers both themes, account rows/footer, edge interaction, clipboard, dismissal, recreation and reopening. Home badges expose stable positive-count semantics without changing rendered content.

Verification with JDK 17:

- 1,216 local tests passed across the app, navigation foundation, Home, five Home list features, Drawer, Quick Filter and Splash; this includes 600 unchanged Roborazzi baselines.
- All 135 API 37 integration cases passed: Home tabs/badges/filters/onboarding, same-server account cancellation, content retention, Drawer/root sheets, filter results, saved-account restoration, installed aliases and root project/build workflows.
- Both app flavor lint tasks, mock app/instrumentation APK builds, production compilation and formatting passed.
- Production references to the retired Home component exist only in the required installed alias and compatibility decoder.

The first broad run exposed duplicate list requests when Android resumed old content during an account switch. Guarding the whole navigation host stopped those requests but broke retention during recreation. Content-only lifecycle capping and unchanged decorator lifecycles resolve both regressions; the ten focused retention/account/alias cases and the complete 135-case suite pass with their original request-count assertions.

Build Details remains the only application-owned native activity. Its eight fragment/sheet hosts, native shell and obsolete infrastructure still need removal. Actual process restoration and final all-module/both-flavor verification remain required. The goal stays active; no push or PR was created.


## Final host removal and restoration verification

The native Build Details activity, eight remaining fragment/sheet hosts, pager/presenter/menu infrastructure and event/router bridges have been removed. Its installed component dispatches through a private MainActivity alias. Incoming legacy build payloads are decoded into complete typed destinations at the app boundary; feature ViewModels use plain serialized arguments. Root routers retain browser, sharing, artifact-file and licenses platform integrations. The obsolete utilities module was removed after moving shared theme-color resolution into `libraries/theme`. Data Binding and the unused native UI dependencies were removed from the app.

Production reference auditing finds MainActivity as the sole application-owned activity, no feature fragment hosts and no internal activity-result launches. Native-host integration fixtures now exercise root destinations, Compose tabs, menus/dialogs and full payload assertions, including external platform Intents.

Cancellation carries its stopped/removed completion notice into the replacement destination, retains it across recreation, and does not replay a dismissed notice. The entire re-add row responds to clicks without changing existing layout. Failed queued-build opening offers Retry, successful queueing offers Show, and sharing retains its chooser title. Twenty-four new feedback screenshots cover both themes, compact/expanded layouts and enlarged text; visual review confirmed their text and actions remain readable. All existing baselines remain unchanged.

Saved UI, normalized launch payloads and pending incoming requests carry their own account markers. Launch ownership is checked before decoding; a newly requested account destination can restore while the saved UI still belongs to the previous account. The saved payload wins over older platform task arguments. Pending requests saved before a Compose frame are applied once. A retained account coordinator's completed stage replaces obsolete activation input. Bare launcher Intents preserve the current normalized installed-alias arguments and visit state.

Final verification with JDK 17:

- All 3,514 application, feature and library local cases passed, including 2,432 Roborazzi screenshot cases. All 15 build-logic tests passed.
- `testDebugUnitTest`, `:app:testMockDebugUnitTest`, `lintDebug`, `:app:lintMockDebug`, `:app:lintProdDebug`, `:app:assembleMockDebug`, `:app:assembleMockDebugAndroidTest`, `:app:compileProdDebugKotlin`, `:build-logic:test`, `spotlessApply` and `spotlessCheck` passed. The last root refinement was rebuilt and verified separately against both app lint variants, app unit tests, both APKs and production compilation.
- All 388 API 37 emulator integration cases passed across 50 test classes. Coverage includes startup/authentication, installed aliases, account switching, pending/restored requests, Home and Build Details tabs, repeated visits, typed results, paging, filters, retained owners, cancel/restart feedback, sharing, downloads, WebView disposal, sheets, both themes and encrypted storage.
- Real process restoration was checked outside ActivityScenario: the normal mock app was backgrounded until STOPPED, then killed with `am kill` while its Recents task remained. On the final APK, process 17203 exited and task 7490 resumed in new process 17528 with build #2459, Parameters selected, `sdk=24` and `userName=Murdock`. Back returned to the originating History and then Home. Earlier host-removal verification also restored Settings after process death and retained Settings on a warm installed-launcher relaunch. The final seven-case restoration suite verifies warm alias relaunch and saved launch precedence over older task arguments.
- Production audits and whitespace checks passed: MainActivity is the sole application-owned activity, feature fragment hosts and internal activity-result launches are gone, and features have no implementation-to-implementation dependencies.

All eight phases and completion gates are satisfied. The migration is saved as coherent local GitButler checkpoints on `codex/single-activity-navigation`; no push or pull request was created. Broader Room, network serialization, preference storage and WorkManager migrations remain outside this completed navigation goal.

## Follow-up cleanup and app structure proposal (2026-10-12)

The post-migration audit covers all 28 feature implementations. Typed destination registrations, ViewModels, trackers, live platform routers and their Hilt scopes remain. The ten obsolete feature Activity-navigation interfaces/implementations and their navigation-only bindings are removed: About, Settings, Login, Create Account, Manage Accounts, Projects, Build History, Artifacts, Test Details and Change Details. The unused app Build Details launcher wrapper is also removed. Wrapper-only tests are retired; analytics, destination/result, installed-alias and runtime navigation coverage remain. The only remaining activity-entry test helper lives in `app/src/androidTest/kotlin`, with no production launcher interface/binding. Legacy Intent keys belong to the app dispatcher; implementation saved-state keys belong to their feature ViewModels. Three platform routers require an explicit close callback rather than defaulting to finishing the root activity.

| Audited implementations | Outcome |
| --- | --- |
| About, Settings, Login, Create Account, Manage Accounts, Projects, Build History, Artifacts, Test Details, Change Details | Removed obsolete entry contracts, wrappers/bindings and forwarding-only tests; retained typed registrations and live behavior |
| Home, Build Details | Retained typed shell navigation; replaced centered onboarding dialogs with the shared control-anchored coachmark |
| Agents, Favorites, Running Builds, Build Queue | Retained typed retained-pane routes; drawer controls report their actual bounds to Home onboarding |
| Drawer, Quick Filter, Action Bottom Sheet | Retained live typed sheet destinations/results and UI-scoped actions |
| Build Overview, Changes, Test Occurrences, Build Log, Properties, Snapshot Dependencies | Retained live child destinations, retained state and platform resource contracts |
| Splash, Run Build, Filter Builds | Retained startup/account preparation and origin-targeted workflow/results registrations |

Home's drawer and action FABs, Build Details' menu and Build History's Run/Filter/Favorite buttons use the same shared spotlight/explanation component in `libraries/theme`. Anchors come from real layout coordinates, translated into the overlay host; removed child controls remove their anchors. Explanations appear beside the relevant screen edge, below toolbar controls or above FABs. The overlay blocks underlying actions and accessibility traversal, supports dismissal and retains History's persistence/retry behavior. Existing onboarding preference keys and acknowledgement behavior remain.

The following app structure is a proposal only. No app data/storage/package reorganization has been executed as part of this follow-up.

```text
app/src/main/kotlin/com/github/vase4kin/teamcityapp/
  application/                  # application initialization and app-wide composition
  navigation/                   # MainActivity, root/account flow, incoming alias dispatcher
  integration/
    authentication/             # current auth and account-session compatibility adapters
    features/<feature>/         # adapters still requiring app-owned API/cache/account state
  di/                           # app composition bindings, organized by responsibility
app/src/main/java/               # Java only; keep existing DTO packages until compatibility is verified
features/<feature>/impl/         # feature UI, ViewModel, mapping, tracker, destination/platform router
libraries/data/                 # proposed shared server repositories, network/cache implementations
libraries/session/              # proposed account identity, session/auth/storage contracts
```

The inventory found 15 Kotlin files under `app/src/main/java` and 28 Java files there. Move those Kotlin sources to matching `src/main/kotlin` package paths first, preserving their fully qualified names; no simultaneous package/model rewrite is needed. Keep Kotlin in the Kotlin roots for every variant and test source set as well. The current app Kotlin tree contains composition, compatibility and repository adapters rather than remaining screen UI.

Keep MainActivity, installed-alias decoding, account navigation coordination and app-wide DI composition in `app`. Keep feature behavior and feature-local mapping in the corresponding implementation. Once shared server/session contracts are extracted, adapters such as `AppAgentsRepository`, `AppBuildHistoryRepository` and `AppBuildOverviewRepository` can move into their feature implementations or dedicated shared data implementations without depending on `app`. Do not move them while they still require app-owned `Repository`, `ApiSession`, DTOs or storage.

Shared TeamCity wire DTOs, Retrofit/Gson services, RxCache providers and cache implementations belong in the proposed shared data module, separate from immutable domain models. Preserve serialized class names, field names and cache schemas during extraction; moving a source file between roots/modules does not require changing its package. Shared encrypted account persistence and API-session ownership belong behind the proposed account-session contract. Replace the existing account-scoped Dagger graph with Hilt as a separate data/session migration, then remove its obsolete component/scope classes. A folder rename alone would leave the coupling intact.

Use `di` for future binding packages rather than a mixture of `dagger`, `modules`, `data` and screen-named directories. Keep repository logic out of DI files. Avoid a new catch-all `utils`, `models` or `api` package: name data by its server/account responsibility and keep DTO, domain and UI representations distinct. Extract shared contracts before moving implementations, verify persistence/cache compatibility, then remove app adapters after their final callers migrate.

Follow-up verification:

- All 3,505 local cases passed, including 2,432 screenshot cases. The shared anchor placement/disposal and Home/Build Details interaction tests pass.
- All 389 integration cases were verified on API 37. The broad run passed 388 cases and identified one Overview onboarding fixture waiting for content hidden by the active tooltip. The fixture now waits for the tip; all 31 Overview cases passed in the corrected APK. The new Home onboarding case covers a non-Projects initial tab, real retained-child drawer targets, recreation, all action tips and persisted acknowledgement.
- Mock app/instrumentation APK builds, production-debug Kotlin compilation, all-library lint, both app flavor lint tasks and formatting passed. The corrected instrumentation fixture was rebuilt and its formatting checked separately.
- Exactly 48 existing onboarding PNGs were updated after comparison and visual review: 30 Home and 18 Build Details. All 168 History screenshot cases and all non-onboarding baselines remain unchanged. Both themes, compact/expanded layouts and enlarged text are covered.
- The follow-up is saved in separate local cleanup, onboarding and documentation checkpoints on `codex/single-activity-navigation`. No push or pull request was created. App data/storage/package moves remain a proposal.


## App structure extraction and single sheet handle (2026-10-12)

The structure proposal above is now implemented with the approved names `libraries/data` and `libraries/session`. The extraction proceeded in independently compiled stages. Source-root moves preserved file contents; module/package changes preserve wire DTO class names, cache payloads, encrypted preference keys and account-session behavior.

```text
app/src/main/kotlin/com/github/vase4kin/teamcityapp/
  TeamCityApplication*.kt        # installed application names and composition
  navigation/                   # MainActivity, destination/account flow and aliases
  di/
    components/, modules/, scopes/ # existing account graph and root bindings
    authentication/, navigation/, features/ # focused composition bindings
  integration/
    authentication/, session/   # app/platform-dependent account adapters
    filters/, features/         # adapters coupled to the app filter-event boundary
    platform/artifacts/         # app-owned file/platform launch integration
app/src/main/java/               # Java only: AppModule
features/<feature>/impl/.../data/ # independent feature repositories and mappings
libraries/data/                  # existing Retrofit/Gson, RxCache, wire DTOs and build helpers
libraries/session/               # encrypted account storage, identity, session ports and lazy API session
```

All 15 Kotlin files formerly under `app/src/main/java` now use the Kotlin root. The remaining 13 app variant/test/R8 Kotlin sources also use their matching Kotlin roots; Java roots contain Java only. Java wire/storage sources moved into their owning libraries with their original packages. `Collectible` belongs to the existing `libraries/api` contract, avoiding a data/session dependency cycle. `storage-models` exposes its existing public API dependency. The application and MainActivity framework names and installed aliases remain unchanged.

Independent repositories/result adapters now belong to Navigation, Favorites, Changes, Tests, History, Overview, Snapshot Dependencies, Artifacts, Run Build, Build Details, Manage Accounts, Drawer, Splash, Build Log and Filter Builds. Shared build hydration/launch mapping/legacy decoding belong to `data`. Home, Agents, Running Builds, Queue and Quick Filter adapters remain under app integration because their filter-event coordination still belongs to the app. Authentication and artifact platform adapters remain app integrations for the same ownership reason. Feature implementations have no implementation-to-implementation dependencies.

`session` owns the existing encrypted storage classes and account identity guard. Its generic `ApiSession` receives an opaque graph factory; the concrete Dagger component, factories, scopes and account-flow preparation remain in app DI. This preserves lazy initialization, credential-sensitive rebuilds, cancellation guards and clear behavior without making session depend on the app. Replacing that concrete graph with Hilt, migrating wire serialization and replacing legacy persistence remain separate work.

Existing repository/session tests moved with their owners where internal access requires it; app boundary integration tests continue to exercise public adapters. New data tests lock persisted DTO class names, Gson wire fields and the legacy serializable filter round trip. No dependency versions or serialized model schemas changed.

Drawer content and the shared Action/Quick Filter header no longer draw a handle. Navigation 3's Material sheet host supplies the single native handle. The root sheet integration suite asserts exactly one accessible drag handle for every hosted sheet. Exactly 216 current Drawer, Action and Quick Filter screenshot baselines were compared, visually reviewed and recorded across their existing state/theme/layout/font coverage.


Verification with JDK 17:

- All 3,507 local cases passed, including 2,432 Roborazzi screenshot cases and the new wire/filter compatibility guards. Data has 13 local cases; Session has seven.
- All 389 API 37 emulator integration cases passed in one complete run. All eight root sheet cases pass the single-native-handle assertion, alongside account switching/restoration, downloads, WebView lifecycle, encrypted storage and existing feature workflows.
- `testDebugUnitTest`, `:app:testMockDebugUnitTest`, `lintDebug`, `:app:lintMockDebug`, `:app:lintProdDebug`, `:app:assembleMockDebug`, `:app:assembleMockDebugAndroidTest`, `:app:compileProdDebugKotlin`, `spotlessApply` and `spotlessCheck` passed.
- The final 13 variant/test source-root moves were independently rebuilt against both debug flavors, app tests, lint and formatting. `:app:compileProdR8VerificationKotlin` and `:app:compileProdR8VerificationAndroidTestKotlin -Pr8Verification` also passed; no R8 smoke-check code changed.
- Source/dependency audits confirm unchanged wire/storage packages, no app dependencies from the new libraries and no feature implementation dependencies on other feature implementations.
- Local checkpoints use the existing `codex/single-activity-navigation` branch. No push or pull request is part of this follow-up.

## Build Details Overview hub (October 2026)

Build Details now uses the approved Floating actions layout: status with View log,
Results (Changes, Tests, Artifacts), Execution (branch, agent, trigger and optional
execution details), Build setup (Parameters, Dependencies), and a separate
Configuration/project group. Share and Open in browser sit in a floating toolbar
paired with an extended Stop/Remove/Restart action. These controls overlay the
scrollable content with no full-width bottom surface. Measured trailing scroll
space keeps the final row reachable when controls wrap at enlarged text sizes.
Build-action education uses History’s shared coachmark, anchored separately to
Stop/Remove/Restart, Share and Open in browser. Existing mutation-tip preferences
remain compatible; Share and Open in browser have independent DataStore preferences.
Acknowledgement failures keep the same tip visible with an explicit retry.

Grouped rows push a `BuildDetailsSectionDestination`; Back returns to Overview.
Duplicate section visits have distinct owners. The Overview retains completed
loads and scroll position while another visit is visible. Counts on Overview and
standalone Changes/Tests screens have independently retryable owners. Installed
Build Details aliases and serialized build DTO/cache contracts remain compatible.

The `build-log` modules and obsolete WebView/cookie/consent adapters are removed.
`BuildDetailsLogLink` builds the active server URL using an encoded build ID and
`guest=1` for guest accounts. Custom Tabs receives no application credentials.

Verification covers module ViewModels/mappers, section serialization and complete
arguments, grouped-row and floating-action callbacks, count retries, screenshots
in both themes and three layout/font variants, app unit tests and mock APKs, and
emulator cases for recreation, repeated visits, artifact results and Custom Tabs.

Validation: 722 local Android cases passed, including 372 screenshot cases;
46 CI policy tests passed; all 98 affected emulator cases passed on the final APK.
Mock app/test APKs, production Kotlin compilation, mock-debug lint and formatting
checks passed. Lint ran separately after kapt compilation to avoid its generated
stub analysis race.


## Shared composition-root ownership audit

The portable root registers destinations, constructs dependencies, owns account
and journey restoration, retains child stacks, and delivers results to exact live
originating visits. Feature implementations own their `Shared*Route` containers:

- Login owns SSL/demo dialogs and demo-server policy; About owns action links.
- Home owns responsive presentation and selection callbacks; Projects owns rating
  presentation. Favorites, Running, Queue, Agents and Snapshot Dependencies own
  state collection and screen lifecycle behavior.
- History owns Paging collection, refresh claims, append states and notices.
  Changes/Tests keep their existing feature containers; Change Details owns diff
  links and view/action telemetry; Test Details owns search/wrap/match state.
- Build Details owns loaded-build education, confirmations, notices, management
  command dispatch and replacement presentation. Overview owns row actions,
  browser/share/log policy and reference-loading presentation. The root composes
  them through callbacks and a content slot, without cross-feature implementation
  dependencies. Section count/result delivery remains in the root.
- Artifacts owns download lifetime, action-sheet consumption policy, one-shot
  export claims and retries. The root supplies typed-result access, native ports
  and whether the visit is covered by a sheet or has been left.
- Settings owns state collection and view telemetry. Drawer and Manage Accounts
  own their public-account mappings over the session-manager contract; account
  generation changes and root replacement remain application orchestration.

Reusable TeamCity web-link validation lives in `libraries/network`. Native theme
preferences, artifact file writes and response-cache bytes live in
`libraries/theme-preferences`, `libraries/shared-data` and `libraries/network`.
The host still owns UIKit presentation and app/scene callbacks. Preference keys,
file locations, serialized navigation and installed compatibility routes remain
unchanged. Feature tests move with their behavior; cross-feature account and
navigation integration coverage stays in `shared-app`.

The boundary check rejects feature resources, feature UI-state/paging code and
screen lifecycle hooks in common production root sources, in addition to the
existing tracker/SDK and implementation-dependency checks.

Verification: final Android host reports contain 3,862 cases with zero failures
and one existing skip; 74 app unit tests, 474 iOS simulator tests and 103 repository
script tests pass. Root, Login and Artifacts Roborazzi verification passes with
unchanged golden bytes. Production/mock debug APKs and the mock instrumentation
APK build, and all 53 selected account/login/navigation/rating/test-details device
integration cases pass. The reserved-character diff-link regression found during
the move is fixed and covered in Change Details on both platforms.
