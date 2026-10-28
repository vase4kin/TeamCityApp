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

package teamcityapp.features.tests.impl.data

import teamcityapp.features.tests.api.*
import teamcityapp.libraries.accounts.*
import teamcityapp.libraries.network.*

class TeamCityTestsRepository(private val account: AccountScope, private val api: TeamCityApi) : TestsRepository {
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
