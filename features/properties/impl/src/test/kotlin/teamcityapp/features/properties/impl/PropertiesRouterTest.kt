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

package teamcityapp.features.properties.impl

import android.app.Application
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import teamcityapp.features.properties.impl.router.PropertiesRouterImpl
import teamcityapp.libraries.clipboard.ClipboardWriter

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35], application = Application::class)
class PropertiesRouterTest {
    private class RecordingClipboard : ClipboardWriter {
        val copied = mutableListOf<Pair<String, String>>()
        override fun copy(label: String, value: String) {
            copied += label to value
        }
    }

    @Test fun android13AndNewerCopyOnceWithoutRequestingDuplicateFeedback() {
        val clipboard = RecordingClipboard()
        val value = "  https://server/path?raw=1\n" + "long original value ".repeat(20)
        assertFalse(PropertiesRouterImpl(clipboard).copyValue("same", value))
        assertEquals(listOf("same" to value), clipboard.copied)
    }

    @Test
    @Config(sdk = [32])
    fun olderAndroidRequestsBriefUiFeedbackAfterCopy() {
        val clipboard = RecordingClipboard()
        assertTrue(PropertiesRouterImpl(clipboard).copyValue("sdk", "24"))
        assertEquals(listOf("sdk" to "24"), clipboard.copied)
    }

    @Test fun emptyValueDoesNotWriteClipboardOrRequestFeedback() {
        val clipboard = RecordingClipboard()
        assertFalse(PropertiesRouterImpl(clipboard).copyValue("empty", ""))
        assertTrue(clipboard.copied.isEmpty())
    }
}
