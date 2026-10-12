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
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.junit4.StateRestorationTester
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
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
class NavigationHostTest {
    @get:Rule val compose = createComposeRule()

    class EntryViewModel : ViewModel() {
        var cleared = false
            private set
        override fun onCleared() {
            cleared = true
        }
    }

    private val models = mutableMapOf<String, EntryViewModel>()
    private fun registry() = DestinationRegistry(
        setOf(
            object : FeatureDestination<Root>(Root::class, Root.serializer()) {
                @Composable override fun Content(destination: Root, navigator: Navigator, entryId: String) {
                    content(entryId)
                }
            },
            object : FeatureDestination<Detail>(Detail::class, Detail.serializer()) {
                @Composable override fun Content(destination: Detail, navigator: Navigator, entryId: String) {
                    content(entryId)
                }
            }
        )
    )

    @Composable private fun content(entryId: String) {
        val model = viewModel<EntryViewModel>()
        SideEffect { models[entryId] = model }
        val count = rememberSaveable { mutableIntStateOf(0) }
        Button(onClick = { count.intValue++ }) { Text("$entryId:${count.intValue}") }
    }

    private fun settle() {
        compose.mainClock.advanceTimeBy(1_000)
        compose.waitForIdle()
    }

    @Test fun visitsScopeViewModelsAndBackRetainsThePreviousOwner() {
        lateinit var nav: Navigator
        compose.setContent {
            val registry = remember { registry() }
            nav = rememberNavigator(Root, registry)
            NavigationHost(nav, registry, {})
        }
        settle()
        val root = nav.current.id
        val rootModel = models.getValue(root)
        lateinit var first: String
        compose.runOnIdle { first = nav.push(Detail("same-build")) }
        settle()
        val firstModel = models.getValue(first)
        lateinit var second: String
        compose.runOnIdle { second = nav.push(Detail("same-build")) }
        settle()
        val secondModel = models.getValue(second)
        assertNotSame(firstModel, secondModel)
        assertFalse(rootModel.cleared)
        assertFalse(firstModel.cleared)
        compose.runOnIdle { nav.pop() }
        settle()
        assertTrue(secondModel.cleared)
        assertFalse(firstModel.cleared)
        assertSame(firstModel, models.getValue(nav.current.id))
        compose.runOnIdle { nav.pop() }
        settle()
        assertTrue(firstModel.cleared)
        assertSame(rootModel, models.getValue(nav.current.id))
        assertFalse(rootModel.cleared)
    }

    @Test fun accountResetClearsEveryPreviousOwnerWithIdenticalDestinationArguments() {
        lateinit var nav: Navigator
        compose.setContent {
            val registry = remember { registry() }
            nav = rememberNavigator(Root, registry)
            NavigationHost(nav, registry, {})
        }
        settle()
        compose.runOnIdle { nav.push(Detail("same-server")) }
        settle()
        val old = models.toMap()
        compose.runOnIdle { nav.reset(Root) }
        settle()
        assertTrue(old.values.all { it.cleared })
        assertFalse(models.getValue(nav.current.id).cleared)
        assertFalse(nav.current.id in old)
    }

    @Test fun savedStateRestoresVisitIdentityAndComposableState() {
        lateinit var nav: Navigator
        val restoration = StateRestorationTester(compose)
        restoration.setContent {
            val registry = remember { registry() }
            nav = rememberNavigator(Root, registry)
            NavigationHost(nav, registry, {})
        }
        settle()
        compose.runOnIdle { nav.push(Detail("project")) }
        settle()
        val before = nav.snapshot
        val visit = nav.current.id
        compose.onNodeWithText("$visit:0").performClick()
        settle()
        restoration.emulateSavedInstanceStateRestore()
        settle()
        assertEquals(before, nav.snapshot)
        compose.onNodeWithText("$visit:1").assertExists()
    }

    @Test fun restorationDiscardsStateFromAnAccountThatIsNoLongerActive() {
        lateinit var nav: Navigator
        var session = "account-one"
        val restoration = StateRestorationTester(compose)
        restoration.setContent { nav = rememberNavigator(Root, remember { testRegistry() }, session) }
        compose.runOnIdle {
            nav.push(Detail("filter"))
            nav.finish(Selected("previous-account"))
            nav.push(Detail("same-server-project"))
        }
        val previousRoot = nav.snapshot.entries.first().id
        // Change the current account while the composition is absent, as with process restoration.
        compose.runOnIdle { session = "account-two" }
        restoration.emulateSavedInstanceStateRestore()
        compose.runOnIdle {
            assertEquals(Root, nav.current.destination)
            assertNotEquals(previousRoot, nav.current.id)
            assertTrue(nav.snapshot.results.isEmpty())
        }
    }

    @Test fun savedStateRestoresUnconsumedResultsToTheirOriginalVisit() {
        lateinit var nav: Navigator
        val restoration = StateRestorationTester(compose)
        restoration.setContent { nav = rememberNavigator(Root, remember { testRegistry() }) }
        compose.runOnIdle {
            nav.push(Detail("filter"))
            nav.finish(Selected("branch"))
        }
        val before = nav.snapshot
        val recipient = nav.current.id
        restoration.emulateSavedInstanceStateRestore()
        compose.runOnIdle {
            assertEquals(before, nav.snapshot)
            assertEquals(Selected("branch"), nav.consumeResult(recipient, Selected::class))
            assertNull(nav.consumeResult(recipient, Selected::class))
        }
    }
}
