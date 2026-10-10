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
import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import javax.inject.Inject
import teamcityapp.features.artifacts.api.*
import teamcityapp.libraries.builds.BuildLaunchData
import teamcityapp.libraries.builds.compatibility.BuildLaunchCodec

class ArtifactsNavigationImpl @Inject constructor(private val codec: BuildLaunchCodec) : ArtifactsNavigation {
    override fun createFragment(build: BuildLaunchData, url: String): Fragment = ArtifactsFragment().apply {
        arguments = launchArguments(build, url, "")
    }
    override fun open(activity: Activity, name: String, build: BuildLaunchData, url: String) {
        activity.startActivity(Intent().setClassName(activity.packageName, ArtifactsNavigation.LEGACY_ACTIVITY).putExtras(launchArguments(build, url, name)))
        activity.overridePendingTransition(R.anim.pull_in_right, R.anim.push_out_left)
    }
    private fun launchArguments(build: BuildLaunchData, url: String, name: String) = Bundle().apply {
        putSerializable(ArtifactsNavigation.BUILD, codec.encode(build))
        putString(ArtifactsNavigation.URL, url)
        putString(ArtifactsNavigation.NAME, name)
    }
}
