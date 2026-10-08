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

package teamcityapp.features.bottom_sheet.impl.router
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import teamcityapp.features.bottom_sheet.api.*
import teamcityapp.features.bottom_sheet.impl.BottomSheetDialogFragment
class BottomSheetRouterImpl(private val fragment: BottomSheetDialogFragment, private val actions: BottomSheetAppActions) : BottomSheetRouter {
    override fun perform(item: SheetItem) {
        if (item.action == SheetAction.Copy) {
            val clipboard = fragment.requireContext().getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            clipboard.setPrimaryClip(ClipData.newPlainText("", item.description))
        }
        actions.dispatch(item)
        fragment.dismiss()
    }
}
