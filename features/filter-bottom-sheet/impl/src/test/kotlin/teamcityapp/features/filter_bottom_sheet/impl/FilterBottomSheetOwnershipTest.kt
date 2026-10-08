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

package teamcityapp.features.filter_bottom_sheet.impl
import android.os.Bundle
import androidx.fragment.app.FragmentFactory
import com.google.firebase.analytics.FirebaseAnalytics
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.ArgumentCaptor
import org.mockito.Mockito.*
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import teamcityapp.features.filter_bottom_sheet.api.QuickFilter
import teamcityapp.features.filter_bottom_sheet.impl.navigation.FilterBottomSheetNavigationImpl
import teamcityapp.features.filter_bottom_sheet.impl.router.FilterBottomSheetRouterImpl
import teamcityapp.features.filter_bottom_sheet.impl.tracker.QuickFilterTrackerImpl
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35], application = android.app.Application::class)
class FilterBottomSheetOwnershipTest {
    @Test fun navigationRetainsEveryFilterCodeOnRestoration() {
        QuickFilter.entries.forEach { filter ->
            val original = FilterBottomSheetNavigationImpl().createBottomSheetDialog(filter.ordinal)
            val restored = FragmentFactory().instantiate(original.javaClass.classLoader!!, original.javaClass.name)
            restored.arguments = original.arguments
            assertTrue(restored is FilterBottomSheetDialogFragment)
            assertEquals(filter.ordinal, restored.requireArguments().getInt("arg_code"))
        }
    }

    @Test fun routerDismissesTheOwningSheet() {
        val fragment = mock(FilterBottomSheetDialogFragment::class.java)
        FilterBottomSheetRouterImpl(fragment).close()
        verify(fragment).dismiss()
        verifyNoMoreInteractions(fragment)
    }

    @Test fun analyticsPreserveAllLegacyFilterNamesAndEvents() {
        val analytics = mock(FirebaseAnalytics::class.java)
        val tracker = QuickFilterTrackerImpl(analytics)
        QuickFilter.entries.forEach(tracker::selected)
        val events = ArgumentCaptor.forClass(String::class.java)
        val payloads = ArgumentCaptor.forClass(Bundle::class.java)
        verify(analytics, times(6)).logEvent(events.capture(), payloads.capture())
        assertEquals(listOf("filter_running_builds_selected", "filter_running_builds_selected", "filter_queued_builds_selected", "filter_queued_builds_selected", "filter_agents_selected", "filter_agents_selected"), events.allValues)
        assertEquals(listOf("RUNNING_ALL", "RUNNING_FAVORITES", "QUEUE_ALL", "QUEUE_FAVORITES", "AGENTS_CONNECTED", "AGENTS_DISCONNECTED"), payloads.allValues.map { it.getString("filter") })
        payloads.allValues.forEach { assertEquals(1, it.size()) }
        verifyNoMoreInteractions(analytics)
    }
}
