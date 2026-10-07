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
import android.content.Context
import com.github.vase4kin.teamcityapp.TeamCityApplicationBase
import com.github.vase4kin.teamcityapp.storage.SharedUserStorage
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.*
import okhttp3.*
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.kotlin.*
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import teamcityapp.libraries.authentication.*
import teamcityapp.libraries.remote.url.UrlFormatter

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35], application = Application::class)
class AppAuthenticationRepositoryTest {
    private val dispatcher = StandardTestDispatcher()
    private val context = mock<Context>()
    private val application = mock<TeamCityApplicationBase>()
    private val storage = mock<SharedUserStorage>()
    private val safe = mock<OkHttpClient>()
    private val unsafe = mock<OkHttpClient>()
    private val call = mock<Call>()
    private val formatter = mock<UrlFormatter>()
    private val credentials = AccountCredentials("https://server.example/server", "user", "pass")
    private val normalized = "https://server.example/server/"
    private lateinit var repository: AppAuthenticationRepository
    private var authenticator: Authenticator? = null
    private var status = 200
    private var stored = true

    @Before fun setup() {
        whenever(context.applicationContext).thenReturn(application)
        whenever(context.getString(any())).thenReturn("Warning")
        whenever(formatter.formatServerUrl(any())).thenReturn(normalized)
        for (client in listOf(safe, unsafe)) {
            val builder = mock<OkHttpClient.Builder>()
            whenever(client.newBuilder()).thenReturn(builder)
            whenever(builder.authenticator(any())).thenAnswer {
                authenticator = it.getArgument(0)
                builder
            }
            whenever(builder.build()).thenReturn(client)
            whenever(client.newCall(any())).thenReturn(call)
        }
        whenever(call.enqueue(any())).thenAnswer { invocation ->
            invocation.getArgument<Callback>(0).onResponse(call, Response.Builder().request(Request.Builder().url("https://server.example").build()).protocol(Protocol.HTTP_1_1).code(status).message("Response").body("Server message".toResponseBody()).build())
        }
        whenever(storage.saveUserAccountAndSetItAsActive(any(), any(), any(), any(), any())).thenAnswer { invocation ->
            val listener = invocation.getArgument<SharedUserStorage.OnStorageListener>(4)
            if (stored) listener.onSuccess() else listener.onFail()
        }
        repository = AppAuthenticationRepository(context, safe, unsafe, storage, formatter, dispatcher)
    }

    @Test fun userAuthenticationUsesChallengeAndNormalizedStorageBeforeSessionInitialization() = runTest(dispatcher) {
        assertEquals(AuthenticationResult.Success, repository.signIn(credentials, true, false))
        val request = argumentCaptor<Request>()
        verify(safe).newCall(request.capture())
        assertEquals("https://server.example/server/httpAuth/app/rest/server", request.firstValue.url.toString())
        assertNull(request.firstValue.header("Authorization"))
        val challenge = Response.Builder().request(request.firstValue).protocol(Protocol.HTTP_1_1).code(401).message("Unauthorized").body("".toResponseBody()).build()
        val authenticated = requireNotNull(authenticator).authenticate(null, challenge)!!
        assertEquals(Credentials.basic("user", "pass"), authenticated.header("Authorization"))
        assertNull(authenticator!!.authenticate(null, challenge.newBuilder().request(authenticated).build()))
        inOrder(storage, application) {
            verify(storage).saveUserAccountAndSetItAsActive(eq(normalized), eq("user"), eq("pass"), eq(false), any())
            verify(application).buildRestApiInjectorWithBaseUrl(normalized)
        }
    }

    @Test fun guestUsesGuestEndpointAndUnsafeClientOnlyWhenExplicitlySelected() = runTest(dispatcher) {
        assertEquals(AuthenticationResult.Success, repository.signIn(credentials.copy(guest = true, sslDisabled = true), true, false))
        val request = argumentCaptor<Request>()
        verify(unsafe).newCall(request.capture())
        assertEquals("https://server.example/server/guestAuth/app/rest/server", request.firstValue.url.toString())
        verify(unsafe, never()).newBuilder()
        verify(safe, never()).newCall(any())
        verify(storage).saveGuestUserAccountAndSetItAsActive(normalized, true)
        verify(application).buildRestApiInjectorWithBaseUrl(normalized)
    }

    @Test fun encryptionFailureLeavesCurrentAccountAndApiSessionUntouched() = runTest(dispatcher) {
        stored = false
        assertEquals(AuthenticationResult.SaveFailed, repository.signIn(credentials, true, false))
        verify(application, never()).buildRestApiInjectorWithBaseUrl(any())
        verify(storage, never()).saveGuestUserAccountAndSetItAsActive(any(), any())
    }

    @Test fun duplicateNormalizedAccountIsRejectedBeforeNetworkOrStorageWrites() = runTest(dispatcher) {
        whenever(storage.hasAccountWithUrl(normalized, "user")).thenReturn(true)
        assertEquals(AuthenticationResult.DuplicateAccount, repository.signIn(credentials, false, true))
        verify(safe, never()).newCall(any())
        verify(storage, never()).saveUserAccountAndSetItAsActive(any(), any(), any(), any(), any())
        verify(application, never()).buildRestApiInjectorWithBaseUrl(any())
    }

    @Test fun httpRequiresConsentOnFirstLoginAndIsAllowedAfterConsent() = runTest(dispatcher) {
        val insecure = credentials.copy(serverUrl = "http://server.example")
        assertTrue(repository.signIn(insecure, true, false) is AuthenticationResult.Failure)
        verify(safe, never()).newCall(any())
        assertEquals(AuthenticationResult.Success, repository.signIn(insecure, false, false))
    }

    @Test fun unauthorizedResponseDoesNotPersistOrSwitchAccounts() = runTest(dispatcher) {
        status = 401
        assertEquals(AuthenticationResult.Failure(401, "Server message"), repository.signIn(credentials, true, false))
        verify(storage, never()).saveUserAccountAndSetItAsActive(any(), any(), any(), any(), any())
        verify(application, never()).buildRestApiInjectorWithBaseUrl(any())
    }
}
