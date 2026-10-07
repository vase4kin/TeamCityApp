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

import android.app.Application
import java.io.IOException
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.async
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import okhttp3.*
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.kotlin.*
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import teamcityapp.libraries.authentication.AuthenticationResult

/** The coroutine bridge must cancel the request and release every response body. */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35], application = Application::class)
@OptIn(ExperimentalCoroutinesApi::class)
class AuthenticationCallTest {
    @Test fun cancellationCancelsTheCallAndLateResponseIsClosed() = runTest {
        val call = mock<Call>()
        val callback = argumentCaptor<Callback>()
        val result = async { call.awaitAuthentication() }
        runCurrent()
        verify(call).enqueue(callback.capture())
        result.cancel()
        runCurrent()
        verify(call).cancel()
        val body = spy("success".toResponseBody())
        callback.firstValue.onResponse(call, response(200, body))
        verify(body).close()
        assertTrue(result.isCancelled)
    }

    @Test fun successfulAuthenticationClosesItsResponse() = runTest {
        val call = mock<Call>()
        val body = spy("server".toResponseBody())
        whenever(call.enqueue(any())).thenAnswer { invocation ->
            invocation.getArgument<Callback>(0).onResponse(call, response(200, body))
        }
        assertEquals(AuthenticationResult.Success, call.awaitAuthentication())
        verify(body).close()
    }

    @Test fun failedAuthenticationPreservesStatusAndServerMessage() = runTest {
        val call = mock<Call>()
        whenever(call.enqueue(any())).thenAnswer { invocation ->
            invocation.getArgument<Callback>(0).onResponse(call, response(401, "Guest login is disabled".toResponseBody()))
        }
        assertEquals(AuthenticationResult.Failure(401, "Guest login is disabled"), call.awaitAuthentication())
    }

    @Test fun blankErrorBodyFallsBackToHttpMessage() = runTest {
        val call = mock<Call>()
        whenever(call.enqueue(any())).thenAnswer { invocation ->
            invocation.getArgument<Callback>(0).onResponse(call, response(403, "".toResponseBody()))
        }
        assertEquals(AuthenticationResult.Failure(403, "Forbidden"), call.awaitAuthentication())
    }

    @Test fun networkFailureIsPropagatedWithoutInventingAResponse() = runTest {
        val call = mock<Call>()
        val error = IOException("Disconnected")
        whenever(call.enqueue(any())).thenAnswer { invocation ->
            invocation.getArgument<Callback>(0).onFailure(call, error)
        }
        try {
            call.awaitAuthentication()
            fail("Expected network failure")
        } catch (actual: IOException) {
            assertEquals(error.message, actual.message)
        }
    }

    private fun response(code: Int, body: ResponseBody) = Response.Builder().request(Request.Builder().url("https://server.example/httpAuth/app/rest/server").build()).protocol(Protocol.HTTP_1_1).code(code).message("Forbidden").body(body).build()
}
