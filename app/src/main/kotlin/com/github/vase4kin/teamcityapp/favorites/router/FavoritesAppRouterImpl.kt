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

package com.github.vase4kin.teamcityapp.favorites.router

import android.app.Activity
import androidx.fragment.app.FragmentActivity
import javax.inject.Inject
import teamcityapp.features.build_history.api.BuildHistoryNavigation
import teamcityapp.features.drawer.api.navigation.DrawerNavigation
import teamcityapp.features.favorites.api.FavoritesAppRouter
import teamcityapp.features.navigation.api.NavigationNavigation
import teamcityapp.libraries.build_configurations.BuildConfigurationSummary
import teamcityapp.libraries.build_configurations.ProjectReference

class FavoritesAppRouterImpl @Inject constructor(
    private val navigation: NavigationNavigation,
    private val drawerNavigation: DrawerNavigation,
    private val historyNavigation: BuildHistoryNavigation
) : FavoritesAppRouter {
    override fun openDrawer(activity: Activity) {
        drawerNavigation.open((activity as FragmentActivity).supportFragmentManager)
    }
    override fun openProject(activity: Activity, project: ProjectReference) = navigation.open(activity, project)
    override fun openBuildConfiguration(activity: Activity, configuration: BuildConfigurationSummary) {
        historyNavigation.open(activity, configuration.id, configuration.name)
    }
}
