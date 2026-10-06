/*
 * Copyright 2020 Andrey Tolpeev
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

package teamcityapp.features.drawer.impl
import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject
import teamcityapp.features.drawer.impl.router.DrawerRouter
import teamcityapp.libraries.theme.TeamCityTheme

/** Legacy Home entry-point adapter. The sheet content and state are entirely Compose. */
@AndroidEntryPoint
class DrawerBottomSheetDialogFragment : BottomSheetDialogFragment() {
    @Inject lateinit var router: DrawerRouter
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?) = ComposeView(requireContext()).apply {
        setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
        setContent { TeamCityTheme { DrawerRoute(router) } }
    }
    fun setInteractionsEnabled(enabled: Boolean) {
        isCancelable = enabled
        (dialog as? BottomSheetDialog)?.let {
            it.setCanceledOnTouchOutside(enabled)
            it.behavior.isDraggable = enabled
        }
    }
}
