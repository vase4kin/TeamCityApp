/*
 * Copyright 2019 Andrey Tolpeev
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

package teamcityapp.features.build_overview.impl

import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.core.view.MenuHost
import androidx.core.view.MenuProvider
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject
import teamcityapp.features.build_overview.api.BuildOverviewAction
import teamcityapp.features.build_overview.impl.router.BuildOverviewFragmentRouter
import teamcityapp.libraries.theme.TeamCityTheme

/** Compose owns the overview; the existing BuildDetails toolbar remains a host boundary. */
@AndroidEntryPoint
@Suppress("DEPRECATION")
class BuildOverviewFragment :
    Fragment(),
    MenuProvider {
    @Inject lateinit var router: BuildOverviewFragmentRouter
    private val visible = mutableStateOf(true)
    private var latest = BuildOverviewUiState()
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): ComposeView {
        visible.value = !isHidden && userVisibleHint
        return ComposeView(requireContext()).apply {
            setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
            setContent { TeamCityTheme { BuildOverviewRoute(router, visible.value, ::updateMenu) } }
        }
    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        (requireActivity() as MenuHost).addMenuProvider(this, viewLifecycleOwner, Lifecycle.State.RESUMED)
    }
    private fun updateMenu(state: BuildOverviewUiState) {
        latest = state
        (requireActivity() as MenuHost).invalidateMenu()
    }
    override fun setUserVisibleHint(isVisibleToUser: Boolean) {
        super.setUserVisibleHint(isVisibleToUser)
        visible.value = isVisibleToUser && !isHidden
        activity?.invalidateOptionsMenu()
    }
    override fun onHiddenChanged(hidden: Boolean) {
        super.onHiddenChanged(hidden)
        visible.value = !hidden && userVisibleHint
        activity?.invalidateOptionsMenu()
    }
    override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
        if (!visible.value) return
        latest.actions.forEach { action -> menu.add(MENU_GROUP, MENU_BASE + action.ordinal, action.ordinal, action.label()).setShowAsAction(MenuItem.SHOW_AS_ACTION_NEVER) }
    }
    override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
        val action = BuildOverviewAction.entries.firstOrNull { MENU_BASE + it.ordinal == menuItem.itemId } ?: return false
        val build = latest.build ?: return false
        if (!visible.value || action !in latest.actions) return false
        router.dispatch(action, build)
        return true
    }
    override fun onDestroyView() {
        latest = BuildOverviewUiState()
        super.onDestroyView()
    }
    companion object {
        private const val MENU_GROUP = 63100
        private const val MENU_BASE = 63200
    }
}
