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

# Phase 4 — Build management

Starting checkpoint: `ddc0ecdcf67429acce23f223a528fd694a68c65c`, the completed
Phase 3 documentation checkpoint on `codex/kmp-phase-1`. The user authorized
continuing with Phase 4. Phase 5 is not started.

Completed 2026-10-22. [Private Phase 4 report](https://teamcityapp-kmp-phase-four-20261022.andrey-tolpeev.chatgpt.site)
is published and verified. Implementation, required checks, actual UI evidence and
local checkpoint commits are complete.

Private Site identity: `appgprj_6ada1e9ddfa08191a06ceb4f610375de`.
The Site is owner-private; its audience is unchanged.

- Report source: `d5ffc4599c06b1bdd50409170f187fb4f2acb294`
- Saved version: `appgprj_6ada1e9ddfa08191a06ceb4f610375de~appgver_45e5e2a9cc3c8191ac8fcb17f851bfd5`
- Successful private deployment: `appgdep_6ada28abf99481919f0d8e32b999280a`

## Outcome and ownership

The normal iOS app exposes Run Build and Filter Builds from History, returns their
results to the live originating visit, resolves queued-build references and opens
the shared Build Details management shell. Running and queued builds expose Stop
and Remove; completed builds expose Restart. Confirmations, the Stop/Re-add option,
progress, disabled interactions, failure notices, retries and replacement behavior
are shared with Android.

Five presentations are added: `features/run-build/presentation`,
`filter-builds/presentation`, `filter-bottom-sheet/presentation`,
`bottom-sheet/presentation` and `build-details/presentation`. Three existing APIs
move to common roots: Run Build, Filter Builds and Bottom Sheet. The existing
Quick Filter and Build Details APIs were already portable. The Java-Serializable
Filter Builds result adapter stays in `filter-builds/api/src/androidMain` for
installed Android callers.

The repository now has 108 included modules, 55 using KMP and 21 shared feature
presentations. Those modules contain 213 common production Kotlin files and 53
common test files. These are ownership counts, not code-sharing percentages or a
claim of complete iOS parity.

| Slice | Common implementation | Platform ownership |
| --- | --- | --- |
| Run Build | Actual form, option/agent/parameter dialogs, state, branch/agent loaders, submission, errors, tracking contract and result consumption | Android Hilt wrapper, Retrofit/Rx repository, installed router and analytics; native account-scoped Ktor repository and lifecycle factory |
| Filter Builds | Actual form, branches/status/flags, immutable filter and locator mapping | Android legacy serializable adapter remains; native typed result targets the exact History visit |
| Quick Filter | Actual sheet, descriptions, opposite selection, apply/failure state and one-time completion | Existing Android preference adapter; native generation-scoped preference repository and sheet registration |
| Action Bottom Sheet | Menu ordering, rows, resource descriptors, screen and ViewModel | Android platform actions remain; native exposes supported branch/configuration/project navigation actions |
| Build Details | Actual numbered header, floating controls, confirmations, operation guards, notices, saved confirmation/current/replacement state and management ViewModel | Android counts/onboarding, Hilt and platform dispatchers remain; native wraps the existing shared Overview and metadata repositories |
| Shared controls | BranchField, bottom action surface, sheet header and floating-action clearance move into `libraries/design-system` | Existing Kotlin package names and Android callers remain compatible |

Shared ViewModels use plain constructor injection. Thin `Android<Name>ViewModel`
classes carry Hilt annotations and delegate to them. The common form routes own
lifecycle collection, dialog state and navigation callbacks; Android route entry
points delegate to those routes. No second native layout is introduced.

Resources use Compose StringResource descriptors resolved in composition. The
shared confirmation and notice labels remain state-dependent. Notice persistence
stores a serializable message key rather than attempting to serialize a platform
resource ID or a StringResource object. An older transient notice carrying an
integer resource ID is safely dropped during restoration; the build remains usable.
Existing Android resources are retained
where installed callers and unmigrated integration code still need them.

## Run Build and exact request behavior

The default request retains `personal=false`, `queueAtTop=false` and
`cleanSources=true`. A single branch is selected automatically; multiple branches
leave the default choice. Branch and agent failures are independent, preserving
usable content in the other section. The agent query uses compatible agents when
the configuration advertises that endpoint and falls back to connected agents
otherwise. Native continuation links retain the transport guards.

The parameter editor now uses UI-owned TextFieldState so text, selection and the
composing region stay together while the dialog recomposes. Native verification
caught a caret reset after the empty-name validation state; retaining atomic editing
state corrects it. Technical names and values disable autocorrection. The dialog
publishes plain strings to its immutable model, and confirmation reads the current
field states before submission. This follows the [state-based text field approach](https://developer.android.com/develop/ui/compose/text/migrate-state-based).

The form permits duplicate parameter names and empty values, rejects an empty
parameter name, preserves parameter ordering and clears the complete parameter
list together. Inputs freeze during submission. Duplicate taps do not queue twice.
After a success is consumed, the same ViewModel still rejects another submission;
the destination returns its result and closes. General/forbidden failures remain
on the form and allow an explicit retry. A previous snackbar cannot clear a later
failure with a different event ID.

The native JSON request uses the documented Build envelope: `buildType.id`,
`branchName`, `personal`, optional agent/properties, and
`triggeringOptions.cleanSources/queueAtTop`. Tests inspect every flag and parameter,
including empty and duplicate values. Android keeps its existing wire DTO mapper
and compatibility tests. [JetBrains' REST contract](https://www.jetbrains.com/help/teamcity/rest/start-and-cancel-builds.html)
provides the endpoint/payload reference; the implementation record describes this
app's own retry and ownership decisions.

## Filters, restoration and navigation results

The shared `BuildFilter.toTeamCityLocator()` matches the installed Android
`BuildListFilterImpl` byte for byte. It preserves Success, Failed, Error, Cancelled,
FailedToStart, Running, Queued and None; default/explicit branch selection;
personal/pinned flags; queued `pinned:any`; and the absence of `count:10` for
Running/Queued. An explicit empty filter is distinct from History's default
all-inclusive query. Android retains its legacy result adapter and request path.

Run and Filter destinations store a recipient visit ID. Their typed result can be
delivered only while that History visit remains alive. Results are serialized by
the shared registry and consumed once on resume. Cancellation returns without a
result. Replacing/removing an owner discards its pending results; a different
account owns a different navigation session.

History's effective locator is written into its public destination arguments on
iOS. `Navigator.updateDestination` permits a same-type argument update while
retaining the visit ID, ViewModel store and pending results. It rejects a type
change, for which normal replacement and a new owner remain required. Tests cover
retained owner identity and one-time consumption. A filtered native History
restores after process relaunch without replaying the completed form submission.

Home retains its existing inline choices. Quick Filter has a shared native sheet
registration for callers that use that destination; this phase does not replace
Home's inline controls with a new navigation design. Action Bottom Sheet uses the
shared Material host's single drag handle. Native branch actions return to their
exact live Overview consumer and open History with the installed branch locator
semantics. Explicit copy/artifact actions remain capability-gated on iOS.

## Stop, Remove, Restart and queued references

Actions use the loaded build snapshot, including refreshed configuration and branch
information. A running or queued build prepares ownership-sensitive confirmation
copy; a completed build prepares Restart confirmation. Repeated confirmation or
operation requests are ignored while a confirmation, operation or replacement is
already active. Stop alone offers Re-add; a queued removal cannot accidentally set
that flag. Dismissing a confirmation sends no mutation.

Native cancellation sends a BuildCancelRequest to the confirmed build href and
resolves its acknowledgement through a full build projection before replacing the
normal details host. Restart preserves the confirmed build's configuration, branch
and properties. Its queued acknowledgement remains a reference, with a Show action.
The normal native reference loader forces a server read before entering the
complete-snapshot Build Details destination. Loading and retry states therefore
remain separate visits rather than exposing an incomplete build to sections.

Success replaces stopped/removed owners once, discarding obsolete child content.
Failures preserve a usable screen. Cancellation failures refresh Overview because
the server may already have applied the action; forbidden and general messages
remain distinct. A notice/replacement is consumed only by its live, resumed owner.
A later account switch clears the old graph and destination scopes.

The common shell disables its management and Up controls while an operation is
busy. The native Overview receives the shell's measured floating-action clearance
as bottom content padding so final rows remain reachable. Account ownership and
retained pane rules continue to apply when Home selects another live child stack.
A retained in-session operation belongs to that visit; removing its owner cancels
its ViewModel scope. Restoring navigation does not automatically submit a mutation.

## Account, authentication and cache boundary

`TeamCityManagementRepositories` owns separate Run, Filter and Build Details
contracts inside one account activation. All network operations execute through
`AccountScope.run`, which validates the current generation before and after work,
cancels the underlying request on account replacement, and rejects future calls
from an obsolete scope. Different users on the same server remain isolated.

POST uses the existing server/context/REST-link guards. It sends JSON Accept and
Content-Type headers and retains challenge-based Basic authentication. Only an
explicit initial Basic challenge can replay a write once. General HTTP/engine
errors do not trigger automatic retry. Concurrent initial challenges remember
whether their own request carried authorization, so one request learning the
credential does not prevent another initial 401 from retrying correctly.

Mutation attempts invalidate this activation's cached documents, nodes, first
pages and build snapshots. Subsequent refresh/reference loads cannot silently use
the pre-mutation cache. No credential/account/cache file format is replaced.
Android continues to use its installed Retrofit/Gson, RxCache, encrypted account
and filter serialization adapters. Their coroutine cancellation remains connected
to Rx subscription disposal.

An interrupted request may already have reached the server. Local cancellation
cannot promise rollback; this is why uncertain writes are not automatically
resubmitted. A user may explicitly retry after inspecting/refetching current state.

## Actual UI evidence and verification

Before edits, the five Android management screenshot owners passed and 1,120
management/related baseline images were copied and hashed. The final owning suites
verify the actual shared composables. All saved baseline bytes remain unchanged;
no golden was rerecorded to hide a regression. The private report provides curated
states in both themes, compact/expanded sizes and enlarged text, with the complete
hash index available for download.

Actual Android emulator integration captures complement these deterministic
Robolectric renders. Optional end-of-test capture may find that an ActivityScenario
has already closed its display surface. The capture rule retries briefly and
records a separate availability artifact instead of changing behavioral assertions.
Only genuine, useful images are presented as UI evidence.

The report includes 336 curated Android comparisons (56 states × six rendering
variants), eight selected actual Android emulator images, and 59 native frames.
The native report uses full-screen XCTest attachments from the normal signed app.
It distinguishes the historical Phase 3 read-only Overview from the new management
shell and labels newly available native forms as first baselines. All mutations
use explicitly enabled synthetic local `/context/manage` fixture contexts. The
existing approved public TeamCity guest journey remains read-only.

| Check | Result |
| --- | --- |
| Complete common Native suite | 443 tests, zero failures/errors/skips |
| Selected shared Android host suites | 126 cases, zero failures/errors; one pre-existing host skip |
| Android management, related History/Overview and shared harness golden checks | Passed; 1,120 saved baseline hashes unchanged |
| Android app units | 231 tests, zero failures |
| Android mock/prod debug, test APK and app lint | Passed |
| Android management device integration | 37/37 passed; final atomic-editor Run smoke 10/10 passed |
| Signed full iPhone regression + management suite | 17/17 passed, including read-only public guest journey |
| Dark iPhone, expanded iPad and accessibility-largest text | Passed; final phone/iPad/accessibility-largest exact-input journeys 3/3 passed |
| iOS arm64 device and Apple Silicon simulator frameworks | Both compile |
| Convention tests, formatting and CI-script tests | Passed; 66 Python script tests |
| Detailed private Sites report | Published privately; deployment succeeded on 2026-10-22 |

### Observed issues and corrections

- A string alias rendered literally in the first shared parameter dialog. The
  actual/before/comparison PNGs identified it; the portable value was corrected.
- Identical Kotlin route file names across shared/adapter modules shadowed JVM
  facade classes. Shared route files now have distinct names; Android entry-point
  APIs remain unchanged.
- Exact native input assertions exposed a caret reset after the first character
  following parameter-name validation. Waiting for the keyboard alone did not fix
  it. The shared dialog now retains text/selection/composition in TextFieldState.
  The final test asserts every typed character and the exact accepted synthetic
  payload, including flags, agent, parameters and a single write.
- The screenshot helper initially required an app bar on a native modal sheet.
  It now settles against an available screen/app anchor. The agent chooser uses
  a stable shared test tag rather than an assumed UIKit StaticText classification.
- An Android test asserted the mocked queue call immediately after a UI tap, before
  the IO coroutine ran. It now waits for that observable request with a bounded
  Mockito timeout and retains all payload assertions.
- Optional Android end-of-test screenshot capture can race a closed scenario
  surface. Availability is recorded separately from behavioral test success.
- Visual inspection found missing native floating-action clearance wiring; the
  final native host now passes the shell's measured clearance to the Overview list.

### Reproduction and limits

Use JDK 17. Keep Native account/unit suites and signed XCTest runs sequential when
they would control the same simulator. Independent UI runs use distinct devices.
Generated output lives under `build/kmp-phase-4/`, outside intentional fixtures.

```sh
./gradlew -Dorg.gradle.jvmargs=-Xmx6g --max-workers=2 iosSimulatorArm64Test \
  :shared-app:linkDebugFrameworkIosArm64 \
  :shared-app:linkDebugFrameworkIosSimulatorArm64
./gradlew :features:run-build:impl:verifyRoborazziDebug \
  :features:filter-builds:impl:verifyRoborazziDebug \
  :features:filter-bottom-sheet:impl:verifyRoborazziDebug \
  :features:bottom-sheet:impl:verifyRoborazziDebug \
  :features:build-details:impl:verifyRoborazziDebug \
  :features:build-history:impl:verifyRoborazziDebug \
  :features:build-overview:impl:verifyRoborazziDebug \
  :shared-app:verifyRoborazziAndroidHostTest
./gradlew -Dorg.gradle.jvmargs=-Xmx6g --max-workers=2 \
  :app:assembleMockDebug :app:assembleProdDebug \
  :app:assembleMockDebugAndroidTest :app:testMockDebugUnitTest \
  :app:lintMockDebug spotlessCheck
python3 scripts/kmp-platform-fixture.py --enable-mutations \
  --output build/kmp-phase-4/platform-fixture
python3 -m unittest discover -s scripts/tests
```

Signed Xcode UI runs use `CODE_SIGN_IDENTITY=- CODE_SIGNING_ALLOWED=YES
CODE_SIGNING_REQUIRED=YES`, isolated Keychain test vaults, and opt in with
`TEST_RUNNER_TEAMCITY_MANAGEMENT_UI=1`. Broader deterministic browsing opts in
with `TEST_RUNNER_TEAMCITY_FIXTURE_UI=1`. Public read-only verification is a separate
`TEST_RUNNER_TEAMCITY_LIVE_UI=1` option. The fixture binds only localhost, rejects
writes by default, and permits synthetic writes only in management contexts when
`--enable-mutations` is supplied. CI starts/stops that fixture explicitly; GitHub
Actions remains the CI platform.

Local verification uses Xcode 27.1, iOS 26.4 Apple Silicon simulators and an API 34
Android emulator. Android goldens retain their pinned API 35 settings. The iOS 16
minimum target compiles for both arm64 devices and simulators; no iOS 16 runtime,
physical Apple device, TestFlight or release-candidate run is claimed. Toolchain
versions remain those verified in the preceding phases.

## Checkpoints and next phase

- Implementation and tests: `1be3c3f552a384a22735f966fd9516c2d6c984eb`
  (`feat(kmp): share build management across platforms`).
- CI verification: `12502dce705d1ab53f8ef844cf06ff0b2c449119`
  (`chore(ci): verify shared build management on iOS`).
- Documentation/report evidence: accompanying local documentation checkpoint.

All 132 selected implementation/test files match the saved checkpoint tree, with
zero content mismatches and zero unrelated committed paths.

Application changes remain local; no application branch
push or pull request was requested. Unrelated README/store-artwork work is preserved.

Phase 5 is not started. Artifacts, downloads, browser/log, sharing, explicit
clipboard/file actions, Settings/theme, broader account management and platform
integrations remain on that checklist. Physical-device/release validation remains
in Phase 6. See [the migration checklist](kmp-migration.md).
