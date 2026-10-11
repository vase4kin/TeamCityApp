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

package teamcityapp.features.change_details.impl.navigation

import androidx.activity.compose.LocalActivity
import androidx.compose.runtime.*
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import dagger.multibindings.IntoSet
import javax.inject.Inject
import teamcityapp.features.change_details.api.navigation.ChangeDetailsDestination
import teamcityapp.features.change_details.impl.ChangeDetailsArguments
import teamcityapp.features.change_details.impl.ChangeDetailsRoute
import teamcityapp.features.change_details.impl.router.ChangeDetailsRouter
import teamcityapp.features.change_details.impl.router.ChangeDetailsRouterImpl
import teamcityapp.libraries.chrome_tabs.ChromeCustomTabsImpl
import teamcityapp.libraries.navigation.*
import teamcityapp.libraries.storage.Storage

class ChangeDetailsDestinationRegistration @Inject constructor(private val storage: Storage) : FeatureDestination<ChangeDetailsDestination>(ChangeDetailsDestination::class, ChangeDetailsDestination.serializer()) {
    @Composable override fun Content(destination: ChangeDetailsDestination, navigator: Navigator, entryId: String) {
        val activity = checkNotNull(LocalActivity.current)
        val arguments = remember(destination) { destination.details?.let(ChangeDetailsArguments::bundle) ?: android.os.Bundle() }
        val router = remember(activity, navigator, entryId) {
            val platform = ChangeDetailsRouterImpl(activity, ChromeCustomTabsImpl(activity), storage)
            object : ChangeDetailsRouter by platform {
                override fun close() {
                    if (!navigator.pop()) activity.finish()
                }
            }
        }
        DestinationArguments(arguments) { ChangeDetailsRoute(router) }
    }
}

@Module
@InstallIn(SingletonComponent::class)
abstract class ChangeDetailsDestinationModule {
    @Binds @IntoSet
    abstract fun registration(implementation: ChangeDetailsDestinationRegistration): DestinationRegistration
}
