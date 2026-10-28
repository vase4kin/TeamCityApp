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

package teamcityapp.features.build_details.api

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable
import teamcityapp.libraries.builds.BuildLaunchData
import teamcityapp.libraries.navigation.NavigationResult

/** A separate build-section visit with its complete, restorable launch arguments. */
@Serializable
data class BuildDetailsSectionDestination(val build: BuildLaunchData, val section: BuildDetailsSection, val configurationName: String? = null, val buildNumber: String = build.number ?: build.id, val originEntryId: String? = null) : NavKey {
    init {
        require(section in build.detailsSections()) { "This build section is unavailable" }
    }
}

/** A refreshed section count belongs to the exact build visit that opened it. */
@Serializable
data class BuildDetailsSectionCountResult(val buildId: String, val section: BuildDetailsSection, val count: Int) : NavigationResult

fun BuildLaunchData.detailsSections(): List<BuildDetailsSection> = buildList {
    if (changes?.href != null) add(BuildDetailsSection.Changes)
    if (tests?.href != null) add(BuildDetailsSection.Tests)
    add(BuildDetailsSection.Parameters)
    if (artifacts?.href != null && !isQueued && !isRunning) add(BuildDetailsSection.Artifacts)
    if (snapshotDependencies != null) add(BuildDetailsSection.Snapshots)
}
