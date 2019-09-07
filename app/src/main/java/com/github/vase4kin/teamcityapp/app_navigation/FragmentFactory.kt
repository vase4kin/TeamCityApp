/*
 * Copyright 2019 Andrey Tolpeev
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
import com.github.vase4kin.teamcityapp.agents.view.AgentListFragment
import com.github.vase4kin.teamcityapp.favorites.view.FavoritesFragment
import com.github.vase4kin.teamcityapp.home.router.HomeRouter
import com.github.vase4kin.teamcityapp.navigation.view.NavigationListFragment
import com.github.vase4kin.teamcityapp.queue.view.BuildQueueFragment
import com.github.vase4kin.teamcityapp.runningbuilds.view.RunningBuildsFragment

interface FragmentFactory {
    fun createFragment(index: Int): Fragment
    fun getSize(): Int
}

class FragmentFactoryImpl : FragmentFactory {
    override fun createFragment(index: Int): Fragment {
        when (index) {
            // TODO: provide a resource here
            AppNavigationItem.PROJECTS.ordinal -> return NavigationListFragment.newInstance(
                "Projects",
                HomeRouter.ROOT_PROJECTS_ID
            )
            AppNavigationItem.FAVORITES.ordinal -> return FavoritesFragment()
            AppNavigationItem.RUNNING_BUILDS.ordinal -> return RunningBuildsFragment()
            AppNavigationItem.BUILD_QUEUE.ordinal -> return BuildQueueFragment()
            AppNavigationItem.AGENTS.ordinal -> return AgentListFragment()
        }
        throw IllegalStateException("Wrong index")
    }

    override fun getSize(): Int = AppNavigationItem.values().size
}
