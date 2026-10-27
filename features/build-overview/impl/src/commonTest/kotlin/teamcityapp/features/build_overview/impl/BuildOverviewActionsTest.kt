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

package teamcityapp.features.build_overview.impl

import kotlin.test.*
import teamcityapp.features.build_overview.api.BuildOverviewAction
import teamcityapp.features.build_overview.impl.router.BuildOverviewActions
import teamcityapp.features.build_overview.impl.tracker.BuildOverviewTracker
import teamcityapp.libraries.builds.BuildLaunchData

class BuildOverviewActionsTest {
    @Test fun browserShareAndLogTrackAndUseTheCurrentBuildAndActiveServer() {
        val tracked = mutableListOf<Pair<BuildOverviewAction, String?>>()
        val browser = mutableListOf<String>()
        val shares = mutableListOf<String>()
        val failures = mutableListOf<BuildOverviewAction>()
        val tracker = object : BuildOverviewTracker {
            override fun action(action: BuildOverviewAction, branch: String?) {
                tracked += action to branch
            }
        }
        val actions = BuildOverviewActions(tracker, "https://ci.example/context", true, browser::add, shares::add, failures::add)
        val build = BuildLaunchData("42", "/context/app/rest/builds/id:42", webUrl = "/context/viewLog.html?buildId=42")
        actions.perform(BuildOverviewAction.Browser, build)
        actions.perform(BuildOverviewAction.Share, build)
        actions.perform(BuildOverviewAction.Log, build)
        actions.configuration("release/2")
        actions.project()
        assertEquals(listOf("https://ci.example/context/viewLog.html?buildId=42", "https://ci.example/context/viewLog.html?buildId=42&tab=buildLog&guest=1"), browser)
        assertEquals(listOf("https://ci.example/context/viewLog.html?buildId=42"), shares)
        assertEquals(listOf(BuildOverviewAction.Browser to null, BuildOverviewAction.Share to null, BuildOverviewAction.Log to null, BuildOverviewAction.Configuration to "release/2", BuildOverviewAction.Project to null), tracked)
        assertTrue(failures.isEmpty())
        actions.perform(BuildOverviewAction.Browser, build.copy(webUrl = "https://other.example/viewLog.html"))
        actions.perform(BuildOverviewAction.Share, build.copy(webUrl = "/context/viewLog.html?token=private"))
        assertEquals(listOf(BuildOverviewAction.Browser, BuildOverviewAction.Share), failures)
        assertEquals(2, browser.size)
        assertEquals(1, shares.size)
    }
}
