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

package com.github.vase4kin.teamcityapp.buildlog.data

import android.app.Application
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.*
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.kotlin.*
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config
import teamcityapp.features.build_log.api.BuildLogSession
import teamcityapp.libraries.storage.Storage
import teamcityapp.libraries.storage.models.UserAccount

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35], application = Application::class)
class AppBuildLogRepositoryTest {
    private val context = RuntimeEnvironment.getApplication()
    private val storage = mock<Storage>()
    private val account = mock<UserAccount>()

    @Test fun guestLogPreservesGuestQueryAndSslPolicy() = runTest {
        whenever(storage.activeUser).thenReturn(account)
        whenever(account.teamcityUrl).thenReturn("https://teamcity.example")
        whenever(account.isGuestUser).thenReturn(true)
        whenever(account.isSslDisabled).thenReturn(true)
        val repository = AppBuildLogRepository(storage, context, "", StandardTestDispatcher(testScheduler))
        assertEquals(BuildLogSession("https://teamcity.example/viewLog.html?buildId=42&tab=buildLog&guest=1", true, false), repository.session("42"))
    }

    @Test fun consentUsesThePreferenceThatHomeResetsOnAccountChanges() = runTest {
        whenever(storage.activeUser).thenReturn(account)
        whenever(account.teamcityUrl).thenReturn("https://teamcity.example")
        val repository = AppBuildLogRepository(storage, context, "", StandardTestDispatcher(testScheduler))
        assertTrue(repository.session("42").needsAuthentication)
        repository.acknowledgeAuthentication()
        assertFalse(repository.session("42").needsAuthentication)
        BuildLogInteractorImpl(storage, context, null).setAuthDialogStatus(false)
        assertTrue(repository.session("42").needsAuthentication)
    }

    @Test fun mockFlavorOverridesOnlyTheLogUrl() = runTest {
        whenever(storage.activeUser).thenReturn(account)
        whenever(account.isSslDisabled).thenReturn(true)
        val repository = AppBuildLogRepository(storage, context, "file:///android_asset/buildLog.html", StandardTestDispatcher(testScheduler))
        assertEquals(BuildLogSession("file:///android_asset/buildLog.html", true, true), repository.session("42"))
    }
}
