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

package com.github.vase4kin.teamcityapp.tests.data

import com.github.vase4kin.teamcityapp.api.Repository
import com.github.vase4kin.teamcityapp.tests.api.TestOccurrences
import io.reactivex.Single
import javax.inject.Provider
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.async
import kotlinx.coroutines.test.*
import org.junit.Assert.*
import org.junit.Test
import org.mockito.kotlin.*
import teamcityapp.features.test_details.repository.models.TestOccurrence as LegacyTest
import teamcityapp.features.tests.api.TestStatus
import teamcityapp.features.tests.api.TestsFilter

@OptIn(ExperimentalCoroutinesApi::class)
class AppTestsRepositoryTest {
    private fun page(rows: List<LegacyTest>, next: String? = null): TestOccurrences = mock {
        on { objects } doReturn rows
        on { nextHref } doReturn next
    }

    @Test fun mapsStatusesInServerOrderWithoutDependingOnCountField() = runTest {
        val legacy = mock<Repository>()
        val rows = listOf(
            LegacyTest("Failed", "FAILURE", "/tests/5").apply { id = "5" },
            LegacyTest("Passed", "SUCCESS", "/tests/3"),
            LegacyTest("Ignored", "UNKNOWN", "/tests/2"),
            LegacyTest("Error", "ERROR", "/tests/1"),
            LegacyTest("Other", "NEW_STATUS", "/tests/4")
        )
        doReturn(Single.just(page(rows, "opaque&start=10"))).whenever(legacy).listTestOccurrences("build:42,status:FAILURE,count:10", false)
        val result = AppTestsRepository(Provider { legacy }, StandardTestDispatcher(testScheduler)).tests("build:42", TestsFilter.Failed, null, false)
        assertEquals(listOf("5", "/tests/3", "/tests/2", "/tests/1", "/tests/4"), result.items.map { it.id })
        assertEquals(listOf(TestStatus.Failed, TestStatus.Passed, TestStatus.Ignored, TestStatus.Error, TestStatus.Ignored), result.items.map { it.status })
        assertEquals(rows.map { it.name }, result.items.map { it.name })
        assertEquals("opaque&start=10", result.nextHref)
    }

    @Test fun formatsEachFilterAndPreservesForcedRefreshAndOpaqueAppendRequests() = runTest {
        val legacy = mock<Repository>()
        doReturn(Single.just(page(emptyList()))).whenever(legacy).listTestOccurrences(any(), any())
        val repository = AppTestsRepository(Provider { legacy }, StandardTestDispatcher(testScheduler))
        TestsFilter.entries.forEach { repository.tests("build:42", it, null, false) }
        repository.tests("build:42", TestsFilter.Passed, null, true)
        repository.tests("build:42", TestsFilter.Ignored, "opaque&start=10", true)
        verify(legacy).listTestOccurrences("build:42,status:FAILURE,count:10", false)
        verify(legacy).listTestOccurrences("build:42,status:SUCCESS,count:10", false)
        verify(legacy).listTestOccurrences("build:42,status:UNKNOWN,count:10", false)
        verify(legacy).listTestOccurrences("build:42,status:SUCCESS,count:10", true)
        verify(legacy).listTestOccurrences("opaque&start=10", true)
    }

    @Test fun emptyPagesDiscardStaleContinuation() = runTest {
        val legacy = mock<Repository>()
        doReturn(Single.just(page(emptyList(), "stale"))).whenever(legacy).listTestOccurrences(any(), any())
        val result = AppTestsRepository(Provider { legacy }, StandardTestDispatcher(testScheduler)).tests("build:42", TestsFilter.Failed, null, false)
        assertTrue(result.items.isEmpty())
        assertNull(result.nextHref)
    }

    @Test fun missingIdsAndHrefsHaveStableDistinctPageKeys() = runTest {
        val legacy = mock<Repository>()
        doReturn(Single.just(page(listOf(LegacyTest(), LegacyTest())))).whenever(legacy).listTestOccurrences(any(), any())
        val repository = AppTestsRepository(Provider { legacy }, StandardTestDispatcher(testScheduler))
        val first = repository.tests("build:42", TestsFilter.Failed, null, false)
        assertEquals(listOf("build:42,status:FAILURE,count:10:0", "build:42,status:FAILURE,count:10:1"), first.items.map { it.id })
        assertEquals(first, repository.tests("build:42", TestsFilter.Failed, null, false))
    }

    @Test fun cancellationDisposesPendingPageAndCountSubscriptions() = runTest {
        val legacy = mock<Repository>()
        var disposals = 0
        doReturn(Single.never<TestOccurrences>().doOnDispose { disposals++ }).whenever(legacy).listTestOccurrences(any(), any())
        val repository = AppTestsRepository(Provider { legacy }, StandardTestDispatcher(testScheduler))
        val page = async { repository.tests("build:42", TestsFilter.Failed, null, false) }
        val count = async { repository.count("build:42") }
        runCurrent()
        page.cancel()
        count.cancel()
        runCurrent()
        assertEquals(2, disposals)
    }

    @Test fun countUsesUnfilteredForcedRequestAndPropagatesOptionalFailure() = runTest {
        val legacy = mock<Repository>()
        val url = "build:42,count:${Int.MAX_VALUE}&fields=count"
        doReturn(Single.just(TestOccurrences(17))).whenever(legacy).listTestOccurrences(url, true)
        val repository = AppTestsRepository(Provider { legacy }, StandardTestDispatcher(testScheduler))
        assertEquals(17, repository.count("build:42"))
        doReturn(Single.error<TestOccurrences>(IllegalStateException("offline"))).whenever(legacy).listTestOccurrences(url, true)
        assertEquals("offline", runCatching { repository.count("build:42") }.exceptionOrNull()?.message)
    }

    @Test fun resolvesCurrentAccountForEachRequest() = runTest {
        val first = mock<Repository>()
        val second = mock<Repository>()
        doReturn(Single.just(page(emptyList()))).whenever(first).listTestOccurrences(any(), any())
        doReturn(Single.just(page(emptyList()))).whenever(second).listTestOccurrences(any(), any())
        var current = first
        val repository = AppTestsRepository(Provider { current }, StandardTestDispatcher(testScheduler))
        repository.tests("build:42", TestsFilter.Failed, null, false)
        current = second
        repository.tests("build:42", TestsFilter.Passed, "next", true)
        verify(first).listTestOccurrences("build:42,status:FAILURE,count:10", false)
        verify(second).listTestOccurrences("next", true)
    }
}
