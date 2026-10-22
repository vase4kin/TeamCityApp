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
import android.os.Bundle
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.junit4.StateRestorationTester
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelStore
import androidx.lifecycle.ViewModelStoreOwner
import androidx.lifecycle.createSavedStateHandle
import androidx.lifecycle.viewmodel.compose.LocalViewModelStoreOwner
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
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
class DestinationArgumentsTest {
    @get:Rule val compose = createComposeRule()
    class ArgumentViewModel(val handle: SavedStateHandle) : ViewModel()

    private val models = mutableMapOf<String, ArgumentViewModel>()
    private val argument = mutableStateOf("initial")
    private lateinit var nav: Navigator

    private fun registry() = DestinationRegistry(
        setOf(
            object : FeatureDestination<Detail>(Detail::class, Detail.serializer()) {
                @Composable override fun Content(destination: Detail, navigator: Navigator, entryId: String) {
                    val bundle = remember(destination.id, argument.value) {
                        Bundle().apply {
                            putString("project", destination.id)
                            putString("selection", argument.value)
                        }
                    }
                    DestinationArguments(bundle) {
                        val factory = remember { viewModelFactory { initializer { ArgumentViewModel(createSavedStateHandle()) } } }
                        val model = viewModel<ArgumentViewModel>(factory = factory)
                        models[entryId] = model
                        Button(onClick = { model.handle["selection"] = "edited" }) { Text("edit") }
                    }
                }
            }
        )
    )
    private fun settle() {
        compose.mainClock.advanceTimeBy(1_000)
        compose.waitForIdle()
    }

    @Test fun argumentsInitializeTheEntryHandleAndRecompositionDoesNotReplaceUserState() {
        compose.setContent {
            val registry = remember { registry() }
            nav = rememberNavigator(Detail("root"), registry)
            NavigationHost(nav, registry, {})
        }
        settle()
        val original = models.getValue(nav.current.id)
        assertEquals("root", original.handle.get<String>("project"))
        assertEquals("initial", original.handle.get<String>("selection"))
        compose.onNodeWithText("edit").performClick()
        compose.runOnIdle { argument.value = "new-default" }
        settle()
        assertSame(original, models.getValue(nav.current.id))
        assertEquals("edited", original.handle.get<String>("selection"))
    }

    @Test fun repeatedDestinationsHaveIndependentArgumentHandlesAndBackRestoresTheOriginal() {
        compose.setContent {
            val registry = remember { registry() }
            nav = rememberNavigator(Detail("same-project"), registry)
            NavigationHost(nav, registry, {})
        }
        settle()
        val original = models.getValue(nav.current.id)
        compose.onNodeWithText("edit").performClick()
        compose.runOnIdle { nav.push(Detail("same-project")) }
        settle()
        val repeated = models.getValue(nav.current.id)
        assertNotSame(original, repeated)
        assertEquals("initial", repeated.handle.get<String>("selection"))
        compose.runOnIdle { nav.pop() }
        settle()
        assertSame(original, models.getValue(nav.current.id))
        assertEquals("edited", original.handle.get<String>("selection"))
    }

    @Test fun aFreshViewModelAfterRestorationUsesSavedValuesInsteadOfNewArgumentDefaults() {
        val restoration = StateRestorationTester(compose)
        restoration.setContent {
            // A fresh parent store models process death, rather than configuration retention.
            val owner = remember {
                object : ViewModelStoreOwner {
                    override val viewModelStore = ViewModelStore()
                }
            }
            CompositionLocalProvider(LocalViewModelStoreOwner provides owner) {
                val registry = remember { registry() }
                nav = rememberNavigator(Detail("root"), registry)
                NavigationHost(nav, registry, {})
            }
        }
        settle()
        val original = models.getValue(nav.current.id)
        compose.onNodeWithText("edit").performClick()
        compose.runOnIdle { argument.value = "new-default" }
        restoration.emulateSavedInstanceStateRestore()
        settle()
        val restored = models.getValue(nav.current.id)
        assertNotSame(original, restored)
        assertEquals("root", restored.handle.get<String>("project"))
        assertEquals("edited", restored.handle.get<String>("selection"))
    }
}
