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

package com.github.vase4kin.teamcityapp.api

import com.github.vase4kin.teamcityapp.account.create.helper.UrlFormatter
import com.github.vase4kin.teamcityapp.api.cache.CacheProviders
import teamcityapp.features.test_details.repository.models.TestOccurrence
import io.rx_cache2.DynamicKey
import org.mockito.kotlin.any
import org.mockito.kotlin.eq
import org.mockito.kotlin.argumentCaptor
import io.reactivex.Single
import io.reactivex.subjects.SingleSubject
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.*
import org.junit.Assert.*
import org.mockito.Mockito.`when`
import org.mockito.Mockito.verify
import teamcityapp.features.about.api.AboutServerInfo
import teamcityapp.features.about.repository.models.ServerInfo
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.Mockito.verifyNoInteractions
import org.mockito.junit.MockitoJUnitRunner

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(MockitoJUnitRunner::class)
class RepositoryImplTest {

    @Mock
    private lateinit var teamCityService: TeamCityService
    @Mock
    private lateinit var cacheProviders: CacheProviders
    @Mock
    private lateinit var urlFormatter: UrlFormatter
    private lateinit var repository: RepositoryImpl

    @Before
    fun setUp() {
        repository = RepositoryImpl(teamCityService, cacheProviders, urlFormatter, UnconfinedTestDispatcher())
    }

    @Test
    fun listBuildsWithBlankBuildTypeIdReturnsAnError() {
        for (id in listOf("", " ")) {
            repository.listBuilds(id, "count:10", false)
                .test()
                .assertError { error ->
                    error is IllegalArgumentException && error.message == "Build type ID is missing"
                }
        }

        verifyNoInteractions(teamCityService, cacheProviders)
    }

    @Test fun serverInfoUsesCacheAndMapsItsResult() = runTest {
        val upstream = Single.just(ServerInfo("network", "https://network.example"))
        val cached = ServerInfo("cached", "https://cached.example")
        `when`(teamCityService.serverInfo()).thenReturn(upstream)
        `when`(cacheProviders.serverInfo(upstream)).thenReturn(Single.just(cached))

        assertEquals(AboutServerInfo(cached.version, cached.webUrl), repository.serverInfo())
        verify(cacheProviders).serverInfo(upstream)
    }

    @Test fun cancelingServerInfoDisposesCacheSubscription() = runTest {
        val upstream = Single.never<ServerInfo>()
        val cachedRequest = SingleSubject.create<ServerInfo>()
        `when`(teamCityService.serverInfo()).thenReturn(upstream)
        `when`(cacheProviders.serverInfo(upstream)).thenReturn(cachedRequest)
        val job = launch { repository.serverInfo(); fail("Canceled request must not return content") }
        runCurrent()
        assertTrue(cachedRequest.hasObservers())
        job.cancel()
        runCurrent()
        assertFalse(cachedRequest.hasObservers())
        cachedRequest.onSuccess(ServerInfo("late", "https://late.example"))
        assertTrue(job.isCancelled)
    }

    @Test fun serverInfoErrorsReachCaller() = runTest {
        val upstream = Single.never<ServerInfo>()
        val error = IllegalStateException("offline")
        `when`(teamCityService.serverInfo()).thenReturn(upstream)
        `when`(cacheProviders.serverInfo(upstream)).thenReturn(Single.error(error))
        try {
            repository.serverInfo()
            fail("Expected repository error")
        } catch (actual: IllegalStateException) {
            assertEquals(error.message, actual.message)
        }
    }

    @Test fun serverInfoStartsOnInjectedDispatcher() = runTest {
        val queuedRepository = RepositoryImpl(
            teamCityService, cacheProviders, urlFormatter, StandardTestDispatcher(testScheduler)
        )
        val info = ServerInfo("2026.1", "https://teamcity.example")
        val upstream = Single.just(info)
        `when`(teamCityService.serverInfo()).thenReturn(upstream)
        `when`(cacheProviders.serverInfo(upstream)).thenReturn(upstream)
        val job = launch(start = CoroutineStart.UNDISPATCHED) {
            assertEquals(AboutServerInfo(info.version, info.webUrl), queuedRepository.serverInfo())
        }
        verifyNoInteractions(teamCityService, cacheProviders)
        runCurrent()
        job.join()
        verify(teamCityService).serverInfo()
    }

    @Test fun testDetailsUsesFormattedAccountUrlAndCachedDetails() = runTest {
        val upstream = Single.just(TestOccurrence("network"))
        `when`(urlFormatter.formatBasicUrl("/test")).thenReturn("/guestAuth/test")
        `when`(teamCityService.testOccurrence("/guestAuth/test")).thenReturn(upstream)
        `when`(cacheProviders.testOccurrence(eq(upstream), any<DynamicKey>()))
            .thenReturn(Single.just(TestOccurrence("cached <tag> & text")))
        assertEquals("cached <tag> & text", repository.testDetails("/test"))
        val key = argumentCaptor<DynamicKey>()
        verify(cacheProviders).testOccurrence(eq(upstream), key.capture())
        assertEquals("/test", key.firstValue.dynamicKey)
    }

    @Test fun cancelingTestDetailsDisposesCachedRequest() = runTest {
        val upstream = Single.never<TestOccurrence>()
        val pending = SingleSubject.create<TestOccurrence>()
        `when`(urlFormatter.formatBasicUrl("/test")).thenReturn("/guestAuth/test")
        `when`(teamCityService.testOccurrence("/guestAuth/test")).thenReturn(upstream)
        `when`(cacheProviders.testOccurrence(eq(upstream), any<DynamicKey>())).thenReturn(pending)
        val job = launch { repository.testDetails("/test"); fail("Canceled request must not return") }
        runCurrent()
        assertTrue(pending.hasObservers())
        job.cancel()
        runCurrent()
        assertFalse(pending.hasObservers())
        pending.onSuccess(TestOccurrence("late"))
        assertTrue(job.isCancelled)
    }

    @Test fun missingTestDetailsMapsToEmptyAndErrorsReachCaller() = runTest {
        val upstream = Single.never<TestOccurrence>()
        `when`(urlFormatter.formatBasicUrl("/test")).thenReturn("/guestAuth/test")
        `when`(teamCityService.testOccurrence("/guestAuth/test")).thenReturn(upstream)
        `when`(cacheProviders.testOccurrence(eq(upstream), any<DynamicKey>()))
            .thenReturn(Single.just(TestOccurrence()), Single.error(IllegalStateException("offline")))
        assertEquals("", repository.testDetails("/test"))
        try { repository.testDetails("/test"); fail("Expected error") }
        catch (error: IllegalStateException) { assertEquals("offline", error.message) }
    }
    @Test fun testDetailsStartsOnInjectedDispatcher() = runTest {
        val queuedRepository = RepositoryImpl(teamCityService, cacheProviders, urlFormatter, StandardTestDispatcher(testScheduler))
        val upstream = Single.just(TestOccurrence("details"))
        `when`(urlFormatter.formatBasicUrl("/test")).thenReturn("/guestAuth/test")
        `when`(teamCityService.testOccurrence("/guestAuth/test")).thenReturn(upstream)
        `when`(cacheProviders.testOccurrence(eq(upstream), any<DynamicKey>())).thenReturn(upstream)
        val job = launch(start = CoroutineStart.UNDISPATCHED) { assertEquals("details", queuedRepository.testDetails("/test")) }
        verifyNoInteractions(teamCityService, cacheProviders)
        runCurrent()
        job.join()
        verify(teamCityService).testOccurrence("/guestAuth/test")
    }
}
