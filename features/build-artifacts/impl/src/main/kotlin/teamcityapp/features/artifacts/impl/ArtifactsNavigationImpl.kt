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

package teamcityapp.features.artifacts.impl

import android.app.Activity
import javax.inject.Inject
import teamcityapp.features.artifacts.api.*
import teamcityapp.features.artifacts.api.navigation.ArtifactsDestination
import teamcityapp.libraries.builds.BuildLaunchData
import teamcityapp.libraries.navigation.DestinationLauncher

class ArtifactsNavigationImpl @Inject constructor(private val launcher: DestinationLauncher) : ArtifactsNavigation {
    override fun open(activity: Activity, name: String, build: BuildLaunchData, url: String) {
        launcher.open(activity, ArtifactsDestination(build, url, name))
    }
}
