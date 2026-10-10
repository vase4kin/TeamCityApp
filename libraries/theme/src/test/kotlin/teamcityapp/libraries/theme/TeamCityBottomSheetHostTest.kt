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
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.view.View
import android.widget.FrameLayout
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35], qualifiers = "en-rUS-w360dp-h800dp-mdpi")
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class TeamCityBottomSheetHostTest {
    @Test fun transparentHostSurvivesFirstLayoutStateChangesAndReopening() {
        listOf(false, true).forEach { dark ->
            org.robolectric.RuntimeEnvironment.setQualifiers("en-rUS-w360dp-h800dp-${if (dark) "night" else "notnight"}-mdpi")
            val controller = Robolectric.buildActivity(Activity::class.java)
            val activity = controller.get()
            activity.setTheme(R.style.Theme_MyApp)
            controller.setup()
            repeat(2) {
                if (it == 1) {
                    controller.recreate()
                    controller.get().setTheme(R.style.Theme_MyApp)
                }
                val dialog = BottomSheetDialog(controller.get(), R.style.ThemeOverlay_TeamCity_ComposeBottomSheetDialog)
                dialog.setContentView(View(dialog.context), FrameLayout.LayoutParams(360, 200))
                dialog.show()
                val decor = requireNotNull(dialog.window).decorView
                layout(decor)
                val sheet = requireNotNull(dialog.findViewById<View>(com.google.android.material.R.id.design_bottom_sheet))
                assertTransparent(sheet)
                dialog.behavior.state = BottomSheetBehavior.STATE_EXPANDED
                layout(decor)
                assertTransparent(sheet)
                dialog.behavior.state = BottomSheetBehavior.STATE_COLLAPSED
                layout(decor)
                assertTransparent(sheet)
                dialog.dismiss()
            }
            controller.destroy()
        }
    }

    @Test fun clearingLegacyBackgroundBeforeFirstLayoutDoesNotPreventBehaviorReplacement() {
        val controller = Robolectric.buildActivity(Activity::class.java)
        controller.get().setTheme(R.style.Theme_MyApp)
        val activity = controller.setup().get()
        val dialog = BottomSheetDialog(activity, R.style.ThemeOverlay_MyTheme_BottomSheetDialog)
        dialog.setContentView(View(dialog.context), FrameLayout.LayoutParams(360, 200))
        val sheet = requireNotNull(dialog.findViewById<View>(com.google.android.material.R.id.design_bottom_sheet))
        sheet.setBackgroundColor(Color.TRANSPARENT)
        dialog.show()
        layout(requireNotNull(dialog.window).decorView)
        val bitmap = drawBackground(sheet)
        assertTrue("The legacy behavior restores an opaque background", Color.alpha(bitmap.getPixel(bitmap.width / 2, bitmap.height / 2)) > 0)
        dialog.dismiss()
        controller.destroy()
    }

    private fun layout(view: View) {
        view.measure(View.MeasureSpec.makeMeasureSpec(360, View.MeasureSpec.EXACTLY), View.MeasureSpec.makeMeasureSpec(800, View.MeasureSpec.EXACTLY))
        view.layout(0, 0, 360, 800)
    }
    private fun drawBackground(sheet: View): Bitmap {
        val bitmap = Bitmap.createBitmap(360, 200, Bitmap.Config.ARGB_8888)
        sheet.background?.apply {
            setBounds(0, 0, bitmap.width, bitmap.height)
            draw(Canvas(bitmap))
        }
        return bitmap
    }
    private fun assertTransparent(sheet: View) {
        val bitmap = drawBackground(sheet)
        listOf(1 to 1, 358 to 1, 180 to 100).forEach { (x, y) -> assertEquals("Native host must not paint behind the Compose sheet", 0, Color.alpha(bitmap.getPixel(x, y))) }
        assertEquals(0f, sheet.elevation)
    }
}
