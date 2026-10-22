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

package teamcityapp.features.build_details.impl.navigation

import android.content.Intent
import android.os.Bundle
import androidx.activity.compose.LocalActivity
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.*
import androidx.lifecycle.repeatOnLifecycle
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import dagger.multibindings.IntoSet
import javax.inject.Inject
import javax.inject.Provider
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import org.jetbrains.compose.resources.stringResource
import teamcityapp.features.build_details.api.*
import teamcityapp.features.build_details.impl.*
import teamcityapp.features.build_details.impl.R
import teamcityapp.features.build_details.presentation.resources.*
import teamcityapp.features.build_history.api.BuildHistoryFilterAdapter
import teamcityapp.features.build_history.api.navigation.BuildHistoryDestination
import teamcityapp.features.build_overview.api.*
import teamcityapp.features.build_overview.api.navigation.BuildOverviewDestination
import teamcityapp.features.filter_builds.api.*
import teamcityapp.features.navigation.api.navigation.ProjectDestination
import teamcityapp.libraries.builds.BuildLaunchData
import teamcityapp.libraries.chrome_tabs.ChromeCustomTabsImpl
import teamcityapp.libraries.navigation.*

class BuildDetailsDestinationRegistration @Inject constructor(
    private val registry: Provider<DestinationRegistry>,
    private val analytics: BuildOverviewAnalytics,
    private val filterResults: FilterBuildsResultAdapter,
    private val historyFilters: BuildHistoryFilterAdapter,
    private val logLink: teamcityapp.features.build_details.impl.router.BuildDetailsLogLink
) : FeatureDestination<BuildDetailsDestination>(BuildDetailsDestination::class, BuildDetailsDestination.serializer()) {
    @Composable
    override fun Content(destination: BuildDetailsDestination, navigator: Navigator, entryId: String) {
        val activity = checkNotNull(LocalActivity.current)
        val arguments = remember(destination) {
            Bundle().apply {
                putString(BuildDetailsViewModel.BUILD, Json.encodeToString(destination.build))
                putString(BuildDetailsViewModel.NAME, destination.configurationName)
                putString(BuildDetailsViewModel.COMPLETION, destination.completion?.name)
            }
        }
        DestinationArguments(arguments) {
            val model = hiltViewModel<AndroidBuildDetailsViewModel>()
            val state by model.state.collectAsStateWithLifecycle()
            val metadata = hiltViewModel<BuildDetailsMetadataViewModel>()
            val metadataState by metadata.state.collectAsStateWithLifecycle()
            val latestMetadata by rememberUpdatedState(metadataState)
            LaunchedEffect(state.current, metadata) { metadata.loaded(state.current) }
            val lifecycleState by LocalLifecycleOwner.current.lifecycle.currentStateFlow.collectAsStateWithLifecycle()
            LifecycleResumeEffect(navigator.snapshot.results, metadata) {
                while (true) {
                    val result = navigator.consumeResult(entryId, BuildDetailsSectionCountResult::class) { it.buildId == destination.build.id } ?: break
                    metadata.updateCount(result.section, result.count)
                }
                onPauseOrDispose {}
            }
            val latest by rememberUpdatedState(state)
            val snackbar = remember { SnackbarHostState() }
            val scope = rememberCoroutineScope()
            val copied = stringResource(Res.string.build_element_copy_text)
            val platformFailed = stringResource(Res.string.platform_action_failed)
            val tabs = remember(activity, entryId) { ChromeCustomTabsImpl(activity) }
            LifecycleResumeEffect(tabs, model) {
                tabs.initCustomsTabs()
                model.viewed()
                onPauseOrDispose { tabs.unbindCustomsTabs() }
            }
            val host = remember(model, navigator, entryId, activity, tabs) {
                object : BuildDetailsUiHost {
                    override val entryId = entryId
                    override val refreshVersion get() = latest.overviewRefresh
                    override val current get() = model.state.value.current
                    override fun loaded(build: BuildLaunchData) = model.loaded(build)
                    override val countStates get() = latestMetadata.counts
                    override fun retryCount(tab: BuildDetailsSection) = metadata.retryCount(tab)
                    override fun openSection(section: BuildDetailsSection) {
                        val currentState = model.state.value
                        if (!currentState.busy && section in destination.build.detailsSections()) {
                            navigator.push(BuildDetailsSectionDestination(destination.build, section, currentState.subtitle, currentState.current.number ?: currentState.current.id, entryId))
                        }
                    }
                    override fun copied() {
                        scope.launch { snackbar.showSnackbar(copied) }
                    }
                    override fun dispatch(action: BuildDetailsCommand, build: BuildLaunchData, branch: String?) {
                        model.loaded(build)
                        analytics.action(
                            when (action) {
                                BuildDetailsCommand.Share -> BuildOverviewAction.Share
                                BuildDetailsCommand.Browser -> BuildOverviewAction.Browser
                                BuildDetailsCommand.Log -> BuildOverviewAction.Log
                                BuildDetailsCommand.Cancel -> if (build.isRunning) BuildOverviewAction.Stop else BuildOverviewAction.RemoveFromQueue
                                BuildDetailsCommand.Restart -> BuildOverviewAction.Restart
                                BuildDetailsCommand.Configuration -> BuildOverviewAction.Configuration
                                BuildDetailsCommand.Project -> BuildOverviewAction.Project
                            },
                            branch
                        )
                        when (action) {
                            BuildDetailsCommand.Share -> try {
                                activity.startActivity(Intent.createChooser(Intent(Intent.ACTION_SEND).setType("text/plain").putExtra(Intent.EXTRA_TEXT, build.webUrl), activity.getString(R.string.title_share_build)))
                            } catch (_: Exception) {
                                scope.launch { snackbar.showSnackbar(platformFailed) }
                            }

                            BuildDetailsCommand.Browser -> try {
                                tabs.launchUrl(build.webUrl.orEmpty())
                            } catch (_: Exception) {
                                scope.launch { snackbar.showSnackbar(platformFailed) }
                            }

                            BuildDetailsCommand.Log -> try {
                                if (!build.isQueued) tabs.launchUrl(logLink.url(build.id))
                            } catch (_: Exception) {
                                scope.launch { snackbar.showSnackbar(platformFailed) }
                            }

                            BuildDetailsCommand.Cancel -> model.requestCancellation()

                            BuildDetailsCommand.Restart -> model.requestRestart()

                            BuildDetailsCommand.Configuration -> {
                                val id = build.buildTypeId?.takeIf { it.isNotBlank() } ?: return
                                val name = build.configuration?.name ?: destination.configurationName.orEmpty()
                                val locator = branch?.let { historyFilters.locator(filterResults.serialize(BuildFilter(branch = it))) }
                                navigator.push(BuildHistoryDestination(id, name, locator))
                            }

                            BuildDetailsCommand.Project -> {
                                val id = build.configuration?.projectId?.takeIf { it.isNotBlank() } ?: return
                                navigator.push(ProjectDestination(id, build.configuration?.projectName.orEmpty()))
                            }
                        }
                    }
                }
            }
            LifecycleResumeEffect(state.replacement, model) {
                state.replacement?.takeIf { navigator.current.id == entryId }?.let { replacement ->
                    model.consumeReplacement(replacement.id)
                    navigator.replace(BuildDetailsDestination(replacement.build, state.subtitle, forceOverviewRefresh = true, completion = replacement.completion))
                }
                onPauseOrDispose {}
            }
            val lifecycle = LocalLifecycleOwner.current.lifecycle
            val noticeMessage = state.notice?.let { stringResource(it.message) }
            val noticeAction = state.notice?.actionLabel?.let { stringResource(it) }
            LaunchedEffect(state.notice, lifecycle, noticeMessage, noticeAction) {
                val notice = state.notice ?: return@LaunchedEffect
                lifecycle.repeatOnLifecycle(Lifecycle.State.RESUMED) {
                    val result = snackbar.showSnackbar(checkNotNull(noticeMessage), noticeAction)
                    model.consumeNotice(notice.id)
                    if (result == SnackbarResult.ActionPerformed && notice.openQueued) model.queuedBuildReference()?.let { navigator.push(BuildDetailsReferenceDestination(it, state.subtitle, fromRestart = true)) }
                }
            }
            val overview = remember(destination, entryId) {
                NavigationEntry("$entryId/Overview", BuildOverviewDestination(destination.build, destination.forceOverviewRefresh))
            }
            CompositionLocalProvider(LocalBuildDetailsUiHost provides host) {
                val presentation = state.copy(
                    countFailures = metadataState.counts.filterValues { it == BuildDetailsCount.Unavailable }.keys,
                    onboardingFailed = metadataState.onboardingFailed,
                    prompt = metadataState.prompt.takeIf { lifecycleState == Lifecycle.State.RESUMED && navigator.current.id == entryId }
                )
                BuildDetailsScreen(
                    presentation,
                    onUp = { if (!navigator.pop()) activity.finish() },
                    onAction = { action -> host.dispatch(action, state.current) },
                    onConfirm = model::confirm,
                    onDismissConfirmation = model::dismissConfirmation,
                    snackbar = snackbar,
                    onDismissPrompt = metadata::dismissPrompt
                ) { RetainedNavigationPane(listOf(overview), overview.id, navigator, registry.get()) }
            }
        }
    }
}

@Module
@InstallIn(SingletonComponent::class)
abstract class BuildDetailsDestinationModule {
    @Binds @IntoSet
    abstract fun registration(implementation: BuildDetailsDestinationRegistration): DestinationRegistration
}
