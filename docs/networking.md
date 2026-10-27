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
