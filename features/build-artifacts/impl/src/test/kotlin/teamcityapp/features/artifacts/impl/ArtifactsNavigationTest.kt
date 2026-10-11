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
package teamcityapp.features.artifacts.impl

import android.app.Activity
import android.app.Application
import androidx.navigation3.runtime.NavKey
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import teamcityapp.features.artifacts.api.ArtifactsNavigation
import teamcityapp.features.artifacts.api.navigation.ArtifactsDestination
import teamcityapp.libraries.builds.*
import teamcityapp.libraries.navigation.DestinationLauncher

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35], application = Application::class)
class ArtifactsNavigationTest {
    private val build = BuildLaunchData("42", "/build/42", branchName = "raw/branch", tests = BuildTests("/tests", failed = 2), properties = BuildProperties(listOf(BuildProperty("env", "test"))))

    private val requests = mutableListOf<Triple<Activity, NavKey, Int>>()
    private val launcher = object : DestinationLauncher {
        override fun open(activity: Activity, destination: NavKey, flags: Int) {
            requests += Triple(activity, destination, flags)
        }
    }
    private val navigation = ArtifactsNavigationImpl(launcher)

    @Test fun folderNavigationLaunchesTheRootWithTheCompletePlainSnapshot() {
        val controller = Robolectric.buildActivity(Activity::class.java).setup()
        try {
            val activity = controller.get()
            navigation.open(activity, "Archive folder", build, "/children/archive!path")
            assertEquals(listOf(Triple(activity, ArtifactsDestination(build, "/children/archive!path", "Archive folder"), 0)), requests)
        } finally {
            controller.pause().stop().destroy()
        }
    }
}
