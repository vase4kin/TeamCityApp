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

package teamcityapp.features.bottom_sheet.impl
import android.content.ClipboardManager
import android.content.Context
import androidx.fragment.app.FragmentFactory
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito.*
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config
import teamcityapp.features.bottom_sheet.api.*
import teamcityapp.features.bottom_sheet.impl.navigation.BottomSheetNavigationImpl
import teamcityapp.features.bottom_sheet.impl.router.BottomSheetRouterImpl
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35], application = android.app.Application::class)
class BottomSheetOwnershipTest {
    @Test fun navigationPreservesTitleDescriptionsAndMenuTypeOnRestoration() {
        val original = BottomSheetNavigationImpl().createBottomSheetDialog("file", arrayOf("download", "browser"), SheetMenuType.ArtifactBrowser)
        val restored = FragmentFactory().instantiate(original.javaClass.classLoader!!, original.javaClass.name)
        restored.arguments = original.arguments
        assertTrue(restored is BottomSheetDialogFragment)
        assertEquals("file", restored.requireArguments().getString("arg_title"))
        assertArrayEquals(arrayOf("download", "browser"), restored.requireArguments().getStringArray("arg_description"))
        assertEquals(3, restored.requireArguments().getInt("arg_bottom_sheet_type"))
    }

    @Test fun everyActionIsDispatchedBeforeDismissal() {
        SheetAction.entries.forEach { action ->
            val fragment = mock(BottomSheetDialogFragment::class.java)
            val actions = mock(BottomSheetAppActions::class.java)
            `when`(fragment.requireContext()).thenReturn(RuntimeEnvironment.getApplication())
            val item = SheetItem(action, "https://server/path/file.txt")
            BottomSheetRouterImpl(fragment, actions).perform(item)
            val order = inOrder(actions, fragment)
            order.verify(actions).dispatch(item)
            order.verify(fragment).dismiss()
            if (action == SheetAction.Copy) {
                val clipboard = RuntimeEnvironment.getApplication().getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                assertEquals(item.description, clipboard.primaryClip!!.getItemAt(0).text.toString())
            }
        }
    }

    @Test fun failedLegacyActionKeepsTheSheetOpen() {
        val fragment = mock(BottomSheetDialogFragment::class.java)
        val actions = mock(BottomSheetAppActions::class.java)
        val item = SheetItem(SheetAction.Branch, "release")
        doThrow(IllegalStateException("destination unavailable")).`when`(actions).dispatch(item)
        assertThrows(IllegalStateException::class.java) { BottomSheetRouterImpl(fragment, actions).perform(item) }
        verify(fragment, never()).dismiss()
    }
}
