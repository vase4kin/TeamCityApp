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

import androidx.compose.runtime.staticCompositionLocalOf
import teamcityapp.libraries.builds.BuildLaunchData

/** UI-scoped parent port. Child ViewModels never retain a host or its navigation callbacks. */
interface BuildDetailsUiHost {
    val entryId: String
    val refreshVersion: Long
    val current: BuildLaunchData
    fun loaded(build: BuildLaunchData)
    val countStates: Map<BuildDetailsSection, BuildDetailsCount>
    fun retryCount(tab: BuildDetailsSection)
    fun dispatch(action: BuildDetailsCommand, build: BuildLaunchData, branch: String? = null)
    fun openSection(section: BuildDetailsSection)
    fun copied()
}
enum class BuildDetailsCommand { Share, Browser, Log, Cancel, Restart, Configuration, Project }
val LocalBuildDetailsUiHost = staticCompositionLocalOf<BuildDetailsUiHost?> { null }

/** Section content reports fresh metadata to its visible UI owner. */
fun interface BuildDetailsSectionCountHost {
    fun updated(count: Int)
}
val LocalBuildDetailsSectionCountHost = staticCompositionLocalOf<BuildDetailsSectionCountHost?> { null }
