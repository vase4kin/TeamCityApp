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

package teamcityapp.features.build_history.impl

import teamcityapp.features.build_history.api.*
import teamcityapp.features.build_history.impl.tracker.BuildHistoryTracker
import teamcityapp.libraries.builds.*

internal fun historyBuild(id: String = "42", state: String = "finished") = BuildLaunchData(
    id, "/app/rest/builds/id:$id", webUrl = "https://example.com/viewLog.html?buildId=$id", number = id,
    state = state, status = "SUCCESS", statusText = "Tests passed", branchName = "main",
    buildTypeId = "TeamCityApp_Build",
    configuration = BuildConfigurationData("TeamCityApp_Build", "Build Android", "TeamCityApp", "TeamCityApp"),
    startDate = "20261010T102030+0700", tests = BuildTests("/tests", passed = 10), changes = BuildCollectionLink("/changes", 2)
)
internal class FakeHistoryRepository : BuildHistoryRepository {
    data class Request(val query: BuildHistoryQuery, val next: String?, val force: Boolean)
    val requests = mutableListOf<Request>()
    var loadPage: suspend (Request) -> BuildHistoryPage = { BuildHistoryPage(listOf(historyBuild())) }
    var favoriteCalls = 0
    var loadFavorite: suspend () -> Boolean = { false }
    val writes = mutableListOf<Pair<String, Boolean>>()
    var writeFavorite: suspend (String, Boolean) -> Unit = { _, _ -> }
    val queuedRequests = mutableListOf<String>()
    var loadQueued: suspend (String) -> BuildLaunchData = { historyBuild("queued") }
    override suspend fun page(query: BuildHistoryQuery, nextHref: String?, forceRefresh: Boolean): BuildHistoryPage {
        val request = Request(query, nextHref, forceRefresh)
        requests += request
        return loadPage(request)
    }
    override suspend fun queuedBuild(href: String): BuildLaunchData {
        queuedRequests += href
        return loadQueued(href)
    }
    override suspend fun isFavorite(configurationId: String): Boolean {
        favoriteCalls++
        return loadFavorite()
    }
    override suspend fun setFavorite(configurationId: String, favorite: Boolean) {
        writes += configurationId to favorite
        writeFavorite(configurationId, favorite)
    }
}
internal class FakeHistoryOnboarding : BuildHistoryOnboardingRepository {
    var loads = 0
    var pending: suspend () -> List<BuildHistoryPrompt> = { emptyList() }
    val shown = mutableListOf<BuildHistoryPrompt>()
    var write: suspend (BuildHistoryPrompt) -> Unit = {}
    override suspend fun pendingPrompts(): List<BuildHistoryPrompt> {
        loads++
        return pending()
    }
    override suspend fun markShown(prompt: BuildHistoryPrompt) {
        shown += prompt
        write(prompt)
    }
}
internal class FakeHistoryTracker : BuildHistoryTracker {
    var views = 0
    var runs = 0
    var queued = 0
    override fun viewShown() {
        views++
    }
    override fun runBuildPressed() {
        runs++
    }
    override fun queuedBuildRequested() {
        queued++
    }
}
