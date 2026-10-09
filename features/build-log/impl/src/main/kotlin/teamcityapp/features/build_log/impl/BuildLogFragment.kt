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

package teamcityapp.features.build_log.impl
import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.fragment.app.Fragment
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject
import teamcityapp.features.build_log.impl.BuildLogRoute
import teamcityapp.features.build_log.impl.router.BuildLogRouter
import teamcityapp.libraries.theme.TeamCityTheme
@AndroidEntryPoint
class BuildLogFragment : Fragment() {
    @Inject lateinit var router: BuildLogRouter

    @Inject lateinit var configuration: teamcityapp.features.build_log.api.BuildLogConfiguration
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?) = ComposeView(requireContext()).apply {
        setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
        setContent { TeamCityTheme { BuildLogRoute(router, configuration.pageLoadingTimeoutMillis) } }
    }
    companion object {
        fun newInstance(buildId: String): BuildLogFragment {
            val fragment = BuildLogFragment()
            val args = Bundle()
            args.putString("buildId", buildId)
            fragment.arguments = args
            return fragment
        }
    }
}
