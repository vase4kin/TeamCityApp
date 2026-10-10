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

import teamcityapp.libraries.builds.*

internal object BuildOverviewFixtures {
    val finished = BuildLaunchData(
        "b1", "/build/b1", "https://teamcity.example/viewLog.html?buildId=b1", "42", "finished", "SUCCESS", "Build finished successfully",
        "refs/heads/master", "bt", BuildConfigurationData("bt", "Build and test", "project", "Android project"),
        queuedDate = "20160621T225000+0300", startDate = "20160621T230000+0300", finishDate = "20160621T233000+0300",
        triggered = BuildTrigger("vcs", details = "Git"), agent = BuildAgent("a1", "Linux build agent"),
        artifacts = BuildCollectionLink("/artifacts", 4), tests = BuildTests("/tests", 8, 2, 1), properties = teamcityapp.libraries.builds.BuildProperties(emptyList())
    )
    fun build(scene: String): BuildLaunchData = when (scene) {
        "running" -> finished.copy(state = "running", statusText = "Building and testing…", finishDate = null)
        "queued" -> finished.copy(state = "queued", waitReason = "Waiting for an available agent", startEstimate = "20160621T235000+0300")
        "queued_fallback" -> finished.copy(state = "queued", waitReason = null, startEstimate = null)
        "failure" -> finished.copy(status = "FAILURE", statusText = "Tests failed")
        "status_error" -> finished.copy(status = "ERROR", statusText = "Agent disconnected")
        "status_unknown" -> finished.copy(status = "UNKNOWN", statusText = "Unknown result")
        "cancelled" -> finished.copy(status = "FAILURE", statusText = "Cancelled", canceledInfo = BuildCancellation("20160621T231000+0300", BuildUser("john", "John")))
        "cancelled_without_user" -> finished.copy(canceledInfo = BuildCancellation("20160621T231000+0300", null))
        "user_trigger" -> finished.copy(triggered = BuildTrigger("user", user = BuildUser("john", "John")))
        "restarted" -> finished.copy(triggered = BuildTrigger("restarted", user = BuildUser("john", null)))
        "deleted_user" -> finished.copy(triggered = BuildTrigger("user"))
        "configuration_trigger" -> finished.copy(triggered = BuildTrigger("buildType", configuration = BuildConfigurationData("other", "Release", "parent", "Parent project")))
        "deleted_configuration" -> finished.copy(triggered = BuildTrigger("buildType"))
        "unknown_trigger" -> finished.copy(triggered = BuildTrigger("custom"))
        "missing_trigger_details" -> finished.copy(triggered = BuildTrigger("vcs", details = null))
        "personal" -> finished.copy(personal = true, triggered = BuildTrigger("user", user = BuildUser("john", "John")))
        "minimal" -> finished.copy(branchName = null, agent = null, triggered = null, configuration = null)
        "missing_dates" -> finished.copy(startDate = null, finishDate = null, canceledInfo = BuildCancellation(null, null))
        "full_optional" -> finished.copy(personal = true, canceledInfo = BuildCancellation("20160621T231000+0300", BuildUser("john", "John")), triggered = BuildTrigger("restarted", user = BuildUser("john", "John")))
        "long_content" -> finished.copy(statusText = "Tests failed: a very long error description with detailed failure information that wraps across multiple lines on compact screens.", branchName = "refs/heads/a-long-feature-branch-that-describes-the-build-change-in-detail-and-wraps-on-small-screens", agent = BuildAgent("a1", "Linux build agent with a very long name that wraps across multiple lines"), configuration = finished.configuration?.copy(name = "Build and test the Android application with a very long configuration name"))
        else -> finished
    }
}
