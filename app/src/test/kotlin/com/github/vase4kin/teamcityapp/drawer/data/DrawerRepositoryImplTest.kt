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

package com.github.vase4kin.teamcityapp.drawer.data
import com.github.vase4kin.teamcityapp.account.session.AccountSession
import kotlinx.coroutines.*
import kotlinx.coroutines.test.*
import org.junit.Assert.*
import org.junit.Test
import org.mockito.Mockito.*
import teamcityapp.features.drawer.api.*
import teamcityapp.libraries.storage.Storage
import teamcityapp.libraries.storage.models.UserAccount
@OptIn(ExperimentalCoroutinesApi::class)
class DrawerRepositoryImplTest {
    private class Store : Storage {
        val users = mutableListOf<UserAccount>()
        var removed = 0
        var activated = 0
        var switched = 0
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
            switched++
            users.forEach { it.setIsActive(it.teamcityUrl == url && it.userName == userName) }
        }
    }
    private fun user(url: String = "https://server", name: String = "Guest user", active: Boolean = false, ssl: Boolean = false) = UserAccount(url, name, "encrypted".toByteArray(), true, active).apply { setSslDisabled(ssl) }

    private val storage = Store()
    private var cleared = 0
    private fun id(u: UserAccount) = DrawerAccountId(u.teamcityUrl, u.userName)
    private fun repo(d: CoroutineDispatcher) = DrawerRepositoryImpl(storage, AccountSession { cleared++ }, d)

    @Test fun mapsImmutableAccountsInStoredOrderWithoutCredentials() = runTest {
        val one = user(active = true, ssl = true)
        val two = user(name = "Other")
        storage.users += listOf(one, two)
        val snapshot = repo(StandardTestDispatcher(testScheduler)).accounts()
        one.setIsActive(false)
        storage.users.clear()
        assertEquals(listOf(DrawerAccount(id(one), true, true), DrawerAccount(id(two), false, false)), snapshot)
    }

    @Test fun switchesTheFullServerAndUserTupleAndClearsSession() = runTest {
        val active = user(active = true)
        val other = user(name = "Other")
        storage.users += listOf(active, other)
        val result = repo(StandardTestDispatcher(testScheduler)).select(id(other))
        assertEquals(SelectionOutcome.Changed, result.outcome)
        assertEquals(listOf(false, true), result.accounts.map { it.isActive })
        assertEquals(1, storage.switched)
        assertEquals(1, cleared)
    }

    @Test fun selectingTheActiveAccountIsANoop() = runTest {
        val active = user(active = true)
        storage.users += active
        assertEquals(SelectionOutcome.Current, repo(StandardTestDispatcher(testScheduler)).select(id(active)).outcome)
        assertEquals(0, storage.switched)
        assertEquals(0, cleared)
    }

    @Test fun missingAccountPreservesActiveSelectionAndReturnsCurrentSnapshot() = runTest {
        val active = user(active = true)
        storage.users += active
        val result = repo(StandardTestDispatcher(testScheduler)).select(DrawerAccountId("missing", "missing"))
        assertEquals(SelectionOutcome.Missing, result.outcome)
        assertEquals(id(active), result.accounts.single().id)
        assertTrue(storage.activeUser.isActive)
        assertEquals(0, storage.switched)
        assertEquals(0, cleared)
    }

    @Test fun emptyStoreCannotInventAnAccount() = runTest {
        assertEquals(AccountSelection(emptyList(), SelectionOutcome.Missing), repo(StandardTestDispatcher(testScheduler)).select(DrawerAccountId("missing", "missing")))
        assertEquals(0, cleared)
    }

    @Test fun storageFailurePropagatesWithoutClearingSession() = runTest {
        val broken = mock(Storage::class.java)
        `when`(broken.userAccounts).thenThrow(IllegalStateException("store"))
        try {
            DrawerRepositoryImpl(broken, AccountSession { cleared++ }, StandardTestDispatcher(testScheduler)).select(DrawerAccountId("a", "b"))
            fail()
        } catch (e: IllegalStateException) {
            assertEquals("store", e.message)
        }
        assertEquals(0, cleared)
    }

    @Test fun cancellationBeforeIoRunsCannotSwitchAccounts() = runTest {
        val account = user()
        storage.users += account
        val job = launch { repo(StandardTestDispatcher(testScheduler)).select(id(account)) }
        job.cancelAndJoin()
        assertEquals(0, storage.switched)
        assertEquals(0, cleared)
    }

    @Test fun readsAndSwitchesRunOnTheInjectedIoDispatcher() = runTest(UnconfinedTestDispatcher()) {
        val account = user()
        storage.users += account
        val dispatcher = StandardTestDispatcher(testScheduler)
        val read = async { repo(dispatcher).accounts() }
        assertFalse(read.isCompleted)
        runCurrent()
        assertEquals(1, read.await().size)
        val switch = async { repo(dispatcher).select(id(account)) }
        assertFalse(switch.isCompleted)
        assertEquals(0, storage.switched)
        runCurrent()
        assertEquals(SelectionOutcome.Changed, switch.await().outcome)
        assertEquals(1, cleared)
    }
}
