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

package teamcityapp.features.navigation.impl

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
import teamcityapp.features.navigation.impl.router.NavigationFragmentRouter
import teamcityapp.libraries.theme.TeamCityTheme

@AndroidEntryPoint
class NavigationFragment : Fragment() {
    @Inject lateinit var router: NavigationFragmentRouter
    private var visible by mutableStateOf(true)
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?) = ComposeView(requireContext()).apply {
        visible = !isHidden
        setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
        setContent { TeamCityTheme { NavigationRoute(router, root = true, visible = visible) } }
    }
    override fun onHiddenChanged(hidden: Boolean) {
        super.onHiddenChanged(hidden)
        visible = !hidden
    }
    override fun onDestroyView() {
        if (requireActivity().isChangingConfigurations) {
            ViewModelProvider(this)[NavigationViewModel::class.java].onConfigurationRecreation(wasVisible = !isHidden)
        }
        super.onDestroyView()
    }
}
