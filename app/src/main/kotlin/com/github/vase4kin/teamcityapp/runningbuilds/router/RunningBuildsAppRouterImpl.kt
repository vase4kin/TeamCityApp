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

package com.github.vase4kin.teamcityapp.runningbuilds.router

import android.app.Activity
import com.github.vase4kin.teamcityapp.build_details.view.BuildDetailsActivity
import com.github.vase4kin.teamcityapp.builds.data.AppBuildLaunchMapper
import com.github.vase4kin.teamcityapp.home.router.HomeRouter
import javax.inject.Inject
import teamcityapp.features.build_history.api.BuildHistoryNavigation
import teamcityapp.features.running_builds.api.RunningBuildsAppRouter
import teamcityapp.libraries.builds.BuildLaunchData

/** Bind in FragmentComponent: Activity/HomeRouter must never be retained in a ViewModel. */
class RunningBuildsAppRouterImpl @Inject constructor(
    private val activity: Activity,
    private val homeRouter: HomeRouter,
    private val mapper: AppBuildLaunchMapper,
    private val historyNavigation: BuildHistoryNavigation
) : RunningBuildsAppRouter {
    override fun openDrawer() = homeRouter.openDrawer()
    override fun openBuild(build: BuildLaunchData) {
        // Home's old empty arguments pass no configuration title and use the standard task launch.
        BuildDetailsActivity.start(activity, mapper.toLegacyBuild(build), null)
    }
    override fun openBuildHistory(configurationId: String, configurationName: String) {
        historyNavigation.open(activity, configurationId, configurationName)
    }
}
