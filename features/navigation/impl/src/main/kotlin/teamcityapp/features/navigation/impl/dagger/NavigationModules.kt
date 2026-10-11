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

package teamcityapp.features.navigation.impl.dagger

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import teamcityapp.features.navigation.api.NavigationNavigation
import teamcityapp.features.navigation.impl.navigation.NavigationNavigationImpl
import teamcityapp.features.navigation.impl.tracker.NavigationTracker
import teamcityapp.features.navigation.impl.tracker.NavigationTrackerImpl

@Module
@InstallIn(SingletonComponent::class)
abstract class NavigationModules {
    @Binds abstract fun navigation(implementation: NavigationNavigationImpl): NavigationNavigation

    @Binds abstract fun tracker(implementation: NavigationTrackerImpl): NavigationTracker
}
