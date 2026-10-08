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

package com.github.vase4kin.teamcityapp.authentication

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.async
import kotlinx.coroutines.test.*
import org.junit.Assert.*
import org.junit.Test
import org.mockito.kotlin.*
import teamcityapp.features.login.api.DemoServer
import teamcityapp.libraries.remote.RemoteService

@OptIn(ExperimentalCoroutinesApi::class)
class AppLoginRepositoryTest {
    @Test fun successfulResultWinsWhenLegacyServiceFinishesBeforeReportingSuccess() = runTest {
        val remote = mock<RemoteService>()
        whenever(remote.getTryItOutUrl()).thenReturn("https://demo.example")
        whenever(remote.showTryItOut(any(), any(), any())).thenAnswer {
            it.getArgument<() -> Unit>(2).invoke()
            it.getArgument<(Boolean) -> Unit>(0).invoke(true)
            Unit
        }
        assertEquals(DemoServer(true, "https://demo.example"), AppLoginRepository(remote).demoServer())
    }

    @Test fun failedRemoteLoadFinishesWithoutLeavingTheScreenLoading() = runTest {
        val remote = mock<RemoteService>()
        whenever(remote.getTryItOutUrl()).thenReturn("https://demo.example")
        whenever(remote.showTryItOut(any(), any(), any())).thenAnswer {
            it.getArgument<() -> Unit>(2).invoke()
            Unit
        }
        assertEquals(DemoServer(false, "https://demo.example"), AppLoginRepository(remote).demoServer())
    }

    @Test fun cancelledLoadIgnoresLateRemoteCallbacks() = runTest {
        val remote = mock<RemoteService>()
        var success: ((Boolean) -> Unit)? = null
        var finish: (() -> Unit)? = null
        whenever(remote.showTryItOut(any(), any(), any())).thenAnswer {
            success = it.getArgument(0)
            finish = it.getArgument(2)
            Unit
        }
        val pending = async { AppLoginRepository(remote).demoServer() }
        runCurrent()
        pending.cancel()
        runCurrent()
        success!!(true)
        finish!!()
        runCurrent()
        assertTrue(pending.isCancelled)
        verify(remote, never()).getTryItOutUrl()
    }
}
