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

package teamcityapp.features.about.impl.navigation

import androidx.activity.compose.LocalActivity
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import dagger.multibindings.IntoSet
import javax.inject.Inject
import teamcityapp.features.about.api.navigation.AboutDestination
import teamcityapp.features.about.impl.AboutRoute
import teamcityapp.features.about.impl.router.AboutRouterImpl
import teamcityapp.libraries.app_rating.AppRating
import teamcityapp.libraries.chrome_tabs.ChromeCustomTabsImpl
import teamcityapp.libraries.navigation.DestinationRegistration
import teamcityapp.libraries.navigation.FeatureDestination
import teamcityapp.libraries.navigation.Navigator

class AboutDestinationRegistration @Inject constructor(private val appRating: AppRating) : FeatureDestination<AboutDestination>(AboutDestination::class, AboutDestination.serializer()) {
    @Composable
    override fun Content(destination: AboutDestination, navigator: Navigator, entryId: String) {
        val activity = checkNotNull(LocalActivity.current)
        val router = remember(activity, navigator, entryId) {
            AboutRouterImpl(activity, ChromeCustomTabsImpl(activity), appRating) { if (!navigator.pop()) activity.finish() }
        }
        AboutRoute(router)
    }
}

@Module
@InstallIn(SingletonComponent::class)
abstract class AboutDestinationModule {
    @Binds @IntoSet
    abstract fun destination(implementation: AboutDestinationRegistration): DestinationRegistration
}
