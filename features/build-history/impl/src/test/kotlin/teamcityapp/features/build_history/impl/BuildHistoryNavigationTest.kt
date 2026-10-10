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

package teamcityapp.features.build_history.impl

import android.app.Activity
import android.app.Application
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.RobolectricTestRunner
import org.robolectric.Shadows.shadowOf
import org.robolectric.annotation.Config
import teamcityapp.features.build_history.api.BuildHistoryNavigation
import teamcityapp.features.build_history.impl.navigation.BuildHistoryNavigationImpl
import teamcityapp.features.filter_builds.api.navigation.FilterBuildsNavigation
import teamcityapp.features.run_build.api.navigation.RunBuildNavigation

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35], application = Application::class)
class BuildHistoryNavigationTest {
    @Test fun navigationPreservesInstalledComponentIdNameAndNormalizedFilter() {
        val controller = Robolectric.buildActivity(Activity::class.java).setup()
        try {
            val activity = controller.get()
            BuildHistoryNavigationImpl().open(activity, "configuration", "Build Android", "personal:false,pinned:false")
            val intent = shadowOf(activity).nextStartedActivity
            assertEquals(BuildHistoryNavigation.LEGACY_ACTIVITY, intent.component?.className)
            assertEquals(activity.packageName, intent.component?.packageName)
            assertEquals("configuration", intent.getStringExtra("id"))
            assertEquals("Build Android", intent.getStringExtra("name"))
            assertEquals("personal:false,pinned:false", intent.getStringExtra(BuildHistoryNavigation.EXTRA_LOCATOR))
            assertFalse(intent.hasExtra("filter"))
            assertEquals(0, intent.flags)
        } finally {
            controller.pause().stop().destroy()
        }
    }

    @Test fun existingResultContractsRemainUnchanged() {
        assertEquals(489, RunBuildNavigation.REQUEST_CODE)
        assertEquals("href", RunBuildNavigation.EXTRA_HREF)
        assertEquals(12921, FilterBuildsNavigation.REQUEST_CODE)
        assertEquals("filter", FilterBuildsNavigation.EXTRA_FILTER)
    }
}
