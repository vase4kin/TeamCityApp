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

import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.compose.*
import androidx.lifecycle.repeatOnLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.serialization.json.Json
import kotlinx.serialization.modules.*
import org.jetbrains.compose.resources.stringResource
import teamcityapp.features.bottom_sheet.api.*
import teamcityapp.features.bottom_sheet.api.navigation.*
import teamcityapp.features.bottom_sheet.impl.*
import teamcityapp.features.build_details.api.*
import teamcityapp.features.build_details.impl.*
import teamcityapp.features.build_details.impl.tracker.BuildDetailsTracker
import teamcityapp.features.build_history.api.navigation.BuildHistoryDestination
import teamcityapp.features.build_overview.api.BuildOverviewRepository
import teamcityapp.features.build_overview.impl.*
import teamcityapp.features.filter_bottom_sheet.api.navigation.*
import teamcityapp.features.filter_bottom_sheet.impl.*
import teamcityapp.features.filter_bottom_sheet.impl.router.FilterBottomSheetRouter
import teamcityapp.features.filter_builds.api.*
import teamcityapp.features.filter_builds.api.navigation.*
import teamcityapp.features.filter_builds.impl.*
import teamcityapp.features.filter_builds.impl.router.FilterBuildsRouter
import teamcityapp.features.navigation.api.navigation.ProjectDestination
import teamcityapp.features.run_build.api.*
import teamcityapp.features.run_build.api.navigation.*
import teamcityapp.features.run_build.impl.*
import teamcityapp.features.run_build.impl.router.RunBuildRouter
import teamcityapp.libraries.navigation.*
import teamcityapp.libraries.shared_data.TeamCityAccountGraph
import teamcityapp.libraries.theme.LocalFloatingActionClearance

private object NativeRunTracker : RunBuildTracker {
    override fun trackView() {}
    override fun trackUserRunBuildSuccess() {}
    override fun trackUserRunBuildWithCustomParamsSuccess() {}
    override fun trackUserRunBuildFailed() {}
    override fun trackUserRunBuildFailedForbidden() {}
    override fun trackUserClicksOnAddNewBuildParamButton() {}
    override fun trackUserClicksOnClearAllBuildParamsButton() {}
    override fun trackUserAddsBuildParam() {}
}
private object NativeFilterTracker : FilterBuildsTracker {
    override fun trackView() {}
    override fun trackUserFilteredBuilds() {}
}
private object NativeDetailsTracker : BuildDetailsTracker {
    override fun viewed() {}
    override fun confirmedCancel(reAdd: Boolean) {}
    override fun cancelSucceeded() {}
    override fun cancelFailed(forbidden: Boolean) {}
    override fun restartSucceeded() {}
    override fun restartFailed(forbidden: Boolean) {}
    override fun queuedOpened() {}
    override fun queuedFailed() {}
}

internal fun nativeManagementEntries(graph: TeamCityAccountGraph?): Set<DestinationRegistration> {
    if (graph == null) return emptySet()
    val repositories = graph.management
    return setOf(
        SharedEntry(RunBuildDestination::class, RunBuildDestination.serializer(), { builder ->
            builder.polymorphic(NavigationResult::class) { subclass(QueuedBuildResult::class, QueuedBuildResult.serializer()) }
        }) { dest, nav, _ ->
            val vm = viewModel { RunBuildViewModel(repositories.run, SavedStateHandle(mapOf(BUILD_TYPE_ID to dest.configurationId)), NativeRunTracker) }
            val router = remember(dest, nav) {
                object : RunBuildRouter {
                    override fun close() {
                        nav.pop()
                    }
                    override fun queued(href: String) {
                        if (!nav.finish(QueuedBuildResult(href), dest.recipientId.orEmpty())) nav.pop()
                    }
                }
            }
            SharedRunBuildRoute(router, vm)
        },
        SharedEntry(FilterBuildsDestination::class, FilterBuildsDestination.serializer(), { builder ->
            builder.polymorphic(NavigationResult::class) { subclass(BuildFilterResult::class, BuildFilterResult.serializer()) }
        }) { dest, nav, _ ->
            val vm = viewModel { FilterBuildsViewModel(repositories.filters, SavedStateHandle(mapOf(BUILD_TYPE_ID to dest.configurationId)), NativeFilterTracker) }
            val router = remember(dest, nav) {
                object : FilterBuildsRouter {
                    override fun close() {
                        nav.pop()
                    }
                    override fun apply(filter: BuildFilter) {
                        if (!nav.finish(BuildFilterResult(filter), dest.recipientId.orEmpty())) nav.pop()
                    }
                }
            }
            SharedFilterBuildsRoute(router, vm)
        },
        SharedEntry(QuickFilterDestination::class, QuickFilterDestination.serializer(), presentation = DestinationPresentation.BottomSheet) { dest, nav, _ ->
            val vm = viewModel { FilterBottomSheetViewModel(checkNotNull(graph.browsing).filters, SavedStateHandle(mapOf("arg_code" to dest.code))) }
            val router = remember(nav) {
                object : FilterBottomSheetRouter {
                    override fun close() {
                        nav.pop()
                    }
                }
            }
            SharedFilterBottomSheetRoute(router, vm)
        },
        SharedEntry(ActionSheetDestination::class, ActionSheetDestination.serializer(), { builder ->
            builder.polymorphic(NavigationResult::class) { subclass(ActionSheetResult::class, ActionSheetResult.serializer()) }
        }, DestinationPresentation.BottomSheet) { dest, nav, _ ->
            // Explicit clipboard/export integrations retain Phase 5 ownership.
            val items = sheetItems(dest.menu, dest.descriptions).filter { it.action in setOf(SheetAction.Branch, SheetAction.BuildType, SheetAction.Project) }
            BottomSheetScreen(BottomSheetUiState(dest.title, items)) { item -> if (!nav.finish(ActionSheetResult(dest.consumerId, item), dest.recipientId)) nav.pop() }
        },
        SharedEntry(BuildDetailsDestination::class, BuildDetailsDestination.serializer()) { dest, nav, id -> NativeManagedBuild(dest, graph, nav, id) }
    )
}

@Composable
private fun NativeManagedBuild(destination: BuildDetailsDestination, graph: TeamCityAccountGraph, nav: Navigator, entryId: String) {
    val model = viewModel { BuildDetailsViewModel(SavedStateHandle(mapOf(BuildDetailsViewModel.BUILD to Json.encodeToString(destination.build), BuildDetailsViewModel.NAME to destination.configurationName, BuildDetailsViewModel.COMPLETION to destination.completion?.name)), graph.management.details, NativeDetailsTracker) }
    val state by model.state.collectAsStateWithLifecycle()
    val overview = viewModel {
        BuildReferenceViewModel(
            destination.build.href,
            object : BuildOverviewRepository {
                override suspend fun build(href: String, forceRefresh: Boolean) = graph.build(href, forceRefresh || destination.forceOverviewRefresh)
            },
            checkNotNull(graph.browsing).metadata
        )
    }
    val content by overview.state.collectAsStateWithLifecycle()
    LaunchedEffect(content.build) { content.build?.let(model::loaded) }
    LaunchedEffect(state.overviewRefresh) { if (state.overviewRefresh > 0) overview.refresh() }
    val snackbar = remember { SnackbarHostState() }
    val lifecycle = LocalLifecycleOwner.current.lifecycle
    val message = state.notice?.let { stringResource(it.message) }
    val action = state.notice?.actionLabel?.let { stringResource(it) }
    LaunchedEffect(state.notice?.id, lifecycle) {
        val notice = state.notice ?: return@LaunchedEffect
        lifecycle.repeatOnLifecycle(Lifecycle.State.RESUMED) {
            val result = snackbar.showSnackbar(checkNotNull(message), action, duration = SnackbarDuration.Long)
            model.consumeNotice(notice.id)
            if (result == SnackbarResult.ActionPerformed && notice.openQueued) model.queuedBuildReference()?.let { nav.push(BuildDetailsReferenceDestination(it, state.subtitle, fromRestart = true)) }
        }
    }
    LifecycleResumeEffect(state.replacement, model, nav.snapshot.results) {
        state.replacement?.takeIf { nav.current.id == entryId }?.let {
            model.consumeReplacement(it.id)
            nav.replace(BuildDetailsDestination(it.build, state.subtitle, forceOverviewRefresh = true, completion = it.completion))
        }
        while (true) {
            val result = nav.consumeResult(entryId, BuildDetailsSectionCountResult::class) ?: break
            if (result.buildId == destination.build.id) overview.updateCount(result.section, result.count)
        }
        while (true) {
            val result = nav.consumeResult(entryId, ActionSheetResult::class) ?: break
            if (result.consumerId == "overview" && result.item.action == SheetAction.Branch) {
                destination.build.buildTypeId?.let { nav.push(BuildHistoryDestination(it, state.subtitle, BuildFilter(branch = result.item.description).toTeamCityLocator())) }
            }
        }
        onPauseOrDispose {}
    }
    BuildDetailsScreen(state.copy(platformActionsAvailable = false), { nav.pop() }, { command ->
        when (command) {
            BuildDetailsCommand.Cancel -> model.requestCancellation()
            BuildDetailsCommand.Restart -> model.requestRestart()
            else -> Unit
        }
    }, model::confirm, model::dismissConfirmation, snackbar) {
        SharedBuildOverviewScreen(
            content.copy(actions = emptyList(), sections = content.sections.filter { it.section != BuildDetailsSection.Artifacts }),
            overview::refresh,
            overview::retry,
            { row ->
                if (!state.busy) {
                    when (row.action) {
                        OverviewRowAction.Configuration -> state.current.buildTypeId?.let { nav.push(BuildHistoryDestination(it, state.subtitle)) }
                        OverviewRowAction.Project -> state.current.configuration?.projectId?.let { nav.push(ProjectDestination(it, state.current.configuration?.projectName.orEmpty())) }
                        OverviewRowAction.Branch -> (row.value as? OverviewText.Literal)?.let { nav.push(ActionSheetDestination(it.value, listOf(it.value), SheetMenuType.Branch, entryId, "overview")) }
                        else -> Unit
                    }
                }
            },
            onSection = { section -> if (!state.busy) nav.push(BuildDetailsSectionDestination(destination.build, section, state.subtitle, originEntryId = entryId)) },
            onCountRetry = overview::retryCount,
            bottomContentPadding = LocalFloatingActionClearance.current,
            rowActionAvailable = { !state.busy && it.action != OverviewRowAction.Copy }
        )
    }
}
