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

import android.os.Bundle
import com.google.firebase.analytics.FirebaseAnalytics
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.kotlin.*
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34], manifest = Config.NONE)
class AndroidAnalyticsTrackerTest {
    private val firebase = mock<FirebaseAnalytics>()
    private val tracker = AndroidAnalyticsTracker(firebase)

    @Test fun encodesFlagsAsFirebaseLongsAndPreservesTextIncludingEmpty() {
        tracker.track(AnalyticsEvent("login_user_success", mapOf("sslEnabled" to AnalyticsValue.Flag(false))))
        tracker.track(AnalyticsEvent("login_user_failed", mapOf("errorMessage" to AnalyticsValue.Text(""))))
        tracker.track(AnalyticsEvent("tab_selected", mapOf("tab" to AnalyticsValue.Text("RUNNING_BUILDS"))))
        tracker.track(AnalyticsEvent("build_cancel_confirm", mapOf("param_re_add_to_the_queue" to AnalyticsValue.Flag(true))))
        val values = argumentCaptor<Bundle>()
        verify(firebase).logEvent(eq("login_user_success"), values.capture())
        assertTrue(values.lastValue.containsKey("sslEnabled"))
        assertEquals(0L, values.lastValue.get("sslEnabled"))
        verify(firebase).logEvent(eq("login_user_failed"), values.capture())
        assertEquals("", values.lastValue.get("errorMessage"))
        verify(firebase).logEvent(eq("tab_selected"), values.capture())
        assertEquals("RUNNING_BUILDS", values.lastValue.get("tab"))
        verify(firebase).logEvent(eq("build_cancel_confirm"), values.capture())
        assertEquals(1L, values.lastValue.get("param_re_add_to_the_queue"))
        assertTrue(values.allValues.all { it.size() == 1 })
        verifyNoMoreInteractions(firebase)
    }

    @Test fun eventsWithoutParametersKeepTheLegacyNullBundle() {
        tracker.track(AnalyticsEvent("screen_build_details"))
        tracker.track(AnalyticsEvent("screen_artifacts"))
        verify(firebase).logEvent("screen_build_details", null)
        verify(firebase).logEvent("screen_artifacts", null)
        verifyNoMoreInteractions(firebase)
    }
}
