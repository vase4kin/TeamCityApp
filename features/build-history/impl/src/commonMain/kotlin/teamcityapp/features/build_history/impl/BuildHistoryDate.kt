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

package teamcityapp.features.build_history.impl

import teamcityapp.libraries.builds.BuildLaunchData
import teamcityapp.libraries.time.*
/** Legacy dates intentionally ignore the TeamCity offset suffix and use the device timezone. */
fun historyDate(build: BuildLaunchData, currentYear: Int = currentLocalYear()): String? {
    val date = build.startDate?.let(::parseTeamCityLocalDate) ?: return null
    val pattern = if (formatTeamCityLocalDate(date, "yyyy").toInt() == currentYear) "dd MMMM" else "dd MMMM yyyy"
    return formatTeamCityLocalDate(date, pattern)
}
fun historySectionKey(build: BuildLaunchData, currentYear: Int = currentLocalYear()): String = if (build.isQueued) "queued" else historyDate(build, currentYear) ?: "unknown"
