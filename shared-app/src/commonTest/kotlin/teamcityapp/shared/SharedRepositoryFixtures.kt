/*
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
 */

package teamcityapp.shared

import teamcityapp.features.artifacts.api.ArtifactFileStore
import teamcityapp.features.artifacts.api.ArtifactTarget
import teamcityapp.libraries.accounts.*
import teamcityapp.libraries.network.TeamCityApi
import teamcityapp.libraries.shared_data.TeamCityAccountGraph

/** Exercise the production composition with deterministic transport and durable storage. */
internal fun sharedRepositoryFixture(
    account: AccountScope,
    api: TeamCityApi,
    initial: AccountPreferences,
    appLaunchId: String,
    persist: suspend ((AccountPreferences) -> AccountPreferences) -> AccountPreferences
): SharedAccountFeatures = SharedAccountFeatures(
    TeamCityAccountGraph(account, api, AccountPreferenceState(account, initial, persist)),
    object : ArtifactFileStore {
        override fun create(target: ArtifactTarget) = error("This fixture does not download files")
    },
    appLaunchId
)
