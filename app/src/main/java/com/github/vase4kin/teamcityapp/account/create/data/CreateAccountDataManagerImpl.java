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
import android.text.TextUtils;

import com.github.vase4kin.teamcityapp.R;
import com.github.vase4kin.teamcityapp.TeamCityApplication;
import com.github.vase4kin.teamcityapp.api.TeamCityService;
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

/**
 * Impl of {@link CreateAccountDataManager}
 */
public class CreateAccountDataManagerImpl implements CreateAccountDataManager {

    private static final String AUTH_URL = "httpAuth/app/rest/server";
    private static final String AUTH_GUEST_URL = "guestAuth/app/rest/server";

    /**
     * Default error code
     */
    private static final int DEFAULT_ERROR_CODE = 0;

    private Context mContext;
    private OkHttpClient mOkHttpClient;
    private SharedUserStorage mSharedUserStorage;

    public CreateAccountDataManagerImpl(Context context,
                                        OkHttpClient okHttpClient,
                                        SharedUserStorage sharedUserStorage) {
        this.mContext = context;
        this.mOkHttpClient = okHttpClient;
        this.mSharedUserStorage = sharedUserStorage;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void loadData(@NonNull final CustomOnLoadingListener<String> listener,
                         final String url,
                         final String userName,
                         final String password,
                         boolean isGuestUser) {

        if (TextUtils.isEmpty(url)) {
            listener.onFail(DEFAULT_ERROR_CODE, mContext.getString(R.string.server_cannot_be_empty));
            return;
        }

        if (!isGuestUser) {
            if (TextUtils.isEmpty(userName)) {
                listener.onFail(DEFAULT_ERROR_CODE, "Username cannot be empty!");
                return;
            }

            if (TextUtils.isEmpty(password)) {
                listener.onFail(DEFAULT_ERROR_CODE, "Password cannot be empty!");
                return;
            }
        }

        final Handler handler = new Handler();

        try {

            String serverUrl = Uri.parse(url).buildUpon()
                    .appendEncodedPath(isGuestUser ? AUTH_GUEST_URL : AUTH_URL)
                    .toString();

            final Request request = new Request.Builder()
                    .url(serverUrl)
                    .build();

            mOkHttpClient = isGuestUser
                    ? mOkHttpClient
                    : mOkHttpClient.newBuilder().authenticator(new Authenticator() {
                @Override
                public Request authenticate(Route route, Response response) throws IOException {
                    String credential = Credentials.basic(userName, password);

                    if (credential.equals(response.request().header(TeamCityService.AUTHORIZATION))) {
                        return null; // If we already failed with these credentials, don't retry.
                    }

                    return response.request().newBuilder()
                            .header(TeamCityService.AUTHORIZATION, credential)
                            .build();
                }
            }).build();

            mOkHttpClient
                    .newCall(request)
                    .enqueue(new Callback() {
                        @Override
                public void onFailure(final Call call, final IOException e) {
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
                public void onResponse(Call call, final Response response) throws IOException {
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            if (response.isSuccessful()) {
                                listener.onSuccess(url);
                            } else {
                                listener.onFail(response.code(), response.message());
                            }
                        }
                    });
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
    public void saveNewUserAccount(String serverUrl, String userName, String password) {
        mSharedUserStorage.saveUserAccountAndSetItAsActive(serverUrl, userName, password);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void saveGuestUserAccount(String url) {
        mSharedUserStorage.saveGuestUserAccountAndSetItAsActive(url);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void initTeamCityService(String url) {
        ((TeamCityApplication) mContext.getApplicationContext()).buildRestApiInjectorWithBaseUrl(url);
    }
}
