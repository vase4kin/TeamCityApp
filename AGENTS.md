# TeamCityApp Agent Guide

Use this file as the working contract for AI coding agents in this repository.
The architecture below is the direction for TeamCityApp. The current codebase
has not completed that migration. Check nearby code and module build files
before editing, preserve unrelated local work, and keep each change scoped to
the requested behavior or an explicitly requested migration slice.

## Project snapshot

- TeamCityApp is a native Android client for JetBrains TeamCity.
- The app currently uses multiple activities and fragments, XML layouts, Data Binding,
  presenters and ViewModels, RxJava 2, Dagger 2 with `dagger.android`, Retrofit/Gson,
  RxCache, and SharedPreferences. Java and Kotlin coexist.
- Compose, Material 3, Hilt, Coroutines/Flow, Room, DataStore, WorkManager, and
  Navigation 3 are target technologies, not descriptions of current code.
- Builds use Groovy Gradle files, `buildSrc` dependency constants, and JDK 17.
- The app has `mock` and `prod` flavors, and `debug` and `release` build types.
  `mockRelease` is disabled.

## Repository layout

- `app/`: application, TeamCity API and cache integration, shared app flows, and
  features that have not been moved to standalone modules.
- `features/`: feature modules. Some features use `models`, `repository`, and
  `feature` modules; others are single modules. Follow the owning feature's layout.
- `libraries/`: shared API, storage, models, resources, theme, networking helpers,
  security, utilities, and other reusable components.
- `buildSrc/src/main/kotlin/teamcityapp/buildsrc/Dependencies.kt`: SDK settings,
  application version, and dependency/plugin coordinates.
- `settings.gradle`: included modules. `build.gradle` and module `build.gradle`
  files contain Groovy build configuration.
- `.github/workflows/build.yml`: build, lint, unit test, instrumentation, and
  coverage CI. `scripts/` contains shared Gradle scripts.

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
- Existing screens still use Dagger, RxJava, XML, and Data Binding. Keep them
  working until their migration slice is implemented and verified. Use explicit
  adapters at old/new boundaries instead of mixing state systems inside a screen.
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

- The target build uses Kotlin DSL, a version catalog at `gradle/libs.versions.toml`,
  type-safe project accessors, and reusable convention plugins in `build-logic`.
  Migrate build infrastructure deliberately rather than pretending it exists.
- Until the catalog is established, put shared dependency versions and plugin
  coordinates in `Dependencies.kt`; do not scatter new versions through module
  build files. After migration, use the catalog as the source of truth.
- Add modules to `settings.gradle`, give Android modules a namespace, and declare
  only the dependencies they need.
- Use existing Groovy build files for unrelated changes. For a build migration,
  update the affected modules and verification commands together; keep
  convention plugins additive and reusable.
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
