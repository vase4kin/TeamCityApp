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

import android.content.Context
import android.net.Uri
import com.github.vase4kin.teamcityapp.TeamCityApplicationBase
import com.github.vase4kin.teamcityapp.api.AUTHORIZATION
import com.github.vase4kin.teamcityapp.dagger.modules.AppModule
import com.github.vase4kin.teamcityapp.storage.SharedUserStorage
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.IOException
import java.net.UnknownHostException
import javax.inject.Inject
import javax.inject.Named
import kotlin.coroutines.resume
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import okhttp3.*
import teamcityapp.libraries.authentication.*
import teamcityapp.libraries.authentication.R as AuthR
import teamcityapp.libraries.coroutines.MainDispatcher
import teamcityapp.libraries.remote.url.UrlFormatter

/** Keeps the existing challenge-based Basic auth, encrypted storage, and API session boundary. */
class AppAuthenticationRepository @Inject constructor(
    @ApplicationContext private val context: Context,
    @Named(AppModule.CLIENT_BASE) private val safeClient: OkHttpClient,
    @Named(AppModule.CLIENT_BASE_UNSAFE) private val unsafeClient: OkHttpClient,
    private val storage: SharedUserStorage,
    private val formatter: UrlFormatter,
    @MainDispatcher private val mainDispatcher: CoroutineDispatcher
) : AuthenticationRepository {
    override suspend fun signIn(credentials: AccountCredentials, checkSecureConnection: Boolean, checkDuplicate: Boolean): AuthenticationResult {
        val normalizedUrl = formatter.formatServerUrl(credentials.serverUrl)
        val duplicate = withContext(mainDispatcher) {
            checkDuplicate && if (credentials.guest) {
                storage.hasGuestAccountWithUrl(credentials.serverUrl) || storage.hasGuestAccountWithUrl(normalizedUrl)
            } else {
                storage.hasAccountWithUrl(credentials.serverUrl, credentials.userName) || storage.hasAccountWithUrl(normalizedUrl, credentials.userName)
            }
        }
        if (duplicate) return AuthenticationResult.DuplicateAccount
        val uri = Uri.parse(credentials.serverUrl).buildUpon().appendEncodedPath(if (credentials.guest) "guestAuth/app/rest/server" else "httpAuth/app/rest/server").build()
        if (checkSecureConnection && uri.scheme == "http") return AuthenticationResult.Failure(-1, context.getString(AuthR.string.server_not_secure_http))
        val response = try {
            val base = if (credentials.sslDisabled) unsafeClient else safeClient
            val client = if (credentials.guest) {
                base
            } else {
                base.newBuilder().authenticator { _, response ->
                    val basic = Credentials.basic(credentials.userName, credentials.password)
                    if (basic == response.request.header(AUTHORIZATION)) null else response.request.newBuilder().header(AUTHORIZATION, basic).build()
                }.build()
            }
            val request = Request.Builder().url(uri.toString()).build()
            client.newCall(request).awaitAuthentication()
        } catch (cancelled: CancellationException) {
            throw cancelled
        } catch (_: UnknownHostException) {
            return AuthenticationResult.Failure(0, context.getString(AuthR.string.server_no_such_server))
        } catch (_: IllegalArgumentException) {
            return AuthenticationResult.Failure(0, context.getString(AuthR.string.server_correct_url))
        } catch (error: IOException) {
            return AuthenticationResult.Failure(0, error.message.orEmpty())
        }
        if (response != AuthenticationResult.Success) return response
        // Storage and API-session initialization remain on the same thread as existing consumers.
        // No suspension between successful persistence and rebuilding the account API graph.
        return withContext(mainDispatcher) {
            var saved = credentials.guest
            if (credentials.guest) {
                storage.saveGuestUserAccountAndSetItAsActive(normalizedUrl, credentials.sslDisabled)
            } else {
                storage.saveUserAccountAndSetItAsActive(
                    normalizedUrl,
                    credentials.userName,
                    credentials.password,
                    credentials.sslDisabled,
                    object : SharedUserStorage.OnStorageListener {
                        override fun onSuccess() {
                            saved = true
                        }
                        override fun onFail() {
                            saved = false
                        }
                    }
                )
            }
            if (saved) {
                (context.applicationContext as TeamCityApplicationBase).buildRestApiInjectorWithBaseUrl(normalizedUrl)
                AuthenticationResult.Success
            } else {
                AuthenticationResult.SaveFailed
            }
        }
    }
}

internal suspend fun Call.awaitAuthentication(): AuthenticationResult = suspendCancellableCoroutine { continuation ->
    continuation.invokeOnCancellation { cancel() }
    enqueue(object : Callback {
        override fun onFailure(call: Call, error: IOException) {
            if (continuation.isActive) continuation.resumeWith(Result.failure(error))
        }
        override fun onResponse(call: Call, response: Response) {
            val result = response.use {
                if (it.isSuccessful) {
                    AuthenticationResult.Success
                } else {
                    val message = try {
                        it.body.string().ifBlank { it.message }
                    } catch (_: IOException) {
                        it.message
                    }
                    AuthenticationResult.Failure(it.code, message)
                }
            }
            if (continuation.isActive) continuation.resume(result)
        }
    })
}
