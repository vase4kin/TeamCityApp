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

package teamcityapp.features.navigation.impl.navigation

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import javax.inject.Inject
import teamcityapp.features.navigation.api.NavigationNavigation
import teamcityapp.features.navigation.impl.NavigationFragment
import teamcityapp.features.navigation.impl.R
import teamcityapp.libraries.build_configurations.ProjectReference

class NavigationNavigationImpl @Inject constructor() : NavigationNavigation {
    override fun createFragment() = NavigationFragment().apply {
        arguments = Bundle().apply {
            putString(NavigationNavigation.PROJECT_ID, NavigationNavigation.ROOT_PROJECT_ID)
            putString(NavigationNavigation.PROJECT_NAME, "")
        }
    }
    override fun open(activity: Activity, project: ProjectReference) {
        activity.startActivity(
            Intent().setClassName(activity, NavigationNavigation.LEGACY_ACTIVITY).apply {
                putExtra(NavigationNavigation.PROJECT_ID, project.id)
                putExtra(NavigationNavigation.PROJECT_NAME, project.name)
            }
        )
        activity.overridePendingTransition(R.anim.pull_in_right, R.anim.push_out_left)
    }
}
