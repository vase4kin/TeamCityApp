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

package teamcityapp.features.running_builds.impl

import android.app.Application
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import teamcityapp.features.running_builds.api.RunningBuildsAppRouter
import teamcityapp.features.running_builds.impl.navigation.RunningBuildsNavigationImpl
import teamcityapp.features.running_builds.impl.router.RunningBuildsRouterImpl
import teamcityapp.libraries.builds.BuildLaunchData

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35], application = Application::class)
class RunningBuildsNavigationTest {
    @Test fun entryPointCreatesFeatureFragmentWithoutInventingArguments() {
        val fragment = RunningBuildsNavigationImpl().createFragment()
        assertTrue(fragment is RunningBuildsFragment)
        assertNull(fragment.arguments)
    }

    @Test fun routerDelegatesDrawerFullBuildAndHistoryActions() {
        var drawer = false
        var opened: BuildLaunchData? = null
        var history: Pair<String, String>? = null
        val router = RunningBuildsRouterImpl(object : RunningBuildsAppRouter {
            override fun openDrawer() {
                drawer = true
            }
            override fun openBuild(build: BuildLaunchData) {
                opened = build
            }
            override fun openBuildHistory(configurationId: String, configurationName: String) {
                history = configurationId to configurationName
            }
        })
        val build = buildRow().copy(personal = true, pinned = true, queueAtTop = true)
        router.openDrawer()
        router.openBuild(build)
        router.openBuildHistory("Android_Debug", "Debug")
        assertTrue(drawer)
        assertSame(build, opened)
        assertEquals("Android_Debug" to "Debug", history)
    }
}
