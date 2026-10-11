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

package teamcityapp.features.navigation.impl

import android.app.Activity
import android.app.Application
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.RobolectricTestRunner
import org.robolectric.Shadows.shadowOf
import org.robolectric.annotation.Config
import teamcityapp.features.navigation.api.NavigationNavigation
import teamcityapp.features.navigation.impl.navigation.NavigationNavigationImpl
import teamcityapp.libraries.build_configurations.ProjectReference

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35], application = Application::class)
class NavigationNavigationTest {
    private val navigation = NavigationNavigationImpl()

    @Test fun rootFragmentUsesCompatibleRootArguments() {
        val fragment = navigation.createFragment()
        assertTrue(fragment is NavigationFragment)
        assertEquals("_Root", fragment.requireArguments().getString("id"))
        assertEquals("", fragment.requireArguments().getString("name"))
    }

    @Test fun nestedNavigationTargetsInstalledComponentAndPreservesIdAndName() {
        val controller = Robolectric.buildActivity(Activity::class.java).setup()
        try {
            val activity = controller.get()
            navigation.open(activity, ProjectReference("nested-id", "Nested project"))
            val intent = shadowOf(activity).nextStartedActivity
            assertEquals(NavigationNavigation.LEGACY_ACTIVITY, intent.component?.className)
            assertEquals(activity.packageName, intent.component?.packageName)
            assertEquals("nested-id", intent.getStringExtra("id"))
            assertEquals("Nested project", intent.getStringExtra("name"))
            assertEquals(0, intent.flags)
        } finally {
            controller.pause().stop().destroy()
        }
    }
}
