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

package teamcityapp.features.build_history.impl.navigation

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
import teamcityapp.features.build_details.api.BuildDetailsDestination
import teamcityapp.features.build_history.api.*
import teamcityapp.features.build_history.api.navigation.BuildHistoryDestination
import teamcityapp.features.build_history.impl.BuildHistoryRoute
import teamcityapp.features.build_history.impl.BuildHistoryViewModel
import teamcityapp.features.build_history.impl.router.BuildHistoryRouter
import teamcityapp.features.filter_builds.api.FilterBuildsResultAdapter
import teamcityapp.features.filter_builds.api.navigation.*
import teamcityapp.features.home.api.*
import teamcityapp.features.run_build.api.navigation.*
import teamcityapp.libraries.builds.BuildLaunchData
import teamcityapp.libraries.list_ui.ConfigurationRecreationEffect
import teamcityapp.libraries.navigation.*

class BuildHistoryDestinationRegistration @Inject constructor(
    private val filters: BuildHistoryFilterAdapter,
    private val filterResult: FilterBuildsResultAdapter
) : FeatureDestination<BuildHistoryDestination>(BuildHistoryDestination::class, BuildHistoryDestination.serializer()) {
    @Composable override fun Content(destination: BuildHistoryDestination, navigator: Navigator, entryId: String) {
        val activity = checkNotNull(LocalActivity.current)
        val arguments = remember(destination) {
            Bundle().apply {
                putString(BuildHistoryNavigation.EXTRA_ID, destination.configurationId)
                putString(BuildHistoryNavigation.EXTRA_NAME, destination.configurationName)
                destination.locator?.let { putString(BuildHistoryNavigation.EXTRA_LOCATOR, it) }
            }
        }
        DestinationArguments(arguments) {
            val model = hiltViewModel<BuildHistoryViewModel>()
            ConfigurationRecreationEffect(model, model::onConfigurationRecreation)
            LifecycleResumeEffect(navigator.snapshot.results, model) {
                while (true) {
                    val result = navigator.consumeResult(entryId, BuildFilterResult::class) ?: break
                    model.applyFilter(filters.locator(filterResult.serialize(result.filter)))
                }
                while (true) {
                    val result = navigator.consumeResult(entryId, QueuedBuildResult::class) ?: break
                    model.onQueuedBuildResult(result.href)
                }
                onPauseOrDispose {}
            }
            val router = remember(activity, navigator, entryId) {
                object : BuildHistoryRouter {
                    override fun navigateUp() {
                        if (!navigator.pop()) activity.finish()
                    }
                    override fun openBuild(build: BuildLaunchData, configurationName: String) {
                        navigator.push(BuildDetailsDestination(build, configurationName))
                    }
                    override fun openRunBuild(configurationId: String) {
                        navigator.push(RunBuildDestination(configurationId, entryId))
                    }
                    override fun openFilterBuilds(configurationId: String) {
                        navigator.push(FilterBuildsDestination(configurationId, entryId))
                    }
                    override fun openFavorites() {
                        val home = navigator.snapshot.entries.lastOrNull { it.destination is HomeDestination }
                        if (home == null) {
                            navigator.reset(HomeDestination(HomeTab.Favorites))
                        } else {
                            navigator.popTo(home.id, HomeTabSelection(HomeTab.Favorites))
                        }
                    }
                }
            }
            BuildHistoryRoute(router, model)
        }
    }
}

@Module
@InstallIn(SingletonComponent::class)
abstract class BuildHistoryDestinationModule {
    @Binds @IntoSet
    abstract fun registration(implementation: BuildHistoryDestinationRegistration): DestinationRegistration
}
