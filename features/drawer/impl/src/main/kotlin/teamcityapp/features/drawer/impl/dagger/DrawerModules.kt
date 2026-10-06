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

package teamcityapp.features.drawer.impl.dagger
import androidx.fragment.app.Fragment
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.FragmentComponent
import dagger.hilt.android.scopes.FragmentScoped
import dagger.hilt.components.SingletonComponent
import teamcityapp.features.drawer.api.navigation.DrawerNavigation
import teamcityapp.features.drawer.api.router.DrawerAppRouter
import teamcityapp.features.drawer.impl.DrawerBottomSheetDialogFragment
import teamcityapp.features.drawer.impl.navigation.DrawerNavigationImpl
import teamcityapp.features.drawer.impl.router.DrawerRouter
import teamcityapp.features.drawer.impl.router.DrawerRouterImpl
import teamcityapp.libraries.app_rating.AppRating
import teamcityapp.libraries.chrome_tabs.ChromeCustomTabsImpl
import teamcityapp.libraries.utils.requireScreenOwner
@Module
@InstallIn(SingletonComponent::class)
abstract class DrawerNavigationModule {
    @Binds abstract fun navigation(impl: DrawerNavigationImpl): DrawerNavigation
}

@Module
@InstallIn(FragmentComponent::class)
object DrawerScreenModule {
    @Provides @FragmentScoped
    fun router(owner: Fragment, appRouter: DrawerAppRouter, rating: AppRating): DrawerRouter {
        val fragment = owner.requireScreenOwner<DrawerBottomSheetDialogFragment>()
        return DrawerRouterImpl(fragment, ChromeCustomTabsImpl(fragment.requireActivity()), appRouter, rating)
    }
}
