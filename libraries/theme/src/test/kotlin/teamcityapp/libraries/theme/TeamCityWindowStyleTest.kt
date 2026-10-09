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

package teamcityapp.libraries.theme

import android.app.Activity
import android.app.Dialog
import androidx.core.view.WindowCompat
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [28])
class TeamCityWindowStyleTest {
    @Test fun lightAndDarkSetBothSystemBarIconAppearances() {
        val activity = Robolectric.buildActivity(Activity::class.java).setup().get()
        val window = activity.window
        val controller = WindowCompat.getInsetsController(window, window.decorView)
        applyTeamCityWindowStyle(window, false)
        assertTrue(controller.isAppearanceLightStatusBars)
        assertTrue(controller.isAppearanceLightNavigationBars)
        applyTeamCityWindowStyle(window, true)
        assertFalse(controller.isAppearanceLightStatusBars)
        assertFalse(controller.isAppearanceLightNavigationBars)
    }

    @Test fun stylingDialogDoesNotChangeUnderlyingActivityWindow() {
        val activity = Robolectric.buildActivity(Activity::class.java).setup().get()
        applyTeamCityWindowStyle(activity.window, false)
        val dialog = Dialog(activity).apply { show() }
        applyTeamCityWindowStyle(requireNotNull(dialog.window), true)
        val activityController = WindowCompat.getInsetsController(activity.window, activity.window.decorView)
        assertTrue(activityController.isAppearanceLightStatusBars)
        assertTrue(activityController.isAppearanceLightNavigationBars)
        val dialogWindow = requireNotNull(dialog.window)
        val dialogController = WindowCompat.getInsetsController(dialogWindow, dialogWindow.decorView)
        assertFalse(dialogController.isAppearanceLightStatusBars)
        assertFalse(dialogController.isAppearanceLightNavigationBars)
        dialog.dismiss()
    }
}
