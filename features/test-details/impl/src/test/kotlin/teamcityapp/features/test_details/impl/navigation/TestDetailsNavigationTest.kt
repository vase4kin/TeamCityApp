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

package teamcityapp.features.test_details.impl.navigation

import android.app.Activity
import android.app.Application
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.RobolectricTestRunner
import org.robolectric.Shadows.shadowOf
import org.robolectric.annotation.Config
import teamcityapp.features.test_details.impl.TestDetailsActivity
import teamcityapp.features.test_details.impl.TestDetailsViewModel

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35], application = Application::class)
class TestDetailsNavigationTest {
    @Test fun opensDetailsWithOriginalUrlAndSlideTransition() {
        Robolectric.buildActivity(Activity::class.java).setup().use { controller ->
            val activity = controller.get()
            TestDetailsNavigationImpl().open(activity, "/guestAuth/test?id=123")
            val intent = shadowOf(activity).nextStartedActivity
            assertEquals(TestDetailsActivity::class.java.name, intent.component!!.className)
            assertEquals("/guestAuth/test?id=123", intent.getStringExtra(TestDetailsViewModel.ARG_TEST_URL))
            assertEquals(teamcityapp.libraries.utils.R.anim.slide_in_bottom, shadowOf(activity).pendingTransitionEnterAnimationResourceId)
            assertEquals(teamcityapp.libraries.utils.R.anim.hold, shadowOf(activity).pendingTransitionExitAnimationResourceId)
        }
    }
}
