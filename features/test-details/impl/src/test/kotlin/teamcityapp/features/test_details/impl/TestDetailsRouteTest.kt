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

package teamcityapp.features.test_details.impl

import android.app.Application
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModelStore
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode
import teamcityapp.features.test_details.api.TestDetailsRepository
import teamcityapp.features.test_details.impl.router.TestDetailsRouter
import teamcityapp.features.test_details.impl.tracker.TestDetailsTracker
import teamcityapp.libraries.theme.TeamCityTheme

@OptIn(ExperimentalTestApi::class)
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35], application = Application::class, qualifiers = "en-rUS-w360dp-h800dp-mdpi")
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class TestDetailsRouteTest {
    @get:Rule val compose = createComposeRule()
    private val store = ViewModelStore()

    @After fun tearDown() {
        compose.runOnIdle { store.clear() }
    }

    @Test fun tracksEachScreenEntryWithoutRetrackingOnRecomposition() {
        var trackedViews = 0
        var loads = 0
        var closes = 0
        val tracker = object : TestDetailsTracker {
            override fun trackView() { trackedViews++ }
        }
        fun router() = object : TestDetailsRouter {
            override fun close() { closes++ }
            override fun closeInvalidInput() = error("The input is valid")
        }
        val currentRouter = mutableStateOf(router())
        val visible = mutableStateOf(true)
        compose.setContent {
            val viewModel = remember {
                TestDetailsViewModel(
                    SavedStateHandle(mapOf(TestDetailsViewModel.ARG_TEST_URL to "/test")),
                    object : TestDetailsRepository {
                        override suspend fun testDetails(url: String): String {
                            loads++
                            return ""
                        }
                    },
                    tracker,
                ).also { store.put("details", it) }
            }
            TeamCityTheme {
                if (visible.value) TestDetailsRoute(currentRouter.value, viewModel)
            }
        }
        compose.onNodeWithText("No test details").assertIsDisplayed()
        compose.runOnIdle {
            assertEquals(1, trackedViews)
            currentRouter.value = router()
        }
        compose.runOnIdle { assertEquals(1, trackedViews) }
        compose.onNodeWithContentDescription("Close").performClick()
        compose.runOnIdle {
            assertEquals(1, closes)
            assertEquals(1, trackedViews)
            visible.value = false
        }
        compose.runOnIdle { visible.value = true }
        compose.onNodeWithText("No test details").assertIsDisplayed()
        compose.runOnIdle {
            assertEquals(2, trackedViews)
            assertEquals(1, loads)
        }
    }
}
