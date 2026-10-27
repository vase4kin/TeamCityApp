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

package teamcityapp.features.running_builds.impl.data

import kotlinx.coroutines.flow.*
import teamcityapp.features.running_builds.api.*
import teamcityapp.libraries.accounts.*
import teamcityapp.libraries.network.*

class TeamCityRunningBuildsRepository(private val account: AccountScope, private val api: TeamCityApi, private val preferences: AccountPreferenceState) : RunningBuildsRepository {
    override val query = preferences.state.map { RunningBuildsQuery(account.identity.key, if (it.runningFavorites) RunningBuildsFilter.Favorites else RunningBuildsFilter.All, if (it.runningFavorites) it.favoriteIds.toList() else emptyList()) }.distinctUntilChanged()
    override suspend fun builds(query: RunningBuildsQuery, forceRefresh: Boolean) = account.run {
        require(query.accountKey == account.identity.key)
        api.monitoringBuilds(false, if (query.filter == RunningBuildsFilter.Favorites) query.favoriteConfigurationIds else null, forceRefresh)
    }
}
