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
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.junit4.StateRestorationTester
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.scene.SceneStrategyScope
import kotlinx.serialization.Serializable
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

@Serializable private data object FullScreen : NavKey

@OptIn(ExperimentalTestApi::class)
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35], application = Application::class, qualifiers = "en-rUS-w360dp-h800dp-mdpi")
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class BottomSheetNavigationTest {
    @get:Rule val compose = createComposeRule()
    private lateinit var nav: Navigator
    private lateinit var interactions: SheetInteractions
    private val lifecycles = mutableMapOf<String, Lifecycle>()
    private val resources = mutableMapOf<String, Int>()

    private fun registry() = DestinationRegistry(
        setOf(
            object : FeatureDestination<Root>(Root::class, Root.serializer()) {
                @Composable override fun Content(destination: Root, navigator: Navigator, entryId: String) {
                    Screen("root", entryId)
                }
            },
            object : FeatureDestination<Detail>(Detail::class, Detail.serializer()) {
                override val presentation = DestinationPresentation.BottomSheet

                @Composable override fun Content(destination: Detail, navigator: Navigator, entryId: String) {
                    val gate = checkNotNull(LocalSheetInteractions.current)
                    SideEffect { interactions = gate }
                    Screen("sheet", entryId)
                    Button(onClick = { navigator.pop() }) { Text("dismiss") }
                }
            },
            object : FeatureDestination<FullScreen>(FullScreen::class, FullScreen.serializer()) {
                @Composable override fun Content(destination: FullScreen, navigator: Navigator, entryId: String) {
                    Screen("full", entryId)
                }
            }
        )
    )

    @Composable private fun Screen(label: String, entryId: String) {
        val lifecycle = LocalLifecycleOwner.current.lifecycle
        SideEffect { lifecycles[entryId] = lifecycle }
        DisposableEffect(entryId) {
            resources[entryId] = resources.getOrDefault(entryId, 0) + 1
            onDispose { resources[entryId] = resources.getValue(entryId) - 1 }
        }
        val count = rememberSaveable { mutableIntStateOf(0) }
        Button(onClick = { count.intValue++ }) { Text("$label:${count.intValue}") }
    }

    private fun content() {
        compose.setContent {
            val registry = remember { registry() }
            nav = rememberNavigator(Root, registry)
            NavigationHost(nav, registry, {})
        }
        settle()
    }
    private fun settle() {
        compose.mainClock.advanceTimeBy(1_000)
        compose.waitForIdle()
    }

    @Test fun sheetOverlaysTheRootAndDismissalRestoresItsLifecycleAndState() {
        content()
        val rootId = nav.current.id
        compose.onNodeWithText("root:0").performClick()
        compose.runOnIdle { nav.push(Detail("filter")) }
        settle()
        val sheetId = nav.current.id
        compose.onNodeWithText("sheet:0").assertExists()
        compose.onNodeWithText("root:1").assertExists()
        assertEquals(1, resources.getValue(rootId))
        assertEquals(Lifecycle.State.STARTED, lifecycles.getValue(rootId).currentState)
        assertEquals(Lifecycle.State.RESUMED, lifecycles.getValue(sheetId).currentState)
        compose.onNodeWithText("dismiss").performClick()
        settle()
        assertEquals(rootId, nav.current.id)
        assertEquals(0, resources.getValue(sheetId))
        assertEquals(Lifecycle.State.RESUMED, lifecycles.getValue(rootId).currentState)
        compose.onNodeWithText("root:1").assertExists()
    }

    @Test fun aScreenOpenedFromASheetCanReturnToAndDismissThatSheet() {
        content()
        val rootId = nav.current.id
        compose.runOnIdle { nav.push(Detail("action")) }
        settle()
        val sheetId = nav.current.id
        compose.onNodeWithText("sheet:0").performClick()
        compose.runOnIdle { nav.push(FullScreen) }
        settle()
        compose.onNodeWithText("full:0").assertExists()
        compose.onNodeWithText("sheet:1").assertDoesNotExist()
        compose.runOnIdle { nav.pop() }
        settle()
        assertEquals(sheetId, nav.current.id)
        compose.onNodeWithText("sheet:1").assertExists()
        compose.onNodeWithText("dismiss").performClick()
        settle()
        assertEquals(rootId, nav.current.id)
    }

    @Test fun restorationPreservesTheSheetVisitAndBothSavedStates() {
        val restoration = StateRestorationTester(compose)
        restoration.setContent {
            val registry = remember { registry() }
            nav = rememberNavigator(Root, registry)
            NavigationHost(nav, registry, {})
        }
        settle()
        compose.onNodeWithText("root:0").performClick()
        compose.runOnIdle { nav.push(Detail("filter")) }
        settle()
        compose.onNodeWithText("sheet:0").performClick()
        val snapshot = nav.snapshot
        restoration.emulateSavedInstanceStateRestore()
        settle()
        assertEquals(snapshot, nav.snapshot)
        compose.onNodeWithText("sheet:1").assertExists()
        compose.onNodeWithText("root:1").assertExists()
        compose.onNodeWithText("dismiss").performClick()
        settle()
        compose.onNodeWithText("root:1").assertExists()
    }

    @Test fun busySheetsCannotBeDismissedThroughTheScrimAndUnlockAfterCompletion() {
        content()
        val rootId = nav.current.id
        compose.runOnIdle { nav.push(Detail("account")) }
        settle()
        val sheetId = nav.current.id
        compose.runOnIdle { interactions.updateEnabled(false) }
        settle()
        compose.onNodeWithContentDescription("Close sheet").assertDoesNotExist()
        assertEquals(sheetId, nav.current.id)
        compose.runOnIdle { interactions.updateEnabled(true) }
        settle()
        compose.onNodeWithContentDescription("Close sheet").performClick()
        settle()
        assertEquals(rootId, nav.current.id)
    }

    @Test(expected = IllegalArgumentException::class)
    fun aSheetCannotBeTheRootDestination() {
        val nav = Navigator(androidx.compose.runtime.mutableStateOf(NavigationSnapshot(listOf(NavigationEntry("sheet", Detail("filter"))))))
        val entry = registry().entry(nav.current, nav)
        with(BottomSheetSceneStrategy<NavigationEntry>()) { SceneStrategyScope<NavigationEntry>().calculateScene(listOf(entry)) }
    }
}
