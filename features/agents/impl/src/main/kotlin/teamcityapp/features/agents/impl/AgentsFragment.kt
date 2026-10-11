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

package teamcityapp.features.agents.impl

import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject
import teamcityapp.features.agents.impl.router.AgentsRouter
import teamcityapp.libraries.theme.TeamCityTheme

@AndroidEntryPoint
class AgentsFragment : Fragment() {
    @Inject lateinit var router: AgentsRouter
    private var visible by mutableStateOf(true)

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?) = ComposeView(requireContext()).apply {
        visible = !isHidden
        setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
        setContent { TeamCityTheme { AgentsRoute(router, visible = visible) } }
    }

    override fun onHiddenChanged(hidden: Boolean) {
        super.onHiddenChanged(hidden)
        // Home switches tabs with hide/show, which leaves the Fragment lifecycle resumed.
        visible = !hidden
    }

    override fun onDestroyView() {
        // Configuration recreation retains the Fragment ViewModel and its completed data.
        if (requireActivity().isChangingConfigurations) {
            ViewModelProvider(this)[AgentsViewModel::class.java].onConfigurationRecreation(wasVisible = !isHidden)
        }
        super.onDestroyView()
    }
}
