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

import androidx.paging.*
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Test
import teamcityapp.features.build_history.api.*
import teamcityapp.libraries.builds.BuildLaunchData

class BuildHistoryPagingSourceTest {
    private val repository = FakeHistoryRepository()
    private val query = BuildHistoryQuery("configuration")
    private fun source(force: Boolean = false) = BuildHistoryPagingSource(repository, query, force)
    private fun refresh() = PagingSource.LoadParams.Refresh<String>(null, 10, false)

    @Test fun defaultLocatorPreservesAnyPersonalAndPinnedAndFirstPageAllowsCache() = runTest {
        val next = "/builds?locator=buildType:(id:configuration),start:10&fields=build(id,href),nextHref"
        repository.loadPage = { BuildHistoryPage(listOf(historyBuild()), next) }
        val page = source().load(refresh()) as PagingSource.LoadResult.Page<String, BuildLaunchData>
        assertEquals(listOf(historyBuild()), page.data)
        assertEquals(next, page.nextKey)
        assertNull(page.prevKey)
        assertEquals(FakeHistoryRepository.Request(query, null, false), repository.requests.single())
        assertEquals("state:any,branch:default:any,personal:any,pinned:any,canceled:any,failedToStart:any,count:10", query.locator)
    }

    @Test fun forcedRefreshAndContinuationBypassCacheWithoutRewritingLocatorOrHref() = runTest {
        source(true).load(refresh())
        source().load(PagingSource.LoadParams.Append("opaque&branch:name:feature/test", 10, false))
        assertEquals(listOf(true, true), repository.requests.map { it.force })
        assertEquals("opaque&branch:name:feature/test", repository.requests.last().next)
    }

    @Test fun queuedRowsMoveFirstWithoutReorderingOthersAndPreserveFullLaunchData() = runTest {
        val builds = listOf(historyBuild("finished"), historyBuild("queued1", "queued"), historyBuild("running", "running"), historyBuild("queued2", "queued"))
        repository.loadPage = { BuildHistoryPage(builds) }
        val page = source().load(refresh()) as PagingSource.LoadResult.Page<String, BuildLaunchData>
        assertEquals(listOf(builds[1], builds[3], builds[0], builds[2]), page.data)
    }

    @Test fun emptyPageEndsPagingEvenIfServerRetainsStaleContinuation() = runTest {
        repository.loadPage = { BuildHistoryPage(emptyList(), "stale next") }
        assertNull((source().load(refresh()) as PagingSource.LoadResult.Page<String, BuildLaunchData>).nextKey)
    }

    @Test fun blankContinuationIsTerminal() = runTest {
        repository.loadPage = { BuildHistoryPage(listOf(historyBuild()), " ") }
        assertNull((source().load(refresh()) as PagingSource.LoadResult.Page<String, BuildLaunchData>).nextKey)
    }

    @Test fun errorsCanRetryAndCancellationNeverBecomesLoadError() = runTest {
        val offline = IllegalStateException("offline")
        repository.loadPage = { throw offline }
        assertSame(offline, (source().load(refresh()) as PagingSource.LoadResult.Error<String, BuildLaunchData>).throwable)
        repository.loadPage = { throw CancellationException("paused") }
        try {
            source().load(refresh())
            fail("Expected cancellation")
        } catch (expected: CancellationException) {
            assertEquals("paused", expected.message)
        }
    }

    @Test fun refreshAlwaysRestartsAtFirstPage() {
        assertNull(source().getRefreshKey(PagingState(emptyList(), 20, PagingConfig(10), 0)))
    }
}
