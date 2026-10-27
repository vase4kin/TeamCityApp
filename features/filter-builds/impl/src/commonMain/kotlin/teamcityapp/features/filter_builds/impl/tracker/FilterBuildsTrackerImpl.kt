/*
 * Copyright 2019 Andrey Tolpeev
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

package teamcityapp.features.filter_builds.impl.tracker

import teamcityapp.features.filter_builds.api.FilterBuildsTracker
import teamcityapp.libraries.analytics.*

class FilterBuildsTrackerImpl(private val analytics: AnalyticsTracker) : FilterBuildsTracker {
    override fun trackView() = analytics.track(FilterBuildsTracker.SCREEN_NAME_FILTER_BUILDS)
    override fun trackUserFilteredBuilds() = analytics.track(FilterBuildsTracker.EVENT_RUN_BUILD_BUTTON_PRESSED)
}
