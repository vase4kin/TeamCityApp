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
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation3.runtime.NavKey
import kotlin.reflect.KClass
import kotlinx.coroutines.CancellationException
import kotlinx.serialization.KSerializer
import kotlinx.serialization.modules.SerializersModuleBuilder
import kotlinx.serialization.modules.polymorphic
import kotlinx.serialization.modules.subclass
import org.jetbrains.compose.resources.stringResource
import teamcityapp.features.build_details.api.BuildDetailsReferenceDestination
import teamcityapp.features.build_details.impl.tracker.BuildDetailsReferenceTrackerImpl
import teamcityapp.features.build_history.api.navigation.BuildHistoryDestination
import teamcityapp.features.build_history.impl.*
import teamcityapp.features.build_history.impl.tracker.BuildHistoryTrackerImpl
import teamcityapp.features.build_overview.impl.*
import teamcityapp.features.filter_builds.api.toTeamCityLocator
import teamcityapp.features.login.api.navigation.LoginDestination
import teamcityapp.features.login.impl.*
import teamcityapp.features.login.impl.tracker.LoginTrackerImpl
import teamcityapp.features.navigation.api.NavigationEntry as ProjectEntry
import teamcityapp.features.navigation.api.navigation.ProjectDestination
import teamcityapp.features.navigation.impl.*
import teamcityapp.features.navigation.impl.tracker.NavigationTrackerImpl
import teamcityapp.libraries.accounts.*
import teamcityapp.libraries.navigation.*
import teamcityapp.libraries.theme.*
import teamcityapp.shared.resources.*

internal val LocalSharedEducationVisible = staticCompositionLocalOf { false }

internal typealias AccountsDestination = teamcityapp.features.drawer.api.navigation.DrawerDestination

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

/** A platform-decoded launch carries public destination data and its exact account owner. */
data class SharedAppLaunch(val id: String, val destination: NavKey, val accountKey: String)

@Composable
fun TeamCityApp(
    model: SharedAppViewModel,
    platform: NativePlatform,
    launch: SharedAppLaunch? = null,
    onNavigator: (Navigator?) -> Unit = {},
    onExit: () -> Unit = {},
    onLaunchHandled: (String) -> Unit = {},
    ready: Boolean = true,
    chrome: @Composable () -> Unit = {}
) {
    CompositionLocalProvider(LocalNativePlatform provides platform) {
        val powerSaving by platform.powerSaving.collectAsStateWithLifecycle(false)
        val themeMode by platform.theme.theme.collectAsStateWithLifecycle(teamcityapp.libraries.app_theme.ThemeMode.System)
        val platformFailure by platform.failure.collectAsStateWithLifecycle()
        val session by model.sessions.state.collectAsStateWithLifecycle()
        val failed by model.restoreFailed.collectAsStateWithLifecycle()
        TeamCityTheme(
            when (themeMode) {
                teamcityapp.libraries.app_theme.ThemeMode.Light -> false
                teamcityapp.libraries.app_theme.ThemeMode.Dark -> true
                teamcityapp.libraries.app_theme.ThemeMode.AutoBattery -> powerSaving
                else -> androidx.compose.foundation.isSystemInDarkTheme()
            }
        ) {
            chrome()
            CompositionLocalProvider(LocalNativeAppLifecycle provides androidx.lifecycle.compose.LocalLifecycleOwner.current.lifecycle) {
                Surface(Modifier.fillMaxSize()) {
                    if (failed) {
                        SharedErrorContent(Modifier.fillMaxSize(), onRetry = model::restore)
                    } else {
                        if (!ready) {
                            LoadingContent(Modifier.fillMaxSize())
                        } else {
                            when (val current = session) {
                                AccountSessionState.Loading -> LoadingContent(Modifier.fillMaxSize())
                                AccountSessionState.SignedOut -> key("signed-out") { SharedJourney(model, null, launch, onNavigator, onExit, onLaunchHandled) }
                                is AccountSessionState.Active -> key(current.scope.generation) { SharedJourney(model, current.scope, launch, onNavigator, onExit, onLaunchHandled) }
                            }
                        }
                    }
                }
                platformFailure?.let { failure ->
                    NativeFailureDialog(failure, platform::clearFailure)
                }
            }
        }
    }
}

@Composable
private fun SharedJourney(model: SharedAppViewModel, account: AccountScope?, launch: SharedAppLaunch?, onNavigator: (Navigator?) -> Unit, onExit: () -> Unit, onLaunchHandled: (String) -> Unit) {
    val platform = LocalNativePlatform.current
    var graphFailure by remember(account) { mutableStateOf(false) }
    var graphRevision by remember(account) { mutableIntStateOf(0) }
    val graph by produceState<SharedAccountFeatures?>(null, account, graphRevision) {
        graphFailure = false
        try {
            if (account != null) value = SharedAccountFeatures(model.graph(account, platform.responseCache), platform.files, model.appLaunchId)
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
                SharedEntry(teamcityapp.features.home.api.HomeDestination::class, teamcityapp.features.home.api.HomeDestination.serializer(), { builder ->
                    builder.polymorphic(NavigationResult::class) { subclass(teamcityapp.features.home.api.HomeTabSelection::class, teamcityapp.features.home.api.HomeTabSelection.serializer()) }
                }) { destination, navigator, entryId -> SharedHomeContent(destination, checkNotNull(graph), navigator, result, entryId) },
                SharedEntry(LoginDestination::class, LoginDestination.serializer()) { _, _, _ -> LoginContent(model) },
                SharedEntry(AccountsDestination::class, AccountsDestination.serializer()) { _, navigator, _ -> SharedAccountContent(model, navigator) },
                SharedEntry(ProjectDestination::class, ProjectDestination.serializer()) { destination, navigator, _ -> ProjectsContent(destination, checkNotNull(graph), navigator) },
                SharedEntry(BuildHistoryDestination::class, BuildHistoryDestination.serializer()) { destination, navigator, entryId -> HistoryContent(destination, checkNotNull(graph), navigator, entryId) },
                SharedEntry(BuildDetailsReferenceDestination::class, BuildDetailsReferenceDestination.serializer()) { destination, navigator, entryId -> OverviewContent(destination, checkNotNull(graph), navigator, entryId) }
            ) + sharedAccountEntries(model) + sharedBrowsingEntries(graph, platform.analytics) + sharedManagementEntries(graph, platform.analytics) + sharedArtifactEntries(graph) + sharedSettingsEntries(graph)
        )
        result
    }
    val sessionKey = account?.identity?.key ?: "signed-out"
    val journeyMemory = remember { SharedJourneyMemory() }
    var restorationReady by remember { mutableStateOf(false) }
    var restorationFailed by remember { mutableStateOf(false) }
    var restorationRevision by remember { mutableIntStateOf(0) }
    var restored by remember { mutableStateOf<NavigationSnapshot?>(null) }
    LaunchedEffect(sessionKey, registry, restorationRevision) {
        restorationFailed = false
        val restoration = model.readInitialNavigation()
        if (restoration == NavigationRestoration.Failed) {
            restorationFailed = true
            return@LaunchedEffect
        }
        if (restoration is NavigationRestoration.Snapshot) {
            val decoded = decodeSharedSnapshot(restoration.encoded, registry, sessionKey)
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
    if (restorationFailed) {
        SharedErrorContent(Modifier.fillMaxSize(), { restorationRevision++ })
        return
    }
    if (!restorationReady) {
        LoadingContent(Modifier.fillMaxSize())
        return
    }
    val navigator = rememberNavigator(if (account == null) LoginDestination else teamcityapp.features.home.api.HomeDestination(), registry, sessionKey, restoredSnapshot = restored)
    DisposableEffect(navigator) {
        onNavigator(navigator)
        onDispose { onNavigator(null) }
    }
    var handledLaunch by androidx.compose.runtime.saveable.rememberSaveable { mutableStateOf<String?>(null) }
    LaunchedEffect(launch?.id, sessionKey) {
        if (launch != null && handledLaunch != launch.id) {
            handledLaunch = launch.id
            navigator.acceptLaunch(launch, sessionKey, account != null)
        }
        launch?.let { onLaunchHandled(it.id) }
    }
    val snapshot = navigator.snapshot
    val homes = journeyMemory.homes.filterKeys { id -> snapshot.entries.any { it.id == id } }
    var saveFailed by remember { mutableStateOf(false) }
    var saveRevision by remember { mutableIntStateOf(0) }
    LaunchedEffect(snapshot, homes, saveRevision) {
        saveFailed = !model.saveNavigation(sharedJourneyJson.encodeToString(SavedSharedJourney(registry.encodeSnapshot(snapshot), homes)))
    }
    CompositionLocalProvider(LocalSharedJourneyMemory provides journeyMemory) {
        val safeArea = WindowInsets.safeDrawing.only(WindowInsetsSides.Top + WindowInsetsSides.Horizontal).asPaddingValues()
        Column(Modifier.fillMaxSize().padding(safeArea).consumeWindowInsets(safeArea)) {
            NavigationHost(navigator, registry, onExit = onExit, modifier = Modifier.weight(1f))
            if (saveFailed) NavigationSaveError({ saveRevision++ })
        }
    }
}

@Composable
private fun LoginContent(model: SharedAppViewModel) {
    val analytics = LocalNativePlatform.current.analytics
    val vm = viewModel {
        LoginViewModel(
            model.authentication,
            PublicDemoLoginRepository(),
            LoginTrackerImpl(analytics)
        )
    }
    SharedLoginRoute(vm)
}

@Composable
private fun ProjectsContent(destination: ProjectDestination, graph: SharedAccountFeatures, navigator: Navigator) {
    val analytics = LocalNativePlatform.current.analytics
    val vm = viewModel {
        NavigationViewModel(
            destinationState(
                mapOf(
                    NavigationViewModel.PROJECT_ID to destination.projectId,
                    NavigationViewModel.PROJECT_NAME to destination.projectName,
                    NavigationViewModel.ROOT_SCREEN to destination.root
                )
            ),
            graph.navigation,
            graph.rating,
            NavigationTrackerImpl(analytics)
        )
    }
    val platform = LocalNativePlatform.current
    SharedNavigationRoute(
        vm,
        LocalSharedEducationVisible.current,
        { if (destination.root) navigator.push(AccountsDestination) else navigator.pop() },
        { entry ->
            when (entry) {
                is ProjectEntry.Project -> navigator.push(ProjectDestination(entry.reference.id, entry.reference.name))
                is ProjectEntry.Configuration -> navigator.push(BuildHistoryDestination(entry.configuration.id, entry.configuration.name))
            }
        },
        platform::rate
    )
}

@Composable
private fun HistoryContent(destination: BuildHistoryDestination, graph: SharedAccountFeatures, navigator: Navigator, entryId: String) {
    val analytics = LocalNativePlatform.current.analytics
    val vm = viewModel {
        BuildHistoryViewModel(
            destinationState(
                mapOf(
                    BuildHistoryViewModel.EXTRA_ID to destination.configurationId,
                    BuildHistoryViewModel.EXTRA_NAME to destination.configurationName,
                    BuildHistoryViewModel.EXTRA_LOCATOR to destination.locator
                )
            ),
            graph.history,
            graph.historyOnboarding,
            BuildHistoryTrackerImpl(analytics)
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
    SharedBuildHistoryRoute(
        vm, destination.configurationName,
        { navigator.updateDestination(entryId, destination.copy(locator = it)) },
        { navigator.pop() },
        { navigator.push(teamcityapp.features.run_build.api.navigation.RunBuildDestination(destination.configurationId, entryId)) },
        { navigator.push(teamcityapp.features.filter_builds.api.navigation.FilterBuildsDestination(destination.configurationId, entryId)) },
        { navigator.openBuildSnapshot(entryId, it, destination.configurationName) },
        { navigator.push(BuildDetailsReferenceDestination(it, destination.configurationName)) },
        { navigator.selectHome(teamcityapp.features.home.api.HomeTab.Favorites) }
    )
}

@Composable
private fun OverviewContent(destination: BuildDetailsReferenceDestination, graph: SharedAccountFeatures, navigator: Navigator, entryId: String) {
    val analytics = LocalNativePlatform.current.analytics
    val vm = viewModel {
        BuildReferenceViewModel(
            destination.href,
            object : teamcityapp.features.build_overview.api.BuildOverviewRepository {
                override suspend fun build(href: String, forceRefresh: Boolean) = graph.overview.build(href, true)
            },
            BuildDetailsReferenceTrackerImpl(analytics, destination.fromRestart),
            graph.metadata
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
    SharedBuildReferenceRoute(vm, destination.configurationName.orEmpty(), { navigator.pop() }) { build ->
        if (navigator.current.id == entryId) navigator.replace(teamcityapp.features.build_details.api.BuildDetailsDestination(build, destination.configurationName))
    }
}

@Composable
internal fun NavigationSaveError(onRetry: () -> Unit, modifier: Modifier = Modifier) {
    SharedErrorNotice(stringResource(Res.string.navigation_save_failed), onRetry, modifier.fillMaxWidth().navigationBarsPadding().testTag("navigation:save-error"))
}
