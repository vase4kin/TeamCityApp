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

package teamcityapp.features.favorites.impl.navigation

import androidx.activity.compose.LocalActivity
import androidx.compose.runtime.*
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import dagger.multibindings.IntoSet
import javax.inject.Inject
import teamcityapp.features.build_history.api.navigation.BuildHistoryDestination
import teamcityapp.features.drawer.api.navigation.DrawerDestination
import teamcityapp.features.favorites.api.FavoritesAppRouter
import teamcityapp.features.favorites.api.navigation.FavoritesDestination
import teamcityapp.features.favorites.impl.FavoritesRoute
import teamcityapp.features.favorites.impl.router.FavoritesRouter
import teamcityapp.features.favorites.impl.tracker.FavoritesTracker
import teamcityapp.features.navigation.api.navigation.ProjectDestination
import teamcityapp.libraries.build_configurations.*
import teamcityapp.libraries.navigation.*

class FavoritesDestinationRegistration @Inject constructor(private val tracker: FavoritesTracker) : FeatureDestination<FavoritesDestination>(FavoritesDestination::class, FavoritesDestination.serializer()) {
    @Composable override fun Content(destination: FavoritesDestination, navigator: Navigator, entryId: String) {
        val activity = checkNotNull(LocalActivity.current)
        val router = remember(activity, navigator, entryId) {
            object : FavoritesRouter {
                override fun openDrawer() {
                    navigator.push(DrawerDestination)
                }
                override fun openProject(project: ProjectReference) {
                    navigator.push(ProjectDestination(project.id, project.name, false))
                }
                override fun openConfiguration(configuration: BuildConfigurationSummary) {
                    tracker.configurationOpened()
                    navigator.push(BuildHistoryDestination(configuration.id, configuration.name))
                }
            }
        }
        FavoritesRoute(router)
    }
}

@Module
@InstallIn(SingletonComponent::class)
abstract class FavoritesDestinationModule {
    @Binds @IntoSet
    abstract fun registration(implementation: FavoritesDestinationRegistration): DestinationRegistration
}
