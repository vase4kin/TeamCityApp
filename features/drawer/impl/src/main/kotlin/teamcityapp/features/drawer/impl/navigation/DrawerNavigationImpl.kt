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

package teamcityapp.features.drawer.impl.navigation
import androidx.fragment.app.FragmentManager
import javax.inject.Inject
import teamcityapp.features.drawer.api.navigation.DrawerNavigation
import teamcityapp.features.drawer.impl.DrawerBottomSheetDialogFragment
class DrawerNavigationImpl @Inject constructor() : DrawerNavigation {
    override fun open(fragmentManager: FragmentManager) {
        if (!fragmentManager.isStateSaved && fragmentManager.findFragmentByTag(TAG) == null) {
            DrawerBottomSheetDialogFragment().showNow(fragmentManager, TAG)
        }
    }
    companion object {
        const val TAG = "Tag drawer bottom sheet"
    }
}
