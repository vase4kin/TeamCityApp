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

import kotlin.test.*
import teamcityapp.libraries.analytics.*

class BuildDetailsTrackerImplTest {
    private val records = mutableListOf<AnalyticsEvent>()
    private val analytics = AnalyticsTracker { records += it }
    private fun flag(name: String, key: String, value: Boolean) = AnalyticsEvent(name, mapOf(key to AnalyticsValue.Flag(value)))

    @Test fun buildDetailsRetainsQueueFlagsAndDistinctFailureCategories() {
        val tracker = BuildDetailsTrackerImpl(analytics)
        tracker.viewed()
        tracker.confirmedCancel(true)
        tracker.confirmedCancel(false)
        tracker.cancelSucceeded()
        tracker.cancelFailed(true)
        tracker.cancelFailed(false)
        tracker.restartSucceeded()
        tracker.restartFailed(true)
        tracker.restartFailed(false)
        tracker.queuedOpened()
        tracker.queuedFailed()
        assertEquals(
            listOf(
                AnalyticsEvent("screen_build_details"),
                flag("build_cancel_confirm", "param_re_add_to_the_queue", true),
                flag("build_cancel_confirm", "param_re_add_to_the_queue", false)
            ) + listOf("build_cancel_success", "build_cancel_forbidden_error", "build_cancel_server_error", "build_restart_success", "build_restart_forbidden_error", "build_restart_server_error", "build_restart_show_success", "build_restart_show_failure").map { AnalyticsEvent(it) },
            records
        )
    }
}
