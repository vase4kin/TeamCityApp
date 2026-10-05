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

package teamcityapp.features.properties.impl

import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.fragment.app.Fragment
import dagger.hilt.android.AndroidEntryPoint
import teamcityapp.features.properties.api.Property
import teamcityapp.features.properties.impl.router.PropertiesRouter
import teamcityapp.libraries.theme.TeamCityTheme
import javax.inject.Inject

@AndroidEntryPoint
class PropertiesFragment : Fragment() {
    @Inject lateinit var router: PropertiesRouter

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?) =
        ComposeView(requireContext()).apply {
            setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
            setContent { TeamCityTheme { PropertiesRoute(router) } }
        }

    companion object {
        fun create(properties: List<Property>): PropertiesFragment = PropertiesFragment().apply {
            arguments = Bundle().apply {
                putStringArrayList(PropertiesViewModel.ARG_NAMES, ArrayList(properties.map { it.name }))
                putStringArrayList(PropertiesViewModel.ARG_VALUES, ArrayList(properties.map { it.value }))
            }
        }
    }
}
