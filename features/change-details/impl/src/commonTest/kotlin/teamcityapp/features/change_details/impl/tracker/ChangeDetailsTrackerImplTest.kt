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

package teamcityapp.features.change_details.impl.tracker

import kotlin.test.*
import teamcityapp.libraries.analytics.*

class ChangeDetailsTrackerImplTest {
    private val records = mutableListOf<AnalyticsEvent>()
    private val analytics = AnalyticsTracker { records += it }
    private fun expect(vararg names: String) = assertEquals(names.map { AnalyticsEvent(it) }, records)

    @Test fun preservesScreenBrowserAndDiffEvents() {
        val tracker = ChangeDetailsTrackerImpl(analytics)
        tracker.trackView()
        tracker.trackMoreDetails()
        tracker.trackFileDiff()
        expect("screen_open_change", "change_click_more_details", "change_click_view_file_diff")
    }
}
