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

package com.github.vase4kin.teamcityapp.dagger.modules

import com.github.vase4kin.teamcityapp.api.TeamCityService
import io.reactivex.Single
import io.reactivex.subjects.SingleSubject
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.*
import org.junit.Assert.*
import org.junit.Test
import org.mockito.Mockito.*
import teamcityapp.features.test_details.repository.models.TestOccurrence
import teamcityapp.features.about.api.AboutServerInfo
import teamcityapp.features.about.repository.models.ServerInfo

@OptIn(ExperimentalCoroutinesApi::class)
class FakeRepositoryImplTest {
    @Test fun serverInfoIsMappedOnInjectedDispatcher() = runTest {
        val service = mock(TeamCityService::class.java)
        val info = ServerInfo("2026.1", "https://teamcity.example")
        `when`(service.serverInfo()).thenReturn(Single.just(info))
        val repository = FakeRepositoryImpl(service, StandardTestDispatcher(testScheduler))
        val job = launch(start = CoroutineStart.UNDISPATCHED) {
            assertEquals(AboutServerInfo(info.version, info.webUrl), repository.serverInfo())
        }
        verifyNoInteractions(service)
        runCurrent()
        job.join()
        verify(service).serverInfo()
    }

    @Test fun coroutineCancellationDisposesRxSubscription() = runTest {
        val service = mock(TeamCityService::class.java)
        val request = SingleSubject.create<ServerInfo>()
        `when`(service.serverInfo()).thenReturn(request)
        val repository = FakeRepositoryImpl(service, StandardTestDispatcher(testScheduler))
        val job = launch { repository.serverInfo(); fail("Canceled request must not return content") }
        runCurrent()
        assertTrue(request.hasObservers())
        job.cancel()
        runCurrent()
        assertFalse(request.hasObservers())
        request.onSuccess(ServerInfo("late", "url"))
        assertTrue(job.isCancelled)
    }

    @Test fun repositoryErrorsReachCaller() = runTest {
        val service = mock(TeamCityService::class.java)
        val error = IllegalStateException("offline")
        `when`(service.serverInfo()).thenReturn(Single.error(error))
        val repository = FakeRepositoryImpl(service, UnconfinedTestDispatcher(testScheduler))
        try {
            repository.serverInfo()
            fail("Expected repository error")
        } catch (actual: IllegalStateException) {
            assertEquals(error.message, actual.message)
        }
    }
    @Test fun testDetailsIsMappedOnInjectedDispatcher() = runTest {
        val service = mock(TeamCityService::class.java)
        `when`(service.testOccurrence("/test")).thenReturn(Single.just(TestOccurrence("<tag> & raw text")))
        val repository = FakeRepositoryImpl(service, StandardTestDispatcher(testScheduler))
        val job = launch(start = CoroutineStart.UNDISPATCHED) { assertEquals("<tag> & raw text", repository.testDetails("/test")) }
        verifyNoInteractions(service)
        runCurrent()
        job.join()
        verify(service).testOccurrence("/test")
    }

    @Test fun cancelingTestDetailsDisposesRxSubscription() = runTest {
        val service = mock(TeamCityService::class.java)
        val pending = SingleSubject.create<TestOccurrence>()
        `when`(service.testOccurrence("/test")).thenReturn(pending)
        val repository = FakeRepositoryImpl(service, StandardTestDispatcher(testScheduler))
        val job = launch { repository.testDetails("/test"); fail("Canceled request must not return") }
        runCurrent()
        assertTrue(pending.hasObservers())
        job.cancel()
        runCurrent()
        assertFalse(pending.hasObservers())
    }

    @Test fun missingDetailsAreEmptyAndFailuresReachCaller() = runTest {
        val service = mock(TeamCityService::class.java)
        `when`(service.testOccurrence("/test")).thenReturn(Single.just(TestOccurrence()), Single.error(IllegalStateException("offline")))
        val repository = FakeRepositoryImpl(service, UnconfinedTestDispatcher(testScheduler))
        assertEquals("", repository.testDetails("/test"))
        try { repository.testDetails("/test"); fail("Expected error") }
        catch (error: IllegalStateException) { assertEquals("offline", error.message) }
    }
}
