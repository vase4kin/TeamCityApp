package com.github.vase4kin.teamcityapp.test_details

import com.github.vase4kin.teamcityapp.api.Repository
import com.github.vase4kin.teamcityapp.test_details.dagger.RxTestDetailsRepository
import io.reactivex.Single
import io.reactivex.subjects.SingleSubject
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.*
import org.junit.Assert.*
import org.junit.Test
import org.mockito.Mockito.*
import teamcityapp.features.test_details.repository.models.TestOccurrence
import javax.inject.Provider

@OptIn(ExperimentalCoroutinesApi::class)
class RxTestDetailsRepositoryTest {
    @Test fun resolvesCurrentAccountForEachRequestAndMapsDto() = runTest {
        val first = mock(Repository::class.java)
        val second = mock(Repository::class.java)
        `when`(first.testOccurrence("/test")).thenReturn(Single.just(TestOccurrence("first")))
        `when`(second.testOccurrence("/test")).thenReturn(Single.just(TestOccurrence("second")))
        var current = first
        val adapter = RxTestDetailsRepository(Provider { current }, UnconfinedTestDispatcher(testScheduler))
        assertEquals("first", adapter.details("/test").text)
        current = second
        assertEquals("second", adapter.details("/test").text)
    }

    @Test fun cancellationDisposesNetworkSubscription() = runTest {
        val repository = mock(Repository::class.java)
        val request = SingleSubject.create<TestOccurrence>()
        `when`(repository.testOccurrence("/test")).thenReturn(request)
        val adapter = RxTestDetailsRepository(Provider { repository }, StandardTestDispatcher(testScheduler))
        val job = launch { adapter.details("/test") }
        runCurrent()
        assertTrue(request.hasObservers())
        job.cancel()
        runCurrent()
        assertFalse(request.hasObservers())
    }
}
