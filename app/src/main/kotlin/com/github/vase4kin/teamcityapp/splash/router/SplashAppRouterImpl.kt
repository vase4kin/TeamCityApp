/*
 * Copyright 2019 Andrey Tolpeev
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

package com.github.vase4kin.teamcityapp.splash.router

import android.app.Activity
import com.github.vase4kin.teamcityapp.home.view.HomeActivity
import javax.inject.Inject
import teamcityapp.features.login.api.navigation.LoginNavigation
import teamcityapp.features.splash.api.SplashDestination
import teamcityapp.features.splash.api.router.SplashAppRouter

class SplashAppRouterImpl @Inject constructor(private val loginNavigation: LoginNavigation) : SplashAppRouter {
    override fun open(activity: Activity, destination: SplashDestination) {
        when (destination) {
            SplashDestination.Home -> HomeActivity.start(activity)
            SplashDestination.Login -> loginNavigation.open(activity)
        }
    }
}
