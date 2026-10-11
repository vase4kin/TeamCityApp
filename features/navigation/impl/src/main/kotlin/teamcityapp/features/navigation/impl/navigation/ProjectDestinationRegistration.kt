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

package teamcityapp.features.navigation.impl.navigation

import android.os.Bundle
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
import teamcityapp.features.navigation.api.*
import teamcityapp.features.navigation.api.navigation.ProjectDestination
import teamcityapp.features.navigation.impl.NavigationRoute
import teamcityapp.features.navigation.impl.NavigationViewModel
import teamcityapp.features.navigation.impl.router.NavigationRouter
import teamcityapp.libraries.app_rating.AppRating
import teamcityapp.libraries.navigation.*

class ProjectDestinationRegistration @Inject constructor(private val rating: AppRating) : FeatureDestination<ProjectDestination>(ProjectDestination::class, ProjectDestination.serializer()) {
    @Composable override fun Content(destination: ProjectDestination, navigator: Navigator, entryId: String) {
        val activity = checkNotNull(LocalActivity.current)
        val arguments = remember(destination) {
            Bundle().apply {
                putString(NavigationNavigation.PROJECT_ID, destination.projectId)
                putString(NavigationNavigation.PROJECT_NAME, destination.projectName)
                putBoolean(NavigationViewModel.ROOT_SCREEN, destination.root)
            }
        }
        val router = remember(activity, navigator, entryId) {
            object : NavigationRouter {
                override fun navigateUp() {
                    if (destination.root) {
                        navigator.push(DrawerDestination)
                    } else if (!navigator.pop()) {
                        activity.finish()
                    }
                }
                override fun open(entry: teamcityapp.features.navigation.api.NavigationEntry) {
                    when (entry) {
                        is teamcityapp.features.navigation.api.NavigationEntry.Project -> navigator.push(ProjectDestination(entry.reference.id, entry.reference.name, false))

                        is teamcityapp.features.navigation.api.NavigationEntry.Configuration -> {
                            navigator.push(BuildHistoryDestination(entry.configuration.id, entry.configuration.name))
                        }
                    }
                }
                override fun openRating() = rating.open(activity)
            }
        }
        DestinationArguments(arguments) { NavigationRoute(router) }
    }
}

@Module
@InstallIn(SingletonComponent::class)
abstract class ProjectDestinationModule {
    @Binds @IntoSet
    abstract fun registration(implementation: ProjectDestinationRegistration): DestinationRegistration
}
