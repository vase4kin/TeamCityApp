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
import com.github.vase4kin.teamcityapp.api.LoggingInterceptor;
import com.github.vase4kin.teamcityapp.storage.SharedUserStorage;

import java.util.concurrent.TimeUnit;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import de.greenrobot.event.EventBus;
import okhttp3.OkHttpClient;

@Module
public class AppModule {

    TeamCityApplication mApplication;

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
    @Provides
    @Singleton
    public OkHttpClient provideOkHttpClient() {
        return new OkHttpClient.Builder()
                .connectTimeout(10, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)
                .writeTimeout(10, TimeUnit.SECONDS)
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
