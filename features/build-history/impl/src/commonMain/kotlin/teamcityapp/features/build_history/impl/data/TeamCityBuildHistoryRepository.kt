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

package teamcityapp.features.build_history.impl.data

import teamcityapp.features.build_history.api.*
import teamcityapp.libraries.accounts.*
import teamcityapp.libraries.network.*

class TeamCityBuildHistoryRepository(private val account: AccountScope, private val api: TeamCityApi, private val preferences: AccountPreferenceState) : BuildHistoryRepository {
    override suspend fun page(query: BuildHistoryQuery, nextHref: String?, forceRefresh: Boolean): BuildHistoryPage = account.run {
        val page = api.page(query.configurationId, query.locator, nextHref, forceRefresh)
        BuildHistoryPage(page.builds, page.nextHref)
    }
    override suspend fun isFavorite(configurationId: String): Boolean = account.run { configurationId in preferences.state.value.favoriteIds }
    override suspend fun setFavorite(configurationId: String, favorite: Boolean) {
        configurationId.entityId()
        preferences.update { it.copy(favoriteIds = if (favorite) (it.favoriteIds + configurationId).distinct() else it.favoriteIds - configurationId) }
    }
}
