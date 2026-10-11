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

import androidx.compose.runtime.Composable
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import dagger.multibindings.IntoSet
import javax.inject.Inject
import teamcityapp.features.splash.api.navigation.AccountActivationDestination
import teamcityapp.features.splash.impl.SplashScreen
import teamcityapp.features.splash.impl.SplashUiState
import teamcityapp.libraries.navigation.*

class AccountActivationRegistration @Inject constructor(private val accounts: AccountFlowNavigation) : FeatureDestination<AccountActivationDestination>(AccountActivationDestination::class, AccountActivationDestination.serializer()) {
    @Composable override fun Content(destination: AccountActivationDestination, navigator: Navigator, entryId: String) {
        SplashScreen(if (destination.failed) SplashUiState.Error else SplashUiState.Loading, onRetry = {
            destination.destination?.let { accounts.activate(it, destination.resetSession) } ?: accounts.home(destination.resetSession)
        })
    }
}

@Module
@InstallIn(SingletonComponent::class)
abstract class AccountActivationModule {
    @Binds @IntoSet
    abstract fun registration(implementation: AccountActivationRegistration): DestinationRegistration
}
