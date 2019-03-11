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

package com.github.vase4kin.teamcityapp;

import android.app.Application;
import android.support.annotation.NonNull;
import android.support.annotation.VisibleForTesting;
import android.text.TextUtils;

import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkManager;
import androidx.work.WorkRequest;

import com.crashlytics.android.Crashlytics;
import com.github.vase4kin.teamcityapp.dagger.components.AppComponent;
import com.github.vase4kin.teamcityapp.dagger.components.DaggerAppComponent;
import com.github.vase4kin.teamcityapp.dagger.components.DaggerRestApiComponent;
import com.github.vase4kin.teamcityapp.dagger.components.RestApiComponent;
import com.github.vase4kin.teamcityapp.dagger.modules.AppModule;
import com.github.vase4kin.teamcityapp.dagger.modules.RestApiModule;
import com.github.vase4kin.teamcityapp.utils.NotificationUtilsKt;
import com.github.vase4kin.teamcityapp.workmanager.NotifyAboutNewBuildsWorker;
import com.joanzapata.iconify.Iconify;
import com.joanzapata.iconify.fonts.FontAwesomeModule;
import com.joanzapata.iconify.fonts.MaterialCommunityModule;
import com.joanzapata.iconify.fonts.MaterialModule;

import java.util.concurrent.TimeUnit;

import io.fabric.sdk.android.Fabric;

/**
 * TeamCityApplication class
 */
public class TeamCityApplication extends Application {

    private RestApiComponent mRestApiInjector;
    private AppComponent mAppInjector;

    @Override
    public void onCreate() {
        super.onCreate();

        //#=============== Fabric ================#//
        if (!BuildConfig.DEBUG) {
            Fabric.with(this, new Crashlytics());
        }

        //#=============== Iconify ================#//
        Iconify
                .with(new MaterialModule())
                .with(new MaterialCommunityModule())
                .with(new FontAwesomeModule());

        //#=============== Dagger ================#//
        // app injector init
        // net injector init
        mAppInjector = DaggerAppComponent.builder()
                .appModule(new AppModule(this))
                .build();

        //Get default url
        String mBaseUrl = mAppInjector.sharedUserStorage().getActiveUser().getTeamcityUrl();
        // Rest api init
        if (!TextUtils.isEmpty(mBaseUrl)) {
            buildRestApiInjectorWithBaseUrl(mBaseUrl);
            NotificationUtilsKt.initAppNotificationChannels(this);
            initWorkManagerRequests();
        }
    }

    /**
     * Build rest api injector with provided url
     */
    public void buildRestApiInjectorWithBaseUrl(@NonNull String baseUrl) {
        mRestApiInjector = DaggerRestApiComponent.builder()
                .restApiModule(new RestApiModule(baseUrl))
                .appComponent(mAppInjector)
                .build();
    }

    /**
     * @return instance of RestApiInjector
     */
    public RestApiComponent getRestApiInjector() {
        return mRestApiInjector;
    }

    /**
     * Setter for RestApiInjector
     */
    @VisibleForTesting
    public void setRestApiInjector(RestApiComponent mRestApiComponent) {
        this.mRestApiInjector = mRestApiComponent;
    }

    /**
     * @return instance of AppInjector
     */
    public AppComponent getAppInjector() {
        return mAppInjector;
    }

    /**
     * Setter for AppInjector
     */
    @VisibleForTesting
    public void setAppInjector(AppComponent mAppComponent) {
        this.mAppInjector = mAppComponent;
    }
}
