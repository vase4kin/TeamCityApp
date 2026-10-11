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
import androidx.compose.material3.windowsizeclass.*
import androidx.compose.runtime.*
import androidx.compose.ui.res.stringResource
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
import teamcityapp.features.artifacts.api.navigation.ArtifactsDestination
import teamcityapp.features.build_details.api.*
import teamcityapp.features.build_details.impl.*
import teamcityapp.features.build_details.impl.R
import teamcityapp.features.build_history.api.BuildHistoryFilterAdapter
import teamcityapp.features.build_history.api.navigation.BuildHistoryDestination
import teamcityapp.features.build_log.api.navigation.BuildLogDestination
import teamcityapp.features.build_overview.api.*
import teamcityapp.features.build_overview.api.navigation.BuildOverviewDestination
import teamcityapp.features.changes.api.navigation.ChangesDestination
import teamcityapp.features.filter_builds.api.*
import teamcityapp.features.navigation.api.navigation.ProjectDestination
import teamcityapp.features.properties.api.*
import teamcityapp.features.properties.api.navigation.PropertiesDestination
import teamcityapp.features.snapshot_dependencies.api.navigation.SnapshotDependenciesDestination
import teamcityapp.features.tests.api.navigation.TestsDestination
import teamcityapp.libraries.builds.BuildLaunchData
import teamcityapp.libraries.chrome_tabs.ChromeCustomTabsImpl
import teamcityapp.libraries.navigation.*

class BuildDetailsDestinationRegistration @Inject constructor(
    private val registry: Provider<DestinationRegistry>,
    private val analytics: BuildOverviewAnalytics,
    private val filterResults: FilterBuildsResultAdapter,
    private val historyFilters: BuildHistoryFilterAdapter
) : FeatureDestination<BuildDetailsDestination>(BuildDetailsDestination::class, BuildDetailsDestination.serializer()) {
    @OptIn(ExperimentalMaterial3WindowSizeClassApi::class)
    @Composable
    override fun Content(destination: BuildDetailsDestination, navigator: Navigator, entryId: String) {
        val activity = checkNotNull(LocalActivity.current)
        val arguments = remember(destination) {
            Bundle().apply {
                putString(BuildDetailsViewModel.BUILD, Json.encodeToString(destination.build))
                putString(BuildDetailsViewModel.NAME, destination.configurationName)
            }
        }
        DestinationArguments(arguments) {
            val model = hiltViewModel<BuildDetailsViewModel>()
            val state by model.state.collectAsStateWithLifecycle()
            val latest by rememberUpdatedState(state)
            val snackbar = remember { SnackbarHostState() }
            val scope = rememberCoroutineScope()
            val copied = stringResource(R.string.build_element_copy_text)
            val show = stringResource(R.string.text_show_build)
            val platformFailed = stringResource(R.string.platform_action_failed)
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
                    override val current get() = latest.current
                    override fun loaded(build: BuildLaunchData) = model.loaded(build)
                    override fun count(tab: BuildDetailsTab, count: Int) = model.count(tab, count)
                    override fun copied() {
                        scope.launch { snackbar.showSnackbar(copied) }
                    }
                    override fun dispatch(action: BuildDetailsCommand, build: BuildLaunchData, branch: String?) {
                        model.loaded(build)
                        analytics.action(
                            when (action) {
                                BuildDetailsCommand.Share -> BuildOverviewAction.Share
                                BuildDetailsCommand.Browser -> BuildOverviewAction.Browser
                                BuildDetailsCommand.Cancel -> if (build.isRunning) BuildOverviewAction.Stop else BuildOverviewAction.RemoveFromQueue
                                BuildDetailsCommand.Restart -> BuildOverviewAction.Restart
                                BuildDetailsCommand.Configuration -> BuildOverviewAction.Configuration
                                BuildDetailsCommand.Project -> BuildOverviewAction.Project
                            },
                            branch
                        )
                        when (action) {
                            BuildDetailsCommand.Share -> try {
                                activity.startActivity(Intent.createChooser(Intent(Intent.ACTION_SEND).setType("text/plain").putExtra(Intent.EXTRA_TEXT, build.webUrl), null))
                            } catch (_: Exception) {
                                scope.launch { snackbar.showSnackbar(platformFailed) }
                            }

                            BuildDetailsCommand.Browser -> try {
                                tabs.launchUrl(build.webUrl.orEmpty())
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
                    navigator.replace(BuildDetailsDestination(replacement.build, state.subtitle, forceOverviewRefresh = true))
                }
                onPauseOrDispose {}
            }
            val lifecycle = LocalLifecycleOwner.current.lifecycle
            LaunchedEffect(state.notice, lifecycle) {
                val notice = state.notice ?: return@LaunchedEffect
                lifecycle.repeatOnLifecycle(Lifecycle.State.RESUMED) {
                    val result = snackbar.showSnackbar(activity.getString(notice.message), if (notice.openQueued) show else null)
                    model.consumeNotice(notice.id)
                    if (result == SnackbarResult.ActionPerformed && notice.openQueued) model.openQueued()
                }
            }
            val children = remember(destination, entryId) {
                destination.build.detailsTabs().map { tab ->
                    val build = destination.build
                    val key = when (tab) {
                        BuildDetailsTab.Overview -> BuildOverviewDestination(build, destination.forceOverviewRefresh)
                        BuildDetailsTab.Changes -> ChangesDestination(checkNotNull(build.changes?.href))
                        BuildDetailsTab.Tests -> checkNotNull(build.tests).let { TestsDestination(checkNotNull(it.href), it.passed, it.failed, it.ignored) }
                        BuildDetailsTab.BuildLog -> BuildLogDestination(build.id)
                        BuildDetailsTab.Parameters -> PropertiesDestination(build.properties?.items.orEmpty().map { Property(it.name.orEmpty(), it.value.orEmpty()) })
                        BuildDetailsTab.Artifacts -> ArtifactsDestination(build, checkNotNull(build.artifacts?.href), showToolbar = false)
                        BuildDetailsTab.Snapshots -> SnapshotDependenciesDestination(build.id)
                    }
                    NavigationEntry("$entryId/${tab.name}", key)
                }
            }
            CompositionLocalProvider(LocalBuildDetailsUiHost provides host) {
                BuildDetailsScreen(
                    state,
                    calculateWindowSizeClass(activity).widthSizeClass != WindowWidthSizeClass.Compact,
                    onUp = { if (!navigator.pop()) activity.finish() },
                    onSelect = model::select,
                    onAction = { action -> host.dispatch(action, state.current) },
                    onConfirm = model::confirm,
                    onDismissConfirmation = model::dismissConfirmation,
                    snackbar = snackbar
                ) { RetainedNavigationPane(children, "$entryId/${state.selected.name}", navigator, registry.get()) }
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
