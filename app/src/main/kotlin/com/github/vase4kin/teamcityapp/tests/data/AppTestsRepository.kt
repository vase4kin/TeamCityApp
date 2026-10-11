/*
 * Copyright 2019 Andrey Tolpeev
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

package com.github.vase4kin.teamcityapp.tests.data

import com.github.vase4kin.teamcityapp.api.Repository
import javax.inject.Inject
import javax.inject.Provider
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.rx2.await
import kotlinx.coroutines.withContext
import teamcityapp.features.tests.api.TestOccurrence
import teamcityapp.features.tests.api.TestStatus
import teamcityapp.features.tests.api.TestsFilter
import teamcityapp.features.tests.api.TestsPage
import teamcityapp.features.tests.api.TestsRepository
import teamcityapp.libraries.coroutines.IoDispatcher

/** Keeps serialized legacy DTOs and cache policy behind the feature's immutable contract. */
class AppTestsRepository @Inject constructor(
    private val repository: Provider<Repository>,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher
) : TestsRepository {
    override suspend fun tests(url: String, filter: TestsFilter, nextHref: String?, forceRefresh: Boolean): TestsPage = withContext(ioDispatcher) {
        val status = when (filter) {
            TestsFilter.Failed -> "FAILURE"
            TestsFilter.Passed -> "SUCCESS"
            TestsFilter.Ignored -> "UNKNOWN"
        }
        val pageUrl = nextHref ?: "$url,status:$status,count:10"
        val page = repository.get().listTestOccurrences(pageUrl, forceRefresh).await()
        val items = page.objects.mapIndexed { index, legacy ->
            val href = legacy.href.orEmpty()
            TestOccurrence(
                id = legacy.id?.takeIf { it.isNotBlank() } ?: href.takeIf { it.isNotBlank() } ?: "$pageUrl:$index",
                name = legacy.name.orEmpty(),
                status = when (legacy.status) {
                    "FAILURE" -> TestStatus.Failed
                    "SUCCESS" -> TestStatus.Passed
                    "ERROR" -> TestStatus.Error
                    else -> TestStatus.Ignored
                },
                href = href
            )
        }
        TestsPage(items, page.nextHref?.takeIf { items.isNotEmpty() && it.isNotBlank() })
    }

    override suspend fun count(url: String): Int = withContext(ioDispatcher) {
        repository.get().listTestOccurrences("$url,count:${Int.MAX_VALUE}&fields=count", true).await().count
    }
}
