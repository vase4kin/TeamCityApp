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

package com.github.vase4kin.teamcityapp.overview.data

import android.app.Application
import com.github.vase4kin.teamcityapp.api.Repository
import com.github.vase4kin.teamcityapp.build_details.data.BuildDetailsArguments
import com.github.vase4kin.teamcityapp.buildlist.api.Build
import com.github.vase4kin.teamcityapp.builds.data.AppBuildLaunchMapper
import io.reactivex.Single
import io.reactivex.subjects.SingleSubject
import javax.inject.Provider
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.async
import kotlinx.coroutines.test.*
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.kotlin.*
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35], application = Application::class)
class AppBuildOverviewRepositoryTest {
    private val mapper = AppBuildLaunchMapper()

    @Test fun detailRequestPreservesCacheFlagOpaqueHrefAndFullLoadedSnapshot() = runTest {
        val legacy = mock<Repository>()
        val response = mapper.toLegacyBuild(loadedOverview)
        doReturn(Single.just(response)).whenever(legacy).build("opaque/build:42&fields=all", false)
        doReturn(Single.just(response)).whenever(legacy).build("opaque/build:42&fields=all", true)
        val repository = AppBuildOverviewRepository(Provider { legacy }, mapper, StandardTestDispatcher(testScheduler))
        assertEquals(loadedOverview, repository.build("opaque/build:42&fields=all", false))
        assertEquals(loadedOverview, repository.build("opaque/build:42&fields=all", true))
        verify(legacy).build("opaque/build:42&fields=all", true)
    }

    @Test fun pendingDetailCancellationDisposesRxWithoutPublishingAResult() = runTest {
        val legacy = mock<Repository>()
        var disposed = false
        doReturn(Single.never<Build>().doOnDispose { disposed = true }).whenever(legacy).build("href", true)
        val repository = AppBuildOverviewRepository(Provider { legacy }, mapper, StandardTestDispatcher(testScheduler))
        val pending = async { repository.build("href", true) }
        runCurrent()
        pending.cancel()
        runCurrent()
        assertTrue(disposed)
        assertTrue(pending.isCancelled)
    }

    @Test fun aCancelledRefreshCannotReplaceCompletedHostArgumentsEvenWhenOldRxCompletesLate() = runTest {
        val legacy = mock<Repository>()
        val response = SingleSubject.create<Build>()
        doReturn(response).whenever(legacy).build("/builds/42", true)
        val repository = AppBuildOverviewRepository(Provider { legacy }, mapper, StandardTestDispatcher(testScheduler))
        val arguments = BuildDetailsArguments(overviewArguments())
        arguments.publishLoaded(mapper.toLegacyBuild(loadedOverview))
        val pending = async { arguments.publishLoaded(mapper.toLegacyBuild(repository.build("/builds/42", true))) }
        runCurrent()
        assertTrue(response.hasObservers())
        pending.cancel()
        runCurrent()
        assertFalse(response.hasObservers())
        response.onSuccess(mapper.toLegacyBuild(loadedOverview.copy(number = "obsolete-refresh")))
        runCurrent()
        assertEquals("latest-number", arguments.current.number)
        assertTrue(pending.isCancelled)
    }

    @Test fun repositoryFailurePropagatesForTheFeatureRetainedErrorState() = runTest {
        val legacy = mock<Repository>()
        doReturn(Single.error<Build>(IllegalStateException("offline"))).whenever(legacy).build("href", true)
        val repository = AppBuildOverviewRepository(Provider { legacy }, mapper, StandardTestDispatcher(testScheduler))
        assertEquals("offline", runCatching { repository.build("href", true) }.exceptionOrNull()?.message)
    }

    @Test fun eachLoadResolvesTheCurrentAccountSessionOnce() = runTest {
        val first = mock<Repository>()
        val second = mock<Repository>()
        doReturn(Single.just(mapper.toLegacyBuild(incomingOverview))).whenever(first).build("href", false)
        doReturn(Single.just(mapper.toLegacyBuild(loadedOverview))).whenever(second).build("href", true)
        var current = first
        var resolutions = 0
        val repository = AppBuildOverviewRepository(
            Provider {
                resolutions++
                current
            },
            mapper,
            StandardTestDispatcher(testScheduler)
        )
        assertEquals(incomingOverview, repository.build("href", false))
        current = second
        assertEquals(loadedOverview, repository.build("href", true))
        assertEquals(2, resolutions)
    }
}
