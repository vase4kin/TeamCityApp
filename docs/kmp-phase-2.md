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

# Phase 2 — accounts and the first real workflow

Baseline: `7f1be123dc18a9dff970901bd86a80188a50fb03` on
`codex/kmp-phase-1`. The user approved JetBrains' public guest server,
`https://teamcity.jetbrains.com`, for read-only live verification. No private
production credentials or build mutations were used. Phase 3 is not authorized.

Report: [Private Phase 2 review](https://teamcityapp-kmp-phase-two-20261022.andrey-tolpeev.chatgpt.site).
Final verification and checkpoint references are recorded below.

## What is shared now

The normal iOS launch is a real Login → Projects → Configurations → History →
Build Overview application. The Phase 1 About preview remains available only
through its explicit preview launch environment for regression verification.

| Ownership | Implementation |
| --- | --- |
| Account identity and lifetime | `libraries/accounts`: public identity, redacted secret, secure-storage contract, atomic vault format, generation-scoped sessions and cancellation |
| Authentication and repositories | `libraries/shared-data`: validated sign-in, per-account graph, project/configuration/history/build repositories |
| Transport and mapping | `libraries/network`: Ktor, Kotlin serialization, challenge-based Basic authentication, guest paths, URL/link guards, full build mapping and in-memory caches |
| Navigation | `libraries/navigation`: typed serialized keys, unique visits, account-bound snapshots, destination owners, retained panes and single-consumer results |
| Screen state and UI | `features/{login,navigation,build-history,build-overview}/presentation`: common ViewModels, immutable state, Compose screens and resources |
| Lists and controls | `libraries/{authentication-shared,list-presentation,build-presentation,design-system}`: forms, typed text, errors, row models, paging views, Material controls |
| Models and dates | KMP feature APIs, `libraries/{builds,build-configurations,list-state,time}` |
| Native application composition | `shared-app`: lifecycle factories, real account chooser, graph/navigation wiring; iOS Keychain and navigation-store adapters |

There are 92 included modules, 24 using the KMP conventions (4 at Phase 1).
Phase 2 adds 11 modules and converts 9 existing modules. This is a module count,
not a claim that 26% of the application's code or product functionality is shared.
The inventory records the exact ownership delta.

Android feature `impl` modules now supply thin Hilt ViewModel subclasses, routes,
trackers, coachmarks, destination registrations and platform routers. Shared
ViewModels use plain constructor injection and lifecycle-owned factories. Shared
UI has no dependency on Hilt, Android resource IDs, Context, Retrofit or RxJava.
The existing package names are retained to limit Android caller churn.

## Android compatibility boundary

Android continues to use its installed Retrofit/RxCache and authentication
repositories through the portable feature contracts. Its account preparation
also restores and validates the common session manager before initializing the
legacy API graph. iOS uses the new Ktor account graph. Replacing Android's entire
network/cache stack would invalidate compatibility work outside this phase.

`LegacyAndroidAccountStorage` adapts `SharedUserStorage` and `CryptoManager`.
It preserves the UsersContainer Gson JSON, preference key, encrypted password
bytes, reserved guest marker, account order and mutable Favorites lists. Writes
commit the complete snapshot before replacing in-memory state. Encryption or
storage failure leaves the previous account intact. Identity keys preserve the
installed Gson escaping, including U+2028/U+2029. A different user on the same
server remains a different account; changing SSL policy does not create a
duplicate saved row.

Android retains its Home tabs, filters, Run/Stop/Restart actions, View Log,
sharing, browser launches, existing account UI and section destinations. Those
integrations have not been represented as completed iOS features.

## Credentials, requests and cancellation

- iOS saves one atomic account vault in Keychain, using
  `WhenUnlockedThisDeviceOnly`, with synchronization disabled. Navigation stores
  public account identity and reference destinations in NSUserDefaults; it never
  stores passwords or authenticated request headers.
- A fresh generation owns each account activation. Replacing it cancels its
  SupervisorJob and in-flight child requests. A request must still belong to the
  current generation before its value can be accepted. Destination cancellation
  cancels the same child request. Closed graphs release clients and caches.
- Storage mutation and session publication form a cancellation-safe transaction:
  cancellation is checked before starting, then a committed write is followed by
  publishing the corresponding session. Failed writes do not publish a session.
- Basic credentials are sent only after a 401 response offering Basic, with one
  retry and challenge charset handling. Guest requests use `guestAuth` and never
  attach Authorization. Authenticated requests use `httpAuth`.
- Continuations must keep the exact origin, scheme, port and server context.
  User information, fragments, traversal and non-REST destinations are rejected.
  Encoded opaque queries are preserved. Automatic redirects are disabled.
- HTTP requires explicit user consent before a request. Trust bypass requires an
  explicit per-account SSL setting and confirmation. iOS ATS allows arbitrary
  loads to support user-selected HTTP servers; the common gate enforces consent.
- Darwin's credential storage, cookies and URL cache are disabled for the API
  client. API credentials remain separate from browser authentication. Error
  messages are typed/sanitized; passwords are redacted from diagnostic strings.

No password is serialized in ViewModel saved state. Keychain writes necessarily
contain secrets; the temporary authentication form remains memory-only and its
visit is cleared after successful account activation.

## Data and navigation behavior

The new JSON mapping explicitly requests and maps the complete incoming build
snapshot: status/state, IDs and names, dates, branch, agent, trigger, cancellation,
properties, tests, changes, dependencies and optional counts. Absent optional
objects remain distinguishable from present-but-empty data. Native reference
visits resolve the complete snapshot before rendering Overview.

Initial project/configuration pages and builds can be retained in the current
account graph's memory cache. Explicit refresh bypasses it; continuation pages
are requested fresh using TeamCity's opaque `nextHref`. There is no new iOS disk
cache or claim of offline-first synchronization. Retained content can remain
visible with an independently retryable refresh error. Android's existing disk
cache remains owned by its compatibility adapters.

Typed navigation is shared, including unique visit IDs, saved entry state,
ViewModel ownership, replacement/reset cleanup, retained panes, and results
targeted once to a live originating visit. Native cold restoration is bounded,
decoded against the registered destination serializers, and checked against the
active account key and allowed root. Switching identities creates fresh owners;
saved navigation from another user on the same server is rejected.

The native account chooser has loading, empty, error, content, busy, mutation
failure/retry and removal-confirmation states. Its ViewModel owns reads and
mutations; the screen owns only confirmation and navigation. Selecting an account,
signing out, removing it, and adding another account use the secure-storage
contract. Sign-out preserves saved accounts for a later explicit selection.

## Actual UI differences

Android's existing screen layouts and baseline pixels are retained. Resource
resolution is moved into Compose Multiplatform resources behind typed
`SharedUiText`; Android wrappers retain resource compatibility for unmigrated
callers. Four new Login error scenarios and the native account chooser gain
intentional golden fixtures.

iOS uses those same Material Compose screen bodies, status cards, list rows,
forms and color definitions. Native safe areas, fonts, keyboard and system bars
still affect the final image; pixel identity across operating systems is not a
promise. A standalone Projects header and account chooser replace the Android
Home shell for this phase. History management/filter actions and Overview's
platform buttons/section links are hidden on iOS until their owning phases.
The shared History layout removes the unused floating-toolbar clearance when
actions are hidden. Android continues to render the complete action toolbar.

There was no iOS Login/Projects/History/Overview screen in Phase 1. The report
labels these as **first iOS baselines** and shows the old About preview separately.
It compares equivalent Android fixture renders before and after; live Android
and iOS captures use real server data and are explicitly not identical fixtures.

## Verification and reproducibility

Final acceptance Gradle runs use JDK 17. Generated logs, raw PNGs and xcresult bundles
are retained under `build/kmp-phase-2/`; they are not committed. Intentional
Roborazzi PNGs are committed in the owning modules.

AGP is patched from 9.3.1 to 9.3.2 after full application lint exposed the
bundled JavaDocParser `List.removeLast()` crash on JDK 17. Google's
[AGP 9.3 release notes](https://developer.android.com/build/releases/agp-9-3-0-release-notes)
identify this exact issue as fixed in 9.3.2. No lint detector is disabled and the
project keeps JDK 17. Kotlin, Compose and Gradle retain the Phase 1 versions.

Relevant commands (use the repository JDK 17 and normal SDK paths):

```sh
./gradlew iosSimulatorArm64Test --max-workers=2
./gradlew :shared-app:linkDebugFrameworkIosArm64 :shared-app:linkDebugFrameworkIosSimulatorArm64
./gradlew :shared-app:verifyRoborazziAndroidHostTest
./gradlew :features:login:impl:verifyRoborazziDebug :features:navigation:impl:verifyRoborazziDebug
./gradlew :features:build-history:impl:verifyRoborazziDebug :features:build-overview:impl:verifyRoborazziDebug
./gradlew :libraries:session:testDebugUnitTest :app:testMockDebugUnitTest
./gradlew :app:assembleMockDebug :app:assembleProdDebug :app:assembleMockDebugAndroidTest :app:lintMockDebug
./gradlew :build-logic:test spotlessCheck
python3 -m unittest discover -s scripts/tests
```

The opt-in live REST test uses `-PteamcityLiveServer=https://teamcity.jetbrains.com`
and `-PteamcityLiveEvidence=<absolute JSON output path>` on
`:libraries:network:testAndroidHostTest --tests '*TeamCityLiveSmokeTest'`.
The Python HTTP/TLS fixture is started with
`python3 scripts/kmp-platform-fixture.py --help`; its generated certificate and
private key belong under `build/`. Enable the corresponding real-engine tests
with `-PteamcityPlatformFixture` on the network Android host and iOS simulator
test tasks. These tests use dummy local users, not a private production server.

For UI tests, ad-hoc signing is required for the simulator Keychain:

```sh
TEST_RUNNER_TEAMCITY_LIVE_UI=1 xcodebuild \
  -project iosApp/TeamCityApp.xcodeproj -scheme TeamCityApp -configuration Debug \
  -destination 'platform=iOS Simulator,name=iPhone 17' \
  -derivedDataPath build/kmp-phase-2/DerivedData \
  -resultBundlePath build/kmp-phase-2/ios-smoke.xcresult \
  -parallel-testing-enabled NO -collect-test-diagnostics never \
  CODE_SIGN_IDENTITY=- CODE_SIGNING_ALLOWED=YES CODE_SIGNING_REQUIRED=YES test
```

Use a fresh result-bundle path. Live tests are opt-in; deterministic validation,
consent and unreachable-server UI tests run without public-server availability.
The test vault selector works only in debug binaries and isolates real Keychain
items from normal accounts. Run Native unit tests before XCTest, rather than
concurrently controlling the same simulator. CI keeps GitHub Actions and runs
common Native tests, arm64 frameworks, signed simulator UI checks and evidence
uploads. KMP Android host screenshot modules are included in the weighted unit
test scheduler.

## Remaining phases and effort estimates

These are engineering ranges, not deadlines. Phase 2 required 11 new modules,
9 module conversions, 4 screen-presentation extractions, two engine adapters and
storage/navigation compatibility work. That foundational work is now reusable;
the remaining feature count and platform behaviors drive the following estimate.
Each phase includes behavioral tests, actual UI evidence and a private report.

| Phase | Estimate | Main uncertainty |
| --- | --- | --- |
| 3 — Home and read-only browsing | 6–10 focused days | Four retained/polling panes and five detail sections; filters, paging/count failures and adaptive Home shell |
| 4 — Build management | 4–7 focused days | Write-operation confirmation/retry, legacy filter locators, live-origin results and test-server permissions |
| 5 — Platform integrations | 6–10 focused days | Downloads/background cancellation, file export, browser/share, settings, analytics and account-management parity |
| 6 — Release validation | 4–7 focused days plus external review time | Physical iPhone/iPad testing, release signing, performance, accessibility, distribution and store requirements |
| 7 — Agreed refinements | 2–4 focused days after scope agreement | User-selected platform differences; background downloads remain a separate product decision |

Re-estimate after Phase 3 using the first complete Home/section migration.
Physical-device testing, release readiness, full iOS feature parity and private
production-server acceptance are not Phase 2 completion claims.

## Final evidence and checkpoints

| Check | Result / local evidence |
| --- | --- |
| Shared iOS behavioral tests | 211 tests, no failures; `final-common-native.log` |
| JVM/common and Android regression | Passed on patched AGP; `final-regression-patched.log` |
| Focused Android suites | Accounts 67; Login 186; Navigation 97; History 245; Overview 216; session 13; application 231; all pass |
| Real network engines | Four deterministic HTTP/TLS tests on each of OkHttp and Darwin; `platform-engines.log`, preserved `platform-android.xml` / `platform-native.xml` |
| Android runtime integration | Seven emulator tests pass: host back/aliases/recreation plus same-server account switching; `android-instrumentation.log` |
| Android app / tooling | Both debug APKs, instrumentation APK, app lint, formatting and convention-plugin tests pass; `final-build-lint.log` |
| iOS device framework | Final arm64 debug framework builds; `final-device-framework.log` |
| Python CI scheduler | 62 tests pass; `python-final.log` |
| Live REST | Passed approved HTTPS guest access; `live-rest.log` / `live-rest.json` |
| iPhone UI | Complete suite: 10 tests, no failures; `ios-complete-phone.log` (144 seconds) |
| iPad UI | Dark live browsing, pagination, secure relaunch, Back, sign-out and saved-account reselection pass in `ios-ipad-account-final.log`; deterministic HTTP/SSL cases pass in `ios-ipad-dark-2.log` |

The public server reported 2026.2.1 (build 239120). The live REST test loaded
configuration `bt446`, page sizes 10 and 8, and build 3643838 (#2571). Device UI
tests also deliberately open older paginated builds; their Overview values need
not match the first build or the deterministic screenshot fixture.

Raw UI evidence is in `after/android/`, `ios-complete-phone.xcresult`,
`ios-ipad-account-final.xcresult`, and the exported attachment folders. The report
copies reviewed PNGs without altering them. Android airplane mode was restored
after the offline/recovery check. The only private-auth and self-signed-TLS
servers used were local read-only fixtures with dummy users. There is no claim
of verification against a private production TeamCity deployment.

The initial full lint attempt failed in AGP's JavaDocParser; 9.3.2 fixes it and
the final full check passes. XCTest's batched text synthesis dropped characters
in a URL, and a post-rotation Back tap arrived before Compose settled. The test
now types/asserts the URL deterministically and waits for stable geometry.
The first iPad test-launch service stalled and recovered after restarting the
task-owned simulator. These failed exploratory attempts are preserved in build
logs; the final passing runs are the acceptance evidence.

Implementation checkpoint: `44676c6cedd34c7471086f6570aee8cfdcda9176`
(`feat(kmp): share accounts and the first iOS build workflow`). CI checkpoint:
`7175484662fef26af594bbabc88bae26beefe357`
(`chore(ci): verify shared account workflows on both platforms`). The accompanying
`chore(docs): record KMP phase two evidence` checkpoint saves this record, the
checklist, inventory and agent contract on `codex/kmp-phase-1`. Only task sources
and intentional fixtures are included; existing README/store artwork edits are
preserved separately. No application push or PR is performed.

Private Sites publication succeeded on 2026-10-22. Site project:
`appgprj_6ad99414f1348191be93d7036ba10193`; source checkpoint:
`ade17a418b80fdaa2438552e43c467555e2a4d20`; deployment:
`appgdep_6ad9998c42388191b798e98a0b32ab47` (succeeded, owner-private).
The report was reviewed in the browser at normal and phone widths, with
working architecture selectors, fixture comparisons, iOS evidence filters and
full-image dialogs. All 79 referenced evidence asset paths exist; the browser
reported no failed loaded images or page-width overflow.
