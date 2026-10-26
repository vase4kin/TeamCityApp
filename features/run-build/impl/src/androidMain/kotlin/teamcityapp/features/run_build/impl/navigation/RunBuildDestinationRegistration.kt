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

package teamcityapp.features.run_build.impl.navigation

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
import teamcityapp.features.run_build.api.navigation.*
import teamcityapp.features.run_build.impl.RunBuildRoute
import teamcityapp.features.run_build.impl.router.RunBuildRouter
import teamcityapp.libraries.navigation.*

class RunBuildDestinationRegistration @Inject constructor() : FeatureDestination<RunBuildDestination>(RunBuildDestination::class, RunBuildDestination.serializer()) {

    override fun registerSerializers(builder: SerializersModuleBuilder) {
        super.registerSerializers(builder)
        builder.polymorphic(NavigationResult::class) { subclass(QueuedBuildResult::class, QueuedBuildResult.serializer()) }
    }

    @Composable override fun Content(destination: RunBuildDestination, navigator: Navigator, entryId: String) {
        val activity = checkNotNull(LocalActivity.current)
        val arguments = remember(destination) { Bundle().apply { putString("BuildTypeId", destination.configurationId) } }
        val router = remember(activity, navigator, entryId) {
            object : RunBuildRouter {
                override fun close() {
                    if (!navigator.pop()) activity.finish()
                }
                override fun queued(href: String) {
                    if (!navigator.finish(QueuedBuildResult(href), destination.recipientId ?: "")) close()
                }
            }
        }
        DestinationArguments(arguments) { RunBuildRoute(router) }
    }
}

@Module
@InstallIn(SingletonComponent::class)
abstract class RunBuildDestinationModule {
    @Binds @IntoSet
    abstract fun registration(implementation: RunBuildDestinationRegistration): DestinationRegistration
}
