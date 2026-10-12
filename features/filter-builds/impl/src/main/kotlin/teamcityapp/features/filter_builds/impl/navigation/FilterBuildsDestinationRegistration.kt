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

package teamcityapp.features.filter_builds.impl.navigation

import android.os.Bundle
import androidx.activity.compose.LocalActivity
import androidx.compose.runtime.*
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import dagger.multibindings.IntoSet
import javax.inject.Inject
import kotlinx.serialization.modules.*
import teamcityapp.features.filter_builds.api.navigation.*
import teamcityapp.features.filter_builds.impl.FilterBuildsRoute
import teamcityapp.features.filter_builds.impl.router.FilterBuildsRouter
import teamcityapp.libraries.navigation.*

class FilterBuildsDestinationRegistration @Inject constructor() : FeatureDestination<FilterBuildsDestination>(FilterBuildsDestination::class, FilterBuildsDestination.serializer()) {

    override fun registerSerializers(builder: SerializersModuleBuilder) {
        super.registerSerializers(builder)
        builder.polymorphic(NavigationResult::class) { subclass(BuildFilterResult::class, BuildFilterResult.serializer()) }
    }

    @Composable override fun Content(destination: FilterBuildsDestination, navigator: Navigator, entryId: String) {
        val activity = checkNotNull(LocalActivity.current)
        val arguments = remember(destination) { Bundle().apply { putString("BuildTypeId", destination.configurationId) } }
        val router = remember(activity, navigator, entryId) {
            object : FilterBuildsRouter {
                override fun close() {
                    if (!navigator.pop()) activity.finish()
                }
                override fun apply(filter: teamcityapp.features.filter_builds.api.BuildFilter) {
                    if (!navigator.finish(BuildFilterResult(filter), destination.recipientId ?: "")) close()
                }
            }
        }
        DestinationArguments(arguments) { FilterBuildsRoute(router) }
    }
}

@Module
@InstallIn(SingletonComponent::class)
abstract class FilterBuildsDestinationModule {
    @Binds @IntoSet
    abstract fun registration(implementation: FilterBuildsDestinationRegistration): DestinationRegistration
}
