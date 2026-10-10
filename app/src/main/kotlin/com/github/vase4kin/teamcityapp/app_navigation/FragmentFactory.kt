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

package com.github.vase4kin.teamcityapp.app_navigation

import androidx.fragment.app.Fragment
import com.github.vase4kin.teamcityapp.queue.view.BuildQueueFragment
import com.github.vase4kin.teamcityapp.runningbuilds.view.RunningBuildsFragment
import teamcityapp.features.agents.api.AgentsNavigation
import teamcityapp.features.favorites.api.FavoritesNavigation
import teamcityapp.features.navigation.api.NavigationNavigation

interface FragmentFactory {
    fun createFragment(index: Int): Fragment
    fun getSize(): Int
}

class FragmentFactoryImpl(
    private val agentsNavigation: AgentsNavigation,
    private val navigation: NavigationNavigation,
    private val favoritesNavigation: FavoritesNavigation
) : FragmentFactory {
    override fun createFragment(index: Int): Fragment {
        when (index) {
            AppNavigationItem.PROJECTS.ordinal -> return navigation.createFragment()
            AppNavigationItem.FAVORITES.ordinal -> return favoritesNavigation.createFragment()
            AppNavigationItem.RUNNING_BUILDS.ordinal -> return RunningBuildsFragment()
            AppNavigationItem.BUILD_QUEUE.ordinal -> return BuildQueueFragment()
            AppNavigationItem.AGENTS.ordinal -> return agentsNavigation.createFragment()
        }
        throw IllegalStateException("Wrong index")
    }

    override fun getSize(): Int = AppNavigationItem.values().size
}
