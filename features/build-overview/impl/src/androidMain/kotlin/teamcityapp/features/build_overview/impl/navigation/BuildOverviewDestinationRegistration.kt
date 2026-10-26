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

package teamcityapp.features.build_overview.impl.navigation

import android.os.Bundle
import androidx.activity.compose.LocalActivity
import androidx.compose.runtime.*
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.LifecycleResumeEffect
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import dagger.multibindings.IntoSet
import javax.inject.Inject
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.serialization.json.Json
import teamcityapp.features.bottom_sheet.api.*
import teamcityapp.features.bottom_sheet.api.navigation.*
import teamcityapp.features.build_details.api.*
import teamcityapp.features.build_overview.api.*
import teamcityapp.features.build_overview.api.navigation.BuildOverviewDestination
import teamcityapp.features.build_overview.impl.*
import teamcityapp.features.build_overview.impl.router.BuildOverviewRouter
import teamcityapp.libraries.builds.BuildLaunchData
import teamcityapp.libraries.navigation.*

class BuildOverviewDestinationRegistration @Inject constructor() : FeatureDestination<BuildOverviewDestination>(BuildOverviewDestination::class, BuildOverviewDestination.serializer()) {
    @Composable override fun Content(destination: BuildOverviewDestination, navigator: Navigator, entryId: String) {
        val activity = checkNotNull(LocalActivity.current)
        val host = checkNotNull(LocalBuildDetailsUiHost.current) { "Overview requires its Build Details owner" }
        val arguments = remember(destination) {
            Bundle().apply {
                putString(BuildOverviewViewModel.JSON_BUILD, Json.encodeToString(destination.build))
                putBoolean(BuildOverviewViewModel.FORCE_INITIAL, destination.forceRefresh)
            }
        }
        val router = remember(activity, navigator, entryId, host) {
            object : BuildOverviewRouter {
                override val requests = emptyFlow<BuildOverviewRequest>()
                override fun loaded(build: BuildLaunchData) = host.loaded(build)
                override fun resumed(build: BuildLaunchData) = host.loaded(build)
                override fun section(section: BuildDetailsSection) = host.openSection(section)
                override fun row(row: OverviewRow) {
                    if (row.action == OverviewRowAction.Configuration && !host.current.buildTypeId.isNullOrBlank()) {
                        dispatch(BuildOverviewAction.Configuration, host.current)
                        return
                    }
                    if (row.action == OverviewRowAction.Project && !host.current.configuration?.projectId.isNullOrBlank()) {
                        dispatch(BuildOverviewAction.Project, host.current)
                        return
                    }
                    val value = when (val text = row.value) {
                        is OverviewText.Literal -> text.value
                        else -> activity.getString(text.androidResource())
                    }
                    val menu = when (row.action) {
                        OverviewRowAction.Copy -> SheetMenuType.Default
                        OverviewRowAction.Branch -> SheetMenuType.Branch
                        OverviewRowAction.Configuration -> SheetMenuType.BuildType
                        OverviewRowAction.Project -> SheetMenuType.Project
                    }
                    navigator.push(ActionSheetDestination(activity.getString(row.labelRes), listOf(value), menu, host.entryId, entryId))
                }
                override fun dispatch(action: BuildOverviewAction, build: BuildLaunchData, branch: String?) = host.dispatch(
                    when (action) {
                        BuildOverviewAction.Share -> BuildDetailsCommand.Share
                        BuildOverviewAction.Browser -> BuildDetailsCommand.Browser
                        BuildOverviewAction.Log -> BuildDetailsCommand.Log
                        BuildOverviewAction.Stop, BuildOverviewAction.RemoveFromQueue -> BuildDetailsCommand.Cancel
                        BuildOverviewAction.Restart -> BuildDetailsCommand.Restart
                        BuildOverviewAction.Configuration -> BuildDetailsCommand.Configuration
                        BuildOverviewAction.Project -> BuildDetailsCommand.Project
                    },
                    build,
                    branch
                )
                override fun dispose() {}
            }
        }
        DestinationArguments(arguments) {
            val model = hiltViewModel<AndroidBuildOverviewViewModel>()
            LaunchedEffect(host.countStates, model) { model.metadata(host.countStates) }
            LaunchedEffect(host.refreshVersion, model) { model.refreshFromHost(host.refreshVersion) }
            LifecycleResumeEffect(navigator.snapshot.results, model, router) {
                while (true) {
                    val result = navigator.consumeResult(host.entryId, ActionSheetResult::class) { it.consumerId == entryId } ?: break
                    when (result.item.action) {
                        SheetAction.Copy -> host.copied()
                        SheetAction.Branch -> router.dispatch(BuildOverviewAction.Configuration, host.current, result.item.description)
                        SheetAction.BuildType -> router.dispatch(BuildOverviewAction.Configuration, host.current)
                        SheetAction.Project -> router.dispatch(BuildOverviewAction.Project, host.current)
                        else -> Unit
                    }
                }
                onPauseOrDispose {}
            }
            BuildOverviewRoute(router, true, {}, model, host::retryCount)
        }
    }
}

@Module
@InstallIn(SingletonComponent::class)
abstract class BuildOverviewDestinationModule {
    @Binds @IntoSet
    abstract fun registration(implementation: BuildOverviewDestinationRegistration): DestinationRegistration
}
