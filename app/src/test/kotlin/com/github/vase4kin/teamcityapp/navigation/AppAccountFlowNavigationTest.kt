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

package com.github.vase4kin.teamcityapp.navigation

import kotlinx.coroutines.*
import kotlinx.coroutines.test.*
import org.junit.Assert.*
import org.junit.Test
import teamcityapp.features.home.api.HomeDestination
import teamcityapp.features.login.api.navigation.LoginDestination
import teamcityapp.features.splash.api.navigation.*

@OptIn(ExperimentalCoroutinesApi::class)
class AppAccountFlowNavigationTest {
    private var key = "same-server/alice"
    private var clears = 0
    private var cancelled = 0
    private val resets = mutableListOf<Boolean>()
    private var preparationAction: suspend (String) -> Unit = {}
    private val preparation = object : AccountSessionPreparation {
        override fun currentKey() = key
        override suspend fun prepare(expectedKey: String, resetSession: Boolean) {
            resets += resetSession
            try {
                preparationAction(expectedKey)
            } catch (error: CancellationException) {
                cancelled++
                throw error
            }
        }
        override fun clear() {
            clears++
        }
    }

    @Test fun preparationFinishesWithoutAnySourceScreenOrNavigatorOwner() = runTest {
        val gate = CompletableDeferred<Unit>()
        preparationAction = { gate.await() }
        val coordinator = AppAccountFlowNavigation(preparation, backgroundScope)
        coordinator.home()
        runCurrent()
        assertTrue(coordinator.session.value.initial is AccountActivationDestination)
        gate.complete(Unit)
        runCurrent()
        assertEquals(HomeDestination(), coordinator.session.value.initial)
    }

    @Test fun sameServerAccountReplacementCancelsOldPreparationAndOnlyOpensTheNewAccount() = runTest {
        val alice = CompletableDeferred<Unit>()
        preparationAction = { if (it.endsWith("alice")) alice.await() }
        val coordinator = AppAccountFlowNavigation(preparation, backgroundScope)
        coordinator.home()
        runCurrent()
        key = "same-server/bob"
        coordinator.home()
        runCurrent()
        assertEquals(1, cancelled)
        assertEquals(key, coordinator.session.value.key)
        assertEquals(HomeDestination(), coordinator.session.value.initial)
        alice.complete(Unit)
        runCurrent()
        assertEquals("same-server/bob", coordinator.session.value.key)
    }

    @Test fun selectingTheSameAccountStillCreatesANewOwnerReplacementRequest() = runTest {
        val coordinator = AppAccountFlowNavigation(preparation, backgroundScope)
        coordinator.home()
        runCurrent()
        val first = coordinator.session.value
        coordinator.home()
        runCurrent()
        assertEquals(first.key, coordinator.session.value.key)
        assertNotEquals(first.requestId, coordinator.session.value.requestId)
    }

    @Test fun preparationFailureHasAnExplicitRetryableDestination() = runTest {
        preparationAction = { error("cookies") }
        val coordinator = AppAccountFlowNavigation(preparation, backgroundScope)
        coordinator.home()
        runCurrent()
        assertEquals(AccountActivationDestination(failed = true), coordinator.session.value.initial)
        preparationAction = {}
        coordinator.home()
        runCurrent()
        assertEquals(HomeDestination(), coordinator.session.value.initial)
    }

    @Test fun logoutCancelsPreparationClearsTheApiSessionAndCannotBeOverwrittenByItsCompletion() = runTest {
        preparationAction = { awaitCancellation() }
        val coordinator = AppAccountFlowNavigation(preparation, backgroundScope)
        coordinator.home()
        runCurrent()
        coordinator.login()
        runCurrent()
        assertEquals(1, clears)
        assertEquals(1, cancelled)
        assertEquals(LoginDestination, coordinator.session.value.initial)
        assertEquals("signed-out", coordinator.session.value.key)
    }

    @Test fun anAccountChangeDuringPreparationCannotPublishHomeForTheOldAccount() = runTest {
        preparationAction = { key = "same-server/bob" }
        val coordinator = AppAccountFlowNavigation(preparation, backgroundScope)
        coordinator.home()
        runCurrent()
        assertNotEquals(HomeDestination(), coordinator.session.value.initial)
        coordinator.synchronize()
        assertEquals("same-server/bob", coordinator.session.value.key)
        assertEquals(SplashNavigationDestination, coordinator.session.value.initial)
    }

    @Test fun ordinaryStartupPreservesTheExistingAccountConsentAndRetryKeepsThatPolicy() = runTest {
        preparationAction = { error("session") }
        val coordinator = AppAccountFlowNavigation(preparation, backgroundScope)
        coordinator.home(resetSession = false)
        runCurrent()
        assertEquals(AccountActivationDestination(failed = true, resetSession = false), coordinator.session.value.initial)
        preparationAction = {}
        coordinator.home(resetSession = false)
        runCurrent()
        assertEquals(listOf(false, false), resets)
        assertEquals(HomeDestination(), coordinator.session.value.initial)
    }
}
