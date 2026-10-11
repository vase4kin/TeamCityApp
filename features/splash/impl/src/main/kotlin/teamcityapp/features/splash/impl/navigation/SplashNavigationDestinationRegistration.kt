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

package teamcityapp.features.splash.impl.navigation

import androidx.compose.runtime.*
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import dagger.multibindings.IntoSet
import javax.inject.Inject
import teamcityapp.features.splash.api.SplashDestination
import teamcityapp.features.splash.api.navigation.SplashNavigationDestination
import teamcityapp.features.splash.impl.SplashRoute
import teamcityapp.features.splash.impl.router.SplashRouter
import teamcityapp.libraries.navigation.*

class SplashNavigationDestinationRegistration @Inject constructor(private val accounts: AccountFlowNavigation) : FeatureDestination<SplashNavigationDestination>(SplashNavigationDestination::class, SplashNavigationDestination.serializer()) {
    @Composable override fun Content(destination: SplashNavigationDestination, navigator: Navigator, entryId: String) {
        val router = remember(navigator, entryId) {
            object : SplashRouter {
                override fun navigate(destination: SplashDestination) {
                    when (destination) {
                        SplashDestination.Home -> accounts.home(resetSession = false)
                        SplashDestination.Login -> accounts.login()
                    }
                }
            }
        }
        SplashRoute(router)
    }
}

@Module
@InstallIn(SingletonComponent::class)
abstract class SplashNavigationDestinationModule {
    @Binds @IntoSet
    abstract fun registration(implementation: SplashNavigationDestinationRegistration): DestinationRegistration
}
