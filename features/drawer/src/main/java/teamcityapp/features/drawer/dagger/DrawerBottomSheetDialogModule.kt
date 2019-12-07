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

package teamcityapp.features.drawer.dagger

import com.google.firebase.analytics.FirebaseAnalytics
import dagger.Module
import dagger.Provides
import dagger.multibindings.IntKey
import dagger.multibindings.IntoMap
import teamcityapp.features.drawer.drawer.DrawerAppRouter
import teamcityapp.features.drawer.drawer.DrawerRouter
import teamcityapp.features.drawer.drawer.DrawerRouterImpl
import teamcityapp.features.drawer.tracker.DrawerTracker
import teamcityapp.features.drawer.tracker.DrawerTrackerImpl
import teamcityapp.features.drawer.view.AccountViewHolderFactory
import teamcityapp.features.drawer.view.AccountsDividerViewHolderFactory
import teamcityapp.features.drawer.view.BaseDrawerItem
import teamcityapp.features.drawer.view.BaseDrawerViewHolderFactory
import teamcityapp.features.drawer.view.BottomViewHolderFactory
import teamcityapp.features.drawer.view.DividerViewHolderFactory
import teamcityapp.features.drawer.view.DrawerAdapter
import teamcityapp.features.drawer.view.DrawerBottomSheetDialogFragment
import teamcityapp.features.drawer.view.MenuViewHolderFactory
import teamcityapp.features.drawer.view.TYPE_ACCOUNT
import teamcityapp.features.drawer.view.TYPE_ACCOUNTS_DIVIDER
import teamcityapp.features.drawer.view.TYPE_BOTTOM
import teamcityapp.features.drawer.view.TYPE_DIVIDER
import teamcityapp.features.drawer.view.TYPE_MENU
import teamcityapp.features.drawer.viewmodel.DrawerViewModel
import teamcityapp.libraries.chrome_tabs.ChromeCustomTabs
import teamcityapp.libraries.storage.Storage

@Module
class DrawerBottomSheetDialogModule {

    @Provides
    fun providesViewModel(
        fragment: DrawerBottomSheetDialogFragment,
        storage: Storage,
        chromeCustomTabs: ChromeCustomTabs,
        tracker: DrawerTracker
    ): DrawerViewModel {
        val setAdapter: (items: List<BaseDrawerItem>) -> Unit = {
            fragment.setAdapter(it)
        }
        return DrawerViewModel(
            storage,
            chromeCustomTabs,
            setAdapter,
            tracker
        )
    }

    @DrawerBottomSheetDialogScope
    @Provides
    fun providesRouter(
        fragment: DrawerBottomSheetDialogFragment,
        storage: Storage,
        chromeCustomTabs: ChromeCustomTabs,
        router: DrawerAppRouter
    ): DrawerRouter {
        return DrawerRouterImpl(
            fragment,
            storage,
            chromeCustomTabs,
            router
        )
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
