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

package teamcityapp.features.navigation.impl.router

import androidx.fragment.app.Fragment
import dagger.hilt.android.scopes.FragmentScoped
import javax.inject.Inject
import teamcityapp.features.navigation.api.NavigationAppRouter
import teamcityapp.features.navigation.api.NavigationEntry
import teamcityapp.features.navigation.api.NavigationNavigation
import teamcityapp.features.navigation.impl.NavigationFragment
import teamcityapp.libraries.app_rating.AppRating
import teamcityapp.libraries.utils.requireScreenOwner

@FragmentScoped
class NavigationFragmentRouter @Inject constructor(
    owner: Fragment,
    private val navigation: NavigationNavigation,
    private val appRouter: NavigationAppRouter,
    private val rating: AppRating
) : NavigationRouter {
    private val fragment = owner.requireScreenOwner<NavigationFragment>()
    override fun navigateUp() = appRouter.openDrawer(fragment.requireActivity())
    override fun open(entry: NavigationEntry) {
        when (entry) {
            is NavigationEntry.Project -> navigation.open(fragment.requireActivity(), entry.reference)
            is NavigationEntry.Configuration -> appRouter.openBuildConfiguration(fragment.requireActivity(), entry.configuration)
        }
    }
    override fun openRating() = rating.open(fragment.requireActivity())
}
