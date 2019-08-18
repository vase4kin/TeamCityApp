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

package com.github.vase4kin.teamcityapp.buildlist.router

import android.app.Activity
import com.github.vase4kin.teamcityapp.app_navigation.AppNavigationItem
import com.github.vase4kin.teamcityapp.build_details.view.BuildDetailsActivity
import com.github.vase4kin.teamcityapp.buildlist.api.Build
import com.github.vase4kin.teamcityapp.filter_builds.view.FilterBuildsActivity
import com.github.vase4kin.teamcityapp.home.view.HomeActivity
import com.github.vase4kin.teamcityapp.runbuild.view.RunBuildActivity

/**
 * impl of [BuildListRouter]
 */
open class BuildListRouterImpl(protected val activity: Activity) : BuildListRouter {

    /**
     * {@inheritDoc}
     */
    override fun openBuildPage(build: Build, buildTypeName: String?) {
        BuildDetailsActivity.start(activity, build, buildTypeName)
    }

    /**
     * {@inheritDoc}
     */
    override fun openRunBuildPage(buildTypeId: String) {
        RunBuildActivity.startForResult(activity, buildTypeId)
    }

    /**
     * {@inheritDoc}
     */
    override fun openFilterBuildsPage(buildTypeId: String) {
        FilterBuildsActivity.startForResult(activity, buildTypeId)
    }

    /**
     * {@inheritDoc}
     */
    override fun openFavorites() {
        HomeActivity.startWithTabSelected(activity, AppNavigationItem.FAVORITES)
    }
}
