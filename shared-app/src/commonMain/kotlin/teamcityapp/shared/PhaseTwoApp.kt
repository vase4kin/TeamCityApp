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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.repeatOnLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation3.runtime.NavKey
import androidx.paging.LoadState
import androidx.paging.compose.collectAsLazyPagingItems
import kotlin.reflect.KClass
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.launch
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.modules.SerializersModuleBuilder
import org.jetbrains.compose.resources.stringResource
import teamcityapp.features.build_details.api.BuildDetailsReferenceDestination
import teamcityapp.features.build_history.api.*
import teamcityapp.features.build_history.api.navigation.BuildHistoryDestination
import teamcityapp.features.build_history.impl.*
import teamcityapp.features.build_overview.impl.*
import teamcityapp.features.filter_builds.api.toTeamCityLocator
import teamcityapp.features.login.api.*
import teamcityapp.features.login.api.navigation.LoginDestination
import teamcityapp.features.login.impl.*
import teamcityapp.features.navigation.api.NavigationEntry as ProjectEntry
import teamcityapp.features.navigation.api.NavigationRatingRepository
import teamcityapp.features.navigation.api.navigation.ProjectDestination
import teamcityapp.features.navigation.impl.*
import teamcityapp.libraries.accounts.*
import teamcityapp.libraries.authentication.AuthenticationRepository
import teamcityapp.libraries.design_system.resolve
import teamcityapp.libraries.list_ui.PagingListSnapshot
import teamcityapp.libraries.navigation.*
import teamcityapp.libraries.shared_data.TeamCityAccountGraph
import teamcityapp.libraries.theme.*
import teamcityapp.shared.resources.*

@Serializable
internal data object AccountsDestination : NavKey

internal class SharedEntry<T : NavKey>(
    type: KClass<T>,
    serializer: KSerializer<T>,
    private val additionalSerializers: (SerializersModuleBuilder) -> Unit = {},
    override val presentation: DestinationPresentation = DestinationPresentation.Screen,
    private val content: @Composable (T, Navigator, String) -> Unit
) : FeatureDestination<T>(type, serializer) {
    override fun registerSerializers(builder: SerializersModuleBuilder) {
        super.registerSerializers(builder)
        additionalSerializers(builder)
    }

    @Composable override fun Content(destination: T, navigator: Navigator, entryId: String) = content(destination, navigator, entryId)
}

@Composable
fun PhaseTwoApp(model: SharedAppViewModel, darkTheme: Boolean? = null) {
    val platform = LocalNativePlatform.current
    val themeMode by platform.theme.theme.collectAsStateWithLifecycle(teamcityapp.libraries.app_theme.ThemeMode.System)
    val platformFailure by platform.failure.collectAsStateWithLifecycle()
    val session by model.sessions.state.collectAsStateWithLifecycle()
    val failed by model.restoreFailed.collectAsStateWithLifecycle()
    TeamCityTheme(
        darkTheme ?: when (themeMode) {
            teamcityapp.libraries.app_theme.ThemeMode.Light -> false
            teamcityapp.libraries.app_theme.ThemeMode.Dark -> true
            else -> androidx.compose.foundation.isSystemInDarkTheme()
        }
    ) {
        CompositionLocalProvider(LocalNativeAppLifecycle provides androidx.lifecycle.compose.LocalLifecycleOwner.current.lifecycle) {
            Surface(Modifier.fillMaxSize()) {
                if (failed) {
                    SharedErrorContent(Modifier.fillMaxSize(), onRetry = model::restore)
                } else {
                    when (val current = session) {
                        AccountSessionState.Loading -> LoadingContent(Modifier.fillMaxSize())
                        AccountSessionState.SignedOut -> key("signed-out") { SharedJourney(model, null) }
                        is AccountSessionState.Active -> key(current.scope.generation) { SharedJourney(model, current.scope) }
                    }
                }
            }
            platformFailure?.let { failure ->
                NativeFailureDialog(failure, platform::clearFailure)
            }
        }
    }
}

@Composable
private fun SharedJourney(model: SharedAppViewModel, account: AccountScope?) {
    val platform = LocalNativePlatform.current
    var graphFailure by remember(account) { mutableStateOf(false) }
    var graphRevision by remember(account) { mutableIntStateOf(0) }
    val graph by produceState<TeamCityAccountGraph?>(null, account, graphRevision) {
        graphFailure = false
        try {
            if (account != null) value = TeamCityAccountGraph.create(model.sessions, account, platform.files)
        } catch (c: CancellationException) {
            throw c
        } catch (_: Exception) {
            graphFailure = true
        }
    }
    if (graphFailure) {
        SharedErrorContent(Modifier.fillMaxSize(), onRetry = { graphRevision++ })
        return
    }
    if (account != null && graph == null) {
        LoadingContent(Modifier.fillMaxSize())
        return
    }
    val registry = remember(graph, model) {
        lateinit var result: DestinationRegistry
        result = DestinationRegistry(
            setOf(
                SharedEntry(teamcityapp.features.home.api.HomeDestination::class, teamcityapp.features.home.api.HomeDestination.serializer()) { destination, navigator, entryId -> NativeHomeContent(destination, checkNotNull(graph), navigator, result, entryId) },
                SharedEntry(LoginDestination::class, LoginDestination.serializer()) { _, navigator, _ -> LoginContent(model, navigator) },
                SharedEntry(AccountsDestination::class, AccountsDestination.serializer()) { _, navigator, _ -> NativeAccountContent(model, navigator) },
                SharedEntry(ProjectDestination::class, ProjectDestination.serializer()) { destination, navigator, _ -> ProjectsContent(destination, checkNotNull(graph), navigator) },
                SharedEntry(BuildHistoryDestination::class, BuildHistoryDestination.serializer()) { destination, navigator, entryId -> HistoryContent(destination, checkNotNull(graph), navigator, entryId) },
                SharedEntry(BuildDetailsReferenceDestination::class, BuildDetailsReferenceDestination.serializer()) { destination, navigator, entryId -> OverviewContent(destination, checkNotNull(graph), navigator, entryId) }
            ) + nativeAccountEntries(model) + nativeBrowsingEntries(graph) + nativeManagementEntries(graph) + nativeArtifactEntries(graph) + nativeSettingsEntries(graph)
        )
        result
    }
    val sessionKey = account?.identity?.key ?: "signed-out"
    val journeyMemory = remember { NativeJourneyMemory() }
    var restorationReady by remember { mutableStateOf(false) }
    var restored by remember { mutableStateOf<NavigationSnapshot?>(null) }
    LaunchedEffect(sessionKey, registry) {
        if (model.claimRestoration()) {
            val encoded = model.navigationStore.read()
            val decoded = decodeNativeSnapshot(encoded, registry, sessionKey)
            restored = decoded?.first?.takeIf {
                if (account == null) {
                    it.entries.all { visit -> visit.destination is LoginDestination || visit.destination == AccountsDestination || visit.destination == teamcityapp.features.create_account.api.navigation.CreateAccountDestination || visit.destination == teamcityapp.features.manage_accounts.api.navigation.ManageAccountsDestination }
                } else {
                    it.entries.first().destination is teamcityapp.features.home.api.HomeDestination && it.entries.none { visit -> visit.destination is LoginDestination }
                }
            }
            if (restored != null) journeyMemory.homes = decoded?.second.orEmpty()
        }
        restorationReady = true
    }
    if (!restorationReady) {
        LoadingContent(Modifier.fillMaxSize())
        return
    }
    val navigator = rememberNavigator(if (account == null) LoginDestination else teamcityapp.features.home.api.HomeDestination(), registry, sessionKey, restoredSnapshot = restored)
    val snapshot = navigator.snapshot
    val homes = journeyMemory.homes.filterKeys { id -> snapshot.entries.any { it.id == id } }
    LaunchedEffect(snapshot, homes) { model.navigationStore.write(nativeJourneyJson.encodeToString(SavedNativeJourney(registry.encodeSnapshot(snapshot), homes))) }
    CompositionLocalProvider(LocalNativeJourneyMemory provides journeyMemory) {
        val safeArea = WindowInsets.safeDrawing.only(WindowInsetsSides.Top + WindowInsetsSides.Horizontal).asPaddingValues()
        NavigationHost(navigator, registry, onExit = {}, modifier = Modifier.padding(safeArea).consumeWindowInsets(safeArea))
    }
}

@Composable
private fun LoginContent(model: SharedAppViewModel, navigator: Navigator) {
    val authentication = remember(model) {
        object : AuthenticationRepository {
            override suspend fun signIn(credentials: teamcityapp.libraries.authentication.AccountCredentials, checkSecureConnection: Boolean, checkDuplicate: Boolean) = model.authentication.signIn(credentials, checkSecureConnection, checkDuplicate)
        }
    }
    val vm = viewModel {
        LoginViewModel(
            authentication,
            object : LoginRepository {
                override suspend fun demoServer() = DemoServer(true, "https://teamcity.jetbrains.com")
            },
            NativeLoginTracker
        )
    }
    val state by vm.state.collectAsStateWithLifecycle()
    var dialog by remember { mutableStateOf(LoginDialog.None) }
    Box(Modifier.fillMaxSize()) {
        SharedLoginScreen(
            state, vm::update, vm::submit,
            { disabled -> if (disabled) dialog = LoginDialog.Ssl else vm.update(state.form.copy(sslDisabled = false)) },
            {
                vm.demoClicked()
                dialog = LoginDialog.Demo
            },
            dialog,
            onConfirm = {
                when (dialog) {
                    LoginDialog.Ssl -> vm.update(state.form.copy(sslDisabled = true))
                    LoginDialog.Demo -> vm.tryDemo()
                    LoginDialog.None -> Unit
                }
                dialog = LoginDialog.None
            },
            onDecline = {
                if (dialog == LoginDialog.Demo) vm.declineDemo()
                dialog = LoginDialog.None
            },
            onConfirmHttp = vm::confirmHttp, onDeclineHttp = vm::declineHttp, onDismissUnauthorized = vm::dismissUnauthorized
        )
        TextButton(
            { navigator.push(AccountsDestination) },
            Modifier.align(Alignment.TopEnd).safeDrawingPadding().testTag("login:accounts"),
            enabled = !state.form.busy
        ) { Text(stringResource(Res.string.journey_accounts)) }
    }
}

@Composable
private fun ProjectsContent(destination: ProjectDestination, graph: TeamCityAccountGraph, navigator: Navigator) {
    val vm = viewModel {
        NavigationViewModel(
            SavedStateHandle(
                mapOf(
                    NavigationViewModel.PROJECT_ID to destination.projectId,
                    NavigationViewModel.PROJECT_NAME to destination.projectName,
                    NavigationViewModel.ROOT_SCREEN to destination.root
                )
            ),
            graph,
            object : NavigationRatingRepository {
                override suspend fun isEligible() = false
                override suspend fun markHandled() {}
            },
            NativeNavigationTracker
        )
    }
    val state by vm.state.collectAsStateWithLifecycle()
    SharedNavigationScreen(
        state,
        vm::refresh,
        vm::retry,
        { if (destination.root) navigator.push(AccountsDestination) else navigator.pop() },
        { entry ->
            when (entry) {
                is ProjectEntry.Project -> navigator.push(ProjectDestination(entry.reference.id, entry.reference.name))
                is ProjectEntry.Configuration -> navigator.push(BuildHistoryDestination(entry.configuration.id, entry.configuration.name))
            }
        },
        {},
        {},
        onRatingRetry = vm::retryRating
    )
}

@Composable
private fun HistoryContent(destination: BuildHistoryDestination, graph: TeamCityAccountGraph, navigator: Navigator, entryId: String) {
    // Optional controls remain with their owning later phase; the real paging journey is shared now.
    val vm = viewModel {
        BuildHistoryViewModel(
            SavedStateHandle(
                mapOf(
                    BuildHistoryViewModel.EXTRA_ID to destination.configurationId,
                    BuildHistoryViewModel.EXTRA_NAME to destination.configurationName,
                    BuildHistoryViewModel.EXTRA_LOCATOR to destination.locator
                )
            ),
            object : BuildHistoryRepository {
                override suspend fun page(query: BuildHistoryQuery, nextHref: String?, forceRefresh: Boolean) = graph.page(query, nextHref, forceRefresh)
                override suspend fun isFavorite(configurationId: String): Boolean = checkNotNull(graph.browsing).isFavorite(configurationId)
                override suspend fun setFavorite(configurationId: String, favorite: Boolean): Unit = checkNotNull(graph.browsing).setFavorite(configurationId, favorite)
            },
            object : BuildHistoryOnboardingRepository {
                override suspend fun pendingPrompts() = emptyList<BuildHistoryPrompt>()
                override suspend fun markShown(prompt: BuildHistoryPrompt) {}
            },
            NativeHistoryTracker
        )
    }
    androidx.lifecycle.compose.LifecycleResumeEffect(navigator.snapshot.results, vm) {
        while (true) {
            val result = navigator.consumeResult(entryId, teamcityapp.features.filter_builds.api.navigation.BuildFilterResult::class) ?: break
            vm.applyFilter(result.filter.toTeamCityLocator())
        }
        while (true) {
            val result = navigator.consumeResult(entryId, teamcityapp.features.run_build.api.navigation.QueuedBuildResult::class) ?: break
            vm.onQueuedBuildResult(result.href)
        }
        onPauseOrDispose {}
    }
    val controls by vm.controls.collectAsStateWithLifecycle()
    val selection by vm.selection.collectAsStateWithLifecycle()
    LaunchedEffect(selection.query.locator, entryId) {
        navigator.updateDestination(entryId, destination.copy(locator = selection.query.locator))
    }
    key(selection.pages) {
        val builds = selection.rows.collectAsLazyPagingItems()
        val rows = PagingListSnapshot(builds)
        var completed by remember { mutableStateOf(false) }
        if (builds.loadState.refresh is LoadState.NotLoading) SideEffect { completed = true }
        val refresh = {
            vm.prepareRefresh()
            builds.refresh()
        }
        androidx.lifecycle.compose.LifecycleResumeEffect(controls.refreshPending, selection.pages) {
            if (vm.claimRefresh()) refresh()
            onPauseOrDispose {}
        }
        val snackbar = remember { SnackbarHostState() }
        val notice = controls.notice
        val message = notice?.let { stringResource(it.messageRes) }.orEmpty()
        val action = notice?.actionLabelRes?.let { stringResource(it) }
        val lifecycle = androidx.lifecycle.compose.LocalLifecycleOwner.current.lifecycle
        LaunchedEffect(notice?.id, lifecycle) {
            if (notice != null) {
                lifecycle.repeatOnLifecycle(androidx.lifecycle.Lifecycle.State.RESUMED) {
                    val result = snackbar.showSnackbar(message, action, duration = if (notice.kind == BuildHistoryNoticeKind.FiltersApplied) SnackbarDuration.Indefinite else SnackbarDuration.Long)
                    if (result == SnackbarResult.ActionPerformed) {
                        when (notice.kind) {
                            BuildHistoryNoticeKind.Queued -> vm.queuedBuildReference()?.let { navigator.push(BuildDetailsReferenceDestination(it, destination.configurationName)) }
                            BuildHistoryNoticeKind.FiltersApplied -> vm.resetFilters()
                            else -> Unit
                        }
                    }
                    vm.dismissNotice(notice.id)
                }
            }
        }
        SharedBuildHistoryScreen(
            destination.configurationName, historyListState(rows.items.map { it.build }, builds.loadState.refresh, completed),
            controls, rows.itemCount, rows::get, rows::peek,
            when (builds.loadState.append) {
                is LoadState.Loading -> HistoryAppendState.Loading
                is LoadState.Error -> HistoryAppendState.Error
                else -> HistoryAppendState.Idle
            },
            { navigator.pop() },
            { navigator.push(teamcityapp.features.run_build.api.navigation.RunBuildDestination(destination.configurationId, entryId)) },
            { navigator.push(teamcityapp.features.filter_builds.api.navigation.FilterBuildsDestination(destination.configurationId, entryId)) },
            vm::toggleFavorite, vm::retryFavorite, {}, refresh, refresh, builds::retry,
            { navigator.openHistoryBuild(entryId, it, destination.configurationName) },
            snackbar = snackbar
        )
    }
}

@Composable
private fun OverviewContent(destination: BuildDetailsReferenceDestination, graph: TeamCityAccountGraph, navigator: Navigator, entryId: String) {
    val vm = viewModel {
        BuildReferenceViewModel(
            destination.href,
            object : teamcityapp.features.build_overview.api.BuildOverviewRepository {
                override suspend fun build(href: String, forceRefresh: Boolean) = graph.build(href, true)
            },
            checkNotNull(graph.browsing).metadata
        )
    }
    val state by vm.state.collectAsStateWithLifecycle()
    LaunchedEffect(navigator.snapshot.results, entryId, state.build?.id) {
        val buildId = state.build?.id ?: return@LaunchedEffect
        while (true) {
            val result = navigator.consumeResult(entryId, teamcityapp.features.build_details.api.BuildDetailsSectionCountResult::class) ?: break
            if (result.buildId == buildId) vm.updateCount(result.section, result.count)
        }
    }
    val build = state.build
    if (build != null) {
        androidx.lifecycle.compose.LifecycleResumeEffect(build, navigator) {
            if (navigator.current.id == entryId) navigator.replace(teamcityapp.features.build_details.api.BuildDetailsDestination(build, destination.configurationName))
            onPauseOrDispose {}
        }
    }
    SharedTeamCityScreen(destination.configurationName.orEmpty(), { navigator.pop() }) { modifier ->
        SharedBuildOverviewScreen(state.copy(actions = emptyList()), vm::refresh, vm::retry, {}, modifier, rowActionAvailable = { false })
    }
}
