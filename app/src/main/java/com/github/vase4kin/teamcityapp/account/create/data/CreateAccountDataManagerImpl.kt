/*
 * Copyright 2020 Andrey Tolpeev
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

package com.github.vase4kin.teamcityapp.account.create.data

import android.content.Context
import android.net.Uri
import android.os.Handler
import com.github.vase4kin.teamcityapp.R
import com.github.vase4kin.teamcityapp.TeamCityApplication
import com.github.vase4kin.teamcityapp.account.create.helper.UrlFormatter
import com.github.vase4kin.teamcityapp.api.AUTHORIZATION
import com.github.vase4kin.teamcityapp.storage.SharedUserStorage
import com.google.firebase.crashlytics.FirebaseCrashlytics
import okhttp3.Call
import okhttp3.Callback
import okhttp3.Credentials
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import java.io.IOException
import java.net.UnknownHostException

private const val AUTH_URL = "httpAuth/app/rest/server"
private const val AUTH_GUEST_URL = "guestAuth/app/rest/server"

private const val SCHEME_HTTP = "http"

/**
 * Default error code
 */
private const val DEFAULT_ERROR_CODE = 0

/**
 * Impl of [CreateAccountDataManager]
 */
class CreateAccountDataManagerImpl(
    private val context: Context,
    private val baseOkHttpClient: OkHttpClient,
    private val unsafeBaseOkHttpClient: OkHttpClient,
    private val sharedUserStorage: SharedUserStorage,
    private val urlFormatter: UrlFormatter
) : CreateAccountDataManager {

    /**
     * {@inheritDoc}
     */
    override fun authUser(
        listener: CustomOnLoadingListener<String>,
        url: String,
        userName: String,
        password: String,
        isSslDisabled: Boolean,
        checkSecureConnection: Boolean
    ) {
        // Creating okHttpClient with authenticator
        val okHttpClient = createClientWithAuthenticator(userName, password, isSslDisabled)
        // Handling request
        handleAuthRequest(url, AUTH_URL, okHttpClient, listener, checkSecureConnection)
    }

    private fun createClientWithAuthenticator(
        userName: String,
        password: String,
        isSslDisabled: Boolean
    ): OkHttpClient {
        return getClient(isSslDisabled).newBuilder().authenticator { _, response ->
            val credential = Credentials.basic(userName, password)

            if (credential == response.request().header(AUTHORIZATION)) {
                return@authenticator null // If we already failed with these credentials, don't retry.
            }

            response.request().newBuilder()
                .header(AUTHORIZATION, credential)
                .build()
        }.build()
    }

    /**
     * {@inheritDoc}
     */
    override fun authGuestUser(
        listener: CustomOnLoadingListener<String>,
        url: String,
        isSslDisabled: Boolean,
        checkSecureConnection: Boolean
    ) {
        handleAuthRequest(
            url,
            AUTH_GUEST_URL,
            getClient(isSslDisabled),
            listener,
            checkSecureConnection
        )
    }

    /**
     * @return [OkHttpClient] depending on ssl enabled state
     */
    private fun getClient(isSslEnabled: Boolean): OkHttpClient {
        return if (isSslEnabled) unsafeBaseOkHttpClient else baseOkHttpClient
    }

    /**
     * Handle auth request
     *
     * @param serverUrl - Server url
     * @param authUrl - Appended auth url
     * @param okHttpClient - Client to make an auth
     * @param listener - Listener to receive callbacks
     */
    private fun handleAuthRequest(
        serverUrl: String,
        authUrl: String,
        okHttpClient: OkHttpClient,
        listener: CustomOnLoadingListener<String>,
        checkSecureConnection: Boolean
    ) {

        val serverAuthUri = Uri.parse(serverUrl).buildUpon()
            .appendEncodedPath(authUrl).build()

        if (checkSecureConnection && SCHEME_HTTP == serverAuthUri.scheme) {
            listener.onFail(
                CreateAccountDataManager.ERROR_CODE_HTTP_NOT_SECURE,
                context.getString(R.string.server_not_secure_http)
            )
            return
        }

        val handler = Handler(context.mainLooper)

        try {

            val request = Request.Builder()
                .url(serverAuthUri.toString())
                .build()

            okHttpClient
                .newCall(request)
                .enqueue(object : Callback {
                    override fun onFailure(call: Call, e: IOException) {
                        handler.post {
                            if (e is UnknownHostException) {
                                listener.onFail(
                                    DEFAULT_ERROR_CODE,
                                    context.getString(R.string.server_no_such_server)
                                )
                            } else {
                                listener.onFail(DEFAULT_ERROR_CODE, e.message ?: "")
                            }
                        }
                    }

                    override fun onResponse(call: Call, response: Response) {
                        if (response.isSuccessful) {
                            val formattedServerUrl = urlFormatter.formatServerUrl(serverUrl)
                            handler.post { listener.onSuccess(formattedServerUrl) }
                        } else {
                            var message: String
                            if (response.body() != null && response.body()!!.source() != null) {
                                try {
                                    message = response.body()!!.source().readUtf8()
                                } catch (exception: IOException) {
                                    FirebaseCrashlytics.getInstance().recordException(exception)
                                    message = response.message()
                                }
                            } else {
                                message = response.message()
                            }
                            handler.post { listener.onFail(response.code(), message) }
                        }
                    }
                })
        } catch (e: IllegalArgumentException) {
            listener.onFail(DEFAULT_ERROR_CODE, context.getString(R.string.server_correct_url))
        } catch (e: Exception) {
            listener.onFail(DEFAULT_ERROR_CODE, context.getString(R.string.server_check_url))
        }
    }

    /**
     * {@inheritDoc}
     */
    override fun saveNewUserAccount(
        serverUrl: String,
        userName: String,
        password: String,
        isSslDisabled: Boolean,
        listener: OnLoadingListener<String>
    ) {
        sharedUserStorage.saveUserAccountAndSetItAsActive(
            serverUrl,
            userName,
            password,
            isSslDisabled,
            object : SharedUserStorage.OnStorageListener {
                override fun onSuccess() {
                    // On data save success
                    listener.onSuccess(serverUrl)
                }

                override fun onFail() {
                    // On data save fail
                    listener.onFail("")
                }
            })
    }

    /**
     * {@inheritDoc}
     */
    override fun saveGuestUserAccount(url: String, isSslDisabled: Boolean) {
        sharedUserStorage.saveGuestUserAccountAndSetItAsActive(url, isSslDisabled)
    }

    /**
     * {@inheritDoc}
     */
    override fun initTeamCityService(url: String) {
        (context.applicationContext as TeamCityApplication).buildRestApiInjectorWithBaseUrl(url)
    }
}
