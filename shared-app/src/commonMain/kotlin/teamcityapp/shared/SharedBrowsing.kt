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

import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.lifecycle.compose.LifecycleResumeEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.serialization.modules.polymorphic
import kotlinx.serialization.modules.subclass
import teamcityapp.features.agents.api.navigation.AgentsDestination
import teamcityapp.features.agents.impl.*
import teamcityapp.features.build_details.api.*
import teamcityapp.features.build_history.api.navigation.BuildHistoryDestination
import teamcityapp.features.build_queue.api.navigation.BuildQueueDestination
import teamcityapp.features.build_queue.impl.*
import teamcityapp.features.change_details.api.navigation.ChangeDetailsDestination
import teamcityapp.features.change_details.impl.*
import teamcityapp.features.change_details.impl.tracker.ChangeDetailsTrackerImpl
import teamcityapp.features.changes.impl.*
import teamcityapp.features.favorites.api.LocalFavoritesProjectsAction
import teamcityapp.features.favorites.api.navigation.FavoritesDestination
import teamcityapp.features.favorites.impl.*
import teamcityapp.features.favorites.impl.tracker.FavoritesTrackerImpl
import teamcityapp.features.filter_bottom_sheet.impl.tracker.QuickFilterTrackerImpl
import teamcityapp.features.home.api.*
import teamcityapp.features.home.impl.*
import teamcityapp.features.home.impl.tracker.HomeTrackerImpl
import teamcityapp.features.navigation.api.navigation.ProjectDestination
import teamcityapp.features.properties.impl.*
import teamcityapp.features.running_builds.api.navigation.RunningBuildsDestination
import teamcityapp.features.running_builds.impl.*
import teamcityapp.features.snapshot_dependencies.impl.*
import teamcityapp.features.snapshot_dependencies.impl.tracker.SnapshotDependenciesTrackerImpl
import teamcityapp.features.test_details.api.navigation.TestDetailsDestination
import teamcityapp.features.test_details.impl.*
import teamcityapp.features.test_details.impl.tracker.TestDetailsTrackerImpl
import teamcityapp.features.tests.impl.*
import teamcityapp.libraries.analytics.AnalyticsTracker
import teamcityapp.libraries.navigation.*
import teamcityapp.libraries.shared_data.TeamCityAccountGraph

@Composable
internal fun SharedHomeContent(destination: HomeDestination, graph: TeamCityAccountGraph, navigator: Navigator, registry: DestinationRegistry, entryId: String) {
    val repositories = checkNotNull(graph.browsing)
    val analytics = LocalNativePlatform.current.analytics
    val journey = LocalSharedJourneyMemory.current
    val restored = remember(entryId) { journey.homes[entryId] }
    val model = viewModel { HomeViewModel(destinationState(mapOf(HomeViewModel.SELECTED_TAB to (restored?.selected ?: destination.tab).ordinal, HomeViewModel.SELECTED_ACTIVITY_TAB to (restored?.activity ?: HomeTab.Running).ordinal)), repositories.home, HomeTrackerImpl(analytics, QuickFilterTrackerImpl(analytics)), repositories.filters) }
    LifecycleResumeEffect(navigator.snapshot.results, model) {
        while (true) {
            val selection = navigator.consumeResult(entryId, HomeTabSelection::class) ?: break
            model.select(selection.tab)
        }
        onPauseOrDispose {}
    }
    val state by model.state.collectAsStateWithLifecycle()
    val children = remember(entryId) {
        listOf(
            NavigationEntry("$entryId/Projects", ProjectDestination()),
            NavigationEntry("$entryId/Favorites", FavoritesDestination),
            NavigationEntry("$entryId/Running", RunningBuildsDestination),
            NavigationEntry("$entryId/Queue", BuildQueueDestination),
            NavigationEntry("$entryId/Agents", AgentsDestination)
        )
    }
    val stacks = children.associate { child ->
        child.id to key(child.id) {
            val sessionKey = "${navigator.snapshot.sessionKey}/${child.id}"
            val snapshot = remember(child.id) {
                runCatching {
                    restored?.stacks?.get(child.id)?.let(registry::decodeSnapshot)?.takeIf { it.sessionKey == sessionKey && it.entries.size <= 100 && it.entries.first().destination == child.destination }
                }.getOrNull()
            }
            rememberNavigator(child.destination, registry, sessionKey, parent = navigator, useParent = { it == AccountsDestination }, restoredSnapshot = snapshot)
        }
    }
    var activity by rememberSaveable { mutableStateOf(restored?.activity ?: HomeTab.Running) }
    val snapshots = stacks.mapValues { registry.encodeSnapshot(it.value.snapshot) }
    LaunchedEffect(state.selected, snapshots) {
        if (state.isActivity) activity = state.selected
        journey.homes = journey.homes + (entryId to SavedSharedHome(state.selected, activity, snapshots))
    }
    val selectedStack = stacks.getValue("$entryId/${state.selected.name}")
    CompositionLocalProvider(LocalFavoritesProjectsAction provides model::addProjects, LocalSharedEducationVisible provides (state.prompt != null)) {
        SharedHomeRoute(model, state, stacks, registry, entryId, { selectedStack.popToRoot() }, { navigator.push(AccountsDestination) })
    }
}

internal fun sharedBrowsingEntries(graph: TeamCityAccountGraph?, analytics: AnalyticsTracker): Set<DestinationRegistration> {
    if (graph == null) return emptySet()
    val repositories = checkNotNull(graph.browsing)
    return setOf(
        SharedEntry(FavoritesDestination::class, FavoritesDestination.serializer()) { _, nav, _ ->
            val vm = viewModel { FavoritesViewModel(repositories.favorites) }
            SharedFavoritesRoute(
                vm,
                FavoritesTrackerImpl(analytics),
                { nav.push(AccountsDestination) },
                { nav.push(ProjectDestination(it.id, it.name)) },
                { nav.push(BuildHistoryDestination(it.id, it.name)) },
                LocalFavoritesProjectsAction.current ?: {}
            )
        },
        SharedEntry(RunningBuildsDestination::class, RunningBuildsDestination.serializer()) { _, nav, entryId ->
            val vm = viewModel { RunningBuildsViewModel(repositories.running) }
            SharedRunningBuildsRoute(vm, { nav.push(AccountsDestination) }, { nav.openBuildSnapshot(entryId, it) }, { id, name -> nav.push(BuildHistoryDestination(id, name)) })
        },
        SharedEntry(BuildQueueDestination::class, BuildQueueDestination.serializer()) { _, nav, entryId ->
            val vm = viewModel { BuildQueueViewModel(repositories.queue) }
            SharedBuildQueueRoute(vm, { nav.push(AccountsDestination) }, { nav.openBuildSnapshot(entryId, it) }, { id, name -> nav.push(BuildHistoryDestination(id, name)) })
        },
        SharedEntry(AgentsDestination::class, AgentsDestination.serializer()) { _, nav, _ ->
            val vm = viewModel { AgentsViewModel(repositories.agents) }
            SharedAgentsRoute(vm, { nav.push(AccountsDestination) })
        },
        SharedEntry(BuildDetailsSectionDestination::class, BuildDetailsSectionDestination.serializer(), additionalSerializers = { builder ->
            builder.polymorphic(NavigationResult::class) { subclass(BuildDetailsSectionCountResult::class, BuildDetailsSectionCountResult.serializer()) }
        }) { dest, nav, entryId ->
            var count by rememberSaveable { mutableStateOf<Int?>(null) }
            val close = {
                val updated = count
                val origin = dest.originEntryId
                if (updated == null || origin == null || !nav.finish(BuildDetailsSectionCountResult(dest.build.id, dest.section, updated), origin)) nav.pop()
                Unit
            }
            SharedSectionBackHandler(count != null && dest.originEntryId != null && nav.current.id == entryId, close)
            teamcityapp.features.build_overview.impl.SharedBuildSectionScreen(dest.section, close) { modifier ->
                Box(modifier) {
                    when (dest.section) {
                        BuildDetailsSection.Changes -> {
                            val vm = viewModel { ChangesViewModel(destinationState(mapOf(ChangesViewModel.ARG_URL to checkNotNull(dest.build.changes?.href))), repositories.changes) }
                            SharedChangesContent(vm, { nav.push(ChangeDetailsDestination(it)) }, onCount = { count = it })
                        }

                        BuildDetailsSection.Tests -> {
                            val tests = checkNotNull(dest.build.tests)
                            val vm = viewModel { TestsViewModel(destinationState(mapOf(TestsViewModel.ARG_URL to checkNotNull(tests.href), TestsViewModel.ARG_PASSED to tests.passed, TestsViewModel.ARG_FAILED to tests.failed, TestsViewModel.ARG_IGNORED to tests.ignored)), repositories.tests) }
                            SharedTestsContent(vm, { nav.push(TestDetailsDestination(it)) }, onCount = { count = it })
                        }

                        BuildDetailsSection.Parameters -> {
                            val vm = viewModel { PropertiesViewModel(destinationState(mapOf(PropertiesViewModel.ARG_NAMES to ArrayList(dest.build.properties?.items.orEmpty().map { it.name.orEmpty() }), PropertiesViewModel.ARG_VALUES to ArrayList(dest.build.properties?.items.orEmpty().map { it.value.orEmpty() })))) }
                            SharedPropertiesRoute(vm, LocalNativePlatform.current::copy)
                        }

                        BuildDetailsSection.Snapshots -> {
                            val vm = viewModel { SnapshotDependenciesViewModel(repositories.snapshots, destinationState(mapOf(SnapshotDependenciesViewModel.BUILD_ID to dest.build.id, SnapshotDependenciesViewModel.BUILD_TYPE_NAME to dest.configurationName.orEmpty()))) }
                            SharedSnapshotDependenciesRoute(vm, SnapshotDependenciesTrackerImpl(analytics), { nav.openBuildSnapshot(entryId, it) }, { id, name -> nav.push(BuildHistoryDestination(id, name)) })
                        }

                        else -> Unit
                    }
                }
            }
        },
        SharedEntry(ChangeDetailsDestination::class, ChangeDetailsDestination.serializer()) { dest, nav, _ ->
            val vm = viewModel { ChangeDetailsViewModel(destinationState(dest.details?.savedArguments().orEmpty()), ChangeDetailsTrackerImpl(analytics)) }
            val platform = LocalNativePlatform.current
            SharedChangeDetailsRoute(vm, { nav.pop() }, { platform.serverBrowser(graph.account.identity.serverUrl, it) })
        },
        SharedEntry(TestDetailsDestination::class, TestDetailsDestination.serializer()) { dest, nav, _ ->
            val vm = viewModel { TestDetailsViewModel(destinationState(mapOf(TestDetailsViewModel.ARG_TEST_URL to dest.url)), repositories.testDetails, TestDetailsTrackerImpl(analytics)) }
            SharedTestDetailsRoute(vm, { nav.pop() }, LocalNativePlatform.current::copy)
        }
    )
}

/** The system back event returns metadata to the same live originating build visit as Up. */
@OptIn(androidx.compose.ui.ExperimentalComposeUiApi::class)
@Suppress("DEPRECATION")
@Composable
private fun SharedSectionBackHandler(enabled: Boolean, onBack: () -> Unit) {
    androidx.compose.ui.backhandler.BackHandler(enabled, onBack)
}
