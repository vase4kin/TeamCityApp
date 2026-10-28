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

package teamcityapp.features.splash.impl.data
import kotlinx.coroutines.*
import kotlinx.coroutines.test.*
import org.junit.Assert.*
import org.junit.Test
import org.mockito.Mockito.*
import teamcityapp.features.splash.impl.data.DefaultSplashRepository
import teamcityapp.libraries.storage.Storage

@OptIn(ExperimentalCoroutinesApi::class)
class SplashRepositoryImplTest {
    @Test fun mapsExistingAccountsWithoutAccessingCredentials() = runTest {
        val store = mock(Storage::class.java)
        `when`(store.hasUserAccounts()).thenReturn(true)
        assertTrue(DefaultSplashRepository(store, StandardTestDispatcher(testScheduler)).hasAccounts())
        verify(store).hasUserAccounts()
        verifyNoMoreInteractions(store)
    }

    @Test fun emptyStorageReturnsFalse() = runTest {
        assertFalse(DefaultSplashRepository(mock(Storage::class.java), StandardTestDispatcher(testScheduler)).hasAccounts())
    }

    @Test fun storageFailurePropagatesForTheViewModelToHandle() = runTest {
        val store = mock(Storage::class.java)
        `when`(store.hasUserAccounts()).thenThrow(IllegalStateException("store"))
        try {
            DefaultSplashRepository(store, StandardTestDispatcher(testScheduler)).hasAccounts()
            fail()
        } catch (e: IllegalStateException) {
            assertEquals("store", e.message)
        }
    }

    @Test fun cancellationBeforeIoRunsDoesNotReadStorage() = runTest {
        val store = mock(Storage::class.java)
        val job = launch { DefaultSplashRepository(store, StandardTestDispatcher(testScheduler)).hasAccounts() }
        job.cancelAndJoin()
        verifyNoInteractions(store)
    }

    @Test fun storageReadUsesInjectedIoDispatcher() = runTest(UnconfinedTestDispatcher()) {
        val store = mock(Storage::class.java)
        val read = async { DefaultSplashRepository(store, StandardTestDispatcher(testScheduler)).hasAccounts() }
        assertFalse(read.isCompleted)
        verifyNoInteractions(store)
        runCurrent()
        assertFalse(read.await())
        verify(store).hasUserAccounts()
    }
}
