# TeamCityApp Agent Guide

Use this file as the working contract for AI coding agents in this repository.
The target architecture follows the modern approach described in the Now in
Android agent guide. Use it by default for every new feature and whenever
refactoring existing UI, state, data, dependency injection, or models. The current
codebase has not completed that migration. Check nearby code and module build
files before editing, preserve unrelated local work, and keep each change scoped
to the requested behavior and the coherent migration slice needed to support it.

## Project snapshot

- TeamCityApp is a native Android client for JetBrains TeamCity.
- The app uses one application-owned `MainActivity`, Navigation 3, Compose,
  Material 3, Hilt ViewModels, and lifecycle-aware StateFlow collection. Installed
  activity names are aliases dispatched by the root; browser, sharing and Google's
  OSS licenses activities remain platform integrations. Java and Kotlin coexist.
- The Compose features and the Home/Build Details shells use `api`/`impl`
  modules. APIs expose serializable destination keys, small contracts and immutable
  public models. Implementations register existing route composables through Hilt,
  own their UI, ViewModels, trackers and destination/platform routers, and have no
  implementation-to-implementation feature dependencies. Kotlin sources and tests
  use the corresponding `kotlin` roots.
- `libraries/navigation` owns unique visit IDs, saved typed stacks and results,
  destination ViewModel/saveable-state ownership, retained child panes and Compose
  sheets. Results target a live originating visit and are consumed once. Popping,
  replacing or resetting a visit clears its owners and pending results.
  Home has Projects, Favorites and Activity navigation destinations. Its five
  content panes retain independent child owners through selection and recreation;
  Activity groups Running, Queue and Agents and remembers its last selection.
  Build Details retains its Overview and opens grouped sections as
  separate visits with their own content owners. Hidden content cancels unfinished
  loads without discarding completed content or clearing retained ViewModels.
- The app composition root owns installed Intent compatibility, account-flow
  coordination and active-account adapters. Saved navigation and launch arguments
  are validated against account identity before restoring content. Switching users,
  including users on the same server, replaces old owners and cancels their work.
  Account preparation owns API/cache initialization; signed-out sessions return to Login.
- `libraries/data` owns existing wire DTOs, Retrofit/Gson service/repository contracts,
  RxJava/RxCache integration and shared build mapping. `libraries/session` owns encrypted
  account storage, account identity/guards and opaque API-graph lifetime/clear contracts.
  Independent suspend/Flow adapters belong to their feature implementations; app-dependent
  filter/auth/platform bridges remain in `app/integration`. The concrete Dagger account
  graph and Hilt composition remain in `app/di` until the separate graph migration.
  Preserve wire DTOs and their serialized class names for cache compatibility.
  Cancelling coroutine repository calls must dispose their Rx subscriptions.
  Room, broader DataStore migration and WorkManager remain target technologies.
- About maps the legacy server DTO into its plain API model. Settings uses the
  shared `libraries/app-theme` DataStore contract, migrating the old theme preference
  without changing other preferences. Google's licenses screen remains external.
- Login and Create Account share stateless controls and authentication analytics
  in `libraries/authentication`; server URL formatting belongs to `libraries/remote`.
  App adapters preserve challenge-based Basic authentication, explicit HTTP/SSL
  consent, encrypted persistence, duplicate detection and API initialization.
  Pending submissions survive recreation without duplicate requests; passwords
  stay in memory and are cleared after successful persistence.
- Sheet content must not draw its own handle; the Navigation 3 Material sheet host owns
  the single built-in drag handle. Home uses an account avatar and a full-screen
  Account destination, preserving account switching and existing account/app actions.
  Filters are inline actions; only Quick Filter and Action Bottom Sheet use Compose sheets. Run Build and
  Filter Builds deliver typed results to History's originating visit. Legacy filter
  serialization remains behind app adapters to preserve existing request semantics.
- Build Details is an Overview hub with a prominent status card, Results,
  Execution, Build setup, and Configuration/project groups. Available section
  destinations use the complete incoming launch snapshot; floating Share/Browser
  actions and Stop/Remove/Restart use loaded build data and retain confirmation,
  retry and account isolation behavior. Changes, Tests, Parameters, Artifacts and
  Snapshot Dependencies open separate saved visits with their own screen headers.
  Artifact folders/sheets return results to a live section visit. View Log opens
  the active server's build-log URL in a Chrome Custom Tab, preserving guest URLs
  without exposing stored credentials. Browser sign-in and cookies belong to the
  browser; the embedded-log modules, WebView and consent preference are retired.
  Queued-build Show actions navigate immediately to a reference-loading visit; only a
  resolved complete snapshot enters the normal details host and section destinations.
  Downloads retain ViewModel state through recreation, but cancel on screen departure
  or app backgrounding, closing blocked reads and removing temporary files without
  overwriting completed files. Their inline card explains that screen-owned lifetime.
- Lists share immutable models and rows in `libraries/build-configurations`,
  `libraries/builds` and `libraries/build-ui`, plus `libraries/list-ui` and
  `libraries/list-state`. History, Changes and Tests use Paging with opaque TeamCity
  continuation URLs and ViewModel-scoped retention. Optional counts are independently
  retryable; Tests retains its status filter. Favorites preserves saved IDs during
  partial/all-failed loads. Home lists have explicit account/query/visibility gates.
- Superseded feature activities/fragments, native Home/Build Details shells,
  presenters, pager/FragNav infrastructure and the old `libraries/utils` module
  have been removed. Shared theme-color resolution belongs to `libraries/theme`.
  See `docs/single-activity-navigation-migration.md` for the navigation contract,
  compatibility inventory and verification; `docs/compose-list-migration.md` records
  the earlier list migration.
- Feature implementations own behavioral tests and Roborazzi PNG baselines for
  distinct states in both themes, compact/expanded layouts and enlarged text.
  App-level Compose/Espresso integration tests remain in the Marathon APK.
- Builds use Kotlin DSL Gradle files, a version catalog, type-safe project accessors,
  an included `build-logic` build for conventions and SDK/application settings,
  and JDK 17.
- The app has `mock` and `prod` flavors, and `debug` and `release` build types.
  `mockRelease` is disabled.

## Repository layout

- `app/`: application/framework entry points, root navigation/account composition,
  `di` bindings and app-dependent authentication/filter/platform `integration` adapters.
- `libraries/data/`: network DTOs/contracts/cache integration and shared build mapping;
  preserve serialized legacy packages even when source ownership moves.
- `libraries/session/`: encrypted account storage, session identity and graph ownership
  contracts; it must not depend on the app or a concrete Dagger component.
- `features/`: feature `api`/`impl` modules. APIs own small contracts and plain
  public models; implementations own Compose UI, ViewModels, UI-scoped routers,
  private resources, behavioral tests, and screenshot baselines.
- `libraries/`: shared API, storage, models, resources, theme, networking helpers,
  security, utilities, and other reusable components.
- `build-logic/src/main/kotlin/Config.kt`: SDK/JVM settings and application version.
- `build-logic/`: included Kotlin build containing Android application/library,
  Java-only library, Hilt/KSP, Compose, and aggregate coverage conventions.
  All Android modules apply these conventions; `buildSrc` has been retired.
- `gradle/libs.versions.toml`: dependency and plugin versions/coordinates.
- `settings.gradle.kts`: included modules, plugin resolution, and dependency
  repositories. Root and module `build.gradle.kts` files use Kotlin DSL.
- `.github/workflows/build.yml`: build, lint, unit test, instrumentation, and
  coverage CI. Keep GitHub Actions as the CI platform. `scripts/` contains R8
  verification preparation/documentation.

## Target architecture

### Kotlin Multiplatform migration

- Follow `docs/kmp-migration.md` as the migration checklist and phase contract.
  Start only the phase the user authorizes. Each phase has its own goal, verified
  completion gates, checkpoint commits, actual UI evidence, and a detailed private
  Sites report linked from the checklist. Do not mark a phase complete while a
  required check or report remains unfinished. Starting the next phase requires
  a new user instruction.
- See `docs/ios-release.md` for candidate archive, native notice inventory,
  privacy/icon/version audits and signing/TestFlight preparation. Optimized Xcode
  framework builds use the release-scoped heap in `scripts/build-ios-framework.sh`.
  Keep physical-device/manual VoiceOver qualification distinct from simulator
  results; an unsigned archive does not authorize upload or distribution.
- Shared modules use `teamcityapp.kmp.library` or `teamcityapp.kmp.compose`, the
  Android KMP library plugin, and `src/commonMain`, `src/commonTest`,
  `src/androidMain`, and `src/iosMain` Kotlin roots. Keep flavors in Android hosts.
  The current scope targets Android API 24+ and iOS 17+ on Apple Silicon simulators and arm64
  devices, including compact and expanded layouts.
- Shared UI uses Compose Multiplatform resources and typed `SharedUiText`;
  Android resource IDs and the existing `UiText` remain in unmigrated Android
  code. Share portable theme definitions through `libraries/design-system`.
- Feature APIs expose portable models and repository contracts in common code;
  Android destination registrations may remain in `androidMain` during migration.
  About, Login, Navigation, Build History, Build Overview, Home, Favorites,
  Running Builds, Queue, Agents, Changes, Change Details, Tests, Test Details,
  Parameters, Snapshot Dependencies, Run Build, Filter Builds, Quick Filter,
  Action Bottom Sheet, Build Details, Artifacts and Settings `presentation` modules own their common UI
  and ViewModels. Their existing `impl` modules are Android Hilt/router adapters
  and Roborazzi harnesses. This split is within one feature; cross-feature implementation dependencies remain
  prohibited. Do not duplicate shared layouts in platform adapters.
- Shared ViewModels use plain constructor injection and lifecycle-owned factories.
  Hilt remains the Android composition mechanism, with thin Android ViewModels
  when Hilt ownership is required. Common code cannot depend on Hilt, Java
  reflection, Android Context/Bundle, Retrofit/RxJava, or platform storage.
  `libraries/network` uses Ktor and Kotlin serialization, and `shared-data`
  supplies the first journey's account-scoped repositories. `libraries/accounts`
  owns portable identity, secure-storage contracts and generation-scoped lifetime;
  iOS uses Keychain and Android adapts the installed encrypted account snapshot.
  Preserve Android account/cache formats
  and existing authentication/cancellation semantics behind adapters.
- Run common behavioral tests on Android and the iOS simulator, retaining Android
  Roborazzi coverage of shared composables. Inspect actual before/after/comparison
  images before changing goldens. iOS begins with an explicitly labeled first
  baseline. Keep generated screenshots and build logs under `build/`; commit only
  intentional golden fixtures and reproducible source/configuration.
- The normal iOS application runs Login → Home with Projects, Favorites and
  Activity (Running, Queue, Agents), plus History → Build Overview and read-only
  Changes/Change Details, Tests/Test Details, Parameters and Snapshot Dependencies.
  The deterministic About preview remains an explicit regression-test launch mode.
  Home uses retained independent stacks, account-scoped persisted favorites/filters,
  and validated saved public navigation. Native History supports Run/Filter and
  typed live-origin results; the shared Build Details shell supports confirmed
  Stop/Remove/Restart and queued-reference loading. Effective History locators
  survive public navigation restoration without replacing the live visit owner.
  Native artifacts support folders, archive actions and byte-progress downloads.
  Downloads belong to the screen, survive recreation/sheets, and cancel on departure,
  app/scene backgrounding or account replacement; close blocked reads and remove
  partial files. Native completed files use unique cache directories and export
  through the system activity sheet. Android keeps its installed file provider,
  permission adapters and successful replacement policy.
  Native browser/log/diff, sharing and clipboard actions stay in UI-owned adapters;
  API credentials never enter browser links. Settings shares presentation over the
  portable theme-preference contract, retaining Android DataStore and native
  UserDefaults. About links native account actions, Apple rating requests and bundled
  runtime-license texts. Native diagnostics use fixed OS event labels and Apple's
  crash reports; hosted iOS analytics/crash upload is unconfigured.
  Native interactive list rows keep clipped fragments below the standard touch
  target out of VoiceOver until scrolling reveals them. Preserve each action and
  label after reveal; keep Android semantics unchanged and retain strict audits.
  Capability-gate unavailable actions. Keep the native root responsible for top/horizontal safe areas. Return
  section counts once to their exact live originating build visit. Android retains
  its Retrofit/RxCache/authentication compatibility adapters behind shared feature
  contracts; do not claim that its complete data stack has been replaced.
- KMP UI harnesses can apply `teamcityapp.android.screenshot`; verify with
  `:shared-app:verifyRoborazziAndroidHostTest`. Shared native account tests and
  signed XCTest UI runs must not concurrently control the same simulator.
- Management transport writes are account-generation scoped and use the same
  origin/context/authentication guards as reads. Never automatically retry an
  uncertain mutation. Only an explicit initial Basic challenge may replay it.
  Keep technical parameter editing text/selection/composition in UI-owned
  TextFieldState and read final field values at confirmation.
  Verify management writes against `scripts/kmp-platform-fixture.py
  --enable-mutations` and its local `/context/manage` contexts; public TeamCity
  verification remains read-only. Keep fixture writes disabled by default.

The following Android architecture rules continue to apply to unmigrated modules;
the scoped shared-code rules above apply to migrated KMP modules.

- New features and substantive refactors must use this architecture whenever
  feasible. Existing legacy patterns are compatibility constraints, not the
  default for new code. If a concrete constraint prevents adoption, explain it
  and keep the legacy dependency behind a small, explicit adapter.
- UI: use Jetpack Compose, Material 3, shared design-system components, and
  adaptive layouts. Use window size classes for window-level navigation/pane
  decisions and local layout constraints for responsive components. The app uses one
  activity with Navigation 3. Register new destinations through feature APIs and
  implementations; preserve required installed component aliases in the app dispatcher.
- State: use Jetpack ViewModels, Coroutines, and `Flow`/`StateFlow` with
  unidirectional data flow. State-changing and business events go to ViewModels;
  immutable UI state flows back to the UI. UI-only interactions, navigation
  execution, and platform launches belong in the UI/navigation layer. When
  business state determines navigation, expose that state from the ViewModel
  and execute navigation in the UI. Model applicable loading, empty, error, and
  content states explicitly, including failures of optional content sections.
  Keep Activities, Fragments, views, adapters, and UI callbacks out of ViewModels.
- Follow the Now in Android Bookmarks pattern for migrated screens: a route/container
  composable obtains its Hilt ViewModel with `hiltViewModel()`, collects state with
  `collectAsStateWithLifecycle()`, and passes immutable state and callbacks to a
  separate stateless screen/content composable. Navigation 3 destination registrations host the route.
- Select state-dependent messages, action labels, status names, empty-state text,
  and other text alternatives in ViewModel-produced immutable UI models. Expose
  feature implementation resource IDs (`@StringRes`) or unresolved `UiText`
  descriptors from `libraries/theme`; resolve them with `stringResource`/`UiText.resolve`
  in the current composition. Keep feature resources out of domain models and
  feature APIs. Do not inject `Context`, `Resources`, or localized-string providers
  into ViewModels. Map paged row text before rendering, preserving complete domain
  payloads for navigation and keeping Paging item access lazy.
  Static screen copy and layout/theme choices remain in composables. Text driven
  solely by local UI state (for example expansion, SSL consent dialogs, or platform
  launch failures) belongs to that UI state rather than a retained ViewModel.
- Derive UI state from repository flows with operators such as `map`, `onStart`,
  `catch`, and `stateIn(viewModelScope, SharingStarted.WhileSubscribed(...), ...)`.
  Prefer collection-driven loading over Activity calls to ViewModel `start`/`stop`.
  Choose the subscription timeout and completed-state retention deliberately;
  test cancellation, resubscription, and configuration changes. Cancellation must
  not become an error state. Adapt one-shot suspend repositories with a cold flow
  when appropriate; do not invent a continuously observed data source.
- Own feature-specific outgoing actions in a `router` package with an injectable
  interface and UI-scoped implementation. Keep Activity-bound routers out of
  retained ViewModels. Shared platform behavior used by multiple features belongs
  in a named shared module with an explicit contract, such as `libraries/app-rating`,
  rather than a miscellaneous utility extension or another feature implementation.
- Data: expose suspend functions and flows through repositories. UI and ViewModels
  must not call Retrofit, Room, DataStore, or other storage APIs directly. Put
  shared or nontrivial cross-repository operations in use cases. Existing Rx APIs
  and caches may be reused behind coroutine adapters with cancellation support.
- DI: use Hilt for new and refactored dependency-injection infrastructure across
  the app, features, data, storage, background work, and tests that need injection.
  Use `@HiltViewModel` for ViewModels. Inject dispatchers, repositories, services,
  DAOs, and workers rather than constructing dependencies in consumers. Use the
  application-wide `@IoDispatcher`, `@MainDispatcher`, and `@DefaultDispatcher`
  bindings from `libraries/coroutines`; do not create feature-specific dispatcher
  wrappers or repeated `Dispatchers.*` providers. Keep DI modules focused on
  bindings/providers, with repository behavior in repository implementation files.
- Models: use immutable Kotlin domain models and UI state. Keep network DTOs,
  persistence entities, domain models, and UI-specific models distinct at layer
  boundaries, with explicit mappings. Keep domain models independent of Android,
  network serialization, and persistence implementation details.
- Network: use Retrofit/OkHttp and Kotlin serialization for new or migrated network
  contracts. Keep existing Retrofit/Gson services behind repository adapters until
  their owning contracts are migrated; preserve server and authentication behavior.
- Storage and background work: use Room for new structured local storage,
  DataStore for new preferences, and WorkManager for deferrable background work.
  When replacing existing storage, preserve persisted data, provide migrations,
  and test schema compatibility. Preserve protobuf field numbering if proto
  DataStore is introduced.
- Use feature `api`/`impl` modules for new or substantially reworked features,
  following [Now in Android Bookmarks](https://github.com/android/nowinandroid/tree/main/feature/bookmarks).
  API modules expose small entry-point/repository contracts and plain public models.
  Implementation modules own UI, ViewModels, routers, bindings, and private feature
  strings/drawables. Put resources shared across features in a shared resources
  module; app-level UI tests do not make a resource part of a feature's public API.
  Other features depend on APIs; the app composition root may include
  implementations to wire the app. Avoid implementation-to-implementation feature
  dependencies. Keep untouched legacy layouts until their owning slice is migrated.
- Put Kotlin sources in `src/main/kotlin`, local tests in `src/test/kotlin`, and
  instrumentation tests in `src/androidTest/kotlin`, with packages reflecting
  feature `api` or `impl` ownership. Reserve `src/*/java` for Java. Move affected
  Kotlin sources during feature migration, preserving copyright/license headers;
  avoid unrelated repository-wide source moves.

## Working in the current codebase

- Respect existing module boundaries. Put feature behavior in its owning module
  when practical; keep shared interfaces and models in the appropriate library or
  feature API module. Legacy `models`/`repository` modules may remain in untouched
  features. Follow nearby naming and organization while
  applying the target architecture to new or refactored code. Avoid new
  feature-to-feature coupling.
- Keep network and persistence access behind existing repository, data manager,
  or storage interfaces while old implementations remain. Do not add direct
  Retrofit or SharedPreferences access to UI code.
- Screens use Compose and ViewModels; do not add native hosts, presenters or
  callback-holding state holders. Existing RxJava, cache and encrypted account
  implementations remain behind explicit repository adapters. Keep those boundaries
  working until their owning data/storage contracts are migrated.
- Destination and platform routers are UI-scoped. Obtain retained state with
  `@HiltViewModel` and `hiltViewModel()` inside registered entries; keep Activities,
  Fragments, adapters and callbacks out of ViewModels. Use the root destination
  owner and retained child-pane contracts rather than creating native screen hosts.
- The app's `Repository` may extend migrated feature repository API contracts.
  Its production and mock implementations own their corresponding suspend/Flow
  behavior, including Rx bridges and DTO mappings when needed. Expose these
  interfaces through small DI providers rather than putting adapter classes in
  DI module files. Preserve caching and cancellation semantics.
- API dependencies come from `ApiSession`, which initializes lazily and rebuilds
  on account/authentication changes. Clear the session when logging out. Access
  account storage through Hilt rather than through the account API graph.
- Match resource acquisition and cleanup to visible composition and destination
  ownership. Reinitialize AndroidView resources when revisiting their content and
  dispose pending callbacks and platform resources when that content leaves.
- Keep UI rendering separate from data loading and business operations even
  before a screen is moved to Compose.
- Follow local Kotlin/Java naming and formatting conventions. Prefer immutable
  models and small public APIs when adding or changing contracts.
- Preserve existing copyright notices and license headers when editing or
  replacing files. Carry them forward when migrating code into replacement files.
- Add a copyright notice and the repository's standard Apache 2.0 license header
  to every new file, using the appropriate format for that file type. Use the
  current year and the project copyright holder, Andrey Tolpeev; preserve original
  notices when adapting existing code.
- Use Material 3 and shared design-system components for migrated UI. Add
  previews for reusable components. Use Android string/plural resources for
  user-visible text.
- Do not migrate unrelated screens during a narrow bug fix. For refactors and new
  features, modernize the affected vertical slice whenever feasible, including its
  tests, and remove the superseded implementation once no callers need it. A small
  fix in untouched legacy code may retain its implementation when migration would
  expand the task substantially; report that constraint rather than treating the
  legacy approach as the preferred architecture.

## Build and dependencies

- The build uses Kotlin DSL, the version catalog at `gradle/libs.versions.toml`,
  type-safe project accessors, and convention plugins in `build-logic`. Modules
  apply `teamcityapp.android.application`, `teamcityapp.android.library`, or
  `teamcityapp.android.library.java`, plus optional Hilt/KSP
  conventions. Compose modules also apply `teamcityapp.android.compose`.
  The app applies the aggregate coverage convention. Keep shared
  configuration in these plugins rather than root cross-project callbacks.
  See `build-logic/README.md`.
- Keep dependency/plugin versions in the catalog; keep SDK/application settings
  in `Config.kt`. Do not scatter versions through module build files.
- Android modules use KSP for Hilt/Dagger processing of Kotlin and Java
  sources. Do not also register these processors with kapt or `annotationProcessor`.
- Add modules to `settings.gradle.kts`, give Android modules a namespace, and declare
  only the dependencies they need.
- Use Kotlin DSL for build changes. Keep feature dependencies, namespaces,
  flavors, signing, and packaging in the owning module. Declare repositories
  in settings and plugin versions in the catalog. Update affected modules and
  verification commands together; keep convention plugins reusable.
- Use AGP's built-in Kotlin and new Android DSL. Hilt/Dagger processing uses
  `com.google.devtools.ksp` through the shared Hilt convention. Kotlin's JVM target
  follows the shared Java target; Java/resource-only conventions disable Kotlin.
  Configure additional Kotlin roots through `android.sourceSets` or variant sources.
- Use JDK 17. Be explicit about the variant when running app tasks.

## Testing and verification

- Add or update focused tests in the same module as the changed behavior where
  practical. Local tests use JUnit with fakes or Mockito where appropriate.
  Migrated UI uses Compose tests; legacy UI uses Espresso.
- For new or refactored code, test ViewModels, repositories, use cases, mappers,
  and workers with focused local tests. Use `kotlinx.coroutines.test` for coroutine
  behavior and Turbine where Flow assertions warrant it. Prefer test doubles and
  assertions that match the behavior under test and nearby test conventions.
- Use Compose testing APIs for new or migrated Compose UI, with a
  `ComponentActivity` host when appropriate. Robolectric tests are welcome when
  Android framework behavior or local Compose UI coverage warrants them. Add the
  necessary dependencies and module configuration as part of the feature or
  refactor; a separate testing migration request is not required.
- Add Roborazzi golden tests for every new or migrated Compose screen and every
  distinct UI state, including loading, empty, error, content, and optional-section
  failures where applicable. Render the stateless screen with deterministic fixtures;
  keep ViewModel/Flow and interaction tests as separate behavioral coverage.
- Apply `teamcityapp.android.screenshot` in the owning Compose UI module. Use native
  Robolectric graphics with a pinned SDK, locale, density, font scale, and animation
  frame. Cover light/dark themes, compact/expanded layouts, and enlarged text; capture
  scrolled content when actions would otherwise be outside the screenshot.
- Commit reviewed PNG baselines under the module's `src/test/screenshots/<variant>`.
  These are intentional test fixtures, not disposable build output. New states or
  meaningful UI changes must update the tests and relevant goldens together.
  Never re-record images solely to make an unexpected regression pass: inspect
  the original, actual, and comparison images first.
- Normal unit tests verify goldens by default (`roborazzi.test.verify=true`). CI also
  runs `verifyRoborazziDebug` and uploads reports/comparisons. For About, use
  `./gradlew :features:about:impl:verifyRoborazziDebug`; deliberately refresh with
  `./gradlew :features:about:impl:recordRoborazziDebug --tests '*AboutScreenScreenshotTest'`,
  review the PNG changes, then verify again. Use the corresponding variant task
  for flavored modules. Keep comparison images and HTML reports under `build/`.
- Retain Espresso coverage for platform views/WebViews and the existing app-level
  Compose/instrumentation suite for end-to-end behavior. Verify lifecycle cancellation,
  state retention, and old/new integration boundaries when those behaviors change.
- Android API 24 instrumentation uses `-PminimumAndroidInstrumentation` for its
  compatible subclass mock engine; default CI/JVM Mockito remains unchanged.
  See `docs/kmp-phase-6.md` for verified scope and commands. Older display captures
  must wait for exact rendered theme colors and distinguish tooltip/window roots;
  navigation-bar light appearance is only supported from API 26.
- Run the narrowest relevant test first, then broader checks for changes that
  cross module boundaries. Report checks that cannot run and why.
- Prepare debug Google Services files as CI does before local app builds if they
  are absent:
  `cp mock-mockDebug-google-services.json app/src/mock/debug/google-services.json`
  and `cp mock-prodDebug-google-services.json app/src/prod/debug/google-services.json`.
- Convention-plugin verification: `./gradlew :build-logic:test` (JDK 17).
- Formatting: run `./gradlew spotlessApply` locally, review the diff, and verify
  with `./gradlew spotlessCheck`. Local commands cover uncommitted files; use
  `-PspotlessBase=<branch-or-commit>` to include committed changes. CI supplies each
  PR's base SHA and checks changed files across every module and `build-logic`.
  Formatter versions live in the version catalog;
  shared editor settings live in `.editorconfig`. Generated output is excluded.
  See [the formatting guide](docs/formatting.md) for local and CI usage.
- Common tasks: `./gradlew assembleMockDebug`,
  `./gradlew :app:testMockDebugUnitTest`, `./gradlew testMockDebugUnitTest`,
  and `./gradlew lintMockDebug`. CI also runs `testDebugUnitTest` for modules
  without flavors and builds `assembleMockDebugAndroidTest`.
- Instrumentation tests run in CI through Marathon Cloud. Do not assume that
  Gradle-managed devices are configured here; verify available tasks
  before running them. Robolectric/Roborazzi are configured by the opt-in screenshot
  convention described above. Add managed devices or formatting tooling only as
  part of an explicit test/build migration.
- Do not commit generated APKs, reports, build output, `.gradle`, or IDE metadata.

## Version control

- Use GitButler (`but`) for version-control writes: commits, branches, pushes,
  merges, history edits, and pull requests, except for explicitly authorized PR
  merges as described below. Read-only Git commands are fine.
- When the user explicitly asks to merge a PR, `gh pr merge` is allowed without
  additional confirmation or first attempting `but pr auto-merge`. Follow the
  requested CI requirements and repository merge rules.
  This exception applies only to merging the authorized PR; keep using GitButler
  for other version-control writes, including PR creation and stack management.
- Implement and verify before committing. Commit at a working checkpoint on the
  current session's GitButler branch, or create a dedicated `codex/` branch at
  commit time. Use a Conventional Commit message.
- Commit only task-related changes. Select relevant files or hunks when other
  work is present. Keep behavior and its tests in one commit; separate coherent
  generated or docs-only changes when appropriate.
- Do not rewrite pushed, reviewed, shared, or ambiguous history without asking.
  Create a GitButler oplog snapshot before reorganizing multiple commits or branches.
- Do not push or open a pull request unless requested. When asked to ship, use
  `but pr new` for a new draft PR or update the existing branch/PR with GitButler.

### Commit & PR Titles

Use **Conventional Commits** for every commit message and PR title. Release Please
uses these messages to decide whether an application release is needed:

```css
<type>[optional scope][!]: <description>
```

Use the same format for commit messages and PR titles. Only these commit types are allowed:

- `feat` — a releasable addition or intentional change to the deployed
  application, UI, runtime behavior, or application release delivery.
- `fix` — a releasable correction to the deployed application, UI, runtime
  behavior, or application release delivery.
- `chore` — work that does not affect the shipped application or application
  release behavior, including documentation, tests, CI validation, build tooling,
  and repository housekeeping.

Use the optional scope to describe the affected area or work category. Labels
such as `docs`, `ci`, `style`, `test`, `build`, `refactor`, and `perf` are
scopes, not commit types. Choose the type from the effect on the shipped app
and its release behavior, not the file location or the need to rerun a workflow.
Use `feat` or `fix` when the change should create an application release.

CI validation changes that only improve speed, caching, job scheduling, lint,
test execution, or coverage reporting must use `chore(ci)` in both commit
messages and PR titles. Build tooling changes that preserve the shipped app and
release behavior use `chore(build)`. Changes to production deployment or the
shipped artifact can still require `feat` or `fix`; the `ci` or `build` scope
alone does not decide the type.

Before committing or opening/updating a PR, check whether the change affects the
shipped app or application releases and keep the commit and PR types aligned.
Non-release chores must not use `!`, `BREAKING CHANGE`, or `Release-As` markers,
which can trigger a release despite a `chore` type. Apply this rule to all future
changes, including follow-up fixes to CI validation and internal tooling.

Examples:

```scss
feat(ui): add dark mode toggle
fix(zoom): prevent pinch zoom from exceeding bounds
fix(ci): restore production deployment
chore(ci): shorten Android validation pipeline
chore(build): optimize Gradle caching without changing app artifacts
chore(docs): document family-data validation
chore(test): reorganize parser fixtures
```
