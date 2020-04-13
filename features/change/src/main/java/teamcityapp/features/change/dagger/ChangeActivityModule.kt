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

package teamcityapp.features.change.dagger

import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.GroupieViewHolder
import dagger.Module
import dagger.Provides
import teamcityapp.features.change.router.ChangeRouter
import teamcityapp.features.change.router.ChangeRouterImpl
import teamcityapp.features.change.view.ARG_BUNDLE_DATA
import teamcityapp.features.change.view.ChangeActivity
import teamcityapp.features.change.view.ChangeItemsFactory
import teamcityapp.features.change.view.ChangeItemsFactoryImpl
import teamcityapp.features.change.viewmodel.ChangeViewModel
import teamcityapp.libraries.chrome_tabs.ChromeCustomTabs
import teamcityapp.libraries.chrome_tabs.ChromeCustomTabsImpl
import teamcityapp.libraries.storage.Storage

@Module
object ChangeActivityModule {

    @ChangeActivityScope
    @JvmStatic
    @Provides
    fun provideChromeTabs(activity: ChangeActivity): ChromeCustomTabs =
        ChromeCustomTabsImpl(activity)

    @JvmStatic
    @Provides
    fun provideRouter(chromeCustomTabs: ChromeCustomTabs, storage: Storage): ChangeRouter {
        return ChangeRouterImpl(chromeCustomTabs, storage)
    }

    @JvmStatic
    @Provides
    fun provideViewModel(
        activity: ChangeActivity,
        router: ChangeRouter,
        adapter: GroupAdapter<GroupieViewHolder>,
        itemsFactory: ChangeItemsFactory
    ): ChangeViewModel {
        return ChangeViewModel(
            bundleData = activity.intent.getParcelableExtra(ARG_BUNDLE_DATA),
            router = router,
            adapter = adapter,
            finish = { activity.finish() },
            itemsFactory = itemsFactory
        )
    }

    @JvmStatic
    @Provides
    fun provideAdapter(): GroupAdapter<GroupieViewHolder> {
        return GroupAdapter<GroupieViewHolder>()
    }

    @JvmStatic
    @Provides
    fun provideItemsFactory(router: ChangeRouter): ChangeItemsFactory {
        return ChangeItemsFactoryImpl(router)
    }
}
