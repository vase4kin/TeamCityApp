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

package teamcityapp.features.test_details.impl.router

import android.app.Activity
import android.app.Application
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.shadows.ShadowToast

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35], application = Application::class)
class TestDetailsRouterTest {
    @Test fun closeFinishesTheUiOwner() {
        Robolectric.buildActivity(Activity::class.java).setup().use { controller ->
            TestDetailsRouterImpl(controller.get()).close()
            assertTrue(controller.get().isFinishing)
        }
    }

    @Test fun invalidInputShowsTheExistingMessageAndCloses() {
        Robolectric.buildActivity(Activity::class.java).setup().use { controller ->
            TestDetailsRouterImpl(controller.get()).closeInvalidInput()
            assertEquals("There's an error loading the page", ShadowToast.getTextOfLatestToast())
            assertTrue(controller.get().isFinishing)
        }
    }

}
