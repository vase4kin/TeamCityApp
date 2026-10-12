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

package teamcityapp.features.snapshot_dependencies.impl.navigation

import android.os.Bundle
import androidx.compose.runtime.*
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import dagger.multibindings.IntoSet
import javax.inject.Inject
import teamcityapp.features.build_details.api.BuildDetailsDestination
import teamcityapp.features.build_history.api.navigation.BuildHistoryDestination
import teamcityapp.features.snapshot_dependencies.api.navigation.SnapshotDependenciesDestination
import teamcityapp.features.snapshot_dependencies.impl.*
import teamcityapp.features.snapshot_dependencies.impl.router.SnapshotDependenciesRouter
import teamcityapp.features.snapshot_dependencies.impl.tracker.SnapshotDependenciesTracker
import teamcityapp.libraries.builds.BuildLaunchData
import teamcityapp.libraries.list_ui.ConfigurationRecreationEffect
import teamcityapp.libraries.navigation.*

class SnapshotDependenciesDestinationRegistration @Inject constructor(private val tracker: SnapshotDependenciesTracker) : FeatureDestination<SnapshotDependenciesDestination>(SnapshotDependenciesDestination::class, SnapshotDependenciesDestination.serializer()) {
    @Composable override fun Content(destination: SnapshotDependenciesDestination, navigator: Navigator, entryId: String) {
        val arguments = remember(destination) {
            Bundle().apply {
                putString("id", destination.buildId)
                putString("name", destination.configurationName)
            }
        }
        val router = remember(navigator) {
            object : SnapshotDependenciesRouter {
                override fun openBuild(build: BuildLaunchData, buildTypeName: String?) {
                    navigator.push(BuildDetailsDestination(build, buildTypeName))
                }
                override fun openBuildHistory(configurationId: String, configurationName: String) {
                    navigator.push(BuildHistoryDestination(configurationId, configurationName))
                }
            }
        }
        DestinationArguments(arguments) {
            val model = hiltViewModel<SnapshotDependenciesViewModel>()
            ConfigurationRecreationEffect(model) { model.onConfigurationRecreation(true) }
            SnapshotDependenciesRoute(router, tracker, model)
        }
    }
}

@Module
@InstallIn(SingletonComponent::class)
abstract class SnapshotDependenciesDestinationModule {
    @Binds @IntoSet
    abstract fun registration(implementation: SnapshotDependenciesDestinationRegistration): DestinationRegistration
}
