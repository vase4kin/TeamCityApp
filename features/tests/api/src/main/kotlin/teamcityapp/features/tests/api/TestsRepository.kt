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

package teamcityapp.features.tests.api

/** A test occurrence is distinct from its test definition and from network DTOs. */
data class TestOccurrence(val id: String, val name: String, val status: TestStatus, val href: String)

enum class TestStatus { Passed, Failed, Ignored, Error }
enum class TestsFilter { Failed, Passed, Ignored }

data class TestsCounts(val passed: Int, val failed: Int, val ignored: Int) {
    fun count(filter: TestsFilter): Int = when (filter) {
        TestsFilter.Failed -> failed
        TestsFilter.Passed -> passed
        TestsFilter.Ignored -> ignored
    }
}

data class TestsPage(val items: List<TestOccurrence>, val nextHref: String?)

interface TestsRepository {
    suspend fun tests(url: String, filter: TestsFilter, nextHref: String?, forceRefresh: Boolean): TestsPage
    suspend fun count(url: String): Int
}
