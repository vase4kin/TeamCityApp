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

import androidx.paging.PagingConfig
import androidx.paging.PagingSource
import androidx.paging.PagingState
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Test
import teamcityapp.features.change_details.api.ChangeDetails
import teamcityapp.features.changes.api.ChangesPage

class ChangesPagingSourceTest {
    private val repository = FakeChangesRepository()
    private fun source(force: Boolean = false) = ChangesPagingSource(repository, "build:42", force)
    private fun refresh() = PagingSource.LoadParams.Refresh<String>(null, 10, false)

    @Test fun firstRequestAllowsCacheAndPreservesOpaqueNextHref() = runTest {
        val token = "/changes?locator=count:10,start:10&fields=change(id,comment),nextHref"
        repository.load = { ChangesPage(listOf(change()), token) }
        val page = source().load(refresh()) as PagingSource.LoadResult.Page<String, ChangeDetails>
        assertEquals(listOf(change()), page.data)
        assertNull(page.prevKey)
        assertEquals(token, page.nextKey)
        assertEquals(FakeChangesRepository.Request("build:42", null, false), repository.requests.single())
    }

    @Test fun forcedRefreshDoesNotBypassCacheOnAppend() = runTest {
        val source = source(true)
        source.load(refresh())
        source.load(PagingSource.LoadParams.Append("opaque next", 10, false))
        assertEquals(listOf(true, false), repository.requests.map { it.force })
        assertEquals("opaque next", repository.requests.last().next)
    }

    @Test fun emptyPageClearsContinuationAndEndsAppend() = runTest {
        repository.load = { ChangesPage(emptyList(), null) }
        val page = source().load(refresh()) as PagingSource.LoadResult.Page<String, ChangeDetails>
        assertTrue(page.data.isEmpty())
        assertNull(page.nextKey)
    }

    @Test fun missingAndBlankContinuationAreTerminal() = runTest {
        repository.load = { ChangesPage(listOf(change()), " ") }
        assertNull((source().load(refresh()) as PagingSource.LoadResult.Page<String, ChangeDetails>).nextKey)
    }

    @Test fun failuresBecomeRetryableLoadErrors() = runTest {
        val failure = IllegalStateException("offline")
        repository.load = { throw failure }
        assertSame(failure, (source().load(refresh()) as PagingSource.LoadResult.Error<String, ChangeDetails>).throwable)
    }

    @Test fun cancellationPropagatesInsteadOfBecomingAnError() = runTest {
        repository.load = { throw CancellationException("view destroyed") }
        try {
            source().load(refresh())
            fail("Expected cancellation")
        } catch (expected: CancellationException) {
            assertEquals("view destroyed", expected.message)
        }
    }

    @Test fun refreshRestartsAtFirstPageRegardlessOfScrollAnchor() {
        val state = PagingState<String, ChangeDetails>(emptyList(), 12, PagingConfig(10), 0)
        assertNull(source().getRefreshKey(state))
    }
}
