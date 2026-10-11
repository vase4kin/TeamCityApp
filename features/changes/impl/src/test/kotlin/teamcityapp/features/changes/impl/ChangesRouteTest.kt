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

package teamcityapp.features.changes.impl

import android.app.Application
import androidx.compose.ui.semantics.ProgressBarRangeInfo
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.StateRestorationTester
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModelStore
import kotlinx.coroutines.awaitCancellation
import org.junit.After
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode
import teamcityapp.features.change_details.api.ChangeDetails
import teamcityapp.features.changes.api.ChangesPage
import teamcityapp.features.changes.impl.router.ChangesRouter
import teamcityapp.libraries.theme.TeamCityTheme

@OptIn(ExperimentalTestApi::class)
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35], application = Application::class, qualifiers = "en-rUS-w360dp-h800dp-mdpi")
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class ChangesRouteTest {
    @get:Rule val compose = createComposeRule()
    private val store = ViewModelStore()
    private val repository = FakeChangesRepository()
    private val router = FakeRouter()

    @After fun cleanUp() {
        store.clear()
    }

    private fun render(restoration: StateRestorationTester? = null) {
        val vm = ChangesViewModel(SavedStateHandle(mapOf("url" to "build:42")), repository)
        store.put("changes", vm)
        if (restoration == null) {
            compose.setContent { TeamCityTheme { ChangesRoute(router, vm) } }
        } else {
            restoration.setContent { TeamCityTheme { ChangesRoute(router, vm) } }
        }
    }

    @Test fun routePublishesTabCountAndOpensHydratedDetailsThroughUiRouter() {
        render()
        compose.waitUntil(5_000) { router.counts.isNotEmpty() }
        assertEquals(listOf(1), router.counts)
        compose.waitUntil(5_000) { compose.onAllNodesWithTag("changes:change:42").fetchSemanticsNodes().isNotEmpty() }
        compose.onNodeWithTag("changes:change:42").performClick()
        assertEquals(change(), router.opened)
    }

    @Test fun unavailableCountPublishesCompatibilityZeroAndRetryUpdatesTabWithoutReloadingList() {
        repository.loadCount = { error("offline") }
        render()
        compose.waitUntil(5_000) { router.counts.isNotEmpty() }
        assertEquals(listOf(0), router.counts)
        compose.onNodeWithText("Couldn’t load the change count.").assertIsDisplayed()
        compose.waitUntil(5_000) { repository.requests.isNotEmpty() }
        val requests = repository.requests.size
        repository.loadCount = { 7 }
        compose.onNodeWithText("Retry count").performClick()
        compose.waitUntil(5_000) { router.counts.last() == 7 }
        assertEquals(listOf(0, 7), router.counts)
        assertEquals(requests, repository.requests.size)
    }

    @Test fun failedPullRefreshRetainsCompletedEmptyListAndProvidesRetry() {
        repository.loadCount = { 0 }
        repository.load = { request -> if (request.force) error("offline") else ChangesPage(emptyList(), null) }
        val restoration = StateRestorationTester(compose)
        render(restoration)
        compose.waitUntil(5_000) { compose.onAllNodesWithText("No changes").fetchSemanticsNodes().isNotEmpty() }
        restoration.emulateSavedInstanceStateRestore()
        compose.waitForIdle()
        compose.onNodeWithText("No changes").assertIsDisplayed()
        assertEquals(1, repository.requests.size)
        compose.onRoot().performTouchInput {
            swipe(start = center.copy(y = height * .15f), end = center.copy(y = height * .85f), durationMillis = 600)
        }
        compose.waitUntil(5_000) { compose.onAllNodesWithText("Couldn’t refresh. Try again.").fetchSemanticsNodes().isNotEmpty() }
        compose.onNodeWithText("No changes").assertIsDisplayed()
        compose.onNodeWithText("Retry").assertIsDisplayed()
        compose.onNodeWithText("Couldn’t load content").assertDoesNotExist()
        assertEquals(listOf(false, true), repository.requests.map { it.force })
    }

    @Test fun initialErrorRetryStartsNewGenerationAndBypassesCache() {
        repository.load = { request -> if (request.force) ChangesPage(listOf(change()), null) else error("offline") }
        render()
        compose.waitUntil(5_000) { compose.onAllNodesWithText("Try again").fetchSemanticsNodes().isNotEmpty() }
        compose.onNodeWithText("Try again").performClick()
        compose.waitUntil(5_000) { compose.onAllNodesWithTag("changes:change:42").fetchSemanticsNodes().isNotEmpty() }
        assertEquals(listOf(false, true), repository.requests.map { it.force })
    }

    @Test fun pendingInitialRequestDisplaysLoadingInsteadOfPrematureEmptyContent() {
        repository.load = { awaitCancellation() }
        render()
        compose.waitUntil(5_000) { repository.requests.isNotEmpty() }
        compose.onNode(hasProgressBarRangeInfo(ProgressBarRangeInfo.Indeterminate)).assertIsDisplayed()
        compose.onNodeWithText("No changes").assertDoesNotExist()
    }

    private class FakeRouter : ChangesRouter {
        val counts = mutableListOf<Int>()
        var opened: ChangeDetails? = null
        override fun updateTabCount(count: Int) {
            counts += count
        }
        override fun openChange(change: ChangeDetails) {
            opened = change
        }
    }
}
