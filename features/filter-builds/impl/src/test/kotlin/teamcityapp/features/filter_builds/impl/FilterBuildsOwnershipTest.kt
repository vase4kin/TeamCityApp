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
import android.app.Activity
import android.content.Intent
import com.google.firebase.analytics.FirebaseAnalytics
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.ArgumentCaptor
import org.mockito.Mockito.*
import org.robolectric.Robolectric
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import teamcityapp.features.filter_builds.api.*
import teamcityapp.features.filter_builds.api.navigation.FilterBuildsNavigation
import teamcityapp.features.filter_builds.impl.navigation.FilterBuildsNavigationImpl
import teamcityapp.features.filter_builds.impl.router.FilterBuildsRouterImpl
import teamcityapp.features.filter_builds.impl.tracker.FirebaseFilterBuildsTrackerImpl
import teamcityapp.libraries.resources.R as SharedR
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35], application = android.app.Application::class)
class FilterBuildsOwnershipTest {
    @Test fun launchPreservesRequestCodeExtraAndTransition() {
        val owner = spy(Robolectric.buildActivity(Activity::class.java).setup().get())
        doNothing().`when`(owner).startActivityForResult(any(Intent::class.java), anyInt())
        FilterBuildsNavigationImpl().openForResult(owner, "build-type")
        val intent = ArgumentCaptor.forClass(Intent::class.java)
        verify(owner).startActivityForResult(intent.capture(), eq(12921))
        assertEquals(FilterBuildsActivity::class.java.name, intent.value.component!!.className)
        assertEquals("build-type", intent.value.getStringExtra("BuildTypeId"))
        verify(owner).overridePendingTransition(SharedR.anim.slide_in_bottom, SharedR.anim.hold)
    }

    @Test fun successReturnsCompatiblePayloadBeforeFinishing() {
        val owner = mock(FilterBuildsActivity::class.java)
        val selected = BuildFilter(branch = "release", personal = true, pinned = true)
        val results = object : FilterBuildsResultAdapter {
            override fun serialize(filter: BuildFilter): java.io.Serializable {
                assertEquals(selected, filter)
                return "legacy payload"
            }
        }
        val router = FilterBuildsRouterImpl(owner, results)
        router.apply(selected)
        val intent = ArgumentCaptor.forClass(Intent::class.java)
        val order = inOrder(owner)
        order.verify(owner).setResult(eq(Activity.RESULT_OK), intent.capture())
        assertEquals("legacy payload", intent.value.getSerializableExtra("filter"))
        order.verify(owner).finish()
        order.verifyNoMoreInteractions()
    }

    @Test fun cancellationReturnsAnEmptyResult() {
        val owner = mock(FilterBuildsActivity::class.java)
        FilterBuildsRouterImpl(
            owner,
            object : FilterBuildsResultAdapter {
                override fun serialize(filter: BuildFilter): java.io.Serializable = "unused"
            }
        ).close()
        val intent = ArgumentCaptor.forClass(Intent::class.java)
        val order = inOrder(owner)
        order.verify(owner).setResult(eq(Activity.RESULT_CANCELED), intent.capture())
        assertNull(intent.value.extras)
        order.verify(owner).finish()
        order.verifyNoMoreInteractions()
    }

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
