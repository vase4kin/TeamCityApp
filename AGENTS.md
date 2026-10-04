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
- The app currently uses multiple activities and fragments, XML layouts, Data Binding,
  presenters and legacy state holders, RxJava 2, Hilt for Android screen injection,
  a separate Dagger account API graph, Retrofit/Gson,
  RxCache, and SharedPreferences. Java and Kotlin coexist.
- About uses Compose, Material 3, a Hilt ViewModel, and StateFlow. Its suspend
  repository adapts the existing Rx API and cache. Lifecycle-aware state collection
  starts loading; losing the last collector cancels pending work immediately,
  while completed content survives configuration changes. About is split into
  `features/about/api` and `features/about/impl`, with Kotlin source roots.
  The app retains the legacy server DTO and its serialized class name for RxCache
  compatibility; the app repository maps it into About’s plain API model. About UI tests
  run in the app instrumentation suite used by Marathon. The licenses action
  still opens Google's OSS licenses screen.
- Other screens remain legacy. Room, DataStore, WorkManager, and Navigation 3
  remain target technologies.
- Builds use Kotlin DSL Gradle files, a version catalog, type-safe project accessors,
  an included `build-logic` build for conventions and SDK/application settings,
  and JDK 17.
- The app has `mock` and `prod` flavors, and `debug` and `release` build types.
  `mockRelease` is disabled.

## Repository layout

- `app/`: application, TeamCity API and cache integration, shared app flows, and
  features that have not been moved to standalone modules.
- `features/`: feature modules. About uses `api`/`impl`. Unmigrated features
  still use `models`/`repository`/`feature` or single-module layouts; migrate the
  owning feature to `api`/`impl` during substantive refactors.
- `libraries/`: shared API, storage, models, resources, theme, networking helpers,
  security, utilities, and other reusable components.
- `build-logic/src/main/kotlin/Config.kt`: SDK/JVM settings and application version.
- `build-logic/`: included Kotlin build containing Android application/library,
  Java-only library, Hilt/kapt, Compose, Data Binding, and aggregate coverage conventions.
  All Android modules apply these conventions; `buildSrc` has been retired.
- `gradle/libs.versions.toml`: dependency and plugin versions/coordinates.
- `settings.gradle.kts`: included modules, plugin resolution, and dependency
  repositories. Root and module `build.gradle.kts` files use Kotlin DSL.
- `.github/workflows/build.yml`: build, lint, unit test, instrumentation, and
  coverage CI. Keep GitHub Actions as the CI platform. `scripts/` contains R8
  verification preparation/documentation.

## Target architecture

- New features and substantive refactors must use this architecture whenever
  feasible. Existing legacy patterns are compatibility constraints, not the
  default for new code. If a concrete constraint prevents adoption, explain it
  and keep the legacy dependency behind a small, explicit adapter.
- UI: use Jetpack Compose, Material 3, shared design-system components, and
  adaptive layouts. Use window size classes for window-level navigation/pane
  decisions and local layout constraints for responsive components. The destination
  is a single-activity app with
  Navigation 3. Use Navigation 3 for new navigation infrastructure; preserve
  existing activity/fragment entry points through adapters during screen migration.
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
  separate stateless screen/content composable. Activities host the route.
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
- Legacy screens use Hilt, RxJava, XML, and Data Binding. Keep them working
  until their migration slice is implemented and verified. New screens use Compose
  and ViewModels; do not add new presenters or callback-holding legacy state
  holders. Use explicit adapters at old/new boundaries instead of mixing state
  systems inside a screen.
- Hilt screen modules are installed in every Activity/Fragment component. Use
  `requireScreenOwner` for concrete owner bindings; these are runtime ownership
  checks, not isolated screen graphs. Keep screen dependencies in their owning
  screen and qualify shared types when several screens provide them.
- Legacy `*StateHolder` classes are ordinary lifecycle observers, not Jetpack
  ViewModels. They are scoped to their screen component and may hold UI callbacks.
  Do not promote them to retained ViewModels while they capture Activities,
  Fragments, adapters, or view callbacks. A Jetpack migration must move UI effects
  to the view, use `@HiltViewModel`, and retrieve instances through `ViewModelProvider`.
- The app's `Repository` may extend migrated feature repository API contracts.
  Its production and mock implementations own their corresponding suspend/Flow
  behavior, including Rx bridges and DTO mappings when needed. Expose these
  interfaces through small DI providers rather than putting adapter classes in
  DI module files. Preserve caching and cancellation semantics.
- API dependencies come from `ApiSession`, which initializes lazily and rebuilds
  on account/authentication changes. Clear the session when logging out. Access
  account storage through Hilt rather than through the account API graph.
- Match resource acquisition and cleanup to the same lifecycle. View resources
  must reinitialize after `onDestroyView`; dispose presenters before replacing them.
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
  `teamcityapp.android.library.java`, plus optional Hilt/kapt and Data Binding
  conventions. Compose modules also apply `teamcityapp.android.compose`.
  The app applies the aggregate coverage convention. Keep shared
  configuration in these plugins rather than root cross-project callbacks.
  See `build-logic/README.md`.
- Keep dependency/plugin versions in the catalog; keep SDK/application settings
  in `Config.kt`. Do not scatter versions through module build files.
- Kotlin Android modules use kapt for Hilt/Dagger processing of Kotlin and Java
  sources. Do not also register the Dagger compiler with `annotationProcessor`.
- Add modules to `settings.gradle.kts`, give Android modules a namespace, and declare
  only the dependencies they need.
- Use Kotlin DSL for build changes. Keep feature dependencies, namespaces,
  flavors, signing, and packaging in the owning module. Declare repositories
  in settings and plugin versions in the catalog. Update affected modules and
  verification commands together; keep convention plugins reusable.
- External Kotlin, kapt, and the legacy Android DSL remain in use. Preserve
  `android.builtInKotlin=false` and `android.newDsl=false` until their migration
  is explicitly in scope.
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
- Retain Espresso coverage for legacy screens and the existing app-level
  instrumentation suite for end-to-end behavior. Verify lifecycle cancellation,
  state retention, and old/new integration boundaries when those behaviors change.
- Run the narrowest relevant test first, then broader checks for changes that
  cross module boundaries. Report checks that cannot run and why.
- Prepare debug Google Services files as CI does before local app builds if they
  are absent:
  `cp mock-mockDebug-google-services.json app/src/mock/debug/google-services.json`
  and `cp mock-prodDebug-google-services.json app/src/prod/debug/google-services.json`.
- Convention-plugin verification: `./gradlew :build-logic:test` (JDK 17).
- Common tasks: `./gradlew assembleMockDebug`,
  `./gradlew :app:testMockDebugUnitTest`, `./gradlew testMockDebugUnitTest`,
  and `./gradlew lintMockDebug`. CI also runs `testDebugUnitTest` for modules
  without flavors and builds `assembleMockDebugAndroidTest`.
- Instrumentation tests run in CI through Marathon Cloud. Do not assume that
  Gradle-managed devices or Spotless are configured here; verify available tasks
  before running them. Robolectric/Roborazzi are configured by the opt-in screenshot
  convention described above. Add managed devices or formatting tooling only as
  part of an explicit test/build migration.
- Do not commit generated APKs, reports, build output, `.gradle`, or IDE metadata.

## Version control

- Use GitButler (`but`) for all version-control writes: commits, branches, pushes,
  merges, history edits, and pull requests. Read-only Git commands are fine.
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
