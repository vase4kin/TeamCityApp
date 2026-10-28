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

package teamcityapp.features.favorites.impl.data

import kotlinx.coroutines.CancellationException
import teamcityapp.features.favorites.api.*
import teamcityapp.libraries.accounts.*
import teamcityapp.libraries.network.*

class TeamCityFavoritesRepository(private val account: AccountScope, private val api: TeamCityApi, private val preferences: AccountPreferenceState) : FavoritesRepository {
    override suspend fun favorites(forceRefresh: Boolean): FavoriteConfigurations = account.run {
        val ids = preferences.state.value.favoriteIds.toList()
        val unavailable = mutableListOf<String>()
        val configurations = ids.mapNotNull { id ->
            try {
                api.document(api.apiPath("buildTypes/id:${id.entityId()}?fields=id,name,description,projectId,projectName"), forceRefresh).toConfigurationSummary()
            } catch (c: CancellationException) {
                throw c
            } catch (_: Exception) {
                unavailable += id
                null
            }
        }
        FavoriteConfigurations(ids, configurations, unavailable)
    }
}
