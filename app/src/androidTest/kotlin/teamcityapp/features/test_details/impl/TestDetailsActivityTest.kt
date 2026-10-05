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

import android.content.Intent
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createEmptyComposeRule
import androidx.lifecycle.Lifecycle
import androidx.test.core.app.ActivityScenario
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.github.vase4kin.teamcityapp.TeamCityApplicationBase
import com.github.vase4kin.teamcityapp.api.TeamCityService
import com.github.vase4kin.teamcityapp.dagger.modules.FakeTeamCityServiceImpl
import com.github.vase4kin.teamcityapp.dagger.modules.Mocks
import com.github.vase4kin.teamcityapp.helper.HiltApiTestRule
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import io.reactivex.Single
import io.reactivex.subjects.SingleSubject
import org.junit.Assert.*
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito.*
import org.mockito.Spy
import teamcityapp.features.test_details.repository.models.TestOccurrence
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit

@HiltAndroidTest
@RunWith(AndroidJUnit4::class)
class TestDetailsActivityTest {
    @JvmField @Rule(order = 0) val hiltRule = HiltAndroidRule(this)
    @JvmField @Rule(order = 1) val mockitoRule = org.mockito.junit.MockitoJUnit.rule()
        .strictness(org.mockito.quality.Strictness.LENIENT)
    @JvmField @Rule(order = 2) val apiRule = HiltApiTestRule(hiltRule) { service }
    @JvmField @Rule(order = 3) val compose = createEmptyComposeRule()
    @Spy private val service: TeamCityService = FakeTeamCityServiceImpl()
    private val app get() = InstrumentationRegistry.getInstrumentation().targetContext.applicationContext as TeamCityApplicationBase
    private val text = "Test details <tag> & literal text"

    @Before fun setUp() {
        app.appInjector.sharedUserStorage().clearAll()
        app.appInjector.sharedUserStorage().saveGuestUserAccountAndSetItAsActive(Mocks.URL, false)
    }

    private fun launch(url: String = "/test") = ActivityScenario.launch<TestDetailsActivity>(
        Intent(app, TestDetailsActivity::class.java).putExtra(TestDetailsViewModel.ARG_TEST_URL, url))
    private fun awaitText(text: String) {
        compose.waitUntil(5_000) { compose.onAllNodesWithText(text).fetchSemanticsNodes().isNotEmpty() }
        compose.onNodeWithText(text).assertIsDisplayed()
    }

    @Test fun contentIsLiteralAndSurvivesConfigurationChange() {
        `when`(service.testOccurrence(anyString())).thenReturn(Single.just(TestOccurrence(text)))
        launch().use { scenario ->
            awaitText(text)
            compose.onNodeWithText("Details").assertIsDisplayed()
            scenario.recreate()
            awaitText(text)
            verify(service, times(1)).testOccurrence(anyString())
        }
    }

    @Test fun emptyDetailsAreExplicit() {
        `when`(service.testOccurrence(anyString())).thenReturn(Single.just(TestOccurrence("")))
        launch().use { awaitText("No test details") }
    }

    @Test fun failureCanBeRetried() {
        `when`(service.testOccurrence(anyString())).thenReturn(Single.error(IllegalStateException("offline")), Single.just(TestOccurrence(text)))
        launch().use {
            awaitText("There's an error loading the page")
            compose.onNodeWithText("TRY AGAIN").performClick()
            awaitText(text)
            compose.onNodeWithText("TRY AGAIN").assertDoesNotExist()
            verify(service, times(2)).testOccurrence(anyString())
        }
    }

    @Test fun stoppingCancelsAndReturningRestartsPendingRequest() {
        val pending = SingleSubject.create<TestOccurrence>()
        val started = CountDownLatch(1)
        `when`(service.testOccurrence(anyString())).thenReturn(pending.doOnSubscribe { started.countDown() }, Single.just(TestOccurrence(text)))
        launch().use { scenario ->
            assertTrue(started.await(5, TimeUnit.SECONDS))
            scenario.moveToState(Lifecycle.State.CREATED)
            compose.waitUntil(5_000) { !pending.hasObservers() }
            pending.onSuccess(TestOccurrence("late"))
            scenario.moveToState(Lifecycle.State.RESUMED)
            awaitText(text)
            compose.onNodeWithText("late").assertDoesNotExist()
        }
    }

    @Test fun recreatingPendingRequestCancelsAndReloads() {
        val pending = SingleSubject.create<TestOccurrence>()
        val started = CountDownLatch(1)
        `when`(service.testOccurrence(anyString())).thenReturn(pending.doOnSubscribe { started.countDown() }, Single.just(TestOccurrence(text)))
        launch().use { scenario ->
            assertTrue(started.await(5, TimeUnit.SECONDS))
            scenario.recreate()
            awaitText(text)
            compose.waitUntil(5_000) { !pending.hasObservers() }
        }
    }

    @Test fun closeFinishesActivity() {
        `when`(service.testOccurrence(anyString())).thenReturn(Single.just(TestOccurrence(text)))
        launch().use { scenario ->
            awaitText(text)
            compose.onNodeWithContentDescription("Close").performClick()
            compose.waitUntil(5_000) { scenario.state == Lifecycle.State.DESTROYED }
        }
    }

    @Test fun missingInputClosesWithoutRequest() {
        launch("").use { scenario ->
            compose.waitUntil(5_000) { scenario.state == Lifecycle.State.DESTROYED }
            verify(service, never()).testOccurrence(anyString())
        }
    }
}
