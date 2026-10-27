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

package teamcityapp.features.build_details.impl.tracker

import teamcityapp.features.build_overview.api.BuildReferenceTracker
import teamcityapp.libraries.analytics.*

class BuildDetailsReferenceTrackerImpl(analytics: AnalyticsTracker, private val fromRestart: Boolean) : BuildReferenceTracker {
    private val details = BuildDetailsTrackerImpl(analytics)
    override fun loaded() {
        if (fromRestart) details.queuedOpened()
    }
    override fun failed() {
        if (fromRestart) details.queuedFailed()
    }
}
