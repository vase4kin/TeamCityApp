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

package teamcityapp.features.login.impl.navigation

import androidx.activity.compose.LocalActivity
import androidx.compose.runtime.*
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import dagger.multibindings.IntoSet
import javax.inject.Inject
import teamcityapp.features.login.api.navigation.LoginDestination
import teamcityapp.features.login.impl.LoginRoute
import teamcityapp.features.login.impl.router.LoginRouter
import teamcityapp.libraries.navigation.*

class LoginDestinationRegistration @Inject constructor(private val accounts: AccountFlowNavigation) : FeatureDestination<LoginDestination>(LoginDestination::class, LoginDestination.serializer()) {
    @Composable override fun Content(destination: LoginDestination, navigator: Navigator, entryId: String) {
        val activity = checkNotNull(LocalActivity.current)
        val router = remember(activity, navigator, entryId) {
            object : LoginRouter {
                override fun close() {
                    if (!navigator.pop()) activity.finish()
                }
                override fun openProjects() {
                    accounts.home()
                }
            }
        }
        LoginRoute(router)
    }
}

@Module
@InstallIn(SingletonComponent::class)
abstract class LoginDestinationModule {
    @Binds @IntoSet
    abstract fun registration(implementation: LoginDestinationRegistration): DestinationRegistration
}
