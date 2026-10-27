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

import androidx.compose.runtime.*
import androidx.lifecycle.compose.*
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.serialization.json.Json
import kotlinx.serialization.modules.*
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
import teamcityapp.features.build_overview.impl.router.BuildOverviewActions
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
            SharedActionSheetRoute(dest.title, dest.menu, dest.descriptions, platform::copy, { nav.pop() }) { item ->
                if (!nav.finish(ActionSheetResult(dest.consumerId, item), dest.recipientId)) nav.pop()
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
    val content by overview.state.collectAsStateWithLifecycle()
    val actions = remember(tracker, server, platform) {
        BuildOverviewActions(tracker, server, graph.account.identity.guest, platform::browser, platform::share) {
            platform.rejected(if (it == BuildOverviewAction.Share) NativePlatformFailure.Share else NativePlatformFailure.Browser)
        }
    }
    LifecycleResumeEffect(model, nav.snapshot.results) {
        while (true) {
            val result = nav.consumeResult(entryId, BuildDetailsSectionCountResult::class) ?: break
            if (result.buildId == destination.build.id) overview.updateCount(result.section, result.count)
        }
        while (true) {
            val result = nav.consumeResult(entryId, ActionSheetResult::class) ?: break
            if (result.consumerId == "overview" && result.item.action == SheetAction.Branch) {
                destination.build.buildTypeId?.let {
                    actions.configuration(result.item.description)
                    nav.push(BuildHistoryDestination(it, model.state.value.subtitle, BuildFilter(branch = result.item.description).toTeamCityLocator()))
                }
            }
        }
        onPauseOrDispose {}
    }
    SharedBuildDetailsRoute(
        model, education, content.build, { graph.browsing?.buildViewed(it.id) }, overview::refresh,
        actions::perform, { nav.pop() },
        { href, title -> nav.push(BuildDetailsReferenceDestination(href, title, fromRestart = true)) },
        { replacement, title ->
            if (nav.current.id == entryId) {
                nav.replace(BuildDetailsDestination(replacement.build, title, forceOverviewRefresh = true, completion = replacement.completion))
                true
            } else {
                false
            }
        }
    ) { busy, current, subtitle ->
        SharedBuildOverviewRoute(
            overview, content, current, busy, actions,
            { nav.push(BuildHistoryDestination(it, subtitle)) },
            { id, name -> nav.push(ProjectDestination(id, name)) },
            { nav.push(ActionSheetDestination(it, listOf(it), SheetMenuType.Branch, entryId, "overview")) },
            platform::copy,
            { nav.openBuildSection(entryId, destination.build.id, content.build, it, subtitle) },
            LocalFloatingActionClearance.current
        )
    }
}
