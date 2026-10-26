<!--
Copyright 2026 Andrey Tolpeev
Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy at http://www.apache.org/licenses/LICENSE-2.0
Unless required by applicable law or agreed to in writing, software distributed
under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR
CONDITIONS OF ANY KIND, either express or implied. See the License for the
specific language governing permissions and limitations under the License.
-->

# iOS candidate and TestFlight preparation

This procedure prepares and inspects a candidate. Uploading, creating App Store
Connect records, accepting agreements and distributing builds require the owner's
shipping instruction. A successful unsigned archive is packaging evidence, not a
signed or distributed candidate. The migration checklist retains physical-device
qualification as a separate required gate.

## Versions and tools

Use JDK 17 and an Apple Silicon Mac. The shared conventions target iOS 17+ (owner decision on 2026-10-25);
Xcode excludes Intel simulator slices to match the configured Apple Silicon target;
CI pins Xcode 26.4, while the Phase 6 local toolchain is Xcode 27.1. Verify the
actual Xcode installation and installed simulator runtimes before choosing a
destination. SDK compilation does not establish minimum-runtime compatibility. Apple lists
iOS 17+ simulator support for the installed Xcode 27.1. Qualification must run the
selected iOS 17 runtime; increasing the deployment target alone does not pass that gate. Android API 24 uses the explicit
`-PminimumAndroidInstrumentation` profile described in `docs/kmp-phase-6.md`.

`iosApp/Version.xcconfig` supplies the iOS marketing version and is updated by
Release Please alongside `Config.versionName`. Xcode's build number defaults to
`1` for development. Supply a new `CURRENT_PROJECT_VERSION` for each upload;
check the chosen app's App Store Connect build history first. Do not derive an
upload build number from an unverified local counter.

The Xcode framework script gives optimized builds an 8 GiB Gradle compiler heap.
The default 2 GiB debug heap is insufficient for whole-program native optimization.
Run heavy Gradle builds serially; concurrent cache cleanup can contend for the
same journal/transform locks. Keep UI tests and Native unit tests from controlling
the same simulator concurrently. Select a dedicated unit-test device with
`-PteamcityIosTestDevice=<UDID>`.

## Reproducible local checks

```sh
python3 scripts/generate-ios-icon.py
./gradlew :shared-app:exportIosReleaseDependencies
python3 scripts/ios-release-audit.py \
  --dependencies shared-app/build/reports/ios-release-dependencies.tsv \
  --output build/ios-release/source-audit.json
xcodebuild -project iosApp/TeamCityApp.xcodeproj -scheme TeamCityApp \
  -configuration Release -destination 'generic/platform=iOS' \
  -derivedDataPath build/ios-release/DerivedData \
  -archivePath build/ios-release/TeamCityApp.xcarchive \
  CODE_SIGNING_ALLOWED=NO CODE_SIGNING_REQUIRED=NO archive
python3 scripts/ios-release-audit.py \
  --app build/ios-release/TeamCityApp.xcarchive/Products/Applications/TeamCityApp.app \
  --output build/ios-release/archive-audit.json
```

The icon renderer uses the original Android vector and background color, with
no screenshot scaling or replacement branding. The single 1024px iOS asset is
opaque; Apple applies the device's icon shape. Keep generated render intermediates
under `build/`, and commit the intended asset with its generator.

The dependency exporter records the resolved iOS arm64 link inputs, including
interop artifacts. The audit records external coordinates, artifact/POM hashes
and declared license metadata. It rejects missing POM/license information;
it does not infer native subdependencies from Maven metadata alone.

Refresh offline notices after changing dependencies:

1. Read Skiko's pinned `dependencies.skia` value for the resolved Skiko version.
2. Obtain the matching official iOS Release arm64 archive from JetBrains/skia.
3. Run `scripts/collect-ios-notices.py --audit <source-audit.json>
   --skia-archive <downloaded.zip> --kotlin-native <matching-toolchain-directory>`.
4. Inspect the inventory and hashes in `iosApp/TeamCityApp/ThirdPartyNotices.plist`.
   The single iOS resource retains standard Apache text, Kotlin runtime notices
   and native redistribution texts. It is packaging data, with no in-app catalog
   or reader. Review added component terms and notices before distributing.

The collector pins Skiko 0.150.2, Skia m150-a3f509bf52 and the latter's DEPS refs.
Upgrade these deliberately with the toolchain. The collector writes the packaging resource and normalizes combined
notice text to LF before computing its bundled hash, so Git checkout normalization
does not invalidate provenance. Upstream source hashes retain the original bytes. Notices extracted conservatively
from a native archive can include components eliminated from the final executable;
the artifact inventory is a link-input inventory, not a claim about retained code.

## Physical-device and signing qualification

Use `TEAMCITY_BUNDLE_ID_SUFFIX=.validation` to isolate the app and UI runner
from installed apps and their data. Omit the suffix for the final app identity. Keep test vault namespaces enabled only in Debug. For a real candidate,
select the owner's existing signing team and a profile that includes the device;
verify entitlements, Keychain access, signing expiry and device registration.
No team, certificate, password or distribution credential is embedded in source.

A paired device must be unlocked for developer-image preparation. Qualification
covers real account login/relaunch, account replacement, background cancellation,
keyboard/rotation, safe areas, native browser/share/export and VoiceOver operation.
Use read-only server journeys unless explicitly authorized to mutate that server.
Never run synthetic localhost management tests against a live TeamCity server.

For TestFlight, the owner must select the App Store Connect app record, distribution
team and new build number. Build a signed archive with those explicit settings,
inspect its archive validation report and export with the appropriate App Store
Connect distribution profile. Archive Organizer or `xcodebuild -exportArchive`
can then prepare an upload, following a separate shipping instruction. Keep
credentials in the approved local/CI secret mechanism, never in repository files.

## Read-only physical fixture

For predictable native download/export and browsing checks, bind the fixture to
one explicit private IPv4 interface on the Mac:

```sh
python3 scripts/kmp-platform-fixture.py --bind-address <private-interface-ip> \
  --output build/kmp-phase-6/physical-fixture
```

The default remains loopback. Public/wildcard bind addresses are rejected, and
`--enable-mutations` is rejected on every non-loopback interface. Physical read-only
XCTest journeys receive `TEST_RUNNER_TEAMCITY_FIXTURE_BASE_URL` as
`http://<private-interface-ip>:18081/context`, plus their existing browsing/platform
opt-ins. The test harness accepts only canonical private IPv4 addresses at that
port/path. Management XCTest journeys retain their independent loopback URL.
Stop the fixture after testing. The LAN mode does not qualify TLS: its generated
certificate remains for localhost-only certificate-consent fixtures.

The native app's local-network permission text explains connecting to the chosen
TeamCity server. The fixture test accepts only the system's specific local-network
Allow prompt; it does not grant unrelated permissions. Test captures and URLs must
use synthetic fixtures or the approved public guest server. Keep raw device
identifiers, profiles and signing details out of the report.

## Manual VoiceOver walkthrough

Run this only after automated phone control has ended. Use the already-opened
validation app; keep existing personal app/accounts separate. Enable VoiceOver in
Settings → Accessibility, then return to the validation app. Record each outcome,
the device/OS, text size and active keyboard; an automated accessibility audit
cannot fill in this record.

- Login: swipe through URL, guest, username/password, consent and submit controls.
  Labels, switch state and errors must be announced. Enter/edit a URL, including
  punctuation, with the keyboard normally used on the phone. Cancel any failed
  submission; do not provide real credentials for this walkthrough.
- Home and Account: use the approved public Demo or synthetic fixture. Activate
  Projects, Favorites and Activity; verify selected states and Running/Queue/Agents
  choices. Focus must follow content; VoiceOver scrolling must reveal offscreen rows.
  From the avatar, verify Account → Manage Accounts and the distinct Add user
  account form. Hear current-account state, server context, Add and removal labels;
  cancel Add through its discard confirmation. Sign out or remove only isolated
  synthetic test accounts, never personal accounts.
- Build browsing: open a configuration, History and a build. Traverse the overview
  actions and section links, visit a read-only section and return. One Back from
  the build must return to History without a second Overview. Expect named
  controls, meaningful focus order, no extra unlabeled stops and retained visits.
- Native actions: open and dismiss Share without sending anything; open Browser
  and return. From the read-only fixture, download its small report and verify the
  native export sheet offers Save to Files, then dismiss it. Do not trigger real
  Stop/Remove/Restart/Run, rate submission or message delivery.
- Settings/About: change and restore theme, read the app/server version context,
  open offline licenses and return. Toolbar and link actions must be independently
  reachable without duplicate empty focus targets.
- With enlarged text, rotate both ways, dismiss the keyboard, background/foreground
  and relaunch. Important controls must remain reachable and content/safe areas
  usable; verify account/visit restoration. Switch or sign out/reselect only the
  isolated test accounts. For a slow fixture download, backgrounding/departure
  must remove the in-progress transfer rather than exporting a partial file.

For any failure, record the exact screen/control, spoken label and reproduction
steps. Keep the manual gate unchecked until the owner reports the walkthrough's
results and any blocking findings have been resolved and rechecked.

## Accessibility and preview-library risks

The pinned Material 3 iOS port (1.12.0-alpha03) exposes some parent-semantics
wrappers as unlabeled VoiceOver stops. Apple controls bypass Material tooltip
wrappers, and always-expanded action groups use an equivalent static surface
with their existing colors, shape, padding and buttons. Android retains the
Material implementations. Strict signed audits verify the owned screens; they
are not a substitute for a real VoiceOver walkthrough. The version number has a
localized context label, while its visible text remains unchanged.

Material 3's native port is an alpha dependency and shared Navigation 3 is an RC.
They remain pinned rather than being upgraded opportunistically during validation.
The compatibility report and manual qualification must consider that release risk.

## Privacy and submission material

The app bundles `PrivacyInfo.xcprivacy`: app-only UserDefaults (`CA92.1`), file
metadata/content access (`0A2A.1`) and elapsed-time measurements (`35F9.1`). The
reasons cover app-owned preferences/navigation and Compose/native runtime uses;
these values are not used for fingerprinting. The manifest declares no tracking,
tracking domains or developer/SDK data collection. Re-audit the final executable
and included SDK manifests when dependencies or data flows change.

The iOS app sends authentication and requested build operations directly to the
user-selected TeamCity server. Credentials remain in this device's Keychain,
local UI/navigation preferences in app storage, and unfinished downloads in an
app-private cache. Exported Files copies and browser cookies belong to their
respective system apps. Fixed OS event labels contain no account/server/request/
file/clipboard payloads. No hosted iOS analytics/crash upload SDK is configured.
Apple diagnostic sharing remains controlled by the user's OS settings. Android's
existing Firebase integrations are separate and must retain their own disclosure.

Review [Apple's privacy definitions](https://developer.apple.com/app-store/app-privacy-details/)
against this implementation and the selected server's data practices before
submitting App Store Connect privacy answers. The proposed iOS developer-collection
answer is "Data Not Collected"; it is preparation for owner review, not a submitted
or approved declaration. Keep the public privacy policy consistent with both
platforms and clearly describe user-selected server communication.

HTTPS and Keychain use Apple system cryptography. Review
[Apple's export-compliance workflow](https://developer.apple.com/help/app-store-connect/manage-app-information/overview-of-export-compliance/)
before setting an exemption declaration. `ITSAppUsesNonExemptEncryption` remains
unset until the owner confirms the final dependency composition and distribution
territories; no export-compliance answer has been submitted.

Proposed store copy:

- Name: TeamCityApp.
- Subtitle: TeamCity builds on the go.
- Description: Connect to your TeamCity server to browse projects, monitor running
  and queued builds, review results, tests and changes, and download artifacts.
  Save favorite configurations, use build filters and manage builds when your
  server account has the required permission. Supports iPhone and iPad, system
  appearance and larger text. Requires access to a compatible TeamCity server.
- Review notes: provide a stable read-only review account/server through the
  store's approved private review fields. Do not put credentials in source or
  screenshots. The public guest demo is read-only; privileged actions depend on
  server capabilities. Explain user-consented HTTP/certificate overrides in the
  review notes and validate them against store policy before submission.

Use actual final-candidate screenshots for the required current phone/tablet
slots. Simulator evidence, test-fixture screenshots and prior Android store
materials are labeled as such; they are not submitted marketing assets. Confirm
support/privacy URLs, categories, age/content declarations, rights to branding,
localizations and region availability with the owner before shipping.

## Sources

- [Apple Xcode system requirements](https://developer.apple.com/xcode/system-requirements).

- [Apple privacy manifest files](https://developer.apple.com/documentation/bundleresources/privacy-manifest-files).
- [Kotlin/Compose privacy manifest guidance](https://kotlinlang.org/docs/multiplatform/multiplatform-privacy-manifest.html).
- [Apple accessibility audits](https://developer.apple.com/documentation/accessibility/performing-accessibility-audits-for-your-app).
- [Android test runner/orchestrator](https://developer.android.com/training/testing/instrumented-tests/androidx-test-libraries/runner).

## Simulator geometry controls

Use `python3 scripts/build-ios-geometry-control.py` to build the independent
UIKit/SwiftUI simulator fixture under `build/kmp-phase-6/GeometryControl.app`.
Install it only on an owned test simulator. Opt in to
`PhaseSixReleaseTests/testSimulatorRotationControl` with
`TEST_RUNNER_TEAMCITY_GEOMETRY_CONTROL=1`. The test captures every active screen;
it does not turn a failed landscape request into a skipped or passing product
check. Both the control and app must link the same SDK version, and the control
must render its metrics label before its observations are usable.

Header bounds default to the established 20-point portrait minimum. For a device
with independently measured native insets, use
`TEST_RUNNER_TEAMCITY_UI_EXPECTED_SAFE_AREA=<top>,<left>,<bottom>,<right>`.
The iOS 27.1 Duo control measured `0,0,34,84`, including its right-side status
area. The header must fit inside that window rectangle; malformed insets fail.
Do not derive expected insets from the shared header being tested. Keep strict
accessibility audits, original landscape assertions and actual screenshot review.
See the Phase 6 record for the retained Duo rotation limitation and passing
standard-iPhone control. Simulator controls have no account/network behavior and
are separate from the validation app and physical/manual qualification.
