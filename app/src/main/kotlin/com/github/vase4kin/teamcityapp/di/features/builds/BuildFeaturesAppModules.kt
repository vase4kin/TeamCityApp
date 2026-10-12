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

package com.github.vase4kin.teamcityapp.di.features.builds

import com.github.vase4kin.teamcityapp.integration.features.queue.data.AppBuildQueueRepository
import com.github.vase4kin.teamcityapp.integration.features.runningbuilds.data.AppRunningBuildsRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ActivityRetainedComponent
import dagger.hilt.android.scopes.ActivityRetainedScoped
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton
import teamcityapp.features.artifacts.api.ArtifactsRepository
import teamcityapp.features.artifacts.impl.data.DefaultArtifactsRepository
import teamcityapp.features.build_history.api.BuildHistoryFilterAdapter
import teamcityapp.features.build_history.api.BuildHistoryOnboardingRepository
import teamcityapp.features.build_history.api.BuildHistoryRepository
import teamcityapp.features.build_history.impl.data.DefaultBuildHistoryOnboardingRepository
import teamcityapp.features.build_history.impl.data.DefaultBuildHistoryRepository
import teamcityapp.features.build_history.impl.data.LegacyBuildHistoryFilterAdapter
import teamcityapp.features.build_overview.api.BuildOverviewRepository
import teamcityapp.features.build_overview.impl.data.DefaultBuildOverviewRepository
import teamcityapp.features.build_queue.api.BuildQueueRepository
import teamcityapp.features.running_builds.api.RunningBuildsRepository
import teamcityapp.features.snapshot_dependencies.api.SnapshotDependenciesRepository
import teamcityapp.features.snapshot_dependencies.impl.data.DefaultSnapshotDependenciesRepository
import teamcityapp.libraries.builds.compatibility.BuildLaunchCodec
import teamcityapp.libraries.data.builds.LegacyBuildLaunchCodec

@Module
@InstallIn(SingletonComponent::class)
abstract class BuildFeaturesDataModule {
    @Binds @Singleton
    abstract fun codec(implementation: LegacyBuildLaunchCodec): BuildLaunchCodec

    @Binds @Singleton
    abstract fun history(implementation: DefaultBuildHistoryRepository): BuildHistoryRepository

    @Binds @Singleton
    abstract fun historyFilter(implementation: LegacyBuildHistoryFilterAdapter): BuildHistoryFilterAdapter

    @Binds @Singleton
    abstract fun historyOnboarding(implementation: DefaultBuildHistoryOnboardingRepository): BuildHistoryOnboardingRepository

    @Binds @Singleton
    abstract fun snapshots(implementation: DefaultSnapshotDependenciesRepository): SnapshotDependenciesRepository

    @Binds @Singleton
    abstract fun artifacts(implementation: DefaultArtifactsRepository): ArtifactsRepository

    @Binds @Singleton
    abstract fun overview(implementation: DefaultBuildOverviewRepository): BuildOverviewRepository
}

@Module
@InstallIn(ActivityRetainedComponent::class)
abstract class HomeBuildsDataModule {
    @Binds @ActivityRetainedScoped
    abstract fun running(implementation: AppRunningBuildsRepository): RunningBuildsRepository

    @Binds @ActivityRetainedScoped
    abstract fun queue(implementation: AppBuildQueueRepository): BuildQueueRepository
}
