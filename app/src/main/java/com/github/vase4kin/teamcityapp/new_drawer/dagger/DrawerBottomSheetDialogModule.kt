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

package com.github.vase4kin.teamcityapp.new_drawer.dagger

import com.github.vase4kin.teamcityapp.custom_tabs.ChromeCustomTabs
import com.github.vase4kin.teamcityapp.new_drawer.drawer.DrawerRouter
import com.github.vase4kin.teamcityapp.new_drawer.drawer.DrawerRouterImpl
import com.github.vase4kin.teamcityapp.new_drawer.view.BaseDrawerItem
import com.github.vase4kin.teamcityapp.new_drawer.view.DrawerAdapter
import com.github.vase4kin.teamcityapp.new_drawer.view.DrawerBottomSheetDialogFragment
import com.github.vase4kin.teamcityapp.new_drawer.viewmodel.DrawerViewModel
import com.github.vase4kin.teamcityapp.storage.SharedUserStorage
import dagger.Module
import dagger.Provides

@Module
class DrawerBottomSheetDialogModule {

    @Provides
    fun providesViewModel(
        fragment: DrawerBottomSheetDialogFragment,
        sharedUserStorage: SharedUserStorage,
        chromeCustomTabs: ChromeCustomTabs
    ): DrawerViewModel {
        val setAdapter: (items: List<BaseDrawerItem>) -> Unit = {
            fragment.setAdapter(it)
        }
        return DrawerViewModel(sharedUserStorage, chromeCustomTabs, setAdapter)
    }

    @DrawerBottomSheetDialogScope
    @Provides
    fun providesRouter(
        fragment: DrawerBottomSheetDialogFragment,
        sharedUserStorage: SharedUserStorage,
        chromeCustomTabs: ChromeCustomTabs
    ): DrawerRouter {
        return DrawerRouterImpl(fragment, sharedUserStorage, chromeCustomTabs)
    }

    @Provides
    fun providesAdapter(router: DrawerRouter): DrawerAdapter {
        return DrawerAdapter(mutableListOf(), router)
    }
}