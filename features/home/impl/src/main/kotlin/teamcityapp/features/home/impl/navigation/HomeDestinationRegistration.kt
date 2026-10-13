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

package teamcityapp.features.home.impl.navigation

import android.os.Bundle
import androidx.activity.compose.LocalActivity
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.windowsizeclass.*
import androidx.compose.runtime.*
import androidx.compose.ui.res.stringResource
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.LifecycleResumeEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import dagger.multibindings.IntoSet
import javax.inject.Inject
import javax.inject.Provider
import kotlinx.serialization.modules.*
import teamcityapp.features.agents.api.navigation.AgentsDestination
import teamcityapp.features.build_queue.api.navigation.BuildQueueDestination
import teamcityapp.features.drawer.api.navigation.DrawerDestination
import teamcityapp.features.favorites.api.LocalFavoritesProjectsAction
import teamcityapp.features.favorites.api.navigation.FavoritesDestination
import teamcityapp.features.home.api.*
import teamcityapp.features.home.impl.*
import teamcityapp.features.home.impl.R
import teamcityapp.features.home.impl.tracker.*
import teamcityapp.features.navigation.api.navigation.ProjectDestination
import teamcityapp.features.running_builds.api.navigation.RunningBuildsDestination
import teamcityapp.libraries.navigation.*

class HomeDestinationRegistration @Inject constructor(private val registry: Provider<DestinationRegistry>) : FeatureDestination<HomeDestination>(HomeDestination::class, HomeDestination.serializer()) {
    override fun registerSerializers(builder: SerializersModuleBuilder) {
        super.registerSerializers(builder)
        builder.polymorphic(NavigationResult::class) { subclass(HomeTabSelection::class, HomeTabSelection.serializer()) }
    }

    @OptIn(ExperimentalMaterial3WindowSizeClassApi::class)
    @Composable
    override fun Content(destination: HomeDestination, navigator: Navigator, entryId: String) {
        val arguments = remember(destination) { Bundle().apply { putInt(HomeViewModel.SELECTED_TAB, destination.tab.ordinal) } }
        DestinationArguments(arguments) {
            val model = hiltViewModel<HomeViewModel>()
            LifecycleResumeEffect(navigator.snapshot.results, model) {
                while (true) {
                    val selection = navigator.consumeResult(entryId, HomeTabSelection::class) ?: break
                    model.select(selection.tab)
                }
                onPauseOrDispose {}
            }
            val state by model.state.collectAsStateWithLifecycle()
            val snackbar = remember { SnackbarHostState() }
            val buildsFiltered = stringResource(R.string.text_filters_applied)
            val agentsFiltered = stringResource(R.string.text_agents_filters_applied)
            var previousQuery by remember(entryId) { mutableStateOf<HomeQuery?>(null) }
            LaunchedEffect(state.query) {
                val current = state.query
                val previous = previousQuery
                previousQuery = current
                if (current != null && previous != null && current.accountKey == previous.accountKey) {
                    when {
                        current.agentsFilter != previous.agentsFilter -> snackbar.showSnackbar(agentsFiltered)
                        current.runningFilter != previous.runningFilter || current.queueFilter != previous.queueFilter -> snackbar.showSnackbar(buildsFiltered)
                    }
                }
            }
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
                    rememberNavigator(child.destination, registry.get(), "${navigator.snapshot.sessionKey}/${child.id}", parent = navigator, useParent = { it is DrawerDestination })
                }
            }
            val selectedId = "$entryId/${state.selected.name}"
            val selectedStack = stacks.getValue(selectedId)
            LifecycleResumeEffect(model) {
                model.viewed()
                onPauseOrDispose {}
            }
            val onTabSelected: (HomeTab) -> Unit = { tab ->
                if (tab == state.selected) selectedStack.popToRoot()
                model.select(tab)
            }
            val onAccount: () -> Unit = { navigator.push(DrawerDestination) }
            CompositionLocalProvider(
                LocalFavoritesProjectsAction provides {
                    model.fabClicked()
                    model.addProjects()
                }
            ) {
                HomeNavigationScreen(
                    state,
                    calculateWindowSizeClass(checkNotNull(LocalActivity.current)).widthSizeClass != WindowWidthSizeClass.Compact,
                    stacks,
                    registry.get(),
                    entryId,
                    onTabSelected = onTabSelected,
                    onFilter = model::selectFilter,
                    onRetry = model::retry,
                    onDismissPrompt = model::dismissPrompt,
                    snackbar = snackbar,
                    onAccount = onAccount,
                    onActivity = {
                        if (state.isActivity) selectedStack.popToRoot()
                        model.selectActivity()
                    }
                )
            }
        }
    }
}

@Module
@InstallIn(SingletonComponent::class)
abstract class HomeModule {
    @Binds @IntoSet
    abstract fun destination(implementation: HomeDestinationRegistration): DestinationRegistration

    @Binds abstract fun tracker(implementation: FirebaseHomeTracker): HomeTracker
}
