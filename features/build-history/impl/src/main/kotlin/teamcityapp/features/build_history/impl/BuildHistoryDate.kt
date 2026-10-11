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

import java.text.ParsePosition
import java.text.SimpleDateFormat
import java.util.Locale
import teamcityapp.libraries.builds.BuildLaunchData

/** Legacy dates intentionally ignore the TeamCity offset suffix and use the device timezone. */
internal fun historyDate(build: BuildLaunchData): String? {
    val raw = build.startDate ?: return null
    val date = SimpleDateFormat("yyyyMMdd'T'HHmmss", Locale.US).apply { isLenient = false }.parse(raw, ParsePosition(0)) ?: return null
    return SimpleDateFormat("dd MMMM", Locale.US).format(date)
}
internal fun historySectionKey(build: BuildLaunchData): String = if (build.isQueued) "queued" else historyDate(build) ?: "unknown"
