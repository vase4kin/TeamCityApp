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

package teamcityapp.shared

import teamcityapp.features.about.impl.data.TeamCityAboutRepository
import teamcityapp.features.agents.impl.data.TeamCityAgentsRepository
import teamcityapp.features.artifacts.api.ArtifactFileStore
import teamcityapp.features.artifacts.impl.data.TeamCityArtifactsRepository
import teamcityapp.features.build_details.impl.data.TeamCityBuildDetailsMetadataRepository
import teamcityapp.features.build_details.impl.data.TeamCityBuildDetailsRepository
import teamcityapp.features.build_history.impl.data.TeamCityBuildHistoryOnboardingRepository
import teamcityapp.features.build_history.impl.data.TeamCityBuildHistoryRepository
import teamcityapp.features.build_overview.impl.data.TeamCityBuildOverviewRepository
import teamcityapp.features.build_queue.impl.data.TeamCityBuildQueueRepository
import teamcityapp.features.changes.impl.data.TeamCityChangesRepository
import teamcityapp.features.favorites.impl.data.TeamCityFavoritesRepository
import teamcityapp.features.filter_bottom_sheet.impl.data.TeamCityQuickFilterRepository
import teamcityapp.features.filter_builds.impl.data.TeamCityFilterBuildsRepository
import teamcityapp.features.home.impl.data.TeamCityHomeRepository
import teamcityapp.features.navigation.impl.data.TeamCityNavigationRatingRepository
import teamcityapp.features.navigation.impl.data.TeamCityNavigationRepository
import teamcityapp.features.run_build.impl.data.TeamCityRunBuildRepository
import teamcityapp.features.running_builds.impl.data.TeamCityRunningBuildsRepository
import teamcityapp.features.snapshot_dependencies.impl.data.TeamCitySnapshotDependenciesRepository
import teamcityapp.features.test_details.impl.data.TeamCityTestDetailsRepository
import teamcityapp.features.tests.impl.data.TeamCityTestsRepository
import teamcityapp.libraries.shared_data.TeamCityAccountGraph

/** Composition only: each feature owns its repository, mapping, and preference policy. */
internal class SharedAccountFeatures(data: TeamCityAccountGraph, files: ArtifactFileStore, appLaunchId: String) {
    val account = data.account
    val navigation = TeamCityNavigationRepository(account, data.api)
    val history = TeamCityBuildHistoryRepository(account, data.api, data.preferences)
    val overview = TeamCityBuildOverviewRepository(account, data.api)
    val about = TeamCityAboutRepository(account, data.api)
    val quickFilters = TeamCityQuickFilterRepository(data.preferences)
    val home = TeamCityHomeRepository(account, data.api, data.preferences)
    val favorites = TeamCityFavoritesRepository(account, data.api, data.preferences)
    val running = TeamCityRunningBuildsRepository(account, data.api, data.preferences)
    val queue = TeamCityBuildQueueRepository(account, data.api, data.preferences)
    val agents = TeamCityAgentsRepository(account, data.api, data.preferences)
    val changes = TeamCityChangesRepository(account, data.api)
    val tests = TeamCityTestsRepository(account, data.api)
    val testDetails = TeamCityTestDetailsRepository(account, data.api)
    val snapshots = TeamCitySnapshotDependenciesRepository(account, data.api)
    val historyOnboarding = TeamCityBuildHistoryOnboardingRepository(account, data.preferences)
    val run = TeamCityRunBuildRepository(account, data.api)
    val filters = TeamCityFilterBuildsRepository(account, data.api)
    val details = TeamCityBuildDetailsRepository(account, data.api)
    val rating = TeamCityNavigationRatingRepository(account, data.preferences, appLaunchId)
    val metadata = TeamCityBuildDetailsMetadataRepository(account, data.preferences, changes, tests)
    val artifacts = TeamCityArtifactsRepository(account, data.api, files)
}
