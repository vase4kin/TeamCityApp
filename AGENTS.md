# TeamCityApp Agent Guide

Use this file as the working contract for AI coding agents in this repository.
The architecture below is the direction for TeamCityApp. The current codebase
has not completed that migration. Check nearby code and module build files
before editing, preserve unrelated local work, and keep each change scoped to
the requested behavior or an explicitly requested migration slice.

## Project snapshot

- TeamCityApp is a native Android client for JetBrains TeamCity.
- The app currently uses multiple activities and fragments, XML layouts, Data Binding,
  presenters and legacy state holders, RxJava 2, Hilt for Android screen injection,
  a separate Dagger account API graph, Retrofit/Gson,
  RxCache, and SharedPreferences. Java and Kotlin coexist.
- Test Details and Settings use Compose, Material 3, Hilt ViewModels, and
  Coroutines/Flow. Settings persists theme preferences through DataStore with
  a migration from the previous SharedPreferences key. Other screens remain legacy.
- Room, WorkManager, Navigation 3, a single-activity shell, and broader Compose
  migration remain target architecture.
- Builds use Kotlin DSL Gradle files, a version catalog, type-safe project accessors,
  an included `build-logic` build for conventions and SDK/application settings,
  and JDK 17.
- The app has `mock` and `prod` flavors, and `debug` and `release` build types.
  `mockRelease` is disabled.

## Repository layout

- `app/`: application, TeamCity API and cache integration, shared app flows, and
  features that have not been moved to standalone modules.
- `features/`: feature modules. Some features use `models`, `repository`, and
  `feature` modules; others are single modules. Follow the owning feature's layout.
- `libraries/`: shared API, storage, models, resources, theme, networking helpers,
  security, utilities, and other reusable components.
- `build-logic/src/main/kotlin/Config.kt`: SDK/JVM settings and application version.
- `build-logic/`: included Kotlin build containing Android application/library,
  Java-only library, Hilt/kapt, Data Binding, and aggregate coverage conventions.
  All Android modules apply these conventions; `buildSrc` has been retired.
- `gradle/libs.versions.toml`: dependency and plugin versions/coordinates.
- `settings.gradle.kts`: included modules, plugin resolution, and dependency
  repositories. Root and module `build.gradle.kts` files use Kotlin DSL.
- `.github/workflows/build.yml`: build, lint, unit test, instrumentation, and
  coverage CI. `scripts/` contains R8 verification preparation/documentation.

## Target architecture

- Move toward a single-activity app with Jetpack Compose, Material 3, adaptive
  layouts, window size classes, and Navigation 3. Migrate screen by screen while
  preserving existing routes and behavior.
- Use unidirectional data flow. UI events go to ViewModels or state holders;
  immutable UI state flows back via Coroutines and `Flow`. Model loading, empty,
  error, and content states explicitly.
- Keep Compose screen/content functions mostly stateless. Route/container
  composables own ViewModel wiring and collect flows with lifecycle-aware APIs.
- Put data access behind repositories. UI and ViewModels must not call Retrofit,
  Room, DataStore, or other storage APIs directly. Put shared or nontrivial
  cross-repository operations in use cases.
- Use Hilt for new dependency-injection infrastructure when that migration is
  in scope. Keep dispatchers, repositories, services, DAOs, and workers injected.
- Keep network DTOs, persistence entities, and domain models distinct at layer
  boundaries. Use Kotlin serialization for new network models where the
  networking migration has been established.
- Use Room for structured local data, DataStore for preferences, and WorkManager
  for deferrable background work when those capabilities are introduced. Preserve
  persisted data and schema compatibility during migrations.
- Prefer feature `api`/`impl` boundaries for new or substantially reworked
  features. Keep API modules small and avoid implementation-to-implementation
  feature dependencies. Reuse shared `core` or `libraries` modules as appropriate.

## Working in the current codebase

- Respect existing module boundaries. Put feature behavior in its owning module
  when practical; keep shared interfaces and models in the appropriate library or
  feature `models`/`repository` module. Avoid new feature-to-feature coupling.
- Keep network and persistence access behind existing repository, data manager,
  or storage interfaces while old implementations remain. Do not add direct
  Retrofit or SharedPreferences access to UI code.
- Unmigrated screens use Hilt, RxJava, XML, and Data Binding. Keep them
  working until their migration slice is implemented and verified. Use explicit
  adapters at old/new boundaries instead of mixing state systems inside a screen.
- Hilt screen modules are installed in every Activity/Fragment component. Use
  `requireScreenOwner` for concrete owner bindings; these are runtime ownership
  checks, not isolated screen graphs. Keep screen dependencies in their owning
  screen and qualify shared types when several screens provide them.
- Legacy `*StateHolder` classes are ordinary lifecycle observers, not Jetpack
  ViewModels. They are scoped to their screen component and may hold UI callbacks.
  Do not promote them to retained ViewModels while they capture Activities,
  Fragments, adapters, or view callbacks. A Jetpack migration must move UI effects
  to the view, use `@HiltViewModel`, and retrieve instances through `ViewModelProvider`.
- API dependencies come from `ApiSession`, which initializes lazily and rebuilds
  on account/authentication changes. Clear the session when logging out. Access
  account storage through Hilt rather than through the account API graph.
- Match resource acquisition and cleanup to the same lifecycle. View resources
  must reinitialize after `onDestroyView`; dispose presenters before replacing them.
- Keep UI rendering separate from data loading and business operations even
  before a screen is moved to Compose.
- Follow local Kotlin/Java naming and formatting conventions. Prefer immutable
  models and small public APIs when adding or changing contracts.
- Use Material 3 and shared design-system components for migrated UI. Add
  previews for reusable components. Use Android string/plural resources for
  user-visible text.
- Do not migrate unrelated screens during a narrow bug fix. For requested
  modernization work, migrate a coherent vertical slice, including its tests,
  and remove the superseded implementation once no callers need it.

## Build and dependencies

- The build uses Kotlin DSL, the version catalog at `gradle/libs.versions.toml`,
  type-safe project accessors, and convention plugins in `build-logic`. Modules
  apply `teamcityapp.android.application`, `teamcityapp.android.library`, or
  `teamcityapp.android.library.java`, plus optional Hilt/kapt and Data Binding
  conventions. Compose modules also apply `teamcityapp.android.compose`. The app
  applies the aggregate coverage convention. Keep shared
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
  practical. Current local tests use JUnit and Mockito; app UI tests use Espresso.
- For migrated code, use `kotlinx.coroutines.test` and Turbine where Flow behavior
  warrants it, and Compose testing APIs for Compose UI. Use the existing app-level
  instrumentation suite for end-to-end behavior.
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
  Gradle-managed devices, Roborazzi, or Spotless are configured here.
- Add those tools and their tasks only as part of an explicit test/build migration.
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
