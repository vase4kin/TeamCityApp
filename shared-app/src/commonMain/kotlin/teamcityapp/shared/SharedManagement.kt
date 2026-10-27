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
import teamcityapp.features.build_details.impl.tracker.BuildDetailsTrackerImpl
import teamcityapp.features.build_history.api.navigation.BuildHistoryDestination
import teamcityapp.features.build_overview.api.BuildOverviewAction
import teamcityapp.features.build_overview.api.BuildOverviewRepository
import teamcityapp.features.build_overview.api.BuildReferenceTracker
import teamcityapp.features.build_overview.impl.*
import teamcityapp.features.build_overview.impl.tracker.BuildOverviewTrackerImpl
import teamcityapp.features.filter_bottom_sheet.api.navigation.*
import teamcityapp.features.filter_bottom_sheet.impl.*
import teamcityapp.features.filter_bottom_sheet.impl.router.FilterBottomSheetRouter
import teamcityapp.features.filter_bottom_sheet.impl.tracker.QuickFilterTrackerImpl
import teamcityapp.features.filter_builds.api.*
import teamcityapp.features.filter_builds.api.navigation.*
import teamcityapp.features.filter_builds.impl.*
import teamcityapp.features.filter_builds.impl.router.FilterBuildsRouter
import teamcityapp.features.filter_builds.impl.tracker.FilterBuildsTrackerImpl
import teamcityapp.features.navigation.api.navigation.ProjectDestination
import teamcityapp.features.run_build.api.*
import teamcityapp.features.run_build.api.navigation.*
import teamcityapp.features.run_build.impl.*
import teamcityapp.features.run_build.impl.router.RunBuildRouter
import teamcityapp.features.run_build.impl.tracker.RunBuildTrackerImpl
import teamcityapp.libraries.analytics.AnalyticsTracker
import teamcityapp.libraries.navigation.*
import teamcityapp.libraries.shared_data.TeamCityAccountGraph
import teamcityapp.libraries.theme.LocalFloatingActionClearance

internal fun sharedManagementEntries(graph: TeamCityAccountGraph?, analytics: AnalyticsTracker): Set<DestinationRegistration> {
    if (graph == null) return emptySet()
    val repositories = graph.management
    return setOf(
        SharedEntry(RunBuildDestination::class, RunBuildDestination.serializer(), { builder ->
            builder.polymorphic(NavigationResult::class) { subclass(QueuedBuildResult::class, QueuedBuildResult.serializer()) }
        }) { dest, nav, _ ->
            val vm = viewModel { RunBuildViewModel(repositories.run, destinationState(mapOf(BUILD_TYPE_ID to dest.configurationId)), RunBuildTrackerImpl(analytics)) }
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
            val vm = viewModel { FilterBuildsViewModel(repositories.filters, destinationState(mapOf(BUILD_TYPE_ID to dest.configurationId)), FilterBuildsTrackerImpl(analytics)) }
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
            val vm = viewModel { FilterBottomSheetViewModel(checkNotNull(graph.browsing).filters, destinationState(mapOf("arg_code" to dest.code)), QuickFilterTrackerImpl(analytics)) }
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
            val platform = LocalNativePlatform.current
            val items = sheetItems(dest.menu, dest.descriptions)
            BottomSheetScreen(BottomSheetUiState(dest.title, items)) { item ->
                if (item.action == SheetAction.Copy) {
                    platform.copy(item.description)
                    nav.pop()
                } else if (!nav.finish(ActionSheetResult(dest.consumerId, item), dest.recipientId)) {
                    nav.pop()
                }
            }
        },
        SharedEntry(BuildDetailsDestination::class, BuildDetailsDestination.serializer()) { dest, nav, id -> SharedManagedBuild(dest, graph, nav, id) }
    )
}

@Composable
private fun SharedManagedBuild(destination: BuildDetailsDestination, graph: TeamCityAccountGraph, nav: Navigator, entryId: String) {
    val platform = LocalNativePlatform.current
    val tracker = remember(platform.analytics) { BuildOverviewTrackerImpl(platform.analytics) }
    val server = graph.account.identity.serverUrl
    val model = viewModel { BuildDetailsViewModel(destinationState(mapOf(BuildDetailsViewModel.BUILD to Json.encodeToString(destination.build), BuildDetailsViewModel.NAME to destination.configurationName, BuildDetailsViewModel.COMPLETION to destination.completion?.name)), graph.management.details, BuildDetailsTrackerImpl(platform.analytics)) }
    SharedScreenAnalytics(model, model::viewed)
    val state by model.state.collectAsStateWithLifecycle()
    val overview = viewModel {
        BuildReferenceViewModel(
            destination.build.href,
            object : BuildOverviewRepository {
                override suspend fun build(href: String, forceRefresh: Boolean) = graph.build(href, forceRefresh || destination.forceOverviewRefresh)
            },
            BuildReferenceTracker.None,
            checkNotNull(graph.browsing).metadata
        )
    }
    val education = viewModel { BuildDetailsMetadataViewModel(destinationState(mapOf(BuildDetailsViewModel.BUILD to Json.encodeToString(destination.build))), checkNotNull(graph.browsing).metadata, includeCounts = false) }
    val educationState by education.state.collectAsStateWithLifecycle()
    LaunchedEffect(state.current) { education.loaded(state.current) }
    val content by overview.state.collectAsStateWithLifecycle()
    LaunchedEffect(content.build) {
        content.build?.takeIf { it.id == destination.build.id }?.let { build ->
            model.loaded(build)
            // Rating bookkeeping must never turn a successful build response into a UI failure.
            try {
                graph.browsing?.buildViewed(build.id)
            } catch (cancelled: kotlinx.coroutines.CancellationException) {
                throw cancelled
            } catch (_: Exception) { /* A later view can retry this optional preference write. */ }
        }
    }
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
                destination.build.buildTypeId?.let {
                    tracker.action(BuildOverviewAction.Configuration, result.item.description)
                    nav.push(BuildHistoryDestination(it, state.subtitle, BuildFilter(branch = result.item.description).toTeamCityLocator()))
                }
            }
        }
        onPauseOrDispose {}
    }
    BuildDetailsScreen(state.copy(prompt = educationState.prompt, onboardingFailed = educationState.onboardingFailed), { nav.pop() }, { command ->
        when (command) {
            BuildDetailsCommand.Share -> {
                tracker.action(BuildOverviewAction.Share)
                runCatching { activeServerWebUrl(server, state.current.webUrl.orEmpty()) }.onSuccess(platform::share).onFailure { platform.rejected(NativePlatformFailure.Share) }.let { }
            }

            BuildDetailsCommand.Browser -> {
                tracker.action(BuildOverviewAction.Browser)
                platform.serverBrowser(server, state.current.webUrl.orEmpty())
            }

            BuildDetailsCommand.Log -> {
                tracker.action(BuildOverviewAction.Log)
                platform.browser(buildLogUrl(server, state.current, graph.account.identity.guest))
            }

            BuildDetailsCommand.Cancel -> {
                tracker.action(if (state.current.state == "queued") BuildOverviewAction.RemoveFromQueue else BuildOverviewAction.Stop)
                model.requestCancellation()
            }

            BuildDetailsCommand.Restart -> {
                tracker.action(BuildOverviewAction.Restart)
                model.requestRestart()
            }

            else -> Unit
        }
    }, model::confirm, model::dismissConfirmation, snackbar, onDismissPrompt = education::dismissPrompt) {
        SharedBuildOverviewScreen(
            content,
            overview::refresh,
            overview::retry,
            { row ->
                if (!state.busy) {
                    when (row.action) {
                        OverviewRowAction.Configuration -> state.current.buildTypeId?.let {
                            tracker.action(BuildOverviewAction.Configuration)
                            nav.push(BuildHistoryDestination(it, state.subtitle))
                        }

                        OverviewRowAction.Project -> state.current.configuration?.projectId?.let {
                            tracker.action(BuildOverviewAction.Project)
                            nav.push(ProjectDestination(it, state.current.configuration?.projectName.orEmpty()))
                        }

                        OverviewRowAction.Branch -> (row.value as? OverviewText.Literal)?.let { nav.push(ActionSheetDestination(it.value, listOf(it.value), SheetMenuType.Branch, entryId, "overview")) }

                        OverviewRowAction.Copy -> (row.value as? OverviewText.Literal)?.let { platform.copy(it.value) }
                    }
                }
            },
            onSection = { section ->
                if (!state.busy) {
                    nav.openBuildSection(entryId, destination.build.id, content.build, section, state.subtitle)
                }
            },
            onLog = {
                tracker.action(BuildOverviewAction.Log)
                platform.browser(buildLogUrl(server, state.current, graph.account.identity.guest))
            },
            onCountRetry = overview::retryCount,
            bottomContentPadding = LocalFloatingActionClearance.current,
            rowActionAvailable = { !state.busy && (it.action != OverviewRowAction.Copy || it.value is OverviewText.Literal) }
        )
    }
}
