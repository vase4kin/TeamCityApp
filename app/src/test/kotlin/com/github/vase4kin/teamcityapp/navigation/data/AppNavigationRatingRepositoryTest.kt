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

package com.github.vase4kin.teamcityapp.navigation.data

import android.content.Context
import android.content.SharedPreferences
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.*
import org.junit.Assert.*
import org.junit.Test
import org.mockito.kotlin.*
import teamcityapp.libraries.remote.RemoteService

@OptIn(ExperimentalCoroutinesApi::class)
class AppNavigationRatingRepositoryTest {
    private val preferences = mock<SharedPreferences>()
    private val context = mock<Context> { on { getSharedPreferences("rateTheAppPref", Context.MODE_PRIVATE) } doReturn preferences }
    private val remote = mock<RemoteService>()

    @Test fun persistedHandledChoiceSuppressesEligibilityLookup() = runTest {
        doReturn(true).whenever(preferences).getBoolean("rated", false)
        assertFalse(AppNavigationRatingRepository(context, remote, StandardTestDispatcher(testScheduler)).isEligible())
        verifyNoInteractions(remote)
    }

    @Test fun unhandledChoiceUsesRemoteEligibility() = runTest {
        doReturn(true).whenever(remote).isNotChurn()
        val repository = AppNavigationRatingRepository(context, remote, StandardTestDispatcher(testScheduler))
        assertTrue(repository.isEligible())
        doReturn(false).whenever(remote).isNotChurn()
        assertFalse(repository.isEligible())
    }

    @Test fun rateAndCancelPersistTheExistingGlobalKey() = runTest {
        val editor = mock<SharedPreferences.Editor>()
        doReturn(editor).whenever(preferences).edit()
        doReturn(editor).whenever(editor).putBoolean("rated", true)
        AppNavigationRatingRepository(context, remote, StandardTestDispatcher(testScheduler)).markHandled()
        verify(editor).putBoolean("rated", true)
        verify(editor).apply()
    }
}
