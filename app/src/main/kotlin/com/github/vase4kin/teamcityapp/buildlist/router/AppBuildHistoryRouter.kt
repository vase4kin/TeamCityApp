/*
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

package com.github.vase4kin.teamcityapp.buildlist.router

import android.app.Activity
import com.github.vase4kin.teamcityapp.app_navigation.AppNavigationItem
import com.github.vase4kin.teamcityapp.build_details.view.BuildDetailsActivity
import com.github.vase4kin.teamcityapp.builds.data.AppBuildLaunchMapper
import com.github.vase4kin.teamcityapp.home.view.HomeActivity
import javax.inject.Inject
import teamcityapp.features.build_history.api.BuildHistoryAppRouter
import teamcityapp.libraries.builds.BuildLaunchData

class AppBuildHistoryRouter @Inject constructor(private val mapper: AppBuildLaunchMapper) : BuildHistoryAppRouter {
    override fun openBuild(activity: Activity, build: BuildLaunchData, configurationName: String?) {
        BuildDetailsActivity.start(activity, mapper.toLegacyBuild(build), configurationName)
    }

    override fun openFavorites(activity: Activity) {
        HomeActivity.startWithTabSelected(activity, AppNavigationItem.FAVORITES)
    }
}
