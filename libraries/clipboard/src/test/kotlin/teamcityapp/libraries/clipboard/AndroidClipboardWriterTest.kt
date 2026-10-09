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

package teamcityapp.libraries.clipboard

import android.app.Application
import android.content.ClipboardManager
import android.content.Context
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35], application = Application::class)
class AndroidClipboardWriterTest {
    @Test fun plainTextWritePreservesTheLabelAndCompleteOriginalValue() {
        val context = RuntimeEnvironment.getApplication()
        val value = "  https://server/raw?a=1&a=2\n" + "original long value ".repeat(30) + "  "
        AndroidClipboardWriter(context).copy("env.value", value)
        val clip = (context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager).primaryClip!!
        assertEquals("env.value", clip.description.label)
        assertEquals(1, clip.itemCount)
        assertEquals(value, clip.getItemAt(0).text.toString())
    }
}
