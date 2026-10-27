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

package teamcityapp.features.build_details.impl

import kotlin.test.*
import teamcityapp.features.build_details.api.BuildDetailsCommand
import teamcityapp.features.build_overview.api.BuildOverviewAction
import teamcityapp.libraries.builds.BuildLaunchData

class BuildDetailsCommandsTest {
    @Test fun managementActionsTrackTheCurrentSnapshotAndRequestConfirmationOnce() {
        for ((command, state, action, request) in listOf(
            CommandCase(BuildDetailsCommand.Share, "finished", BuildOverviewAction.Share, null),
            CommandCase(BuildDetailsCommand.Browser, "running", BuildOverviewAction.Browser, null),
            CommandCase(BuildDetailsCommand.Log, "finished", BuildOverviewAction.Log, null),
            CommandCase(BuildDetailsCommand.Cancel, "queued", BuildOverviewAction.RemoveFromQueue, "cancel"),
            CommandCase(BuildDetailsCommand.Cancel, "running", BuildOverviewAction.Stop, "cancel"),
            CommandCase(BuildDetailsCommand.Restart, "finished", BuildOverviewAction.Restart, "restart")
        )) {
            val build = BuildLaunchData("current", "/build/current", state = state)
            val calls = mutableListOf<String>()
            performBuildDetailsCommand(command, build, { tracked, payload ->
                assertEquals(action, tracked)
                assertSame(build, payload)
                calls += "track"
            }, { calls += "cancel" }, { calls += "restart" })
            assertEquals(listOfNotNull("track", request), calls)
        }
    }
    private data class CommandCase(val command: BuildDetailsCommand, val state: String, val action: BuildOverviewAction, val request: String?)
}
