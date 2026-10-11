/*
 * Copyright 2020 Andrey Tolpeev
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

import com.google.firebase.analytics.FirebaseAnalytics
import javax.inject.Inject

class NavigationTrackerImpl @Inject constructor(private val analytics: FirebaseAnalytics) : NavigationTracker {
    override fun viewShown() = analytics.logEvent("screen_project", null)
    override fun ratingShown() = analytics.logEvent("rate_show", null)
    override fun ratingCancelled() = analytics.logEvent("rate_cancel", null)
    override fun ratingSelected() = analytics.logEvent("rate_now", null)
}
