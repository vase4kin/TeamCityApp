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

package teamcityapp.features.tests.impl

import teamcityapp.features.tests.api.*

internal fun testOccurrence(id: String = "1", status: TestStatus = TestStatus.Failed) = TestOccurrence(id, "BuildQueueTest.processesNextBuild", status, "/testOccurrences/$id")

internal class FakeTestsRepository : TestsRepository {
    data class Request(val url: String, val filter: TestsFilter, val next: String?, val force: Boolean)
    val requests = mutableListOf<Request>()
    var countCalls = 0
    var load: suspend (Request) -> TestsPage = { TestsPage(listOf(testOccurrence()), null) }
    var loadCount: suspend () -> Int = { 16 }
    override suspend fun tests(url: String, filter: TestsFilter, nextHref: String?, forceRefresh: Boolean): TestsPage {
        val request = Request(url, filter, nextHref, forceRefresh)
        requests += request
        return load(request)
    }
    override suspend fun count(url: String): Int {
        countCalls++
        return loadCount()
    }
}
