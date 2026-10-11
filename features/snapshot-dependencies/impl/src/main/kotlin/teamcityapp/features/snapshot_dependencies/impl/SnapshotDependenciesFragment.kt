/*
 * Copyright 2016 Andrey Tolpeev
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

package teamcityapp.features.snapshot_dependencies.impl

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
import teamcityapp.features.snapshot_dependencies.impl.router.SnapshotDependenciesRouter
import teamcityapp.features.snapshot_dependencies.impl.tracker.SnapshotDependenciesTracker
import teamcityapp.libraries.theme.TeamCityTheme

@AndroidEntryPoint
class SnapshotDependenciesFragment : Fragment() {
    @Inject lateinit var router: SnapshotDependenciesRouter

    @Inject lateinit var tracker: SnapshotDependenciesTracker
    private var hidden by mutableStateOf(false)
    private var visibleToUser by mutableStateOf(true)
    internal val contentVisible: Boolean get() = !hidden && visibleToUser

    @Suppress("DEPRECATION")
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?) = ComposeView(requireContext()).apply {
        hidden = isHidden
        visibleToUser = userVisibleHint
        setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
        setContent { TeamCityTheme { SnapshotDependenciesRoute(router, tracker, visible = contentVisible) } }
    }

    override fun onHiddenChanged(hidden: Boolean) {
        super.onHiddenChanged(hidden)
        this.hidden = hidden
    }

    @Suppress("DEPRECATION")
    override fun setUserVisibleHint(isVisibleToUser: Boolean) {
        super.setUserVisibleHint(isVisibleToUser)
        // BuildDetails uses the legacy FragmentPagerAdapter visibility hint.
        visibleToUser = isVisibleToUser
    }

    override fun onDestroyView() {
        if (requireActivity().isChangingConfigurations) {
            ViewModelProvider(this)[SnapshotDependenciesViewModel::class.java].onConfigurationRecreation(contentVisible)
        }
        super.onDestroyView()
    }

    companion object {
        internal const val BUILD_ID = "id"
        internal const val BUILD_TYPE_NAME = "name"
    }
}
