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

package teamcityapp.features.changes.impl

import teamcityapp.features.change_details.api.ChangeDetails
import teamcityapp.features.change_details.api.ChangedFile
import teamcityapp.features.changes.api.ChangesPage
import teamcityapp.features.changes.api.ChangesRepository

internal fun change(id: String = "42") = ChangeDetails(id, "Keep the build queue responsive", "john-117", "30 Jul 16 00:36", listOf(ChangedFile("Build.kt", "edited")), "21312fsd1321", "https://teamcity.example/change/$id")

internal class FakeChangesRepository : ChangesRepository {
    data class Request(val url: String, val next: String?, val force: Boolean)
    val requests = mutableListOf<Request>()
    var countCalls = 0
    var load: suspend (Request) -> ChangesPage = { ChangesPage(listOf(change()), null) }
    var loadCount: suspend () -> Int = { 1 }
    override suspend fun changes(url: String, nextHref: String?, forceRefresh: Boolean): ChangesPage {
        val request = Request(url, nextHref, forceRefresh)
        requests += request
        return load(request)
    }
    override suspend fun count(url: String): Int {
        countCalls++
        return loadCount()
    }
}
