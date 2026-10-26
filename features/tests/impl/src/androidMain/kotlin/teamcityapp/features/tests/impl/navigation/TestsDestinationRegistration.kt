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

package teamcityapp.features.tests.impl.navigation

import android.os.Bundle
import androidx.compose.runtime.*
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import dagger.multibindings.IntoSet
import javax.inject.Inject
import teamcityapp.features.build_details.api.*
import teamcityapp.features.test_details.api.navigation.TestDetailsDestination
import teamcityapp.features.tests.api.navigation.TestsDestination
import teamcityapp.features.tests.impl.*
import teamcityapp.features.tests.impl.router.TestsRouter
import teamcityapp.libraries.navigation.*

class TestsDestinationRegistration @Inject constructor() : FeatureDestination<TestsDestination>(TestsDestination::class, TestsDestination.serializer()) {
    @Composable override fun Content(destination: TestsDestination, navigator: Navigator, entryId: String) {
        val host = LocalBuildDetailsSectionCountHost.current
        val arguments = remember(destination) {
            Bundle().apply {
                putString("url", destination.href)
                putInt("passedCount", destination.passed)
                putInt("failedCount", destination.failed)
                putInt("ignoredCount", destination.ignored)
            }
        }
        val router = remember(navigator, host) {
            object : TestsRouter {
                override fun openFailedTest(url: String) {
                    navigator.push(TestDetailsDestination(url))
                }
                override fun updateTabCount(count: Int) {
                    host?.updated(count)
                }
            }
        }
        DestinationArguments(arguments) { TestsRoute(router) }
    }
}

@Module
@InstallIn(SingletonComponent::class)
abstract class TestsDestinationModule {
    @Binds @IntoSet
    abstract fun registration(implementation: TestsDestinationRegistration): DestinationRegistration
}
