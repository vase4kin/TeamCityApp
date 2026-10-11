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

package teamcityapp.features.tests.impl

import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.fragment.app.Fragment
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject
import teamcityapp.features.tests.impl.router.TestsRouter
import teamcityapp.libraries.theme.TeamCityTheme

@AndroidEntryPoint
class TestsFragment : Fragment() {
    @Inject lateinit var router: TestsRouter

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?) = ComposeView(requireContext()).apply {
        setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
        setContent { TeamCityTheme { TestsRoute(router) } }
    }

    companion object {
        // Preserve incoming argument keys across migration and Fragment restoration.
        const val ARG_URL = "url"
        const val ARG_PASSED = "passedCount"
        const val ARG_FAILED = "failedCount"
        const val ARG_IGNORED = "ignoredCount"
    }
}
