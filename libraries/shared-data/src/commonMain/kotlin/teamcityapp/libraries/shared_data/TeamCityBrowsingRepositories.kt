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

import io.ktor.http.encodeURLQueryComponent
import io.ktor.http.parseQueryString
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.*
import teamcityapp.features.agents.api.*
import teamcityapp.features.build_details.api.*
import teamcityapp.features.build_history.api.*
import teamcityapp.features.build_queue.api.*
import teamcityapp.features.change_details.api.*
import teamcityapp.features.changes.api.*
import teamcityapp.features.favorites.api.*
import teamcityapp.features.filter_bottom_sheet.api.*
import teamcityapp.features.home.api.*
import teamcityapp.features.navigation.api.NavigationRatingRepository
import teamcityapp.features.running_builds.api.*
import teamcityapp.features.snapshot_dependencies.api.*
import teamcityapp.features.test_details.api.*
import teamcityapp.features.tests.api.*
import teamcityapp.libraries.accounts.*
import teamcityapp.libraries.builds.BuildLaunchData
import teamcityapp.libraries.network.*
import teamcityapp.libraries.time.*

private fun JsonObject.text(key: String): String? = (this[key] as? JsonPrimitive)?.takeUnless { it is JsonNull }?.content
private fun JsonObject.count(key: String = "count") = text(key)?.toIntOrNull() ?: 0
private fun JsonObject.obj(key: String) = this[key] as? JsonObject
private fun JsonObject.items(key: String) = (this[key] as? JsonArray)?.map { it.jsonObject }.orEmpty()
private fun String.encoded() = encodeURLQueryComponent(encodeFull = true)
private fun String.entityId(): String = also { require(isNotBlank() && all { c -> c.isLetterOrDigit() || c in "_-." }) }

/** All adapters share one activation and durable preferences. No server result mutates favorite IDs. */
class TeamCityBrowsingRepositories(
    private val account: AccountScope,
    private val api: TeamCityApi,
    initial: AccountPreferences,
    private val persist: suspend ((AccountPreferences) -> AccountPreferences) -> AccountPreferences
) {
    private val preferences = MutableStateFlow(initial)
    private val writes = Mutex()
    private val accountKey get() = account.identity.key
    private suspend fun update(transform: (AccountPreferences) -> AccountPreferences) = writes.withLock {
        account.run {
            withContext(NonCancellable) {
                account.requireCurrent()
                persist(transform).also {
                    account.requireCurrent()
                    preferences.value = it
                }
            }
        }
    }
    suspend fun isFavorite(id: String): Boolean = account.run { id in preferences.value.favoriteIds }
    suspend fun setFavorite(id: String, favorite: Boolean) {
        id.entityId()
        update { it.copy(favoriteIds = if (favorite) (it.favoriteIds + id).distinct() else it.favoriteIds - id) }
    }
    val filters = object : QuickFilterRepository {
        override suspend fun apply(filter: QuickFilter) {
            update {
                when (filter) {
                    QuickFilter.RunningAll, QuickFilter.RunningFavorites -> it.copy(runningFavorites = filter == QuickFilter.RunningFavorites)
                    QuickFilter.QueuedAll, QuickFilter.QueuedFavorites -> it.copy(queueFavorites = filter == QuickFilter.QueuedFavorites)
                    QuickFilter.AgentsConnected, QuickFilter.AgentsDisconnected -> it.copy(agentsConnected = filter == QuickFilter.AgentsConnected)
                }
            }
        }
    }
    private fun AccountPreferences.homeQuery() = HomeQuery(accountKey, favoriteIds.toList(), if (runningFavorites) QuickFilter.RunningFavorites else QuickFilter.RunningAll, if (queueFavorites) QuickFilter.QueuedFavorites else QuickFilter.QueuedAll, if (agentsConnected) QuickFilter.AgentsConnected else QuickFilter.AgentsDisconnected, HomeAccount(account.identity.userName, account.identity.serverUrl))
    private suspend fun acknowledge(key: String) {
        update { it.copy(shownPrompts = it.shownPrompts + key) }
    }
    val rating = object : NavigationRatingRepository {
        override suspend fun isEligible() = account.run { !preferences.value.ratingHandled }
        override suspend fun markHandled() {
            update { it.copy(ratingHandled = true) }
        }
    }
    val historyOnboarding = object : BuildHistoryOnboardingRepository {
        override suspend fun pendingPrompts() = account.run { BuildHistoryPrompt.entries.filterNot { "history.${it.name}" in preferences.value.shownPrompts } }
        override suspend fun markShown(prompt: BuildHistoryPrompt) {
            acknowledge("history.${prompt.name}")
        }
    }
    val home = object : HomeRepository {
        override val query = preferences.map { it.homeQuery() }.distinctUntilChanged()
        override suspend fun pendingPrompts() = account.run { HomePrompt.entries.filterNot { "home.${it.name}" in preferences.value.shownPrompts }.toSet() }
        override suspend fun acknowledgePrompt(prompt: HomePrompt) {
            acknowledge("home.${prompt.name}")
        }
        override suspend fun count(query: HomeQuery, tab: HomeTab): HomeCount = account.run {
            require(query.accountKey == accountKey)
            val value = when (tab) {
                HomeTab.Favorites -> query.favoriteIds.size
                HomeTab.Running -> countBuilds(false, if (query.runningFilter == QuickFilter.RunningFavorites) query.favoriteIds else null)
                HomeTab.Queue -> countBuilds(true, if (query.queueFilter == QuickFilter.QueuedFavorites) query.favoriteIds else null)
                HomeTab.Agents -> api.document(api.apiPath("agents?includeDisconnected=${query.agentsFilter == QuickFilter.AgentsDisconnected}&fields=count"), true).count()
                HomeTab.Projects -> 0
            }
            HomeCount.Available(value)
        }
    }
    val favorites = object : FavoritesRepository {
        override suspend fun favorites(forceRefresh: Boolean): FavoriteConfigurations = account.run {
            val ids = preferences.value.favoriteIds.toList()
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
    val running = object : RunningBuildsRepository {
        override val query = preferences.map { RunningBuildsQuery(accountKey, if (it.runningFavorites) RunningBuildsFilter.Favorites else RunningBuildsFilter.All, if (it.runningFavorites) it.favoriteIds.toList() else emptyList()) }.distinctUntilChanged()
        override suspend fun builds(query: RunningBuildsQuery, forceRefresh: Boolean) = account.run {
            require(query.accountKey == accountKey)
            monitoringBuilds(false, if (query.filter == RunningBuildsFilter.Favorites) query.favoriteConfigurationIds else null, forceRefresh)
        }
    }
    val queue = object : BuildQueueRepository {
        override val query = preferences.map { BuildQueueQuery(accountKey, if (it.queueFavorites) BuildQueueFilter.Favorites else BuildQueueFilter.All, if (it.queueFavorites) it.favoriteIds.toList() else emptyList()) }.distinctUntilChanged()
        override suspend fun builds(query: BuildQueueQuery, forceRefresh: Boolean) = account.run {
            require(query.accountKey == accountKey)
            monitoringBuilds(true, if (query.filter == BuildQueueFilter.Favorites) query.favoriteConfigurationIds else null, forceRefresh)
        }
    }
    val agents = object : AgentsRepository {
        override val filter = preferences.map { if (it.agentsConnected) AgentsFilter.Connected else AgentsFilter.Disconnected }.distinctUntilChanged()
        override suspend fun agents(filter: AgentsFilter, forceRefresh: Boolean): List<Agent> = account.run {
            val occurrences = mutableMapOf<String, Int>()
            allDocuments(api.apiPath("agents?includeDisconnected=${filter == AgentsFilter.Disconnected}&fields=agent(id,name),nextHref"), "agent", forceRefresh).map { value ->
                val name = value.text("name").orEmpty()
                val index = occurrences[name] ?: 0
                occurrences[name] = index + 1
                Agent(value.text("id")?.takeIf(String::isNotBlank) ?: "name:$name:$index", name)
            }
        }
    }
    val changes = object : ChangesRepository {
        override suspend fun changes(url: String, nextHref: String?, forceRefresh: Boolean): ChangesPage = account.run {
            val path = nextHref ?: collectionPath(url, "count:10")
            val page = api.document(path, forceRefresh)
            val items = page.items("change").mapIndexed { index, summary ->
                val href = summary.text("href")?.takeIf(String::isNotBlank)
                val details = if (href == null) summary else api.document(href, forceRefresh)
                val date = details.text("date").orEmpty()
                ChangeDetails(details.text("id")?.takeIf(String::isNotBlank) ?: href ?: "$path:$index", details.text("comment").orEmpty(), details.text("username").orEmpty(), parseTeamCityLocalDate(date)?.let { formatTeamCityLocalDate(it, "dd MMM yy HH:mm") }.orEmpty(), details.obj("files")?.items("file").orEmpty().map { ChangedFile(it.text("file").orEmpty(), it.text("changeType").orEmpty()) }, details.text("version").orEmpty(), details.text("webUrl").orEmpty())
            }
            ChangesPage(items, page.text("nextHref")?.takeIf { items.isNotEmpty() && it.isNotBlank() })
        }
        override suspend fun count(url: String) = account.run { api.document(collectionPath(url, "count:${Int.MAX_VALUE}", "count"), true).count() }
    }
    val tests = object : TestsRepository {
        override suspend fun tests(url: String, filter: TestsFilter, nextHref: String?, forceRefresh: Boolean): TestsPage = account.run {
            val status = when (filter) {
                TestsFilter.Failed -> "FAILURE"
                TestsFilter.Passed -> "SUCCESS"
                TestsFilter.Ignored -> "UNKNOWN"
            }
            val path = nextHref ?: collectionPath(url, "status:$status,count:10")
            val page = api.document(path, forceRefresh)
            val items = page.items("testOccurrence").mapIndexed { index, value ->
                val href = value.text("href").orEmpty()
                TestOccurrence(
                    value.text("id")?.takeIf(String::isNotBlank) ?: href.takeIf(String::isNotBlank) ?: "$path:$index",
                    value.text("name").orEmpty(),
                    when (value.text("status")) {
                        "FAILURE" -> TestStatus.Failed
                        "SUCCESS" -> TestStatus.Passed
                        "ERROR" -> TestStatus.Error
                        else -> TestStatus.Ignored
                    },
                    href
                )
            }
            TestsPage(items, page.text("nextHref")?.takeIf { items.isNotEmpty() && it.isNotBlank() })
        }
        override suspend fun summary(url: String): TestsSummary = account.run {
            val value = api.document(collectionPath(url, "count:${Int.MAX_VALUE}", "count,passed,failed,ignored"), true)
            TestsSummary(value.count(), TestsCounts(value.count("passed"), value.count("failed"), value.count("ignored")))
        }
    }
    val metadata = object : BuildDetailsMetadataRepository {
        override suspend fun count(build: BuildLaunchData, tab: BuildDetailsSection): Int = when (tab) {
            BuildDetailsSection.Changes -> changes.count(checkNotNull(build.changes?.href))
            BuildDetailsSection.Tests -> tests.summary(checkNotNull(build.tests?.href)).total
            else -> 0
        }
        override suspend fun pendingPrompts() = account.run { BuildDetailsPrompt.entries.filterNot { "details.${it.name}" in preferences.value.shownPrompts }.toSet() }
        override suspend fun acknowledgePrompt(prompt: BuildDetailsPrompt) {
            acknowledge("details.${prompt.name}")
        }
    }
    val testDetails = object : TestDetailsRepository {
        override suspend fun testDetails(url: String): String = account.run { api.document(url).text("details").orEmpty() }
    }
    val snapshots = object : SnapshotDependenciesRepository {
        override suspend fun dependencies(buildId: String, forceRefresh: Boolean): List<BuildLaunchData> = account.run {
            val locator = "snapshotDependency:(to:(id:${buildId.entityId()}),includeInitial:true),defaultFilter:false"
            fullBuilds(api.apiPath("builds?locator=${locator.encoded()}&fields=${(BUILD_FIELDS + ",nextHref").encoded()}"), true)
        }
    }
    private fun monitoringPaths(queued: Boolean, ids: List<String>?): List<String> {
        val base = if (queued) "" else "running:true,branch:default:any,personal:false,pinned:false"
        return (ids?.distinct()?.map { listOf(base, "buildType:${it.entityId()}").filter(String::isNotEmpty).joinToString(",") } ?: listOf(base)).map {
            api.apiPath(if (queued) "buildQueue" else "builds") + if (it.isEmpty()) "" else "?locator=${it.encoded()}"
        }
    }
    private fun withFields(path: String, fields: String) = path + (if ('?' in path) "&" else "?") + "fields=" + fields.encoded()
    private suspend fun countBuilds(queued: Boolean, ids: List<String>?) = monitoringPaths(queued, ids).sumOf { api.document(withFields(it, "count"), true).count() }
    private suspend fun monitoringBuilds(queued: Boolean, ids: List<String>?, forceRefresh: Boolean) = monitoringPaths(queued, ids).flatMap { fullBuilds(withFields(it, BUILD_FIELDS + ",nextHref"), forceRefresh) }.distinctBy { it.id }
    private suspend fun fullBuilds(path: String, forceRefresh: Boolean) = allDocuments(path, "build", forceRefresh).map { it.toBuildLaunchData() }
    private suspend fun allDocuments(path: String, key: String, forceRefresh: Boolean): List<JsonObject> {
        val visited = mutableSetOf<String>()
        val result = mutableListOf<JsonObject>()
        var next: String? = path
        while (next != null) {
            require(visited.add(next)) { "Repeated TeamCity continuation URL" }
            val page = api.document(next, forceRefresh)
            val items = page.items(key)
            result += items
            next = page.text("nextHref")?.takeIf { items.isNotEmpty() && it.isNotBlank() }
        }
        return result
    }
}

/** Amend only the initial locator. Continuation URLs are passed through without reconstruction. */
internal fun collectionPath(url: String, suffix: String, fields: String? = null): String {
    val query = parseQueryString(url.substringAfter('?', ""))
    val locator = listOfNotNull(query["locator"]?.takeIf(String::isNotBlank), suffix).joinToString(",")
    val params = query.entries().filter { it.key != "locator" && (fields == null || it.key != "fields") }.flatMap { (key, values) -> values.map { key to it } } + listOf("locator" to locator) + (fields?.let { listOf("fields" to it) } ?: emptyList())
    return url.substringBefore('?') + "?" + params.joinToString("&") { (key, value) -> "${key.encoded()}=${value.encoded()}" }
}
