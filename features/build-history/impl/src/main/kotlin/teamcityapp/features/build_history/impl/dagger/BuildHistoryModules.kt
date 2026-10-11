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

package teamcityapp.features.build_history.impl.dagger

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import teamcityapp.features.build_history.api.BuildHistoryNavigation
import teamcityapp.features.build_history.impl.navigation.BuildHistoryNavigationImpl
import teamcityapp.features.build_history.impl.tracker.BuildHistoryTracker
import teamcityapp.features.build_history.impl.tracker.BuildHistoryTrackerImpl

@Module
@InstallIn(SingletonComponent::class)
abstract class BuildHistoryModules {
    @Binds abstract fun navigation(implementation: BuildHistoryNavigationImpl): BuildHistoryNavigation

    @Binds abstract fun tracker(implementation: BuildHistoryTrackerImpl): BuildHistoryTracker
}
