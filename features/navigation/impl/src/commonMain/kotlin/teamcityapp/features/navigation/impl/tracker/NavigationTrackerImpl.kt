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

package teamcityapp.features.navigation.impl.tracker

import teamcityapp.libraries.analytics.*

class NavigationTrackerImpl(private val analytics: AnalyticsTracker) : NavigationTracker {
    override fun viewShown() = analytics.track("screen_project")
    override fun ratingShown() = analytics.track("rate_show")
    override fun ratingCancelled() = analytics.track("rate_cancel")
    override fun ratingSelected() = analytics.track("rate_now")
}
