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

package teamcityapp.features.tests.impl

import android.app.Application
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModelStore
import kotlinx.coroutines.CompletableDeferred
import org.junit.After
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode
import teamcityapp.features.tests.api.*
import teamcityapp.features.tests.impl.router.TestsRouter
import teamcityapp.libraries.theme.TeamCityTheme

@OptIn(ExperimentalTestApi::class)
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35], application = Application::class, qualifiers = "en-rUS-w360dp-h800dp-mdpi")
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class TestsRouteTest {
    @get:Rule val compose = createComposeRule()
    private val store = ViewModelStore()
    private val repository = FakeTestsRepository()
    private val router = FakeRouter()

    @After fun cleanUp() {
        store.clear()
    }

    private fun render() {
        val vm = TestsViewModel(SavedStateHandle(mapOf("url" to "build:42", "passedCount" to 12, "failedCount" to 2, "ignoredCount" to 2)), repository)
        store.put("tests", vm)
        compose.setContent { TeamCityTheme { TestsRoute(router, vm) } }
    }

    @Test fun routePublishesTotalCountAndOpensFailedDetailsThroughUiRouter() {
        render()
        compose.waitUntil(5_000) { router.counts.isNotEmpty() }
        assertEquals(listOf(16), router.counts)
        compose.waitUntil(5_000) { compose.onAllNodesWithTag("tests:test:1").fetchSemanticsNodes().isNotEmpty() }
        compose.onNodeWithTag("tests:test:1").performClick()
        assertEquals("/testOccurrences/1", router.opened)
    }

    @Test fun filterSwitchClearsOldRowsBeforeNewPageCompletes() {
        val response = CompletableDeferred<TestsPage>()
        repository.load = { request -> if (request.filter == TestsFilter.Passed) response.await() else TestsPage(listOf(testOccurrence()), null) }
        render()
        compose.waitUntil(5_000) { compose.onAllNodesWithTag("tests:test:1").fetchSemanticsNodes().isNotEmpty() }
        compose.onNodeWithTag("tests:filter:Passed").performClick()
        compose.waitForIdle()
        compose.onNodeWithTag("tests:filter:Passed").assertIsSelected()
        compose.onNodeWithTag("tests:test:1").assertDoesNotExist()
        compose.runOnIdle { response.complete(TestsPage(listOf(testOccurrence("2", TestStatus.Passed)), null)) }
        compose.waitUntil(5_000) { compose.onAllNodesWithTag("tests:test:2").fetchSemanticsNodes().isNotEmpty() }
        compose.onNodeWithTag("tests:test:2").assertHasNoClickAction()
        assertEquals(listOf(TestsFilter.Failed, TestsFilter.Passed), repository.requests.map { it.filter })
        assertTrue(repository.requests.all { it.next == null && !it.force })
    }

    @Test fun initialErrorRetryBypassesCacheAsLegacyPullRefreshDid() {
        repository.load = { request -> if (!request.force) error("offline") else TestsPage(listOf(testOccurrence()), null) }
        render()
        compose.waitUntil(5_000) { compose.onAllNodesWithText("Try again").fetchSemanticsNodes().isNotEmpty() }
        compose.onNodeWithText("Try again").performClick()
        compose.waitUntil(5_000) { compose.onAllNodesWithTag("tests:test:1").fetchSemanticsNodes().isNotEmpty() }
        assertEquals(listOf(false, true), repository.requests.map { it.force })
    }

    @Test fun failedPullRefreshRetainsCompletedEmptyList() {
        repository.load = { request -> if (request.force) error("offline") else TestsPage(emptyList(), null) }
        render()
        compose.waitUntil(5_000) { compose.onAllNodesWithText("There are no failed tests").fetchSemanticsNodes().isNotEmpty() }
        compose.onRoot().performTouchInput {
            swipe(start = center.copy(y = height * .15f), end = center.copy(y = height * .85f), durationMillis = 600)
        }
        compose.waitUntil(5_000) { compose.onAllNodesWithText("Couldn’t refresh. Try again.").fetchSemanticsNodes().isNotEmpty() }
        compose.onNodeWithText("There are no failed tests").assertIsDisplayed()
        compose.onNodeWithText("Couldn’t load content").assertDoesNotExist()
        assertEquals(listOf(false, true), repository.requests.map { it.force })
    }

    @Test fun countFailureRetryUpdatesTabWithoutReloadingPages() {
        repository.loadCount = { error("offline") }
        render()
        compose.waitUntil(5_000) { router.counts.isNotEmpty() }
        assertEquals(listOf(0), router.counts)
        compose.waitUntil(5_000) { repository.requests.isNotEmpty() }
        val requests = repository.requests.size
        repository.loadCount = { 21 }
        compose.onNodeWithText("Retry count").performClick()
        compose.waitUntil(5_000) { router.counts.last() == 21 }
        assertEquals(requests, repository.requests.size)
        assertEquals(listOf(0, 21), router.counts)
    }

    private class FakeRouter : TestsRouter {
        val counts = mutableListOf<Int>()
        var opened: String? = null
        override fun updateTabCount(count: Int) {
            counts += count
        }
        override fun openFailedTest(url: String) {
            opened = url
        }
    }
}
