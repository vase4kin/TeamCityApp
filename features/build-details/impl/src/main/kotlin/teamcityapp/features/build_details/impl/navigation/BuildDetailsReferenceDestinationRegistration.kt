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

package teamcityapp.features.build_details.impl.navigation

import android.os.Bundle
import androidx.activity.compose.LocalActivity
import androidx.compose.runtime.*
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.*
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import dagger.multibindings.IntoSet
import javax.inject.Inject
import teamcityapp.features.build_details.api.*
import teamcityapp.features.build_details.impl.*
import teamcityapp.libraries.navigation.*

class BuildDetailsReferenceDestinationRegistration @Inject constructor() : FeatureDestination<BuildDetailsReferenceDestination>(BuildDetailsReferenceDestination::class, BuildDetailsReferenceDestination.serializer()) {
    @Composable override fun Content(destination: BuildDetailsReferenceDestination, navigator: Navigator, entryId: String) {
        val activity = checkNotNull(LocalActivity.current)
        val arguments = remember(destination) {
            Bundle().apply {
                putString(BuildDetailsReferenceViewModel.HREF, destination.href)
                putBoolean(BuildDetailsReferenceViewModel.TRACK_RESTART, destination.fromRestart)
            }
        }
        DestinationArguments(arguments) {
            val model = hiltViewModel<BuildDetailsReferenceViewModel>()
            val state by model.state.collectAsStateWithLifecycle(minActiveState = Lifecycle.State.RESUMED)
            LifecycleResumeEffect(state) {
                (state as? BuildDetailsReferenceState.Content)?.takeIf { navigator.current.id == entryId }?.let {
                    navigator.replace(BuildDetailsDestination(it.build, destination.configurationName))
                }
                onPauseOrDispose {}
            }
            BuildDetailsReferenceScreen(state, { if (!navigator.pop()) activity.finish() }, model::retry)
        }
    }
}

@Module
@InstallIn(SingletonComponent::class)
abstract class BuildDetailsReferenceDestinationModule {
    @Binds @IntoSet
    abstract fun registration(implementation: BuildDetailsReferenceDestinationRegistration): DestinationRegistration
}
