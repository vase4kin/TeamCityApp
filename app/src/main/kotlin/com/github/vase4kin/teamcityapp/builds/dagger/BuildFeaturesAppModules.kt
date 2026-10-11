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

package com.github.vase4kin.teamcityapp.builds.dagger

import com.github.vase4kin.teamcityapp.artifact.data.AppArtifactsEvents
import com.github.vase4kin.teamcityapp.artifact.data.AppArtifactsRepository
import com.github.vase4kin.teamcityapp.artifact.router.AppArtifactsRouterImpl
import com.github.vase4kin.teamcityapp.buildlist.data.AppBuildHistoryFilterAdapter
import com.github.vase4kin.teamcityapp.buildlist.data.AppBuildHistoryOnboardingRepository
import com.github.vase4kin.teamcityapp.buildlist.data.AppBuildHistoryRepository
import com.github.vase4kin.teamcityapp.buildlist.router.AppBuildHistoryRouter
import com.github.vase4kin.teamcityapp.builds.data.AppBuildLaunchCodec
import com.github.vase4kin.teamcityapp.overview.data.AppBuildOverviewEvents
import com.github.vase4kin.teamcityapp.overview.data.AppBuildOverviewRepository
import com.github.vase4kin.teamcityapp.overview.router.AppBuildOverviewRouterImpl
import com.github.vase4kin.teamcityapp.queue.data.AppBuildQueueRepository
import com.github.vase4kin.teamcityapp.queue.router.BuildQueueAppRouterImpl
import com.github.vase4kin.teamcityapp.runningbuilds.data.AppRunningBuildsRepository
import com.github.vase4kin.teamcityapp.runningbuilds.router.RunningBuildsAppRouterImpl
import com.github.vase4kin.teamcityapp.snapshot_dependencies.data.AppSnapshotDependenciesRepository
import com.github.vase4kin.teamcityapp.snapshot_dependencies.router.AppSnapshotDependenciesRouter
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ActivityComponent
import dagger.hilt.android.components.ActivityRetainedComponent
import dagger.hilt.android.components.FragmentComponent
import dagger.hilt.android.scopes.ActivityRetainedScoped
import dagger.hilt.android.scopes.ActivityScoped
import dagger.hilt.android.scopes.FragmentScoped
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton
import teamcityapp.features.artifacts.api.ArtifactsAppRouter
import teamcityapp.features.artifacts.api.ArtifactsEvents
import teamcityapp.features.artifacts.api.ArtifactsRepository
import teamcityapp.features.build_history.api.BuildHistoryAppRouter
import teamcityapp.features.build_history.api.BuildHistoryFilterAdapter
import teamcityapp.features.build_history.api.BuildHistoryOnboardingRepository
import teamcityapp.features.build_history.api.BuildHistoryRepository
import teamcityapp.features.build_overview.api.BuildOverviewAppRouter
import teamcityapp.features.build_overview.api.BuildOverviewEvents
import teamcityapp.features.build_overview.api.BuildOverviewRepository
import teamcityapp.features.build_queue.api.BuildQueueAppRouter
import teamcityapp.features.build_queue.api.BuildQueueRepository
import teamcityapp.features.running_builds.api.RunningBuildsAppRouter
import teamcityapp.features.running_builds.api.RunningBuildsRepository
import teamcityapp.features.snapshot_dependencies.api.SnapshotDependenciesAppRouter
import teamcityapp.features.snapshot_dependencies.api.SnapshotDependenciesRepository
import teamcityapp.libraries.builds.compatibility.BuildLaunchCodec

@Module
@InstallIn(SingletonComponent::class)
abstract class BuildFeaturesDataModule {
    @Binds @Singleton
    abstract fun codec(implementation: AppBuildLaunchCodec): BuildLaunchCodec

    @Binds @Singleton
    abstract fun history(implementation: AppBuildHistoryRepository): BuildHistoryRepository

    @Binds @Singleton
    abstract fun historyFilter(implementation: AppBuildHistoryFilterAdapter): BuildHistoryFilterAdapter

    @Binds @Singleton
    abstract fun historyOnboarding(implementation: AppBuildHistoryOnboardingRepository): BuildHistoryOnboardingRepository

    @Binds @Singleton
    abstract fun historyRouter(implementation: AppBuildHistoryRouter): BuildHistoryAppRouter

    @Binds @Singleton
    abstract fun snapshots(implementation: AppSnapshotDependenciesRepository): SnapshotDependenciesRepository

    @Binds @Singleton
    abstract fun artifacts(implementation: AppArtifactsRepository): ArtifactsRepository

    @Binds @Singleton
    abstract fun artifactEvents(implementation: AppArtifactsEvents): ArtifactsEvents

    @Binds @Singleton
    abstract fun overview(implementation: AppBuildOverviewRepository): BuildOverviewRepository

    @Binds @Singleton
    abstract fun overviewEvents(implementation: AppBuildOverviewEvents): BuildOverviewEvents
}

@Module
@InstallIn(ActivityRetainedComponent::class)
abstract class HomeBuildsDataModule {
    @Binds @ActivityRetainedScoped
    abstract fun running(implementation: AppRunningBuildsRepository): RunningBuildsRepository

    @Binds @ActivityRetainedScoped
    abstract fun queue(implementation: AppBuildQueueRepository): BuildQueueRepository
}

@Module
@InstallIn(ActivityComponent::class)
abstract class BuildFeaturesActivityRouterModule {
    @Binds @ActivityScoped
    abstract fun artifacts(implementation: AppArtifactsRouterImpl): ArtifactsAppRouter

    @Binds @ActivityScoped
    abstract fun overview(implementation: AppBuildOverviewRouterImpl): BuildOverviewAppRouter
}

@Module
@InstallIn(FragmentComponent::class)
abstract class BuildFeaturesFragmentRouterModule {
    @Binds @FragmentScoped
    abstract fun running(implementation: RunningBuildsAppRouterImpl): RunningBuildsAppRouter

    @Binds @FragmentScoped
    abstract fun queue(implementation: BuildQueueAppRouterImpl): BuildQueueAppRouter

    @Binds @FragmentScoped
    abstract fun snapshots(implementation: AppSnapshotDependenciesRouter): SnapshotDependenciesAppRouter
}
