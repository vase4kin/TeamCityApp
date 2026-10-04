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

package com.github.vase4kin.teamcityapp.manage_accounts.data

import com.github.vase4kin.teamcityapp.account.session.AccountSession
import kotlinx.coroutines.*
import kotlinx.coroutines.test.*
import org.junit.Assert.*
import org.junit.Test
import org.mockito.Mockito.*
import teamcityapp.features.manage_accounts.api.*
import teamcityapp.libraries.cache_manager.CacheManager
import teamcityapp.libraries.storage.Storage
import teamcityapp.libraries.storage.models.UserAccount

@OptIn(ExperimentalCoroutinesApi::class)
class ManageAccountsRepositoryImplTest {
    private class Store : Storage {
        val users = mutableListOf<UserAccount>()
        var removed = 0
        var activated = 0
        override val userAccounts get() = users
        override val activeUser get() = users.first { it.isActive }
        override fun hasUserAccounts() = users.isNotEmpty()
        override fun removeUserAccount(userAccount: UserAccount) {
            removed++
            users.removeAll { it == userAccount }
        }
        override fun setOtherUserActive() {
            activated++
            users.forEachIndexed { i, u -> u.setIsActive(i == 0) }
        }
        override fun setUserActive(url: String, userName: String) {
            users.forEach { it.setIsActive(it.teamcityUrl == url && it.userName == userName) }
        }
    }
    private fun user(url: String = "https://server", name: String = "Guest user", active: Boolean = false, ssl: Boolean = false) = UserAccount(url, name, "encrypted".toByteArray(), true, active).apply { setSslDisabled(ssl) }
    private fun id(user: UserAccount) = ManagedAccountId(user.teamcityUrl, user.userName)
    private val storage = Store()
    private val cache = mock(CacheManager::class.java)
    private var cleared = 0
    private fun repo(dispatcher: CoroutineDispatcher) = ManageAccountsRepositoryImpl(storage, cache, AccountSession { cleared++ }, dispatcher)

    @Test fun mapsImmutableAccountsInStoredOrderWithoutCredentials() = runTest {
        val one = user(active = true, ssl = true)
        val two = user(name = "Other")
        storage.users += listOf(one, two)
        val snapshot = repo(StandardTestDispatcher(testScheduler)).accounts()
        one.setIsActive(false)
        storage.users.clear()
        assertEquals(listOf(ManagedAccount(id(one), true, true), ManagedAccount(id(two), false, false)), snapshot)
    }

    @Test fun inactiveRemovalStaysAndDoesNotClearSessionOrCache() = runTest {
        val active = user(active = true)
        val inactive = user(name = "Other")
        storage.users += listOf(active, inactive)
        val result = repo(StandardTestDispatcher(testScheduler)).remove(id(inactive))
        assertEquals(AccountRemovalOutcome.Stay, result.outcome)
        assertTrue(result.removed)
        assertEquals(listOf(id(active)), result.remainingAccounts.map { it.id })
        assertEquals(0, cleared)
        verifyNoInteractions(cache)
    }

    @Test fun identityIncludesUserNameForAccountsSharingAServer() = runTest {
        val active = user(active = true)
        val inactive = user(name = "Other")
        storage.users += listOf(inactive, active)
        repo(StandardTestDispatcher(testScheduler)).remove(id(inactive))
        assertEquals(listOf(active), storage.users)
    }

    @Test fun activeRemovalChoosesFirstRemainingAccountAndClearsSession() = runTest {
        val active = user(active = true)
        val one = user("https://one")
        val two = user("https://two")
        storage.users += listOf(active, one, two)
        val result = repo(StandardTestDispatcher(testScheduler)).remove(id(active))
        assertEquals(AccountRemovalOutcome.Home, result.outcome)
        assertEquals(listOf(true, false), result.remainingAccounts.map { it.isActive })
        assertEquals(1, cleared)
        assertEquals(1, storage.activated)
        verifyNoInteractions(cache)
    }

    @Test fun lastRemovalClearsSessionAndAllCachesBeforeLogin() = runTest {
        val account = user(active = true)
        storage.users += account
        val result = repo(StandardTestDispatcher(testScheduler)).remove(id(account))
        assertEquals(AccountRemoval(emptyList(), AccountRemovalOutcome.Login, true), result)
        assertTrue(storage.users.isEmpty())
        assertEquals(1, cleared)
        verify(cache).evictAllCache()
    }

    @Test fun cacheFailureReturnsRemovedSnapshotAndRetryDoesNotRemoveTwice() = runTest {
        val account = user(active = true)
        storage.users += account
        doThrow(IllegalStateException("cache")).doNothing().`when`(cache).evictAllCache()
        val repo = repo(StandardTestDispatcher(testScheduler))
        assertEquals(AccountRemoval(emptyList(), AccountRemovalOutcome.CleanupFailed, true), repo.remove(id(account)))
        assertEquals(AccountRemoval(emptyList(), AccountRemovalOutcome.Login, false), repo.remove(id(account)))
        assertEquals(1, storage.removed)
        verify(cache, times(2)).evictAllCache()
    }

    @Test fun missingAccountWithValidActiveAccountIsANoop() = runTest {
        storage.users += user(active = true)
        val result = repo(StandardTestDispatcher(testScheduler)).remove(ManagedAccountId("missing", "missing"))
        assertFalse(result.removed)
        assertEquals(AccountRemovalOutcome.Stay, result.outcome)
        assertEquals(0, storage.removed)
        assertEquals(0, cleared)
    }

    @Test fun missingAccountWithNoActiveAccountRepairsSelectionForPartialFailureRetry() = runTest {
        storage.users += user()
        val result = repo(StandardTestDispatcher(testScheduler)).remove(ManagedAccountId("missing", "missing"))
        assertFalse(result.removed)
        assertEquals(AccountRemovalOutcome.Home, result.outcome)
        assertTrue(result.remainingAccounts.single().isActive)
        assertEquals(1, cleared)
    }

    @Test fun storageFailurePropagatesWithoutClearingCaches() = runTest {
        val broken = mock(Storage::class.java)
        `when`(broken.userAccounts).thenThrow(IllegalStateException("store"))
        try {
            ManageAccountsRepositoryImpl(broken, cache, AccountSession { cleared++ }, StandardTestDispatcher(testScheduler)).accounts()
            fail()
        } catch (e: IllegalStateException) {
            assertEquals("store", e.message)
        }
        verifyNoInteractions(cache)
    }

    @Test fun cancellingBeforeTheIoDispatcherRunsCannotRemoveAnAccount() = runTest {
        val account = user(active = true)
        storage.users += account
        val dispatcher = StandardTestDispatcher(testScheduler)
        val job = launch { repo(dispatcher).remove(id(account)) }
        job.cancelAndJoin()
        assertEquals(listOf(account), storage.users)
        assertEquals(0, cleared)
    }

    @Test fun cacheCancellationPropagatesInsteadOfBecomingCleanupFailure() = runTest {
        val account = user(active = true)
        storage.users += account
        doThrow(CancellationException("cancelled")).`when`(cache).evictAllCache()
        try {
            repo(StandardTestDispatcher(testScheduler)).remove(id(account))
            fail()
        } catch (e: CancellationException) {
            assertEquals("cancelled", e.message)
        }
    }

    @Test fun storageAndCacheOperationsRunOnTheInjectedIoDispatcher() = runTest(UnconfinedTestDispatcher()) {
        val dispatcher = StandardTestDispatcher(testScheduler)
        val account = user(active = true)
        storage.users += account
        val read = async { repo(dispatcher).accounts() }
        assertFalse(read.isCompleted)
        runCurrent()
        assertEquals(1, read.await().size)
        val remove = async { repo(dispatcher).remove(id(account)) }
        assertFalse(remove.isCompleted)
        assertEquals(0, storage.removed)
        verifyNoInteractions(cache)
        runCurrent()
        assertEquals(AccountRemovalOutcome.Login, remove.await().outcome)
        assertEquals(1, storage.removed)
        verify(cache).evictAllCache()
    }
}
