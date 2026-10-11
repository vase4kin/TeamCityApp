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

package teamcityapp.features.build_log.impl.navigation

import android.os.Bundle
import androidx.activity.compose.LocalActivity
import androidx.compose.runtime.*
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import dagger.multibindings.IntoSet
import javax.inject.Inject
import teamcityapp.features.build_log.api.BuildLogConfiguration
import teamcityapp.features.build_log.api.navigation.BuildLogDestination
import teamcityapp.features.build_log.impl.BuildLogRoute
import teamcityapp.features.build_log.impl.router.BuildLogRouterImpl
import teamcityapp.libraries.navigation.*

class BuildLogDestinationRegistration @Inject constructor(private val configuration: BuildLogConfiguration) : FeatureDestination<BuildLogDestination>(BuildLogDestination::class, BuildLogDestination.serializer()) {
    @Composable override fun Content(destination: BuildLogDestination, navigator: Navigator, entryId: String) {
        val activity = checkNotNull(LocalActivity.current)
        val arguments = remember(destination) { Bundle().apply { putString("buildId", destination.buildId) } }
        val router = remember(activity, entryId) { BuildLogRouterImpl(activity) }
        DestinationArguments(arguments) { BuildLogRoute(router, configuration.pageLoadingTimeoutMillis) }
    }
}

@Module
@InstallIn(SingletonComponent::class)
abstract class BuildLogDestinationModule {
    @Binds @IntoSet
    abstract fun registration(implementation: BuildLogDestinationRegistration): DestinationRegistration
}
