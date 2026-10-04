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

package teamcityapp.features.about.impl.dagger

import android.app.Activity
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ActivityComponent
import teamcityapp.features.about.impl.AboutActivity
import teamcityapp.libraries.chrome_tabs.ChromeCustomTabs
import teamcityapp.libraries.chrome_tabs.ChromeCustomTabsImpl
import teamcityapp.libraries.utils.requireScreenOwner
import teamcityapp.features.about.impl.router.AboutRouter
import teamcityapp.features.about.impl.router.AboutRouterImpl
import teamcityapp.libraries.app_rating.AppRating
import dagger.hilt.android.scopes.ActivityScoped
import javax.inject.Named

@Module
@InstallIn(ActivityComponent::class)
object AboutActivityModule {
    @Provides
    @ActivityScoped
    fun provideRouter(
        activity: Activity,
        @Named("AboutActivity") chromeTabs: ChromeCustomTabs,
        appRating: AppRating,
    ): AboutRouter = AboutRouterImpl(activity.requireScreenOwner<AboutActivity>(), chromeTabs, appRating)

    @Provides
    @Named("AboutActivity")
    fun provideChromeTabs(activity: Activity): ChromeCustomTabs =
        ChromeCustomTabsImpl(activity.requireScreenOwner<AboutActivity>())
}
