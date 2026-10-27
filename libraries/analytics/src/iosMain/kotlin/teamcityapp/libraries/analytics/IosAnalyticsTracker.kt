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

@file:OptIn(kotlinx.cinterop.ExperimentalForeignApi::class)

package teamcityapp.libraries.analytics

import platform.Foundation.NSLog

/** iOS currently has local analytics labels only; payloads never enter OS logs. */
class IosAnalyticsTracker internal constructor(private val logLabel: (String) -> Unit) : AnalyticsTracker {
    constructor() : this({ label -> NSLog("TeamCityApp: $label") })
    override fun track(event: AnalyticsEvent) = logLabel(event.name)
}
