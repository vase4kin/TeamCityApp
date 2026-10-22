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
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.StateRestorationTester
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
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
class RetainedNavigationStacksTest {
    @get:Rule val compose = createComposeRule()

    class VisitModel : ViewModel() {
        var cleared = false
        var collectors = 0
        val content = MutableStateFlow("loaded").onSubscription { collectors++ }.onCompletion { collectors-- }
        override fun onCleared() {
            cleared = true
        }
    }

    private lateinit var parent: Navigator
    private lateinit var stacks: Map<String, Navigator>
    private lateinit var registry: DestinationRegistry
    private lateinit var selected: MutableState<String>
    private lateinit var activity: ComponentActivity
    private val models = mutableMapOf<String, VisitModel>()

    private fun registry() = DestinationRegistry(
        setOf(
            object : FeatureDestination<Root>(Root::class, Root.serializer()) {
                @Composable override fun Content(destination: Root, navigator: Navigator, entryId: String) {
                    selected = rememberSaveable { mutableStateOf("projects") }
                    stacks = listOf("projects", "favorites", "running", "queue", "agents").associateWith { id ->
                        key(id) {
                            rememberNavigator(Detail(id), registry, "${navigator.snapshot.sessionKey}/$entryId/$id", parent = navigator, useParent = { it == Detail("account") })
                        }
                    }
                    Column {
                        Text("bottom navigation")
                        RetainedNavigationStacks(stacks, selected.value, registry, entryId)
                    }
                }
            },
            object : FeatureDestination<Detail>(Detail::class, Detail.serializer()) {
                @Composable override fun Content(destination: Detail, navigator: Navigator, entryId: String) {
                    val model = viewModel<VisitModel>()
                    val value by model.content.collectAsStateWithLifecycle("initial")
                    SideEffect { models[entryId] = model }
                    val count = rememberSaveable { mutableIntStateOf(0) }
                    Button({ count.intValue++ }) { Text("${destination.id}:$value:${count.intValue}") }
                }
            }
        )
    )

    @Composable private fun host() {
        activity = checkNotNull(LocalActivity.current) as ComponentActivity
        registry = remember { registry() }
        parent = rememberNavigator(Root, registry)
        NavigationHost(parent, registry, {})
    }
    private fun start() {
        compose.setContent { host() }
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

    @Test fun switchingTabsRetainsTheEntireHistoryAndStopsHiddenCollectors() {
        start()
        val projects = stacks.getValue("projects")
        compose.runOnIdle {
            projects.push(Detail("project"))
            projects.push(Detail("history"))
            projects.push(Detail("build"))
        }
        settle()
        val visit = projects.current.id
        assertEquals(visit, parent.visibleSnapshot.entries.last().id)
        val model = models.getValue(visit)
        compose.onNodeWithText("build:loaded:0").performClick()
        select("favorites")
        assertEquals(parent.snapshot, parent.visibleSnapshot)
        assertEquals(0, model.collectors)
        assertFalse(model.cleared)
        compose.runOnIdle { stacks.getValue("favorites").push(Detail("another-build")) }
        settle()
        select("projects")
        assertEquals(visit, projects.current.id)
        assertSame(model, models.getValue(visit))
        assertEquals(1, model.collectors)
        compose.onNodeWithText("build:loaded:1").assertIsDisplayed()
        compose.onNodeWithText("bottom navigation").assertIsDisplayed()
        compose.runOnIdle { activity.onBackPressedDispatcher.onBackPressed() }
        settle()
        assertEquals(Detail("history"), projects.current.destination)
        assertTrue(model.cleared)
        select("favorites")
        compose.onNodeWithText("another-build:loaded:0").assertIsDisplayed()
        assertEquals(1, parent.snapshot.entries.size)
    }

    @Test fun restorationPreservesHiddenStacksVisitIdsAndScreenState() {
        val restoration = StateRestorationTester(compose)
        restoration.setContent { host() }
        settle()
        compose.runOnIdle { stacks.getValue("projects").push(Detail("history")) }
        settle()
        compose.onNodeWithText("history:loaded:0").performClick()
        select("queue")
        compose.runOnIdle { stacks.getValue("queue").push(Detail("build")) }
        settle()
        val before = stacks.mapValues { it.value.snapshot }
        restoration.emulateSavedInstanceStateRestore()
        settle()
        assertEquals("queue", selected.value)
        assertEquals(before, stacks.mapValues { it.value.snapshot })
        compose.onNodeWithText("build:loaded:0").assertIsDisplayed()
        select("projects")
        compose.onNodeWithText("history:loaded:1").assertIsDisplayed()
    }

    @Test fun resettingAccountClearsVisibleAndHiddenOwnersAndHistories() {
        start()
        compose.runOnIdle { stacks.getValue("projects").push(Detail("build")) }
        settle()
        select("running")
        compose.runOnIdle { stacks.getValue("running").push(Detail("same-build")) }
        settle()
        val previous = models.toMap()
        compose.runOnIdle { parent.reset(Root, "another-user-on-same-server") }
        settle()
        assertTrue(previous.values.all { it.cleared })
        assertEquals("projects", selected.value)
        assertTrue(stacks.values.all { it.snapshot.entries.size == 1 && it.snapshot.results.isEmpty() })
        assertFalse(stacks.getValue("projects").current.id in previous)
    }

    @Test fun globalAccountNavigationReturnsToTheSameNestedVisit() {
        start()
        val projects = stacks.getValue("projects")
        compose.runOnIdle { projects.push(Detail("history")) }
        settle()
        val visit = projects.current.id
        val model = models.getValue(visit)
        compose.runOnIdle { projects.push(Detail("account")) }
        settle()
        assertEquals(Detail("account"), parent.current.destination)
        assertEquals(visit, projects.current.id)
        compose.runOnIdle { parent.pop() }
        settle()
        assertEquals(visit, stacks.getValue("projects").current.id)
        assertSame(model, models.getValue(visit))
    }

    @Test fun returningToTabRootClearsItsDeeperOwnersAndKeepsOtherTabHistories() {
        start()
        val projects = stacks.getValue("projects")
        compose.runOnIdle { projects.push(Detail("project-history")) }
        settle()
        val projectVisit = projects.current.id
        select("favorites")
        val favorites = stacks.getValue("favorites")
        val root = favorites.current.id
        val rootModel = models.getValue(root)
        compose.runOnIdle { favorites.push(Detail("favorite-history")) }
        settle()
        val historyModel = models.getValue(favorites.current.id)
        compose.runOnIdle { favorites.push(Detail("favorite-build")) }
        settle()
        val buildModel = models.getValue(favorites.current.id)
        compose.runOnIdle { favorites.popToRoot() }
        settle()
        assertEquals(root, favorites.current.id)
        assertSame(rootModel, models.getValue(root))
        assertFalse(rootModel.cleared)
        assertTrue(historyModel.cleared)
        assertTrue(buildModel.cleared)
        select("projects")
        assertEquals(projectVisit, projects.current.id)
        compose.onNodeWithText("project-history:loaded:0").assertIsDisplayed()
    }
}
