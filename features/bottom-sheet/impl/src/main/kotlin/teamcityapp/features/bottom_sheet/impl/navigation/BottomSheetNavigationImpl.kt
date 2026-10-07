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

package teamcityapp.features.bottom_sheet.impl.navigation
import androidx.fragment.app.DialogFragment
import teamcityapp.features.bottom_sheet.api.BottomSheetNavigation
import teamcityapp.features.bottom_sheet.api.SheetMenuType
import teamcityapp.features.bottom_sheet.impl.BottomSheetDialogFragment
internal class BottomSheetNavigationImpl : BottomSheetNavigation {
    override fun createBottomSheetDialog(title: String, descriptions: Array<String>, menuType: SheetMenuType): DialogFragment = BottomSheetDialogFragment.createBottomSheetDialog(title, descriptions, menuType.ordinal)
}
