/*
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

package teamcityapp.features.favorites.impl.router

import androidx.fragment.app.Fragment
import dagger.hilt.android.scopes.FragmentScoped
import javax.inject.Inject
import teamcityapp.features.favorites.api.FavoritesAppRouter
import teamcityapp.features.favorites.impl.FavoritesFragment
import teamcityapp.features.favorites.impl.tracker.FavoritesTracker
import teamcityapp.libraries.build_configurations.BuildConfigurationSummary
import teamcityapp.libraries.build_configurations.ProjectReference
import teamcityapp.libraries.utils.requireScreenOwner

interface FavoritesRouter {
    fun openDrawer()
    fun openProject(project: ProjectReference)
    fun openConfiguration(configuration: BuildConfigurationSummary)
}

@FragmentScoped
class FavoritesRouterImpl @Inject constructor(
    owner: Fragment,
    private val appRouter: FavoritesAppRouter,
    private val tracker: FavoritesTracker
) : FavoritesRouter {
    private val fragment = owner.requireScreenOwner<FavoritesFragment>()
    override fun openDrawer() = appRouter.openDrawer(fragment.requireActivity())
    override fun openProject(project: ProjectReference) = appRouter.openProject(fragment.requireActivity(), project)
    override fun openConfiguration(configuration: BuildConfigurationSummary) {
        tracker.configurationOpened()
        appRouter.openBuildConfiguration(fragment.requireActivity(), configuration)
    }
}
