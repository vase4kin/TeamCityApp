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

# Shared networking and Android compatibility

Both production hosts execute `SharedAppViewModel` and its account-scoped
`TeamCityAccountGraph`. `libraries/network` owns the Ktor transport, Basic challenge
handling, same-origin/context guards, JSON decoding, bounded response cache and
download streaming. `libraries/shared-data` implements feature repositories over
that transport. Android uses Ktor's OkHttp engine; iOS uses Darwin/URLSession.
Platform adapters own secure storage, atomic cache bytes and file export.

The retired Android Retrofit services, RxJava repository adapters, RxCache runtime,
Jolyglot dependency, Dagger REST component and `ApiSession` have been removed.
The app's `verifyProductionNetworking` task checks production and mock runtime
classpaths and runs with app assembly and `check`. OkHttp remains as Ktor's
Android engine. Gson remains at the previously resolved version for installed
account and launch formats.

## Preserved compatibility

- `libraries/data` keeps legacy DTO packages, serialized fields, filter formats and
  `BuildLaunchMapper`/`LegacyBuildLaunchCodec` for installed Intent payloads.
- `libraries/session` keeps encrypted accounts and the existing Gson storage format.
- `AndroidLaunchRegistry` supplies serializers for installed typed destinations,
  including nested account-activation destinations. It has no Android UI renderer
  or repository dependency. The shared root owns navigation and rendering.
- Old RxCache files remain untouched. They are neither read nor imported into the
  common response cache; the first common request refetches its data.
- Intent aliases, FileProvider exports and Android's external OSS licenses screen
  keep their installed contracts. Screenshot golden files remain unchanged.

## Offline and regression tests

The app's `mock` flavor owns `TeamCityService` as a fixture response contract,
`FakeTeamCityServiceImpl` and `MockTeamCityService`. These classes do not create
HTTP clients or an account graph. `SharedMockClient` converts fixture responses
into Ktor mock-engine responses; the common transport, parser and repositories
then execute normally. Explicit loopback fixtures still use the platform engine.

Fixture methods are suspend functions. `PendingFixture` uses a coroutine gate and
tracks active waiters so tests can verify request cancellation and late responses.
RxJava, its coroutine bridge, Retrofit and RxCache are absent from both flavors.
Authentication UI tests feed this same Ktor fixture seam. Startup tests exercise
shared account restoration. Older route
regression helpers live in each feature's `androidHostTest`, receive explicit fake
ViewModels and have no production Hilt bindings.

The minified R8 smoke suite exercises Ktor parsing and persisted response-cache
restoration, alongside the retained DTO/account compatibility checks. Use the
commands in [R8 verification](../scripts/r8-verification.md). Common networking and
repository tests run through `:libraries:network:testAndroidHostTest`,
`:libraries:shared-data:testAndroidHostTest` and the corresponding
`iosSimulatorArm64Test` tasks. Android launch/fixture checks run with
`:app:testMockDebugUnitTest`; integration suites compile with
`:app:assembleMockDebugAndroidTest`.

## Cleanup verification — 2026-10-27

The production and mock debug APKs and mock instrumentation APK built successfully.
Production debug/release and mock debug runtime dependency checks passed; a
temporary Gradle init script
injecting RxJava was rejected by the guard as expected, without changing source
configuration. Source boundaries and Spotless passed.

Passing checks included 3,828 shared/feature Android host tests (including golden
verification; one existing test skipped), 74 app unit tests, 11 DTO/account
compatibility tests, 59 common
network/repository tests on the iOS simulator and 96 repository script tests.
Screenshot golden bytes remained unchanged.

On the local API 34 emulator, all 53 selected Login, Add Account, startup, About,
Home, Test Details and rating integration tests passed. All six smoke tests also
passed in the
minified, debug-signed R8 verification APK, including common Ktor decoding and
disk-cache restoration. This follow-up changed no shared screen layouts.

## Further unused-code cleanup

The reference audit also removed the old Remote Config rating facade, its copied
test DI modules, and the now-orphaned `libraries/remote` URL formatter module.
Production URL normalization remains in the common Ktor transport. Rating tests
seed the shared account-local eligibility policy directly.

Removed code includes unconsumed analytics implementations/bindings, loading and
filter helpers, an unused session-clear contract, and redundant authentication
and sheet wrappers/interfaces. Fourteen feature modules no longer apply Hilt/KSP
or declare Hilt Compose/Dagger dependencies without corresponding source usages.
The audit also removed 42 unused drawable/animation files, 81 obsolete Android
string/plural/array entries, and unused version-catalog aliases.

Installed DTO/account classes and serialized signatures remain intact. Native
framework callbacks, generated DI entry points, test-used adapters, installed
launch aliases and dynamically loaded OSS resources remain supported. No shared
screen layout or intentional screenshot fixture changed.

## Analytics migration parity audit — 2026-10-27

The audit compared the 18 legacy Android feature tracker implementations and
`AuthenticationAnalytics` at `ca5fbc5b1130bcf0ca9d1d7fd14ae371b8e2cbae` with the common
production composition root. The older quick-filter repository at `dafbb39f1`
also confirms that selection tracking followed successful preference application.
Feature-owned portable trackers preserve the legacy event identities rather than emitting
renamed migration aliases. Each action emits one event; aliases are not emitted
alongside the legacy name.

| Area | Restored or verified legacy events |
| --- | --- |
| Login and Add Account | `screen_first_login`, `screen_create_account`, `login_user_success`, `login_guest_user_success`, `login_user_failed`, `login_guest_user_failed`, and the three `event_user_*try_it_out` actions |
| Home | `screen_home`, `tab_selected`, the four tab-specific FAB actions and `favorites_tab_click_on_action` |
| Projects and rating | `screen_project`, `rate_show`, `rate_cancel`, `rate_now` |
| Build History | `screen_build_list`, `run_build_fab_click`, `build_list_show_queued_details` |
| Run Build | `screen_run_build`, `run_build_success`, `run_build_custom_params_success`, `run_build_failed`, `run_build_forbidden_error`, `run_build_add_custom_param_click`, `run_build_clear_all_params`, `run_build_add_custom_param` |
| Build Filters | `screen_filter_builds`, `build_list_filters_applied` |
| Quick Filters, including inline Home chips | `filter_running_builds_selected`, `filter_queued_builds_selected`, `filter_agents_selected` |
| Build Details | `screen_build_details`, `build_cancel_confirm`, `build_cancel_success`, separate forbidden/server cancel and restart errors, `build_restart_success`, `build_restart_show_success`, `build_restart_show_failure` |
| Overview actions | `share_build`, `open_browser_build`, `cancel_build`, `restart_build`, `open_build_type`, `show_builds_filtered_by_branch`, `open_project` |
| Change and Test Details | `screen_open_change`, `change_click_more_details`, `change_click_view_file_diff`, `screen_test_details` |
| Favorites and Snapshot Dependencies | `favorites_open_build_type`, `screen_snapshot_dependencies` |
| Account destination | `screen_open_drawer` and the seven legacy `drawer_*` actions, despite the current full-screen presentation |
| Manage Accounts | `screen_account_list`, `remove_account`, `account_ssl_warning` |
| Settings | Existing `screen_settings` and all four `theme_set_*` events remain unchanged |
| Artifacts | The old implementation was a no-op. The existing common `screen_artifacts` event remains; no retired stub was reinstated |

Portable `AnalyticsEvent` payloads preserve the exact parameter keys and values:

- `tab`: `PROJECTS`, `FAVORITES`, `RUNNING_BUILDS`, `BUILD_QUEUE`, `AGENTS`.
- `filter`: `RUNNING_ALL`, `RUNNING_FAVORITES`, `QUEUE_ALL`, `QUEUE_FAVORITES`,
  `AGENTS_CONNECTED`, `AGENTS_DISCONNECTED`.
- `sslEnabled` and `param_re_add_to_the_queue`: portable boolean flags, encoded as
  Android `long` values `1`/`0`. This uses the SDK's documented numeric parameter
  types while retaining both true and false values. See the
  [Firebase Android event contract](https://firebase.google.com/docs/reference/android/com/google/firebase/analytics/FirebaseAnalytics#logEvent(java.lang.String,android.os.Bundle)).
- `errorMessage`: existing explicit messages are retained. Typed transport errors
  that previously supplied an empty message use public, stable HTTP/connection/
  invalid-URL/HTTP-consent descriptions. Storage failures retain
  `Failed to save user data!` and the original `login_user_failed` event.

`libraries/analytics` owns the common `AnalyticsTracker` contract and typed payloads.
Its `AndroidAnalyticsTracker` sends them to the existing Firebase backend. Its
`IosAnalyticsTracker` discards parameters and logs only the fixed event label:
no parameter, server, credential or failure-message value enters OS logs, and
no hosted iOS analytics SDK is introduced. Branch names are used only to choose the historical
filtered-history event; their contents are not sent as analytics parameters.

Feature-owned common contract tests cover every tracker method, tab/filter value, boolean state,
and forbidden/server error branch. Analytics-module Android tests capture the actual Firebase
`Bundle`, including false (`0`), true (`1`), empty strings and null bundles.
Filter tracking is a required constructor/interface dependency, so production
callers cannot silently omit it during future migrations. ViewModel tests verify
that duplicate, failed, stale-account and cancelled filter
requests do not emit successful selection events, and that user/guest typed
failures retain informative analytics messages. Shared UI layouts and screenshot
golden files are unchanged.

Parity-restoration verification before the ownership refactor: 1,926 focused
Android host/app tests and 187 iOS simulator tests passed,
including the event-contract and Firebase adapter suites. Production/mock Android
APKs and mock instrumentation compiled, and all 53 selected account/startup/rating/
detail/navigation integration tests passed on an isolated API 34 emulator. Source
boundaries, formatting and unchanged golden bytes were checked.

The call-site audit also restored lifecycle-driven Login, History, Build Details
and Test Details views, History's Run action, and both Change Details browser/diff
actions. A shared screen-view effect tracks visibility and owner replacement,
without recording another view on ordinary recomposition. It invokes existing
ViewModel tracking hooks without introducing additional data refreshes.

Restart-reference outcomes are recorded at the actual reference request boundary,
with cancellation checks, once per request. Count loading/retries and re-collection
of completed content do not replay the outcome. Only `fromRestart` Show visits
participate in `build_restart_show_success` / `build_restart_show_failure`;
ordinary History Show visits and Overview refreshes do not. Reference tracking is
an explicit dependency, with an explicit `None` policy for ordinary Overview loads.

Home's Favorites FAB has been retired by the UI redesign. The surviving Add
Projects CTA retains `favorites_tab_click_on_action`, and Projects navigation
retains `tab_selected`; no FAB click is fabricated for an absent control. The old
FAB mapping remains covered for callers that use that tracker contract.

## Analytics ownership

Each feature implementation owns its dedicated `<Feature>TrackerImpl` in
`src/commonMain/kotlin/.../impl/tracker/`, together with its event-mapping tests
in `commonTest`. Favorites, Snapshot Dependencies and Artifacts each have their
own tracker; they do not share a browsing tracker. `shared-app` contains constructor
wiring and generic visibility orchestration, with no feature event names or
tracker implementations.

`libraries/analytics` contains the portable `AnalyticsTracker`, `AnalyticsEvent`
and `AnalyticsValue` contracts. Its `androidMain` owns `AndroidAnalyticsTracker`
and the Firebase dependency; `iosMain` owns `IosAnalyticsTracker` and local logging.
Backend tests live in that module's platform test source sets. Android binds the
backend in the app's DI package; navigation receives only the interface.

`AuthenticationEventTracker` in `libraries/authentication-shared` is reusable
authentication functionality consumed by the dedicated Login and Add Account
trackers. Home receives the Quick Filter API contract rather than depending on
its implementation. Restart-reference tracking belongs to Build Details and
implements the Build Overview API's `BuildReferenceTracker`. No feature depends
on another feature's implementation.

The source-boundary check rejects tracker implementations or event mappings in
`shared-app`, and Firebase Analytics SDK imports in feature production sources.
The old Android-only tracker implementations and their SDK-specific tests are
replaced by feature-owned portable implementations/tests; the old root tracker
file and navigation-owned analytics backend are removed.

Ownership-refactor verification: 3,853 Android host tests reported zero failures
(one existing skip), with 74 app unit tests, four legacy authentication UI tests,
387 iOS simulator tests and 100 repository script tests passing. Both Android
hosts and the mock instrumentation APK built, and all 53 selected Android
integration tests passed with the injected analytics backend. Event names,
parameter keys/values and screenshot golden bytes remain unchanged by this refactor.
