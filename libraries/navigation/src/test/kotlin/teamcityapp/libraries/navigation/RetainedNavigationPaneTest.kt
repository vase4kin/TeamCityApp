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

package teamcityapp.libraries.navigation

import android.app.Application
import androidx.activity.ComponentActivity
import androidx.activity.compose.LocalActivity
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.junit4.StateRestorationTester
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LifecycleRegistry
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.flow.onSubscription
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

@OptIn(ExperimentalTestApi::class)
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35], application = Application::class, qualifiers = "en-rUS-w360dp-h800dp-mdpi")
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class RetainedNavigationPaneTest {
    @get:Rule val compose = createComposeRule()

    class ChildViewModel : ViewModel() {
        var cleared = false
        var collectors = 0
        var viewResources = 0
        private val content = MutableStateFlow("loaded")
        val updates = content.onSubscription { collectors++ }.onCompletion { collectors-- }
        override fun onCleared() {
            cleared = true
        }
    }

    private val models = mutableMapOf<String, ChildViewModel>()
    private val selected = mutableStateOf("projects")
    private lateinit var nav: Navigator
    private lateinit var activity: ComponentActivity

    private fun registry() = DestinationRegistry(
        setOf(
            object : FeatureDestination<Root>(Root::class, Root.serializer()) {
                @Composable override fun Content(destination: Root, navigator: Navigator, entryId: String) {
                    val children = remember(entryId) {
                        listOf("projects", "favorites", "agents").map { NavigationEntry("$entryId/$it", Detail(it)) }
                    }
                    RetainedNavigationPane(children, "$entryId/${selected.value}", navigator, registry)
                }
            },
            object : FeatureDestination<Detail>(Detail::class, Detail.serializer()) {
                @Composable override fun Content(destination: Detail, navigator: Navigator, entryId: String) {
                    val model = viewModel<ChildViewModel>()
                    val value by model.updates.collectAsStateWithLifecycle("initial")
                    SideEffect { models[entryId] = model }
                    DisposableEffect(model) {
                        model.viewResources++
                        onDispose { model.viewResources-- }
                    }
                    val count = rememberSaveable { mutableIntStateOf(0) }
                    Button(onClick = { count.intValue++ }) { Text("${destination.id}:$value:${count.intValue}") }
                }
            }
        )
    )

    private lateinit var registry: DestinationRegistry
    private fun content(cap: Lifecycle? = null) {
        compose.setContent {
            activity = checkNotNull(LocalActivity.current) as ComponentActivity
            registry = remember { registry() }
            nav = rememberNavigator(Root, registry)
            CompositionLocalProvider(LocalNavigationContentLifecycle provides cap) { NavigationHost(nav, registry, {}) }
        }
        settle()
    }
    private fun settle() {
        compose.mainClock.advanceTimeBy(1_000)
        compose.waitForIdle()
    }
    private fun select(tab: String) {
        compose.runOnIdle { selected.value = tab }
        settle()
    }
    private fun model(tab: String) = models.getValue("${nav.current.id}/$tab")

    @Test fun incomingRequestsPauseChildCollectorsWithoutClearingTheirOwners() {
        val cap = object : LifecycleOwner {
            val registry = LifecycleRegistry(this)
            override val lifecycle: Lifecycle get() = registry
        }
        compose.runOnUiThread { cap.registry.currentState = Lifecycle.State.RESUMED }
        content(cap.lifecycle)
        val projects = model("projects")
        assertEquals(1, projects.collectors)
        compose.runOnIdle { cap.registry.currentState = Lifecycle.State.CREATED }
        settle()
        assertEquals(0, projects.collectors)
        assertFalse(projects.cleared)
        compose.runOnIdle { cap.registry.currentState = Lifecycle.State.RESUMED }
        settle()
        assertSame(projects, model("projects"))
        assertEquals(1, projects.collectors)
        compose.runOnIdle { cap.registry.currentState = Lifecycle.State.CREATED }
        settle()
        compose.runOnIdle { nav.reset(Root, "next-account") }
        settle()
        assertTrue(projects.cleared)
        val replacement = model("projects")
        assertNotSame(projects, replacement)
        assertEquals(0, replacement.collectors)
        compose.runOnIdle { cap.registry.currentState = Lifecycle.State.RESUMED }
        settle()
        assertEquals(1, replacement.collectors)
    }

    @Test fun hiddenTabsStopCollectorsAndReleaseViewsWhileRetainingModelsAndUiState() {
        content()
        val projects = model("projects")
        assertEquals(1, projects.collectors)
        assertEquals(1, projects.viewResources)
        compose.onNodeWithText("projects:loaded:0").performClick()
        select("favorites")
        val favorites = model("favorites")
        assertNotSame(projects, favorites)
        assertFalse(projects.cleared)
        assertEquals(0, projects.collectors)
        assertEquals(0, projects.viewResources)
        assertEquals(1, favorites.collectors)
        select("projects")
        assertSame(projects, model("projects"))
        assertEquals(1, projects.collectors)
        assertEquals(0, favorites.collectors)
        compose.onNodeWithText("projects:loaded:1").assertExists()
        assertEquals(1, nav.snapshot.entries.size)
    }

    @Test fun leavingAndReturningToParentRetainsAllChildrenButRemovingParentClearsThem() {
        content()
        val parentId = nav.current.id
        val projects = model("projects")
        select("favorites")
        val favorites = model("favorites")
        compose.runOnIdle { nav.push(Detail("build")) }
        settle()
        assertFalse(projects.cleared)
        assertFalse(favorites.cleared)
        assertEquals(0, favorites.collectors)
        compose.runOnIdle { nav.pop() }
        settle()
        assertEquals(parentId, nav.current.id)
        assertSame(favorites, model("favorites"))
        assertEquals(1, favorites.collectors)
        compose.runOnIdle { nav.reset(Root, "next-account") }
        settle()
        assertTrue(projects.cleared)
        assertTrue(favorites.cleared)
        assertNotSame(favorites, model("favorites"))
    }

    @Test fun hiddenAndSelectedSaveableTabStateSurvivesRestoration() {
        val restoration = StateRestorationTester(compose)
        restoration.setContent {
            registry = remember { registry() }
            nav = rememberNavigator(Root, registry)
            NavigationHost(nav, registry, {})
        }
        settle()
        compose.onNodeWithText("projects:loaded:0").performClick()
        select("favorites")
        compose.onNodeWithText("favorites:loaded:0").performClick()
        val parentId = nav.current.id
        restoration.emulateSavedInstanceStateRestore()
        settle()
        assertEquals(parentId, nav.current.id)
        compose.onNodeWithText("favorites:loaded:1").assertExists()
        select("projects")
        compose.onNodeWithText("projects:loaded:1").assertExists()
    }

    @Test fun systemBackPopsTheParentVisitWithoutNavigatingToASiblingTab() {
        content()
        val originalParent = nav.current.id
        select("favorites")
        val originalFavorites = model("favorites")
        compose.runOnIdle { nav.push(Root) }
        settle()
        val repeatedFavorites = model("favorites")
        assertNotSame(originalFavorites, repeatedFavorites)
        compose.runOnIdle { activity.onBackPressedDispatcher.onBackPressed() }
        settle()
        assertEquals(originalParent, nav.current.id)
        assertEquals("favorites", selected.value)
        assertSame(originalFavorites, model("favorites"))
        assertTrue(repeatedFavorites.cleared)
    }
}
