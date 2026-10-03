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

package com.github.vase4kin.teamcityapp

import android.app.Application
import androidx.annotation.VisibleForTesting
import com.github.vase4kin.teamcityapp.dagger.ApiSession
import com.github.vase4kin.teamcityapp.dagger.components.AppComponent
import com.github.vase4kin.teamcityapp.dagger.components.DaggerRestApiComponent
import com.github.vase4kin.teamcityapp.dagger.components.RestApiComponent
import com.github.vase4kin.teamcityapp.dagger.modules.RestApiModule
import dagger.hilt.EntryPoints
import teamcityapp.libraries.utils.applyThemeFromSettings

open class TeamCityApplicationBase : Application() {

    val appInjector: AppComponent
        get() = EntryPoints.get(this, AppComponent::class.java)

    private val apiSession by lazy {
        ApiSession({ appInjector.storage() }) { baseUrl ->
            DaggerRestApiComponent.builder()
                .restApiModule(RestApiModule(baseUrl))
                .appComponent(appInjector)
                .build()
        }
    }

    val restApiInjector: RestApiComponent
        get() = apiSession.requireGraph()

    override fun onCreate() {
        super.onCreate()
        // Custom Hilt test applications create their graph in HiltAndroidRule.
        if (this is TeamCityApplication) {
            val baseUrl = appInjector.sharedUserStorage().activeUser.teamcityUrl
            if (baseUrl.isNotEmpty()) buildRestApiInjectorWithBaseUrl(baseUrl)
        }
        applyThemeFromSettings()
    }

    fun buildRestApiInjectorWithBaseUrl(baseUrl: String) {
        apiSession.rebuild(baseUrl)
    }

    fun clearApiSession() {
        apiSession.clear()
    }

    @VisibleForTesting
    fun setApiGraphFactoryForTesting(factory: (String) -> RestApiComponent) {
        apiSession.setFactoryForTesting(factory)
    }
}
