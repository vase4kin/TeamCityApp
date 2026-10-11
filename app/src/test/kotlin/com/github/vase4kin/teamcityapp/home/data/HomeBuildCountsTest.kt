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

package com.github.vase4kin.teamcityapp.home.data

import com.github.vase4kin.teamcityapp.api.Repository
import com.github.vase4kin.teamcityapp.base.loading.OnLoadingListener
import com.github.vase4kin.teamcityapp.buildlist.api.Builds
import com.github.vase4kin.teamcityapp.storage.SharedUserStorage
import io.reactivex.Single
import io.reactivex.android.plugins.RxAndroidPlugins
import io.reactivex.plugins.RxJavaPlugins
import io.reactivex.schedulers.Schedulers
import io.reactivex.schedulers.TestScheduler
import io.reactivex.subjects.SingleSubject
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito.*

class HomeBuildCountsTest {
    private val repository = mock(Repository::class.java)
    private val storage = mock(SharedUserStorage::class.java)
    private val counts = HomeBuildCounts(repository, storage)

    @Before fun synchronousSchedulers() {
        RxJavaPlugins.setIoSchedulerHandler { Schedulers.trampoline() }
        RxAndroidPlugins.setInitMainThreadSchedulerHandler { Schedulers.trampoline() }
        RxAndroidPlugins.setMainThreadSchedulerHandler { Schedulers.trampoline() }
    }

    @After fun disposeAndReset() {
        counts.unsubscribe()
        RxAndroidPlugins.reset()
        RxJavaPlugins.reset()
    }

    @Test fun runningAllUsesTheExactCountOnlyLocatorAndNormalCache() {
        `when`(repository.listRunningBuilds(RUNNING, "count", false)).thenReturn(Single.just(page(4)))
        val listener = RecordingListener()
        counts.loadRunning(listener)
        assertEquals(listOf(4), listener.values)
        verify(repository).listRunningBuilds(RUNNING, "count", false)
        verifyNoMoreInteractions(repository)
        verifyNoInteractions(storage)
    }

    @Test fun queueAllUsesNoLocatorCountOnlyAndNormalCache() {
        `when`(repository.listQueueBuilds(null, "count", false)).thenReturn(Single.just(page(3)))
        val listener = RecordingListener()
        counts.loadQueue(listener)
        assertEquals(listOf(3), listener.values)
        verify(repository).listQueueBuilds(null, "count", false)
        verifyNoMoreInteractions(repository)
        verifyNoInteractions(storage)
    }

    @Test fun runningFavoritesSubscribeInParallelAndSumOutOfOrderCounts() {
        `when`(storage.favoriteBuildTypeIds).thenReturn(listOf("one", "two"))
        val one = SingleSubject.create<Builds>()
        val two = SingleSubject.create<Builds>()
        `when`(repository.listRunningBuilds("$RUNNING,buildType:one", "count", false)).thenReturn(one)
        `when`(repository.listRunningBuilds("$RUNNING,buildType:two", "count", false)).thenReturn(two)
        val listener = RecordingListener()
        counts.loadRunning(listener, favorites = true)
        assertTrue(one.hasObservers())
        assertTrue(two.hasObservers())
        two.onSuccess(page(5))
        assertTrue(listener.values.isEmpty())
        one.onSuccess(page(2))
        assertEquals(listOf(7), listener.values)
    }

    @Test fun queueFavoritesSubscribeInParallelAndSumOutOfOrderCounts() {
        `when`(storage.favoriteBuildTypeIds).thenReturn(listOf("one", "two"))
        val one = SingleSubject.create<Builds>()
        val two = SingleSubject.create<Builds>()
        `when`(repository.listQueueBuilds("buildType:one", "count", false)).thenReturn(one)
        `when`(repository.listQueueBuilds("buildType:two", "count", false)).thenReturn(two)
        val listener = RecordingListener()
        counts.loadQueue(listener, favorites = true)
        assertTrue(one.hasObservers())
        assertTrue(two.hasObservers())
        two.onSuccess(page(3))
        assertTrue(listener.values.isEmpty())
        one.onSuccess(page(6))
        assertEquals(listOf(9), listener.values)
    }

    @Test fun emptyRunningFavoritesReturnZeroWithoutAnEndpoint() {
        `when`(storage.favoriteBuildTypeIds).thenReturn(emptyList())
        val listener = RecordingListener()
        counts.loadRunning(listener, favorites = true)
        assertEquals(listOf(0), listener.values)
        verifyNoInteractions(repository)
    }

    @Test fun emptyQueueFavoritesReturnZeroWithoutAnEndpoint() {
        `when`(storage.favoriteBuildTypeIds).thenReturn(emptyList())
        val listener = RecordingListener()
        counts.loadQueue(listener, favorites = true)
        assertEquals(listOf(0), listener.values)
        verifyNoInteractions(repository)
    }

    @Test fun runningErrorReportsSuccessfulZero() {
        `when`(repository.listRunningBuilds(RUNNING, "count", false)).thenReturn(Single.error(IllegalStateException("offline")))
        val listener = RecordingListener()
        counts.loadRunning(listener)
        assertEquals(listOf(0), listener.values)
    }

    @Test fun queueErrorReportsSuccessfulZero() {
        `when`(repository.listQueueBuilds(null, "count", false)).thenReturn(Single.error(IllegalStateException("offline")))
        val listener = RecordingListener()
        counts.loadQueue(listener)
        assertEquals(listOf(0), listener.values)
    }

    @Test fun runningFavoriteFailureCancelsItsPendingSiblingAndReturnsZero() {
        `when`(storage.favoriteBuildTypeIds).thenReturn(listOf("one", "two"))
        val one = SingleSubject.create<Builds>()
        val two = SingleSubject.create<Builds>()
        `when`(repository.listRunningBuilds("$RUNNING,buildType:one", "count", false)).thenReturn(one)
        `when`(repository.listRunningBuilds("$RUNNING,buildType:two", "count", false)).thenReturn(two)
        val listener = RecordingListener()
        counts.loadRunning(listener, favorites = true)
        one.onError(IllegalStateException("offline"))
        assertFalse(two.hasObservers())
        assertEquals(listOf(0), listener.values)
        two.onSuccess(page(8))
        assertEquals(listOf(0), listener.values)
    }

    @Test fun queueFavoriteFailureCancelsItsPendingSiblingAndReturnsZero() {
        `when`(storage.favoriteBuildTypeIds).thenReturn(listOf("one", "two"))
        val one = SingleSubject.create<Builds>()
        val two = SingleSubject.create<Builds>()
        `when`(repository.listQueueBuilds("buildType:one", "count", false)).thenReturn(one)
        `when`(repository.listQueueBuilds("buildType:two", "count", false)).thenReturn(two)
        val listener = RecordingListener()
        counts.loadQueue(listener, favorites = true)
        two.onError(IllegalStateException("offline"))
        assertFalse(one.hasObservers())
        assertEquals(listOf(0), listener.values)
    }

    @Test fun replacingRunningCountDisposesTheEarlierRunningRequest() {
        val old = SingleSubject.create<Builds>()
        `when`(repository.listRunningBuilds(RUNNING, "count", false)).thenReturn(old, Single.just(page(2)))
        val oldListener = RecordingListener()
        val current = RecordingListener()
        counts.loadRunning(oldListener)
        counts.loadRunning(current)
        assertFalse(old.hasObservers())
        old.onSuccess(page(7))
        assertTrue(oldListener.values.isEmpty())
        assertEquals(listOf(2), current.values)
    }

    @Test fun replacingQueueCountDisposesTheEarlierQueueRequest() {
        val old = SingleSubject.create<Builds>()
        `when`(repository.listQueueBuilds(null, "count", false)).thenReturn(old, Single.just(page(2)))
        val oldListener = RecordingListener()
        val current = RecordingListener()
        counts.loadQueue(oldListener)
        counts.loadQueue(current)
        assertFalse(old.hasObservers())
        old.onSuccess(page(7))
        assertTrue(oldListener.values.isEmpty())
        assertEquals(listOf(2), current.values)
    }

    @Test fun replacingEitherFamilyLeavesTheOtherPendingCountSubscribed() {
        val running = SingleSubject.create<Builds>()
        val nextRunning = SingleSubject.create<Builds>()
        val queue = SingleSubject.create<Builds>()
        `when`(repository.listRunningBuilds(RUNNING, "count", false)).thenReturn(running, nextRunning)
        `when`(repository.listQueueBuilds(null, "count", false)).thenReturn(queue, Single.just(page(3)))
        counts.loadRunning(RecordingListener())
        counts.loadQueue(RecordingListener())
        assertTrue(running.hasObservers())
        assertTrue(queue.hasObservers())
        counts.loadRunning(RecordingListener())
        assertFalse(running.hasObservers())
        assertTrue(queue.hasObservers())
        counts.loadQueue(RecordingListener())
        assertFalse(queue.hasObservers())
        assertTrue(nextRunning.hasObservers())
    }

    @Test fun unsubscribingDisposesBothFamiliesAndSuppressesLateCallbacks() {
        val running = SingleSubject.create<Builds>()
        val queue = SingleSubject.create<Builds>()
        `when`(repository.listRunningBuilds(RUNNING, "count", false)).thenReturn(running)
        `when`(repository.listQueueBuilds(null, "count", false)).thenReturn(queue)
        val listener = RecordingListener()
        counts.loadRunning(listener)
        counts.loadQueue(listener)
        counts.unsubscribe()
        assertFalse(running.hasObservers())
        assertFalse(queue.hasObservers())
        running.onSuccess(page(5))
        queue.onSuccess(page(7))
        assertTrue(listener.values.isEmpty())
    }

    @Test fun favoritesAreSnapshottedBeforeSubscriptionsAndNeverRemoved() {
        val scheduler = TestScheduler()
        RxJavaPlugins.setIoSchedulerHandler { scheduler }
        val saved = mutableListOf("one", "two")
        `when`(storage.favoriteBuildTypeIds).thenReturn(saved)
        val one = SingleSubject.create<Builds>()
        val two = SingleSubject.create<Builds>()
        `when`(repository.listQueueBuilds("buildType:one", "count", false)).thenReturn(one)
        `when`(repository.listQueueBuilds("buildType:two", "count", false)).thenReturn(two)
        val listener = RecordingListener()
        counts.loadQueue(listener, favorites = true)
        saved.clear()
        scheduler.triggerActions()
        assertTrue(one.hasObservers())
        assertTrue(two.hasObservers())
        two.onSuccess(page(3))
        one.onSuccess(page(4))
        assertEquals(listOf(7), listener.values)
        verify(storage).favoriteBuildTypeIds
        verifyNoMoreInteractions(storage)
    }

    private class RecordingListener : OnLoadingListener<Int> {
        val values = mutableListOf<Int>()
        override fun onSuccess(data: Int) {
            values += data
        }
        override fun onFail(errorMessage: String) {
            fail("Count errors must be successful zero: $errorMessage")
        }
    }
    private fun page(count: Int) = Builds(count, emptyList())
    private companion object {
        const val RUNNING = "running:true,branch:default:any,personal:false,pinned:false"
    }
}
