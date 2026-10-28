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

package teamcityapp.features.build_details.api

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable
import teamcityapp.libraries.builds.BuildLaunchData

/** Each visit retains its complete incoming snapshot, independently of refreshed actions. */
@Serializable
data class BuildDetailsDestination(val build: BuildLaunchData, val configurationName: String? = null, val forceOverviewRefresh: Boolean = false, val completion: BuildDetailsCompletion? = null) : NavKey

@Serializable
enum class BuildDetailsCompletion { Stopped, RemovedFromQueue }

enum class BuildDetailsSection { Changes, Tests, Parameters, Artifacts, Snapshots }
