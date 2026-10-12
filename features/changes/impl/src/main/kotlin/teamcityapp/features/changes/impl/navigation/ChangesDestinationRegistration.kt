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

package teamcityapp.features.changes.impl.navigation

import android.os.Bundle
import androidx.compose.runtime.*
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import dagger.multibindings.IntoSet
import javax.inject.Inject
import teamcityapp.features.build_details.api.*
import teamcityapp.features.change_details.api.ChangeDetails
import teamcityapp.features.change_details.api.navigation.ChangeDetailsDestination
import teamcityapp.features.changes.api.navigation.ChangesDestination
import teamcityapp.features.changes.impl.*
import teamcityapp.features.changes.impl.router.ChangesRouter
import teamcityapp.libraries.navigation.*

class ChangesDestinationRegistration @Inject constructor() : FeatureDestination<ChangesDestination>(ChangesDestination::class, ChangesDestination.serializer()) {
    @Composable override fun Content(destination: ChangesDestination, navigator: Navigator, entryId: String) {
        val host = LocalBuildDetailsUiHost.current
        val arguments = remember(destination) { Bundle().apply { putString("url", destination.href) } }
        val router = remember(navigator, host) {
            object : ChangesRouter {
                override fun openChange(change: ChangeDetails) {
                    navigator.push(ChangeDetailsDestination(change))
                }
                override fun updateTabCount(count: Int) {
                    // The parent metadata owner publishes the shared count state.
                }
            }
        }
        val count = host?.countStates?.get(BuildDetailsTab.Changes)?.let { count ->
            when (count) {
                is BuildDetailsCount.Available -> ChangesCountState.Available(count.count)
                BuildDetailsCount.Loading -> ChangesCountState.Loading
                BuildDetailsCount.Unavailable -> ChangesCountState.Unavailable
            }
        }
        DestinationArguments(arguments) { ChangesRoute(router, parentCount = count, onParentCountRetry = host?.let { { it.retryCount(BuildDetailsTab.Changes) } }) }
    }
}

@Module
@InstallIn(SingletonComponent::class)
abstract class ChangesDestinationModule {
    @Binds @IntoSet
    abstract fun registration(implementation: ChangesDestinationRegistration): DestinationRegistration
}
