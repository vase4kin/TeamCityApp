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

import androidx.paging.PagingConfig
import androidx.paging.PagingSource
import androidx.paging.PagingState
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Test
import teamcityapp.features.tests.api.*

class TestsPagingSourceTest {
    private val repository = FakeTestsRepository()
    private fun source(filter: TestsFilter = TestsFilter.Failed, force: Boolean = false) = TestsPagingSource(repository, "build:42", filter, force)
    private fun refresh() = PagingSource.LoadParams.Refresh<String>(null, 10, false)

    @Test fun initialRequestAllowsCacheAndKeepsOpaqueContinuation() = runTest {
        val token = "/tests?locator=status:FAILURE,count:10,start:10&fields=testOccurrence(id,name),nextHref"
        repository.load = { TestsPage(listOf(testOccurrence()), token) }
        val page = source().load(refresh()) as PagingSource.LoadResult.Page
        assertEquals(listOf(testOccurrence()), page.data)
        assertEquals(token, page.nextKey)
        assertNull(page.prevKey)
        assertEquals(FakeTestsRepository.Request("build:42", TestsFilter.Failed, null, false), repository.requests.single())
    }

    @Test fun appendAlwaysBypassesCacheAndUsesExactContinuation() = runTest {
        val source = source()
        source.load(refresh())
        source.load(PagingSource.LoadParams.Append("opaque next", 10, false))
        assertEquals(listOf(false, true), repository.requests.map { it.force })
        assertEquals("opaque next", repository.requests.last().next)
    }

    @Test fun eachFilterIsPassedToRepositoryAndPullRefreshBypassesCache() = runTest {
        for (filter in TestsFilter.entries) source(filter, true).load(refresh())
        assertEquals(TestsFilter.entries, repository.requests.map { it.filter })
        assertTrue(repository.requests.all { it.force })
    }

    @Test fun emptyResponseClearsPreviousContinuation() = runTest {
        repository.load = { TestsPage(emptyList(), null) }
        val page = source().load(refresh()) as PagingSource.LoadResult.Page
        assertTrue(page.data.isEmpty())
        assertNull(page.nextKey)
    }

    @Test fun blankContinuationIsTerminal() = runTest {
        repository.load = { TestsPage(listOf(testOccurrence()), " ") }
        assertNull((source().load(refresh()) as PagingSource.LoadResult.Page).nextKey)
    }

    @Test fun networkFailureIsRetryable() = runTest {
        val failure = IllegalStateException("offline")
        repository.load = { throw failure }
        assertSame(failure, (source().load(refresh()) as PagingSource.LoadResult.Error).throwable)
    }

    @Test fun cancellationDoesNotRenderAsError() = runTest {
        repository.load = { throw CancellationException("filter replaced") }
        try {
            source().load(refresh())
            fail("Expected cancellation")
        } catch (expected: CancellationException) {
            assertEquals("filter replaced", expected.message)
        }
    }

    @Test fun refreshAlwaysStartsFromFirstPage() {
        assertNull(source().getRefreshKey(PagingState<String, TestOccurrence>(emptyList(), 12, PagingConfig(10), 0)))
    }
}
