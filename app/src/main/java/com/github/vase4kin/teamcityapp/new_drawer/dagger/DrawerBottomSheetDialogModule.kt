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

import com.github.vase4kin.teamcityapp.new_drawer.drawer.DrawerRouter
import com.github.vase4kin.teamcityapp.new_drawer.drawer.DrawerRouterImpl
import com.github.vase4kin.teamcityapp.new_drawer.tracker.DrawerTracker
import com.github.vase4kin.teamcityapp.new_drawer.tracker.DrawerTrackerImpl
import com.github.vase4kin.teamcityapp.new_drawer.view.AccountViewHolderFactory
import com.github.vase4kin.teamcityapp.new_drawer.view.AccountsDividerViewHolderFactory
import com.github.vase4kin.teamcityapp.new_drawer.view.BaseDrawerItem
import com.github.vase4kin.teamcityapp.new_drawer.view.BaseDrawerViewHolderFactory
import com.github.vase4kin.teamcityapp.new_drawer.view.BottomViewHolderFactory
import com.github.vase4kin.teamcityapp.new_drawer.view.DividerViewHolderFactory
import com.github.vase4kin.teamcityapp.new_drawer.view.DrawerAdapter
import com.github.vase4kin.teamcityapp.new_drawer.view.DrawerBottomSheetDialogFragment
import com.github.vase4kin.teamcityapp.new_drawer.view.MenuViewHolderFactory
import com.github.vase4kin.teamcityapp.new_drawer.view.TYPE_ACCOUNT
import com.github.vase4kin.teamcityapp.new_drawer.view.TYPE_ACCOUNTS_DIVIDER
import com.github.vase4kin.teamcityapp.new_drawer.view.TYPE_BOTTOM
import com.github.vase4kin.teamcityapp.new_drawer.view.TYPE_DIVIDER
import com.github.vase4kin.teamcityapp.new_drawer.view.TYPE_MENU
import com.github.vase4kin.teamcityapp.new_drawer.viewmodel.DrawerViewModel
import com.github.vase4kin.teamcityapp.storage.SharedUserStorage
import com.google.firebase.analytics.FirebaseAnalytics
import dagger.Module
import dagger.Provides
import dagger.multibindings.IntKey
import dagger.multibindings.IntoMap
import teamcityapp.chrome_tabs.ChromeCustomTabs

@Module
class DrawerBottomSheetDialogModule {

    @Provides
    fun providesViewModel(
        fragment: DrawerBottomSheetDialogFragment,
        sharedUserStorage: SharedUserStorage,
        chromeCustomTabs: ChromeCustomTabs,
        tracker: DrawerTracker
    ): DrawerViewModel {
        val setAdapter: (items: List<BaseDrawerItem>) -> Unit = {
            fragment.setAdapter(it)
        }
        return DrawerViewModel(sharedUserStorage, chromeCustomTabs, setAdapter, tracker)
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
    fun providesAdapter(
        viewHolderFactories: Map<Int, @JvmSuppressWildcards BaseDrawerViewHolderFactory>
    ): DrawerAdapter {
        return DrawerAdapter(mutableListOf(), viewHolderFactories)
    }

    @Provides
    fun providesTracker(firebaseAnalytics: FirebaseAnalytics): DrawerTracker {
        return DrawerTrackerImpl(firebaseAnalytics)
    }

    @IntoMap
    @IntKey(TYPE_ACCOUNTS_DIVIDER)
    @Provides
    fun providesAccountsDividerViewHolderFactory(): BaseDrawerViewHolderFactory {
        return AccountsDividerViewHolderFactory()
    }

    @IntoMap
    @IntKey(TYPE_DIVIDER)
    @Provides
    fun providesDividerViewHolderFactory(): BaseDrawerViewHolderFactory {
        return DividerViewHolderFactory()
    }

    @IntoMap
    @IntKey(TYPE_BOTTOM)
    @Provides
    fun providesBottomViewHolderFactory(
        router: DrawerRouter,
        tracker: DrawerTracker
    ): BaseDrawerViewHolderFactory {
        return BottomViewHolderFactory(router, tracker)
    }

    @IntoMap
    @IntKey(TYPE_MENU)
    @Provides
    fun providesMenuViewHolderFactory(
        router: DrawerRouter,
        tracker: DrawerTracker
    ): BaseDrawerViewHolderFactory {
        return MenuViewHolderFactory(router, tracker)
    }

    @IntoMap
    @IntKey(TYPE_ACCOUNT)
    @Provides
    fun providesAccountViewHolderFactory(
        router: DrawerRouter,
        tracker: DrawerTracker
    ): BaseDrawerViewHolderFactory {
        return AccountViewHolderFactory(router, tracker)
    }
}
