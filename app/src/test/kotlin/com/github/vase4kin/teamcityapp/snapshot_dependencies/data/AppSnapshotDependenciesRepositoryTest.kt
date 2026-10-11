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

package com.github.vase4kin.teamcityapp.snapshot_dependencies.data

import com.github.vase4kin.teamcityapp.api.Repository
import com.github.vase4kin.teamcityapp.buildlist.api.Builds
import com.github.vase4kin.teamcityapp.builds.data.*
import io.reactivex.Single
import io.reactivex.subjects.SingleSubject
import javax.inject.Provider
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.async
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.test.*
import org.junit.Assert.*
import org.junit.Test
import org.mockito.kotlin.*

@OptIn(ExperimentalCoroutinesApi::class)
class AppSnapshotDependenciesRepositoryTest {
    private val hydrator = AppBuildHydrator(AppBuildLaunchMapper())

    @Test fun capturesOneSessionForListAndDetailsAndPreservesDependencyOrder() = runTest {
        val first = mock<Repository>()
        val nextAccount = mock<Repository>()
        var resolutions = 0
        val provider = Provider { if (resolutions++ == 0) first else nextAccount }
        val rows = listOf(draftBuild("2", "queued"), draftBuild("1", "running"))
        doReturn(Single.just(draftPage(rows))).whenever(first).listSnapshotBuilds("123", true)
        doReturn(Single.just(rows[0])).whenever(first).build("/builds/2", false)
        doReturn(Single.just(rows[1])).whenever(first).build("/builds/1", true)
        val adapter = AppSnapshotDependenciesRepository(provider, hydrator, StandardTestDispatcher(testScheduler))
        assertEquals(listOf("2", "1"), adapter.dependencies("123", true).map { it.id })
        assertEquals(1, resolutions)
        verifyNoInteractions(nextAccount)
        verify(first).listSnapshotBuilds("123", true)
    }

    @Test fun cacheAllowedLoadPassesFalseToSnapshotPage() = runTest {
        val session = mock<Repository>()
        doReturn(Single.just(draftPage(emptyList()))).whenever(session).listSnapshotBuilds("123", false)
        val adapter = AppSnapshotDependenciesRepository(Provider { session }, hydrator, StandardTestDispatcher(testScheduler))
        assertTrue(adapter.dependencies("123", false).isEmpty())
        verify(session).listSnapshotBuilds("123", false)
        verifyNoMoreInteractions(session)
    }

    @Test fun zeroCountDoesNotHydrateStaleRows() = runTest {
        val session = mock<Repository>()
        doReturn(Single.just(draftPage(listOf(draftBuild("stale")), count = 0))).whenever(session).listSnapshotBuilds("123", true)
        val adapter = AppSnapshotDependenciesRepository(Provider { session }, hydrator, StandardTestDispatcher(testScheduler))
        assertTrue(adapter.dependencies("123", true).isEmpty())
        verify(session).listSnapshotBuilds("123", true)
        verifyNoMoreInteractions(session)
    }

    @Test fun cancellationDisposesSnapshotSubscription() = runTest {
        val session = mock<Repository>()
        val pending = SingleSubject.create<Builds>()
        doReturn(pending).whenever(session).listSnapshotBuilds("123", false)
        val adapter = AppSnapshotDependenciesRepository(Provider { session }, hydrator, StandardTestDispatcher(testScheduler))
        val request = async { adapter.dependencies("123", false) }
        runCurrent()
        assertTrue(pending.hasObservers())
        request.cancelAndJoin()
        assertFalse(pending.hasObservers())
        assertTrue(request.isCancelled)
    }
}
