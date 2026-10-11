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

package teamcityapp.features.properties.impl.navigation

import android.os.Bundle
import androidx.compose.runtime.*
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import dagger.multibindings.IntoSet
import javax.inject.Inject
import teamcityapp.features.properties.api.navigation.PropertiesDestination
import teamcityapp.features.properties.impl.PropertiesRoute
import teamcityapp.features.properties.impl.router.PropertiesRouterImpl
import teamcityapp.libraries.clipboard.ClipboardWriter
import teamcityapp.libraries.navigation.*

class PropertiesDestinationRegistration @Inject constructor(private val clipboard: ClipboardWriter) : FeatureDestination<PropertiesDestination>(PropertiesDestination::class, PropertiesDestination.serializer()) {
    @Composable override fun Content(destination: PropertiesDestination, navigator: Navigator, entryId: String) {
        val arguments = remember(destination) {
            Bundle().apply {
                putStringArrayList("property_names", ArrayList(destination.properties.map { it.name }))
                putStringArrayList("property_values", ArrayList(destination.properties.map { it.value }))
            }
        }
        val router = remember(clipboard, entryId) { PropertiesRouterImpl(clipboard) }
        DestinationArguments(arguments) { PropertiesRoute(router) }
    }
}

@Module
@InstallIn(SingletonComponent::class)
abstract class PropertiesDestinationModule {
    @Binds @IntoSet
    abstract fun registration(implementation: PropertiesDestinationRegistration): DestinationRegistration
}
