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

package teamcityapp.features.changes.impl.data

import teamcityapp.features.change_details.api.*
import teamcityapp.features.changes.api.*
import teamcityapp.libraries.accounts.*
import teamcityapp.libraries.network.*
import teamcityapp.libraries.time.*

class TeamCityChangesRepository(private val account: AccountScope, private val api: TeamCityApi) : ChangesRepository {
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
