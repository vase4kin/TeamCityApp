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

package teamcityapp.libraries.shared_data

import kotlinx.coroutines.Job
import teamcityapp.features.build_history.api.*
import teamcityapp.features.build_overview.api.BuildOverviewRepository
import teamcityapp.features.navigation.api.NavigationEntry
import teamcityapp.features.navigation.api.NavigationRepository
import teamcityapp.libraries.accounts.*
import teamcityapp.libraries.network.*

/** One account activation owns the repository cache and engine, and cancels both on replacement. */
class TeamCityAccountGraph(
    val account: AccountScope,
    private val api: TeamCityApi,
    val browsing: TeamCityBrowsingRepositories? = null,
    artifactStore: ArtifactFileStore? = null
) : NavigationRepository,
    BuildOverviewRepository,
    BuildHistoryRepository {
    val artifacts = artifactStore?.let { TeamCityArtifactsRepository(account, api, it) }
    val management = TeamCityManagementRepositories(account, api)

    init {
        account.coroutineScope.coroutineContext[Job]!!.invokeOnCompletion { api.close() }
    }

    suspend fun invalidateCache() = api.invalidateCache()

    suspend fun serverInfo(): teamcityapp.features.about.api.AboutServerInfo = account.run { api.server().let { teamcityapp.features.about.api.AboutServerInfo(it.version, it.webUrl) } }

    override suspend fun entries(projectId: String, forceRefresh: Boolean): List<NavigationEntry> = account.run {
        val page = api.entries(projectId, forceRefresh)
        page.projects.map { NavigationEntry.Project(it, page.descriptions[it.id]) } +
            page.configurations.map { NavigationEntry.Configuration(it) }
    }
    override suspend fun page(query: BuildHistoryQuery, nextHref: String?, forceRefresh: Boolean): BuildHistoryPage = account.run {
        val page = api.page(query.configurationId, query.locator, nextHref, forceRefresh)
        BuildHistoryPage(page.builds, page.nextHref)
    }
    override suspend fun isFavorite(configurationId: String): Boolean = checkNotNull(browsing).isFavorite(configurationId)
    override suspend fun setFavorite(configurationId: String, favorite: Boolean): Unit = checkNotNull(browsing).setFavorite(configurationId, favorite)
    override suspend fun build(href: String, forceRefresh: Boolean) = account.run { api.build(href, forceRefresh) }

    companion object {
        suspend fun create(sessions: AccountSessionManager, account: AccountScope, artifactStore: ArtifactFileStore? = null, responseCache: ResponseCacheStore? = null, clients: TeamCityClientFactory = PlatformTeamCityClientFactory, appLaunchId: String): TeamCityAccountGraph {
            val secret = sessions.secret(account)
            val api = TeamCityApi(TeamCityTransport(clients.create(secret.identity), secret, httpConsent = true, cache = TeamCityResponseCache(account.identity.key, responseCache)))
            return TeamCityAccountGraph(account, api, TeamCityBrowsingRepositories(account, api, sessions.preferences(account), appLaunchId) { transform -> sessions.updatePreferences(account, transform) }, artifactStore)
        }
    }
}
