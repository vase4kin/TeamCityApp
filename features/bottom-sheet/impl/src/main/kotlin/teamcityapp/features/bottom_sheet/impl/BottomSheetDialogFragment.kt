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

package teamcityapp.features.bottom_sheet.impl
import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import com.google.android.material.bottomsheet.BottomSheetDialogFragment as MaterialBottomSheetDialogFragment
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject
import teamcityapp.features.bottom_sheet.impl.BottomSheetRoute
import teamcityapp.features.bottom_sheet.impl.router.BottomSheetRouter
import teamcityapp.libraries.theme.TeamCitySystemBars
import teamcityapp.libraries.theme.TeamCityTheme
@AndroidEntryPoint
class BottomSheetDialogFragment : MaterialBottomSheetDialogFragment() {
    override fun getTheme() = teamcityapp.libraries.theme.R.style.ThemeOverlay_TeamCity_ComposeBottomSheetDialog

    @Inject lateinit var router: BottomSheetRouter
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?) = ComposeView(requireContext()).apply {
        setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
        setContent {
            TeamCityTheme {
                TeamCitySystemBars(dialog?.window)
                BottomSheetRoute(router)
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

        internal const val ARG_TITLE = "arg_title"
        internal const val ARG_DESCRIPTION = "arg_description"
        internal const val ARG_BOTTOM_SHEET_TYPE = "arg_bottom_sheet_type"

        fun createBottomSheetDialog(
            title: String,
            description: String,
            menuType: Int
        ): BottomSheetDialogFragment = createBottomSheetDialog(title, arrayOf(description), menuType)

        fun createBottomSheetDialog(
            title: String,
            descriptions: Array<String>,
            menuType: Int
        ): BottomSheetDialogFragment {
            val bottomSheetDialogFragment = BottomSheetDialogFragment()
            val bundle = Bundle()
            bundle.putString(ARG_TITLE, title)
            bundle.putStringArray(ARG_DESCRIPTION, descriptions)
            bundle.putInt(ARG_BOTTOM_SHEET_TYPE, menuType)
            bottomSheetDialogFragment.arguments = bundle
            return bottomSheetDialogFragment
        }
    }
}
