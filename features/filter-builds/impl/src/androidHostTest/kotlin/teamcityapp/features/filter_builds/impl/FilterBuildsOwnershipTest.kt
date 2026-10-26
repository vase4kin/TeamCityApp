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

package teamcityapp.features.filter_builds.impl
import com.google.firebase.analytics.FirebaseAnalytics
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito.*
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import teamcityapp.features.filter_builds.impl.tracker.FirebaseFilterBuildsTrackerImpl
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35], application = android.app.Application::class)
class FilterBuildsOwnershipTest {
    @Test fun analyticsPreserveLegacyEvents() {
        val analytics = mock(FirebaseAnalytics::class.java)
        val tracker = FirebaseFilterBuildsTrackerImpl(analytics)
        tracker.trackView()
        tracker.trackUserFilteredBuilds()
        val order = inOrder(analytics)
        order.verify(analytics).logEvent("screen_filter_builds", null)
        order.verify(analytics).logEvent("build_list_filters_applied", null)
        order.verifyNoMoreInteractions()
    }
}
