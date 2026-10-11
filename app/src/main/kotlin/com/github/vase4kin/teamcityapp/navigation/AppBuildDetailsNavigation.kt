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

package com.github.vase4kin.teamcityapp.navigation

import android.app.Activity
import javax.inject.Inject
import teamcityapp.features.build_details.api.BuildDetailsDestination
import teamcityapp.features.build_details.api.BuildDetailsNavigation
import teamcityapp.libraries.builds.BuildLaunchData
import teamcityapp.libraries.navigation.DestinationLauncher

/** All incoming build actions launch a complete snapshot in the application-owned root. */
class AppBuildDetailsNavigation @Inject constructor(private val launcher: DestinationLauncher) : BuildDetailsNavigation {
    override fun open(activity: Activity, build: BuildLaunchData, configurationName: String?) = launcher.open(activity, BuildDetailsDestination(build, configurationName))
}
