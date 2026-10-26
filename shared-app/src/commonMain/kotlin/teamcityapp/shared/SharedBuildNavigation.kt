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

package teamcityapp.shared

import androidx.navigation3.runtime.NavKey
import teamcityapp.features.artifacts.api.navigation.ArtifactsDestination
import teamcityapp.features.build_details.api.*
import teamcityapp.libraries.builds.BuildLaunchData
import teamcityapp.libraries.navigation.Navigator

/** History, running, queued and snapshot-dependency rows already contain the complete launch snapshot.
 * A reference-loading visit would add a second transition and discard that snapshot.
 * Queued-result Show actions retain their separate reference-resolution route.
 */
internal fun Navigator.openBuildSnapshot(originEntryId: String, build: BuildLaunchData, configurationName: String? = build.configuration?.name) {
    if (current.id != originEntryId) return
    push(BuildDetailsDestination(build, configurationName))
}

/** The visible Overview and every section launch must use the same complete response snapshot. */
internal fun buildSectionDestination(incomingId: String, loaded: BuildLaunchData?, section: BuildDetailsSection, configurationName: String?, originEntryId: String): NavKey? {
    val build = loaded?.takeIf { it.id == incomingId && section in it.detailsSections() } ?: return null
    return if (section == BuildDetailsSection.Artifacts) {
        ArtifactsDestination(build, checkNotNull(build.artifacts?.href), "")
    } else {
        BuildDetailsSectionDestination(build, section, configurationName, originEntryId = originEntryId)
    }
}

/** Ignore a repeated/delayed tap after the exact originating Overview has left the top. */
internal fun Navigator.openBuildSection(originEntryId: String, incomingId: String, loaded: BuildLaunchData?, section: BuildDetailsSection, configurationName: String?) {
    if (current.id != originEntryId) return
    buildSectionDestination(incomingId, loaded, section, configurationName, originEntryId)?.let(::push)
}
