/*
 * Copyright 2019 Andrey Tolpeev
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

package com.github.vase4kin.teamcityapp

import android.app.Activity
import android.app.Application
import android.text.TextUtils
import androidx.annotation.VisibleForTesting
import com.crashlytics.android.Crashlytics
import com.github.vase4kin.teamcityapp.dagger.components.AppComponent
import com.github.vase4kin.teamcityapp.dagger.components.DaggerAppComponent
import com.github.vase4kin.teamcityapp.dagger.components.DaggerRestApiComponent
import com.github.vase4kin.teamcityapp.dagger.components.RestApiComponent
import com.github.vase4kin.teamcityapp.dagger.modules.AppModule
import com.github.vase4kin.teamcityapp.dagger.modules.RestApiModule
import com.joanzapata.iconify.Iconify
import com.joanzapata.iconify.fonts.FontAwesomeModule
import com.joanzapata.iconify.fonts.MaterialCommunityModule
import com.joanzapata.iconify.fonts.MaterialModule
import dagger.android.AndroidInjector
import dagger.android.DispatchingAndroidInjector
import dagger.android.HasActivityInjector
import io.fabric.sdk.android.Fabric
import javax.inject.Inject

/**
 * TeamCityApplication class
 */
class TeamCityApplication : Application(), HasActivityInjector {

    @Inject
    lateinit var activityInjector: DispatchingAndroidInjector<Activity>

    lateinit var restApiInjector: RestApiComponent
        private set
    lateinit var appInjector: AppComponent
        private set

    override fun onCreate() {
        super.onCreate()

        // #=============== Fabric ================#//
        if (!BuildConfig.DEBUG) {
            Fabric.with(this, Crashlytics())
        }

        // #=============== Iconify ================#//
        Iconify
                .with(MaterialModule())
                .with(MaterialCommunityModule())
                .with(FontAwesomeModule())

        // #=============== Dagger ================#//
        // app injector init
        // net injector init
        appInjector = DaggerAppComponent.builder()
                .appModule(AppModule(this))
                .build()
        appInjector.inject(this)
        // Get default url
        val baseUrl = appInjector.sharedUserStorage().activeUser.teamcityUrl
        // Rest api init
        if (!TextUtils.isEmpty(baseUrl)) {
            buildRestApiInjectorWithBaseUrl(baseUrl)
            restApiInjector.inject(this)
        }
    }

    /**
     * Build rest api injector with provided url
     */
    fun buildRestApiInjectorWithBaseUrl(baseUrl: String) {
        restApiInjector = DaggerRestApiComponent.builder()
                .restApiModule(RestApiModule(baseUrl))
                .appComponent(appInjector)
                .build()
        restApiInjector.inject(this)
    }

    /**
     * Setter for RestApiInjector
     */
    @VisibleForTesting
    fun setRestApiInjector(restApiComponent: RestApiComponent) {
        this.restApiInjector = restApiComponent
        this.restApiInjector.inject(this)
    }

    /**
     * Setter for AppInjector
     */
    @VisibleForTesting
    fun setAppInjector(appComponent: AppComponent) {
        this.appInjector = appComponent
        this.appInjector.inject(this)
    }

    override fun activityInjector(): AndroidInjector<Activity> {
        return activityInjector
    }
}
