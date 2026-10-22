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

# KMP module inventory

Inventory captured during Phase 1. Counts cover production `main`, `prod`, and `mock` sources and migrated `commonMain`/platform sources. Android/JVM imports are direct import counts, not a complete transitive-dependency audit. Legacy DTO/cache compatibility stays behind Android adapters until its owning phase migrates.

| Module | Current ownership | Migration phase | Source files | Android imports | Java/JVM imports |
| --- | --- | --- | ---: | ---: | ---: |
| `:app` | Android host and composition; retained | 2–6 | 56 | 20 | 43 |
| `:libraries:api` | Android; extract portable contracts/behavior | 2 | 4 | 0 | 2 |
| `:libraries:data` | Android; extract portable contracts/behavior | 2 | 47 | 1 | 40 |
| `:libraries:session` | Android; extract portable contracts/behavior | 2 | 6 | 2 | 3 |
| `:libraries:app-rating` | Android; extract portable contracts/behavior | 5 | 3 | 5 | 1 |
| `:libraries:coroutines` | Android; extract portable contracts/behavior | 2 | 2 | 0 | 2 |
| `:libraries:theme` | Android adapter + shared theme dependency | 1 | 16 | 3 | 0 |
| `:libraries:resources` | Android; extract portable contracts/behavior | 2 | 0 | 0 | 0 |
| `:libraries:chrome-tabs` | Android; extract portable contracts/behavior | 5 | 2 | 3 | 0 |
| `:libraries:storage` | Android; extract portable contracts/behavior | 2 | 1 | 0 | 0 |
| `:libraries:storage-models` | Android; extract portable contracts/behavior | 2 | 1 | 0 | 3 |
| `:libraries:cache-manager` | Android; extract portable contracts/behavior | 2 | 1 | 0 | 0 |
| `:libraries:onboarding` | Android; extract portable contracts/behavior | 3 | 2 | 2 | 0 |
| `:libraries:security` | Android; extract portable contracts/behavior | 2 | 2 | 2 | 7 |
| `:libraries:remote` | Android; extract portable contracts/behavior | 2 | 5 | 2 | 1 |
| `:features:splash:api` | Android; migrate feature slice | 2 | 3 | 0 | 0 |
| `:features:splash:impl` | Android; migrate feature slice | 2 | 8 | 0 | 5 |
| `:features:test-details:api` | Android; migrate feature slice | 3 | 2 | 0 | 0 |
| `:features:test-details:impl` | Android; migrate feature slice | 3 | 11 | 4 | 3 |
| `:features:about:api` | Shared + Android navigation adapter | 1 | 3 | 0 | 0 |
| `:features:about:impl` | Android About adapters and verification | 1 | 6 | 5 | 2 |
| `:features:about:presentation` | Shared Kotlin/Compose | 1 | 4 | 0 | 0 |
| `:libraries:design-system` | Shared Kotlin/Compose | 1 | 10 | 0 | 0 |
| `:shared-app` | Shared preview composition + iOS entry | 1 | 2 | 0 | 0 |
| `:features:manage-accounts:api` | Android; migrate feature slice | 5 | 3 | 0 | 0 |
| `:features:manage-accounts:impl` | Android; migrate feature slice | 5 | 10 | 2 | 5 |
| `:features:drawer:api` | Android; migrate feature slice | 5 | 2 | 0 | 0 |
| `:features:drawer:impl` | Android; migrate feature slice | 5 | 11 | 0 | 5 |
| `:features:settings:api` | Android; migrate feature slice | 5 | 1 | 0 | 0 |
| `:features:settings:impl` | Android; migrate feature slice | 5 | 9 | 0 | 3 |
| `:libraries:app-theme` | Android; extract portable contracts/behavior | 5 | 6 | 2 | 5 |
| `:features:change-details:api` | Android; migrate feature slice | 3 | 2 | 0 | 0 |
| `:features:change-details:impl` | Android; migrate feature slice | 3 | 11 | 1 | 3 |
| `:features:properties:api` | Android; migrate feature slice | 3 | 2 | 0 | 0 |
| `:features:properties:impl` | Android; migrate feature slice | 3 | 6 | 2 | 2 |
| `:libraries:authentication` | Android; extract portable contracts/behavior | 2 | 4 | 3 | 1 |
| `:features:login:api` | Android; migrate feature slice | 2 | 3 | 0 | 0 |
| `:features:login:impl` | Android; migrate feature slice | 2 | 7 | 0 | 3 |
| `:features:create-account:api` | Android; migrate feature slice | 2 | 2 | 0 | 0 |
| `:features:create-account:impl` | Android; migrate feature slice | 2 | 7 | 0 | 3 |
| `:features:run-build:api` | Android; migrate feature slice | 4 | 3 | 0 | 0 |
| `:features:run-build:impl` | Android; migrate feature slice | 4 | 8 | 1 | 5 |
| `:features:filter-builds:api` | Android; migrate feature slice | 4 | 4 | 0 | 1 |
| `:features:filter-builds:impl` | Android; migrate feature slice | 4 | 8 | 1 | 4 |
| `:features:filter-bottom-sheet:api` | Android; migrate feature slice | 4 | 3 | 0 | 0 |
| `:features:filter-bottom-sheet:impl` | Android; migrate feature slice | 4 | 7 | 2 | 3 |
| `:features:bottom-sheet:api` | Android; migrate feature slice | 4 | 2 | 0 | 0 |
| `:features:bottom-sheet:impl` | Android; migrate feature slice | 4 | 5 | 1 | 2 |
| `:libraries:clipboard` | Android; extract portable contracts/behavior | 5 | 3 | 3 | 1 |
| `:libraries:list-state` | Android; extract portable contracts/behavior | 3 | 2 | 0 | 2 |
| `:libraries:list-ui` | Android; extract portable contracts/behavior | 3 | 4 | 0 | 0 |
| `:features:agents:api` | Android; migrate feature slice | 3 | 3 | 0 | 0 |
| `:features:agents:impl` | Android; migrate feature slice | 3 | 6 | 0 | 2 |
| `:features:changes:api` | Android; migrate feature slice | 3 | 2 | 0 | 0 |
| `:features:changes:impl` | Android; migrate feature slice | 3 | 10 | 1 | 4 |
| `:features:tests:api` | Android; migrate feature slice | 3 | 2 | 0 | 0 |
| `:features:tests:impl` | Android; migrate feature slice | 3 | 8 | 1 | 4 |
| `:libraries:build-configurations` | Android; extract portable contracts/behavior | 2 | 1 | 0 | 0 |
| `:features:navigation:api` | Android; migrate feature slice | 2 | 3 | 0 | 0 |
| `:features:navigation:impl` | Android; migrate feature slice | 2 | 11 | 2 | 6 |
| `:features:favorites:api` | Android; migrate feature slice | 3 | 3 | 0 | 0 |
| `:features:favorites:impl` | Android; migrate feature slice | 3 | 9 | 0 | 5 |
| `:libraries:builds` | Android; extract portable contracts/behavior | 2 | 2 | 0 | 1 |
| `:libraries:build-ui` | Android; extract portable contracts/behavior | 3 | 4 | 0 | 0 |
| `:features:build-history:api` | Android; migrate feature slice | 2 | 4 | 0 | 1 |
| `:features:build-history:impl` | Android; migrate feature slice | 2 | 16 | 1 | 12 |
| `:features:running-builds:api` | Android; migrate feature slice | 3 | 3 | 0 | 0 |
| `:features:running-builds:impl` | Android; migrate feature slice | 3 | 6 | 0 | 2 |
| `:features:build-queue:api` | Android; migrate feature slice | 3 | 3 | 0 | 0 |
| `:features:build-queue:impl` | Android; migrate feature slice | 3 | 6 | 0 | 2 |
| `:features:snapshot-dependencies:api` | Android; migrate feature slice | 3 | 2 | 0 | 0 |
| `:features:snapshot-dependencies:impl` | Android; migrate feature slice | 3 | 10 | 1 | 5 |
| `:features:build-artifacts:api` | Android; migrate feature slice | 5 | 5 | 2 | 0 |
| `:features:build-artifacts:impl` | Android; migrate feature slice | 5 | 12 | 4 | 7 |
| `:features:build-overview:api` | Android; migrate feature slice | 2 | 4 | 0 | 0 |
| `:features:build-overview:impl` | Android; migrate feature slice | 2 | 12 | 1 | 8 |
| `:libraries:navigation` | Android; extract portable contracts/behavior | 2 | 12 | 1 | 2 |
| `:features:home:api` | Android; migrate feature slice | 3 | 2 | 0 | 0 |
| `:features:home:impl` | Android; migrate feature slice | 3 | 7 | 2 | 4 |
| `:features:build-details:api` | Android; migrate feature slice | 2 | 6 | 0 | 0 |
| `:features:build-details:impl` | Android; migrate feature slice | 2 | 14 | 5 | 14 |

## Platform boundaries

- `iosApp` is the native Xcode/Swift host and UI smoke-test target; its initial screen is deterministic About.
- `features/about/impl` is retained as the Android adapter and Roborazzi harness. It consumes the shared presentation rather than duplicating it.
- `libraries/theme` keeps Android styles/resources/components not needed by About; portable definitions move to `libraries/design-system` with existing Kotlin packages preserved.
- `libraries/navigation` and the app account root remain Android in Phase 1. Their complete visit/result/session contracts migrate in Phase 2.
- Retrofit/Gson/RxCache DTOs and encrypted Android accounts are unchanged in Phase 1.
- Shared code cannot import Android, Java reflection, or Hilt. Platform implementations stay explicitly scoped.


## Phase 2 ownership delta (2026-10-22)

The preceding table is the historical Phase 1 inventory. Phase 2 ends with
92 included modules, of which 24 use the KMP conventions. Eleven modules were
added and nine existing modules converted. Counts below are production Kotlin
files in commonMain and androidMain/iosMain; they exclude tests and resources.
Module count does not measure shared code volume or feature parity.

| KMP module | Common files | Platform files |
| --- | ---: | ---: |
| `:features:about:api` | 2 | 1 |
| `:features:about:presentation` | 4 | 0 |
| `:libraries:design-system` | 16 | 0 |
| `:shared-app` | 5 | 2 |
| `:features:login:api` | 3 | 0 |
| `:libraries:list-state` | 2 | 0 |
| `:libraries:build-configurations` | 1 | 0 |
| `:features:navigation:api` | 3 | 0 |
| `:libraries:builds` | 1 | 1 |
| `:features:build-history:api` | 3 | 1 |
| `:features:build-overview:api` | 4 | 0 |
| `:libraries:navigation` | 11 | 1 |
| `:features:build-details:api` | 6 | 0 |
| `:libraries:accounts` | 2 | 1 |
| `:libraries:network` | 2 | 2 |
| `:libraries:authentication-shared` | 3 | 0 |
| `:features:login:presentation` | 2 | 0 |
| `:libraries:list-presentation` | 3 | 0 |
| `:features:navigation:presentation` | 4 | 0 |
| `:libraries:time` | 1 | 2 |
| `:libraries:build-presentation` | 4 | 0 |
| `:features:build-overview:presentation` | 6 | 0 |
| `:features:build-history:presentation` | 8 | 0 |
| `:libraries:shared-data` | 2 | 0 |

The new modules are accounts, network, shared-data, authentication-shared,
list-presentation, build-presentation, time, and the Login, Navigation, Build
History and Build Overview presentation modules. The converted modules are
those four feature APIs, Build Details API, builds, build-configurations,
list-state and navigation.

Android impl/theme/list-ui/build-ui/authentication modules retain adapters,
platform integrations and screenshot harnesses. Android data/session/security
modules retain installed encrypted account and Retrofit/RxCache compatibility.
iOS now uses the real shared account graph instead of the About-only preview.
See [Phase 2](kmp-phase-2.md) for behavior, UI differences and verification.
