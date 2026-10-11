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

package teamcityapp.features.test_details.impl.navigation

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
import teamcityapp.features.test_details.api.navigation.*
import teamcityapp.features.test_details.impl.TestDetailsRoute
import teamcityapp.features.test_details.impl.router.TestDetailsRouter
import teamcityapp.libraries.navigation.*

class TestDetailsDestinationRegistration @Inject constructor() : FeatureDestination<TestDetailsDestination>(TestDetailsDestination::class, TestDetailsDestination.serializer()) {

    @Composable override fun Content(destination: TestDetailsDestination, navigator: Navigator, entryId: String) {
        val activity = checkNotNull(LocalActivity.current)
        val arguments = remember(destination) { Bundle().apply { putString(teamcityapp.features.test_details.impl.TestDetailsViewModel.ARG_TEST_URL, destination.url) } }
        val router = remember(activity, navigator, entryId) {
            teamcityapp.features.test_details.impl.router.TestDetailsRouterImpl(activity) {
                if (!navigator.pop()) activity.finish()
            }
        }
        DestinationArguments(arguments) { TestDetailsRoute(router) }
    }
}

@Module
@InstallIn(SingletonComponent::class)
abstract class TestDetailsDestinationModule {
    @Binds @IntoSet
    abstract fun registration(implementation: TestDetailsDestinationRegistration): DestinationRegistration
}
