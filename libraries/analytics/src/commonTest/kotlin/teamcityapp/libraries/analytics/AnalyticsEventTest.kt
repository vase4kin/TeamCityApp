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

package teamcityapp.libraries.analytics

import kotlin.test.*

class AnalyticsEventTest {
    @Test fun eventNamesAreFixedLabelsWithinTheBackendLimit() {
        for (name in listOf("", "server.example/path", "event with data", "a".repeat(41))) {
            assertFailsWith<IllegalArgumentException> { AnalyticsEvent(name) }
        }
        assertEquals("screen_home", AnalyticsEvent("screen_home").name)
    }

    @Test fun convenienceFunctionRetainsParameterNamesAndTypedValues() {
        val events = mutableListOf<AnalyticsEvent>()
        val tracker = AnalyticsTracker { events += it }
        tracker.track("tab_selected", "tab" to AnalyticsValue.Text("PROJECTS"), "enabled" to AnalyticsValue.Flag(false))
        assertEquals(listOf(AnalyticsEvent("tab_selected", mapOf("tab" to AnalyticsValue.Text("PROJECTS"), "enabled" to AnalyticsValue.Flag(false)))), events)
    }
}
