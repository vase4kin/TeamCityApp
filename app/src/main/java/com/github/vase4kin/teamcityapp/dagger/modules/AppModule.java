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

package com.github.vase4kin.teamcityapp.dagger.modules;

import android.content.Context;
import android.support.annotation.VisibleForTesting;

import com.github.vase4kin.teamcityapp.TeamCityApplication;
import com.github.vase4kin.teamcityapp.api.GuestUserAuthInterceptor;
import com.github.vase4kin.teamcityapp.api.LoggingInterceptor;
import com.github.vase4kin.teamcityapp.api.TeamCityAuthenticator;
import com.github.vase4kin.teamcityapp.storage.SharedUserStorage;
import com.github.vase4kin.teamcityapp.storage.api.UserAccount;

import java.util.concurrent.TimeUnit;

import javax.inject.Named;
import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import de.greenrobot.event.EventBus;
import okhttp3.OkHttpClient;

@Module
public class AppModule {

    private static final int CONNECTION_TIMEOUT = 10;
    private static final int READ_TIMEOUT = 30;
    private static final int WRITE_TIMEOUT = 10;

    public static final String CLIENT_BASE = "base";
    public static final String CLIENT_AUTH = "auth";

    private TeamCityApplication mApplication;

    public AppModule(TeamCityApplication application) {
        mApplication = application;
    }

    @VisibleForTesting
    @Provides
    @Singleton
    protected Context provideContext() {
        return mApplication.getApplicationContext();
    }

    @VisibleForTesting
    @Provides
    @Singleton
    public SharedUserStorage provideSharedUserStorage() {
        return SharedUserStorage.init(mApplication);
    }

    @VisibleForTesting
    @Named(CLIENT_AUTH)
    @Singleton
    @Provides
    public OkHttpClient providesAuthHttpClient(SharedUserStorage sharedUserStorage,
                                               @Named(CLIENT_BASE) OkHttpClient okHttpClient) {
        UserAccount userAccount = sharedUserStorage.getActiveUser();
        if (userAccount.isGuestUser()) {
            return okHttpClient.newBuilder()
                    .addInterceptor(new GuestUserAuthInterceptor())
                    .build();
        } else {
            return okHttpClient.newBuilder()
                    .authenticator(new TeamCityAuthenticator(sharedUserStorage))
                    .build();
        }
    }

    @VisibleForTesting
    @Named(CLIENT_BASE)
    @Singleton
    @Provides
    public OkHttpClient providesBaseHttpClient() {
        return new OkHttpClient.Builder()
                .connectTimeout(CONNECTION_TIMEOUT, TimeUnit.SECONDS)
                .readTimeout(READ_TIMEOUT, TimeUnit.SECONDS)
                .writeTimeout(WRITE_TIMEOUT, TimeUnit.SECONDS)
                .addInterceptor(new LoggingInterceptor())
                .build();
    }

    @VisibleForTesting
    @Provides
    @Singleton
    protected EventBus providesEventBus() {
        return EventBus.getDefault();
    }
}
