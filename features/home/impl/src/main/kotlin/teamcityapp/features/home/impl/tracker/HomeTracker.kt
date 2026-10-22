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

package teamcityapp.features.home.impl.tracker

import android.os.Bundle
import com.google.firebase.analytics.FirebaseAnalytics
import javax.inject.Inject
import teamcityapp.features.home.api.HomeTab

class FirebaseHomeTracker @Inject constructor(private val analytics: FirebaseAnalytics) : HomeTracker {
    override fun viewed() = analytics.logEvent("screen_home", null)
    override fun selected(tab: HomeTab) = analytics.logEvent(
        "tab_selected",
        Bundle().apply {
            putString(
                "tab",
                when (tab) {
                    HomeTab.Projects -> "PROJECTS"
                    HomeTab.Favorites -> "FAVORITES"
                    HomeTab.Running -> "RUNNING_BUILDS"
                    HomeTab.Queue -> "BUILD_QUEUE"
                    HomeTab.Agents -> "AGENTS"
                }
            )
        }
    )
    override fun fab(tab: HomeTab) {
        val event = when (tab) {
            HomeTab.Favorites -> "favorites_tab_click_on_fab"
            HomeTab.Running -> "running_builds_tab_click_on_filter_fab"
            HomeTab.Queue -> "build_queue_tab_click_on_filter_fab"
            HomeTab.Agents -> "agents_tab_click_on_filter_fab"
            HomeTab.Projects -> return
        }
        analytics.logEvent(event, null)
    }
    override fun addProjects() = analytics.logEvent("favorites_tab_click_on_action", null)
}
