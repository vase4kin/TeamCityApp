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

package teamcityapp.features.build_overview.impl.router

import teamcityapp.features.build_overview.api.BuildOverviewAction
import teamcityapp.features.build_overview.impl.buildLogUrl
import teamcityapp.features.build_overview.impl.tracker.BuildOverviewTracker
import teamcityapp.libraries.builds.BuildLaunchData
import teamcityapp.libraries.network.activeServerWebUrl

/** Feature-owned analytics and browser/share policy; hosts supply UI-scoped OS ports. */
class BuildOverviewActions(
    private val tracker: BuildOverviewTracker,
    private val server: String,
    private val guest: Boolean,
    private val browser: (String) -> Unit,
    private val share: (String) -> Unit,
    private val rejected: (BuildOverviewAction) -> Unit
) {
    fun perform(action: BuildOverviewAction, build: BuildLaunchData) {
        tracker.action(action)
        when (action) {
            BuildOverviewAction.Share -> runCatching { activeServerWebUrl(server, build.webUrl.orEmpty()) }.onSuccess(share).onFailure { rejected(action) }
            BuildOverviewAction.Browser -> runCatching { activeServerWebUrl(server, build.webUrl.orEmpty()) }.onSuccess(browser).onFailure { rejected(action) }
            BuildOverviewAction.Log -> browser(buildLogUrl(server, build, guest))
            else -> Unit
        }
    }
    fun configuration(branch: String? = null) = tracker.action(BuildOverviewAction.Configuration, branch)
    fun project() = tracker.action(BuildOverviewAction.Project)
}
