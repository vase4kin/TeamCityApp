/*
 * Copyright 2020 Andrey Tolpeev
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

package teamcityapp.libraries.builds

/**
 * Complete immutable launch snapshot, hydrated before a build is displayed in a list.
 *
 * Raw server values and nullable section presence are intentional. The legacy details host
 * creates its tabs before loading Overview, so a build ID or row summary cannot replace this.
 * Adapters copy collection values and preserve null versus present-but-empty sections.
 */
data class BuildLaunchData(
    val id: String,
    val href: String,
    val webUrl: String? = null,
    val number: String? = null,
    val state: String? = null,
    val status: String? = null,
    val statusText: String? = null,
    val branchName: String? = null,
    val buildTypeId: String? = null,
    val configuration: BuildConfigurationData? = null,
    val queuedDate: String? = null,
    val startDate: String? = null,
    val finishDate: String? = null,
    val waitReason: String? = null,
    val startEstimate: String? = null,
    val triggered: BuildTrigger? = null,
    val agent: BuildAgent? = null,
    val canceledInfo: BuildCancellation? = null,
    val changes: BuildCollectionLink? = null,
    val artifacts: BuildCollectionLink? = null,
    val tests: BuildTests? = null,
    val properties: BuildProperties? = null,
    val snapshotDependencies: BuildCollectionLink? = null,
    val personal: Boolean = false,
    val pinned: Boolean = false,
    val cleanSources: Boolean = false,
    val queueAtTop: Boolean = false
) {
    init {
        require(id.isNotBlank()) { "A build launch requires an ID" }
        require(href.isNotBlank()) { "A build launch requires a detail URL" }
    }

    val isRunning: Boolean get() = state == "running"
    val isQueued: Boolean get() = state == "queued"
    val isFinished: Boolean get() = state == "finished"
    val isSuccess: Boolean get() = status == "SUCCESS"
    val isFailed: Boolean get() = status == "FAILURE"
}

/** Partial server configurations are valid; a missing project name uses the configuration ID. */
data class BuildConfigurationData(
    val id: String?,
    val name: String? = null,
    val projectId: String? = null,
    val projectName: String? = null,
    val href: String? = null
)

data class BuildCollectionLink(val href: String?, val count: Int? = null, val nextHref: String? = null)
data class BuildTests(
    val href: String?,
    val passed: Int = 0,
    val failed: Int = 0,
    val ignored: Int = 0,
    val count: Int = 0,
    val nextHref: String? = null
)

/** A present wrapper with a null list differs from an absent wrapper and an empty list. */
data class BuildProperties(val items: List<BuildProperty>?, val id: String? = null, val href: String? = null)
data class BuildProperty(
    val name: String?,
    val value: String?,
    val own: Boolean = false,
    val id: String? = null,
    val href: String? = null
)
data class BuildAgent(val id: String?, val name: String?, val href: String? = null)
data class BuildUser(val username: String?, val name: String?, val id: String? = null, val href: String? = null)
data class BuildTrigger(
    val type: String?,
    val date: String? = null,
    val details: String? = null,
    val user: BuildUser? = null,
    val configuration: BuildConfigurationData? = null
)
data class BuildCancellation(val timestamp: String?, val user: BuildUser?)
