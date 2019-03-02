/*
 * Copyright 2016 Andrey Tolpeev
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.github.vase4kin.teamcityapp.account.create.data;

import android.content.Context;
import android.net.Uri;
import android.os.Handler;
import android.support.annotation.NonNull;

import com.crashlytics.android.Crashlytics;
import com.github.vase4kin.teamcityapp.R;
import com.github.vase4kin.teamcityapp.TeamCityApplication;
import com.github.vase4kin.teamcityapp.account.create.helper.UrlFormatter;
import com.github.vase4kin.teamcityapp.storage.SharedUserStorage;

import java.io.IOException;
import java.net.UnknownHostException;

import okhttp3.Authenticator;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Credentials;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.Route;

import static com.github.vase4kin.teamcityapp.api.TeamCityServiceKt.AUTHORIZATION;

/**
 * Impl of {@link CreateAccountDataManager}
 */
public class CreateAccountDataManagerImpl implements CreateAccountDataManager {

    private static final String AUTH_URL = "httpAuth/app/rest/server";
    private static final String AUTH_GUEST_URL = "guestAuth/app/rest/server";

    private static final String SCHEME_HTTP = "http";

    /**
     * Default error code
     */
    private static final int DEFAULT_ERROR_CODE = 0;

    private final Context mContext;
    private final OkHttpClient baseOkHttpClient;
    private final OkHttpClient unsafeBaseOkHttpClient;
    private final SharedUserStorage mSharedUserStorage;
    private final UrlFormatter mUrlFormatter;

    public CreateAccountDataManagerImpl(Context context,
                                        OkHttpClient baseOkHttpClient,
                                        OkHttpClient unsafeBaseOkHttpClient,
                                        SharedUserStorage sharedUserStorage,
                                        UrlFormatter urlFormatter) {
        this.mContext = context;
        this.baseOkHttpClient = baseOkHttpClient;
        this.unsafeBaseOkHttpClient = unsafeBaseOkHttpClient;
        this.mSharedUserStorage = sharedUserStorage;
        this.mUrlFormatter = urlFormatter;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void authUser(@NonNull final CustomOnLoadingListener<String> listener,
                         final String url,
                         final String userName,
                         final String password,
                         boolean isSslDisabled,
                         boolean checkSecureConnection) {
        // Creating okHttpClient with authenticator
        OkHttpClient okHttpClient = createClientWithAuthenticator(userName, password, isSslDisabled);
        // Handling request
        handleAuthRequest(url, AUTH_URL, okHttpClient, listener, checkSecureConnection);
    }

    private OkHttpClient createClientWithAuthenticator(final String userName,
                                                       final String password,
                                                       boolean isSslDisabled) {
        return getClient(isSslDisabled).newBuilder().authenticator(new Authenticator() {
            @Override
            public Request authenticate(@NonNull Route route, @NonNull Response response) {
                String credential = Credentials.basic(userName, password);

                if (credential.equals(response.request().header(AUTHORIZATION))) {
                    return null; // If we already failed with these credentials, don't retry.
                }

                return response.request().newBuilder()
                        .header(AUTHORIZATION, credential)
                        .build();
            }
        }).build();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void authGuestUser(@NonNull final CustomOnLoadingListener<String> listener,
                              final String url,
                              boolean isSslDisabled,
                              boolean checkSecureConnection) {
        handleAuthRequest(url, AUTH_GUEST_URL, getClient(isSslDisabled), listener, checkSecureConnection);
    }

    /**
     * @return {@link OkHttpClient} depending on ssl enabled state
     */
    private OkHttpClient getClient(boolean isSslEnabled) {
        return isSslEnabled ? unsafeBaseOkHttpClient : baseOkHttpClient;
    }

    /**
     * Handle auth request
     *
     * @param serverUrl    - Server url
     * @param authUrl      - Appended auth url
     * @param okHttpClient - Client to make an auth
     * @param listener     - Listener to receive callbacks
     */
    private void handleAuthRequest(final String serverUrl,
                                   final String authUrl,
                                   final OkHttpClient okHttpClient,
                                   final CustomOnLoadingListener<String> listener,
                                   final boolean checkSecureConnection) {

        Uri serverAuthUri = Uri.parse(serverUrl).buildUpon()
                .appendEncodedPath(authUrl).build();

        if (checkSecureConnection && SCHEME_HTTP.equals(serverAuthUri.getScheme())) {
            listener.onFail(ERROR_CODE_HTTP_NOT_SECURE, mContext.getString(R.string.server_not_secure_http));
            return;
        }

        final Handler handler = new Handler(mContext.getMainLooper());

        try {

            final Request request = new Request.Builder()
                    .url(serverAuthUri.toString())
                    .build();

            okHttpClient
                    .newCall(request)
                    .enqueue(new Callback() {
                        @Override
                        public void onFailure(@NonNull final Call call, @NonNull final IOException e) {
                            handler.post(new Runnable() {
                                @Override
                                public void run() {
                                    if (e instanceof UnknownHostException) {
                                        listener.onFail(DEFAULT_ERROR_CODE, mContext.getString(R.string.server_no_such_server));
                                    } else {
                                        listener.onFail(DEFAULT_ERROR_CODE, e.getMessage());
                                    }
                                }
                            });
                        }

                        @Override
                        public void onResponse(@NonNull Call call, @NonNull final Response response) {
                            if (response.isSuccessful()) {
                                final String formattedServerUrl = mUrlFormatter.formatServerUrl(serverUrl);
                                handler.post(new Runnable() {
                                    @Override
                                    public void run() {
                                        listener.onSuccess(formattedServerUrl);
                                    }
                                });
                            } else {
                                String message;
                                if (response.body() != null && response.body().source() != null) {
                                    try {
                                        message = response.body().source().readUtf8();
                                    } catch (IOException exception) {
                                        Crashlytics.logException(exception);
                                        message = response.message();
                                    }
                                } else {
                                    message = response.message();
                                }
                                final String errorMessage = message;
                                handler.post(new Runnable() {
                                    @Override
                                    public void run() {
                                        listener.onFail(response.code(), errorMessage);
                                    }
                                });
                            }
                        }
                    });
        } catch (IllegalArgumentException e) {
            listener.onFail(DEFAULT_ERROR_CODE, mContext.getString(R.string.server_correct_url));
        } catch (Exception e) {
            listener.onFail(DEFAULT_ERROR_CODE, mContext.getString(R.string.server_check_url));
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void saveNewUserAccount(final String serverUrl,
                                   String userName,
                                   String password,
                                   boolean isSslDisabled,
                                   final OnLoadingListener<String> listener) {
        mSharedUserStorage.saveUserAccountAndSetItAsActive(serverUrl, userName, password, isSslDisabled, new SharedUserStorage.OnStorageListener() {
            @Override
            public void onSuccess() {
                // On data save success
                listener.onSuccess(serverUrl);
            }

            @Override
            public void onFail() {
                // On data save fail
                listener.onFail("");
            }
        });
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void saveGuestUserAccount(String url, boolean isSslDisabled) {
        mSharedUserStorage.saveGuestUserAccountAndSetItAsActive(url, isSslDisabled);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void initTeamCityService(String url) {
        ((TeamCityApplication) mContext.getApplicationContext()).buildRestApiInjectorWithBaseUrl(url);
    }
}
