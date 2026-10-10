/*
 * Copyright 2020 Andrey Tolpeev
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

package teamcityapp.features.filter_bottom_sheet.impl
import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject
import teamcityapp.features.filter_bottom_sheet.impl.FilterBottomSheetRoute
import teamcityapp.features.filter_bottom_sheet.impl.router.FilterBottomSheetRouter
import teamcityapp.libraries.theme.TeamCitySystemBars
import teamcityapp.libraries.theme.TeamCityTheme
@AndroidEntryPoint
class FilterBottomSheetDialogFragment : BottomSheetDialogFragment() {
    override fun getTheme() = teamcityapp.libraries.theme.R.style.ThemeOverlay_TeamCity_ComposeBottomSheetDialog

    @Inject lateinit var router: FilterBottomSheetRouter
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?) = ComposeView(requireContext()).apply {
        setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
        setContent {
            TeamCityTheme {
                TeamCitySystemBars(dialog?.window)
                FilterBottomSheetRoute(router)
            }
        }
    }
    override fun onStart() {
        super.onStart()
        (dialog as? com.google.android.material.bottomsheet.BottomSheetDialog)?.behavior?.maxWidth =
            (teamcityapp.libraries.theme.TeamCityDimensions.paneMaxWidth.value * resources.displayMetrics.density).toInt()
        dialog?.window?.let { teamcityapp.libraries.theme.applyTeamCityWindowStyle(it, resources.configuration.uiMode and android.content.res.Configuration.UI_MODE_NIGHT_MASK == android.content.res.Configuration.UI_MODE_NIGHT_YES) }
    }
    companion object {

        const val ARG_CODE = "arg_code"

        fun createBottomSheetDialog(code: Int): FilterBottomSheetDialogFragment {
            val filterBottomSheetDialogFragment = FilterBottomSheetDialogFragment()
            val bundle = Bundle()
            bundle.putInt(ARG_CODE, code)
            filterBottomSheetDialogFragment.arguments = bundle
            return filterBottomSheetDialogFragment
        }
    }
}
