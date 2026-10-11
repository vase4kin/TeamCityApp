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

package com.github.vase4kin.teamcityapp.overview.data

import android.os.Bundle
import com.github.vase4kin.teamcityapp.base.extractor.BundleExtractorValues
import com.github.vase4kin.teamcityapp.builds.data.AppBuildLaunchMapper
import teamcityapp.libraries.builds.*

internal val incomingOverview = BuildLaunchData(
    "42", "/queue/42", number = "old-number", state = "queued", webUrl = "https://ci.example/old",
    branchName = "old-branch", buildTypeId = "old-configuration",
    configuration = BuildConfigurationData("old-configuration", "Old configuration", "old-project", "Old project"),
    triggered = BuildTrigger("user", user = BuildUser("other", "Other user")),
    changes = BuildCollectionLink("/old-changes", 0), tests = BuildTests("/old-tests", 1),
    properties = BuildProperties(emptyList()), artifacts = BuildCollectionLink("/old-artifacts")
)
internal val loadedOverview = incomingOverview.copy(
    href = "/builds/42", number = "latest-number", state = "running", webUrl = "https://ci.example/latest",
    branchName = "latest-branch", buildTypeId = "latest-configuration",
    configuration = BuildConfigurationData("latest-configuration", "Latest configuration", "latest-project", "Latest project"),
    triggered = BuildTrigger("user", user = BuildUser("alice", "Alice")),
    changes = BuildCollectionLink("/latest-changes", 5), tests = BuildTests("/latest-tests", 8, 2, 1),
    properties = BuildProperties(listOf(BuildProperty("env", "latest", true, "property-id", "/property")), "properties-id", "/properties"),
    artifacts = BuildCollectionLink("/latest-artifacts")
)
internal fun overviewArguments(build: BuildLaunchData = incomingOverview, name: String = "Incoming name") = Bundle().apply {
    putSerializable(BundleExtractorValues.BUILD, AppBuildLaunchMapper().toLegacyBuild(build))
    putString(BundleExtractorValues.NAME, name)
}
