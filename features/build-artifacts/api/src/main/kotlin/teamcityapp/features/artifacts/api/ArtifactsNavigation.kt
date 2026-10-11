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

package teamcityapp.features.artifacts.api

import android.app.Activity
import androidx.fragment.app.Fragment
import teamcityapp.libraries.builds.BuildLaunchData

interface ArtifactsNavigation {
    fun createFragment(build: BuildLaunchData, url: String): Fragment
    fun open(activity: Activity, name: String, build: BuildLaunchData, url: String)
    companion object {
        const val BUILD = "build"
        const val URL = "url"
        const val NAME = "name"
        const val LEGACY_ACTIVITY = "com.github.vase4kin.teamcityapp.artifact.view.ArtifactListActivity"
    }
}
