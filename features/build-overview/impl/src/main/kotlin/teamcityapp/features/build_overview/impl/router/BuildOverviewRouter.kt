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

import kotlinx.coroutines.flow.Flow
import teamcityapp.features.build_overview.api.*
import teamcityapp.features.build_overview.impl.OverviewRow
import teamcityapp.libraries.builds.BuildLaunchData

interface BuildOverviewRouter {
    val requests: Flow<BuildOverviewRequest>
    fun loaded(build: BuildLaunchData)
    fun resumed(build: BuildLaunchData)
    fun row(row: OverviewRow)
    fun dispatch(action: BuildOverviewAction, build: BuildLaunchData, branch: String? = null)
    fun dispose()
}
