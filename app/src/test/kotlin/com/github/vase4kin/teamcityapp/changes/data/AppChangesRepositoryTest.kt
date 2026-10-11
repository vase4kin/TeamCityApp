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

package com.github.vase4kin.teamcityapp.changes.data

import com.github.vase4kin.teamcityapp.api.Repository
import com.github.vase4kin.teamcityapp.changes.api.Changes
import io.reactivex.Single
import javax.inject.Provider
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.async
import kotlinx.coroutines.test.*
import org.junit.Assert.*
import org.junit.Test
import org.mockito.kotlin.*

@OptIn(ExperimentalCoroutinesApi::class)
class AppChangesRepositoryTest {
    private fun page(rows: List<Changes.Change>, count: Int = rows.size, next: String? = null): Changes = mock {
        on { objects } doReturn rows
        on { this.count } doReturn count
        on { nextHref } doReturn next
    }
    private fun details(id: String, comment: String): Changes.Change = mock<Changes.Change>().apply {
        whenever(getId()).thenReturn(id)
        whenever(this.comment).thenReturn(comment)
        whenever(username).thenReturn("Author")
        whenever(date).thenReturn("30 Jul 16 00:36")
        whenever(version).thenReturn("revision")
        whenever(webUrl).thenReturn("https://teamcity.example/change/$id")
    }

    @Test fun hydratesInServerOrderAndPreservesInitialRefreshAndOpaqueContinuationPolicies() = runTest {
        val legacy = mock<Repository>()
        val first = Changes.Change("/changes/2")
        val second = Changes.Change("/changes/1")
        doReturn(Single.just(page(listOf(first, second), next = "opaque&start=10"))).whenever(legacy).listChanges("build:42,count:10", false)
        doReturn(Single.just(page(listOf(first)))).whenever(legacy).listChanges("build:42,count:10", true)
        doReturn(Single.just(page(listOf(second)))).whenever(legacy).listChanges("opaque&start=10", false)
        doReturn(Single.just(details("2", " raw comment "))).whenever(legacy).change("/changes/2")
        doReturn(Single.just(details("1", "Second"))).whenever(legacy).change("/changes/1")
        val repository = AppChangesRepository(Provider { legacy }, StandardTestDispatcher(testScheduler))
        val initial = repository.changes("build:42", null, false)
        assertEquals(listOf("2", "1"), initial.items.map { it.id })
        assertEquals(" raw comment ", initial.items.first().comment)
        assertEquals("30 Jul 16 00:36", initial.items.first().date)
        assertEquals("opaque&start=10", initial.nextHref)
        assertEquals(listOf("2"), repository.changes("build:42", null, true).items.map { it.id })
        assertEquals(listOf("1"), repository.changes("build:42", initial.nextHref, false).items.map { it.id })
        verify(legacy).listChanges("build:42,count:10", true)
        verify(legacy).listChanges("opaque&start=10", false)
    }

    @Test fun emptyPagesDiscardStaleContinuationAndDoNotHydrateRows() = runTest {
        val legacy = mock<Repository>()
        doReturn(Single.just(page(listOf(Changes.Change("ignored")), count = 0, next = "stale"))).whenever(legacy).listChanges("build:42,count:10", false)
        val result = AppChangesRepository(Provider { legacy }, StandardTestDispatcher(testScheduler)).changes("build:42", null, false)
        assertTrue(result.items.isEmpty())
        assertNull(result.nextHref)
        verify(legacy, never()).change(any())
    }

    @Test fun missingChangeIdsUseTheSummaryHrefAsAStableKey() = runTest {
        val legacy = mock<Repository>()
        doReturn(Single.just(page(listOf(Changes.Change("/changes/42"))))).whenever(legacy).listChanges("build:42,count:10", false)
        doReturn(Single.just(details("", "Fallback"))).whenever(legacy).change("/changes/42")
        val result = AppChangesRepository(Provider { legacy }, StandardTestDispatcher(testScheduler)).changes("build:42", null, false)
        assertEquals("/changes/42", result.items.single().id)
    }

    @Test fun cancellationDisposesAPendingPageRequest() = runTest {
        val legacy = mock<Repository>()
        var disposed = false
        doReturn(Single.never<Changes>().doOnDispose { disposed = true }).whenever(legacy).listChanges("build:42,count:10", false)
        val repository = AppChangesRepository(Provider { legacy }, StandardTestDispatcher(testScheduler))
        val pending = async { repository.changes("build:42", null, false) }
        runCurrent()
        pending.cancel()
        runCurrent()
        assertTrue(disposed)
    }

    @Test fun cancellationDisposesAPendingHydrationRequest() = runTest {
        val legacy = mock<Repository>()
        var disposed = false
        doReturn(Single.just(page(listOf(Changes.Change("/changes/42"))))).whenever(legacy).listChanges("build:42,count:10", false)
        doReturn(Single.never<Changes.Change>().doOnDispose { disposed = true }).whenever(legacy).change("/changes/42")
        val repository = AppChangesRepository(Provider { legacy }, StandardTestDispatcher(testScheduler))
        val pending = async { repository.changes("build:42", null, false) }
        runCurrent()
        pending.cancel()
        runCurrent()
        assertTrue(disposed)
    }

    @Test fun countUsesTheUnfilteredForcedRequestAndPreservesOptionalFailure() = runTest {
        val legacy = mock<Repository>()
        val countUrl = "build:42,count:${Int.MAX_VALUE}&fields=count"
        doReturn(Single.just(page(emptyList(), count = 17))).whenever(legacy).listChanges(countUrl, true)
        val repository = AppChangesRepository(Provider { legacy }, StandardTestDispatcher(testScheduler))
        assertEquals(17, repository.count("build:42"))
        doReturn(Single.error<Changes>(IllegalStateException("offline"))).whenever(legacy).listChanges(countUrl, true)
        assertEquals("offline", runCatching { repository.count("build:42") }.exceptionOrNull()?.message)
    }

    @Test fun resolvesTheCurrentAccountOncePerPage() = runTest {
        val first = mock<Repository>()
        val second = mock<Repository>()
        doReturn(Single.just(page(emptyList()))).whenever(first).listChanges("build:42,count:10", false)
        doReturn(Single.just(page(emptyList()))).whenever(second).listChanges("next", false)
        var current = first
        var resolutions = 0
        val repository = AppChangesRepository(
            Provider {
                resolutions++
                current
            },
            StandardTestDispatcher(testScheduler)
        )
        repository.changes("build:42", null, false)
        current = second
        repository.changes("build:42", "next", false)
        assertEquals(2, resolutions)
        verify(first).listChanges("build:42,count:10", false)
        verify(second).listChanges("next", false)
    }
}
