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

package teamcityapp.features.running_builds.impl.navigation

import androidx.activity.compose.LocalActivity
import androidx.compose.runtime.*
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import dagger.multibindings.IntoSet
import javax.inject.Inject
import teamcityapp.features.build_details.api.BuildDetailsNavigation
import teamcityapp.features.build_history.api.navigation.BuildHistoryDestination
import teamcityapp.features.drawer.api.navigation.DrawerDestination
import teamcityapp.features.running_builds.api.navigation.RunningBuildsDestination
import teamcityapp.features.running_builds.impl.RunningBuildsRoute
import teamcityapp.features.running_builds.impl.router.RunningBuildsRouter
import teamcityapp.libraries.builds.BuildLaunchData
import teamcityapp.libraries.navigation.*

class RunningBuildsDestinationRegistration @Inject constructor(private val builds: BuildDetailsNavigation) : FeatureDestination<RunningBuildsDestination>(RunningBuildsDestination::class, RunningBuildsDestination.serializer()) {
    @Composable override fun Content(destination: RunningBuildsDestination, navigator: Navigator, entryId: String) {
        val activity = checkNotNull(LocalActivity.current)
        val router = remember(activity, navigator, entryId) {
            object : RunningBuildsRouter {
                override fun openDrawer() {
                    navigator.push(DrawerDestination)
                }
                override fun openBuild(build: BuildLaunchData) = builds.open(activity, build)
                override fun openBuildHistory(configurationId: String, configurationName: String) {
                    navigator.push(BuildHistoryDestination(configurationId, configurationName))
                }
            }
        }
        RunningBuildsRoute(router)
    }
}

@Module
@InstallIn(SingletonComponent::class)
abstract class RunningBuildsDestinationModule {
    @Binds @IntoSet
    abstract fun registration(implementation: RunningBuildsDestinationRegistration): DestinationRegistration
}
