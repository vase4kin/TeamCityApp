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

import android.app.Activity;
import android.app.Application;
import android.text.TextUtils;

import androidx.annotation.NonNull;
import androidx.annotation.VisibleForTesting;

import com.crashlytics.android.Crashlytics;
import com.github.vase4kin.teamcityapp.dagger.components.AppComponent;
import com.github.vase4kin.teamcityapp.dagger.components.DaggerAppComponent;
import com.github.vase4kin.teamcityapp.dagger.components.DaggerRestApiComponent;
import com.github.vase4kin.teamcityapp.dagger.components.RestApiComponent;
import com.github.vase4kin.teamcityapp.dagger.modules.AppModule;
import com.github.vase4kin.teamcityapp.dagger.modules.RestApiModule;
import com.joanzapata.iconify.Iconify;
import com.joanzapata.iconify.fonts.FontAwesomeModule;
import com.joanzapata.iconify.fonts.MaterialCommunityModule;
import com.joanzapata.iconify.fonts.MaterialModule;

import javax.inject.Inject;

import dagger.android.AndroidInjector;
import dagger.android.DispatchingAndroidInjector;
import dagger.android.HasActivityInjector;
import io.fabric.sdk.android.Fabric;

/**
 * TeamCityApplication class
 */
public class TeamCityApplication extends Application implements HasActivityInjector {

    @Inject
    DispatchingAndroidInjector<Activity> activityInjector;

    private RestApiComponent restApiInjector;
    private AppComponent appInjector;

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
        appInjector = DaggerAppComponent.builder()
                .appModule(new AppModule(this))
                .build();
        appInjector.inject(this);
        //Get default url
        String baseUrl = appInjector.sharedUserStorage().getActiveUser().getTeamcityUrl();
        // Rest api init
        if (!TextUtils.isEmpty(baseUrl)) {
            buildRestApiInjectorWithBaseUrl(baseUrl);
            restApiInjector.inject(this);
        }
    }

    /**
     * Build rest api injector with provided url
     */
    public void buildRestApiInjectorWithBaseUrl(@NonNull String baseUrl) {
        restApiInjector = DaggerRestApiComponent.builder()
                .restApiModule(new RestApiModule(baseUrl))
                .appComponent(appInjector)
                .build();
        restApiInjector.inject(this);
    }

    /**
     * @return instance of RestApiInjector
     */
    public RestApiComponent getRestApiInjector() {
        return restApiInjector;
    }

    /**
     * Setter for RestApiInjector
     */
    @VisibleForTesting
    public void setRestApiInjector(RestApiComponent mRestApiComponent) {
        this.restApiInjector = mRestApiComponent;
        this.restApiInjector.inject(this);
    }

    /**
     * @return instance of AppInjector
     */
    public AppComponent getAppInjector() {
        return appInjector;
    }

    /**
     * Setter for AppInjector
     */
    @VisibleForTesting
    public void setAppInjector(AppComponent mAppComponent) {
        this.appInjector = mAppComponent;
        this.appInjector.inject(this);
    }

    @Override
    public AndroidInjector<Activity> activityInjector() {
        return activityInjector;
    }
}
