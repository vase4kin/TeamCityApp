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

package teamcityapp.features.snapshot_dependencies.impl.dagger

import androidx.fragment.app.Fragment
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.FragmentComponent
import dagger.hilt.components.SingletonComponent
import teamcityapp.features.snapshot_dependencies.api.SnapshotDependenciesAppRouter
import teamcityapp.features.snapshot_dependencies.api.SnapshotDependenciesNavigation
import teamcityapp.features.snapshot_dependencies.impl.SnapshotDependenciesFragment
import teamcityapp.features.snapshot_dependencies.impl.navigation.SnapshotDependenciesNavigationImpl
import teamcityapp.features.snapshot_dependencies.impl.router.SnapshotDependenciesRouter
import teamcityapp.features.snapshot_dependencies.impl.router.SnapshotDependenciesRouterImpl
import teamcityapp.features.snapshot_dependencies.impl.tracker.SnapshotDependenciesTracker
import teamcityapp.features.snapshot_dependencies.impl.tracker.SnapshotDependenciesTrackerImpl
import teamcityapp.libraries.utils.requireScreenOwner

@Module
@InstallIn(SingletonComponent::class)
abstract class SnapshotDependenciesNavigationModule {
    @Binds abstract fun navigation(implementation: SnapshotDependenciesNavigationImpl): SnapshotDependenciesNavigation

    @Binds abstract fun tracker(implementation: SnapshotDependenciesTrackerImpl): SnapshotDependenciesTracker
}

@Module
@InstallIn(FragmentComponent::class)
object SnapshotDependenciesRouterModule {
    @Provides fun router(owner: Fragment, appRouter: SnapshotDependenciesAppRouter): SnapshotDependenciesRouter {
        owner.requireScreenOwner<SnapshotDependenciesFragment>()
        return SnapshotDependenciesRouterImpl(appRouter)
    }
}
