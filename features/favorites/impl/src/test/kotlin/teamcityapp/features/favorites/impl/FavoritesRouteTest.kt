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

package teamcityapp.features.favorites.impl

import android.app.Application
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.lifecycle.ViewModelStore
import kotlinx.coroutines.awaitCancellation
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode
import teamcityapp.features.favorites.api.FavoriteConfigurations
import teamcityapp.features.favorites.api.FavoritesRepository
import teamcityapp.features.favorites.impl.router.FavoritesRouter
import teamcityapp.libraries.build_configurations.BuildConfigurationSummary
import teamcityapp.libraries.build_configurations.ProjectReference
import teamcityapp.libraries.theme.TeamCityTheme

@OptIn(ExperimentalTestApi::class)
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35], application = Application::class, qualifiers = "en-rUS-w360dp-h800dp-mdpi")
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class FavoritesRouteTest {
    @get:Rule val compose = createComposeRule()
    private val store = ViewModelStore()
    private val project = ProjectReference("project", "Project")
    private val router = object : FavoritesRouter {
        override fun openDrawer() {}
        override fun openProject(project: ProjectReference) {}
        override fun openConfiguration(configuration: BuildConfigurationSummary) {}
    }

    @After fun tearDown() {
        store.clear()
    }
    private fun viewModel(load: suspend () -> FavoriteConfigurations) = FavoritesViewModel(object : FavoritesRepository {
        override suspend fun favorites(forceRefresh: Boolean) = load()
    }).also { store.put("favorites", it) }

    @Test fun initiallyHiddenHomeTabDoesNotStartLoadingUntilSelected() {
        var calls = 0
        val vm = viewModel {
            calls++
            FavoriteConfigurations(emptyList(), emptyList())
        }
        val visible = mutableStateOf(false)
        compose.setContent { TeamCityTheme { FavoritesRoute(router, vm, visible.value) } }
        compose.waitForIdle()
        assertEquals(0, calls)
        compose.runOnIdle { visible.value = true }
        compose.waitForIdle()
        compose.waitUntil(5_000) { calls >= 1 }
        compose.onNodeWithText("No favorite configurations added").assertIsDisplayed()
        assertEquals(1, calls)
    }

    @Test fun hiddenHomeTabCancelsPendingLoadAndReturnResumesIt() {
        var calls = 0
        var cancelled = false
        val vm = viewModel {
            calls++
            if (calls == 1) {
                try {
                    awaitCancellation()
                } finally {
                    cancelled = true
                }
            }
            FavoriteConfigurations(listOf("1"), listOf(BuildConfigurationSummary("1", "Android", null, project)))
        }
        val visible = mutableStateOf(true)
        compose.setContent { TeamCityTheme { FavoritesRoute(router, vm, visible.value) } }
        compose.waitUntil(5_000) { calls >= 1 }
        compose.runOnIdle { visible.value = false }
        compose.waitForIdle()
        compose.waitUntil(5_000) { cancelled }
        compose.runOnIdle { visible.value = true }
        compose.waitForIdle()
        compose.waitUntil(5_000) { calls >= 2 }
        compose.onNodeWithText("Android").assertIsDisplayed()
        assertEquals(2, calls)
    }

    @Test fun completedHomeTabKeepsScrollAcrossHiddenReturnAndCachedReload() {
        var calls = 0
        val rows = (1..30).map { BuildConfigurationSummary("$it", "Configuration $it", null, project) }
        val vm = viewModel {
            calls++
            FavoriteConfigurations(rows.map { it.id }, rows)
        }
        val visible = mutableStateOf(true)
        compose.setContent { TeamCityTheme { FavoritesRoute(router, vm, visible.value) } }
        compose.onNodeWithTag("favorites:list").performScrollToNode(hasTestTag("favorites:configuration:30"))
        compose.onNodeWithTag("favorites:configuration:30").assertIsDisplayed()
        compose.runOnIdle { visible.value = false }
        compose.waitForIdle()
        compose.runOnIdle { visible.value = true }
        compose.waitForIdle()
        compose.waitUntil(5_000) { calls >= 2 }
        compose.onNodeWithTag("favorites:configuration:30").assertIsDisplayed()
        assertEquals(2, calls)
    }
}
