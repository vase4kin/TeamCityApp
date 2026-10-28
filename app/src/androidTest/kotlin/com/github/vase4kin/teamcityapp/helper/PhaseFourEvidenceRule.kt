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

package com.github.vase4kin.teamcityapp.helper

import android.graphics.Bitmap
import androidx.test.platform.app.InstrumentationRegistry
import java.io.File
import org.junit.rules.TestWatcher
import org.junit.runner.Description

/** Opt-in device evidence is disposable output, independent from intentional Roborazzi fixtures. */
class PhaseFourEvidenceRule : TestWatcher() {
    override fun finished(description: Description) {
        if (InstrumentationRegistry.getArguments().getString("phase4Evidence") != "true") return
        val instrumentation = InstrumentationRegistry.getInstrumentation()
        instrumentation.waitForIdleSync()
        val output = File(instrumentation.targetContext.getExternalFilesDir(null), "kmp-phase-4")
        check(output.isDirectory || output.mkdirs())
        val name = "${description.className.substringAfterLast('.')}_${description.methodName}".replace(Regex("[^A-Za-z0-9_-]"), "_")
        // A scenario may have just closed its surface while the test already passed.
        // Keep artifact availability separate from behavioral assertions.
        var captured: Bitmap? = null
        repeat(5) {
            if (captured == null) {
                captured = instrumentation.uiAutomation.takeScreenshot()
                if (captured == null) android.os.SystemClock.sleep(100)
            }
        }
        val bitmap = captured ?: run {
            File(output, "$name.capture-unavailable.txt").writeText("No display screenshot available after test/scenario completion.")
            return
        }
        File(output, "$name.png").outputStream().use { check(bitmap.compress(Bitmap.CompressFormat.PNG, 100, it)) }
        bitmap.recycle()
    }
}
