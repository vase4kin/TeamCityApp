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

package com.github.vase4kin.teamcityapp.builds.data

import com.github.vase4kin.teamcityapp.api.Repository
import com.github.vase4kin.teamcityapp.buildlist.api.Build
import io.reactivex.Single
import io.reactivex.subjects.SingleSubject
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.async
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.test.*
import org.junit.Assert.*
import org.junit.Test
import org.mockito.kotlin.*

@OptIn(ExperimentalCoroutinesApi::class)
class AppBuildHydratorTest {
    private val hydrator = AppBuildHydrator(AppBuildLaunchMapper())

    @Test fun runningSummaryAlwaysForcesTheDetailRequest() = runTest {
        val session = mock<Repository>()
        val detail = draftBuild("1", "finished", extra = ",\"statusText\":\"Completed\"")
        doReturn(Single.just(detail)).whenever(session).build("/builds/1", true)
        assertEquals("Completed", hydrator.hydrate(session, listOf(draftBuild("1", "running"))).single().statusText)
        verify(session).build("/builds/1", true)
        verifyNoMoreInteractions(session)
    }

    @Test fun matchingFinishedDetailUsesCacheWithoutAnotherNetworkRequest() = runTest {
        val session = mock<Repository>()
        doReturn(Single.just(draftBuild("1"))).whenever(session).build("/builds/1", false)
        assertEquals("1", hydrator.hydrate(session, listOf(draftBuild("1"))).single().id)
        verify(session).build("/builds/1", false)
        verifyNoMoreInteractions(session)
    }

    @Test fun matchingNonFinishedDetailAlsoUsesCache() = runTest {
        val session = mock<Repository>()
        doReturn(Single.just(draftBuild("1", "queued"))).whenever(session).build("/builds/1", false)
        assertTrue(hydrator.hydrate(session, listOf(draftBuild("1", "queued"))).single().isQueued)
        verify(session).build("/builds/1", false)
        verifyNoMoreInteractions(session)
    }

    @Test fun finishedMismatchRefreshesUsingTheCachedDetailHref() = runTest {
        val session = mock<Repository>()
        doReturn(Single.just(draftBuild("1", "running", "/cached/1"))).whenever(session).build("/builds/1", false)
        doReturn(Single.just(draftBuild("1", "finished", "/cached/1"))).whenever(session).build("/cached/1", true)
        assertTrue(hydrator.hydrate(session, listOf(draftBuild("1", "finished"))).single().isFinished)
        verify(session).build("/builds/1", false)
        verify(session).build("/cached/1", true)
        verifyNoMoreInteractions(session)
    }

    @Test fun mismatchAlsoRefreshesWhenServerIsNoLongerFinished() = runTest {
        val session = mock<Repository>()
        doReturn(Single.just(draftBuild("1", "finished"))).whenever(session).build("/builds/1", false)
        doReturn(Single.just(draftBuild("1", "queued"))).whenever(session).build("/builds/1", true)
        assertTrue(hydrator.hydrate(session, listOf(draftBuild("1", "queued"))).single().isQueued)
        verify(session).build("/builds/1", false)
        verify(session).build("/builds/1", true)
        verifyNoMoreInteractions(session)
    }

    @Test fun parallelDetailsRetainServerOrderDespiteReverseCompletion() = runTest {
        val session = mock<Repository>()
        val first = SingleSubject.create<Build>()
        val second = SingleSubject.create<Build>()
        doReturn(first).whenever(session).build("/builds/1", false)
        doReturn(second).whenever(session).build("/builds/2", true)
        val request = async { hydrator.hydrate(session, listOf(draftBuild("1"), draftBuild("2", "running"))) }
        runCurrent()
        assertTrue(first.hasObservers())
        assertTrue(second.hasObservers())
        second.onSuccess(draftBuild("2"))
        runCurrent()
        assertFalse(request.isCompleted)
        first.onSuccess(draftBuild("1"))
        assertEquals(listOf("1", "2"), request.await().map { it.id })
    }

    @Test fun cancellationDisposesAllPendingDetailSubscriptions() = runTest {
        val session = mock<Repository>()
        val first = SingleSubject.create<Build>()
        val second = SingleSubject.create<Build>()
        doReturn(first).whenever(session).build("/builds/1", false)
        doReturn(second).whenever(session).build("/builds/2", true)
        val request = async { hydrator.hydrate(session, listOf(draftBuild("1"), draftBuild("2", "running"))) }
        runCurrent()
        request.cancelAndJoin()
        assertFalse(first.hasObservers())
        assertFalse(second.hasObservers())
        assertTrue(request.isCancelled)
    }

    @Test fun oneDetailFailureCancelsSiblingHydrationWithoutPartialSuccess() = runTest {
        val session = mock<Repository>()
        val pending = SingleSubject.create<Build>()
        doReturn(pending).whenever(session).build("/builds/1", false)
        val failure = IllegalStateException("offline")
        doReturn(Single.error<Build>(failure)).whenever(session).build("/builds/2", false)
        try {
            hydrator.hydrate(session, listOf(draftBuild("1"), draftBuild("2")))
            fail("Expected complete batch failure")
        } catch (actual: IllegalStateException) {
            assertEquals(failure::class, actual::class)
            assertEquals(failure.message, actual.message)
        }
        assertFalse(pending.hasObservers())
    }

    @Test fun immutableResultContainsDetailSectionsAndDoesNotMergeSummaryIds() = runTest {
        val session = mock<Repository>()
        val detail = draftBuild("1", extra = ",\"number\":\"045\",\"personal\":true,\"buildTypeId\":\"top-level\",\"buildType\":{\"id\":\"nested\",\"name\":\"Android\"},\"testOccurrences\":{\"href\":\"/tests\",\"failed\":2}")
        doReturn(Single.just(detail)).whenever(session).build("/builds/1", false)
        val result = hydrator.hydrate(session, listOf(draftBuild("1"))).single()
        assertEquals("045", result.number)
        assertTrue(result.personal)
        assertEquals("top-level", result.buildTypeId)
        assertEquals("nested", result.configuration?.id)
        assertEquals("/tests", result.tests?.href)
        assertEquals(2, result.tests?.failed)
    }
}
