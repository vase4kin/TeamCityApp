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
import com.github.vase4kin.teamcityapp.di.modules.FakeTeamCityServiceImpl
import com.github.vase4kin.teamcityapp.di.modules.Mocks
import com.github.vase4kin.teamcityapp.di.modules.PendingFixture
import com.github.vase4kin.teamcityapp.helper.HiltApiTestRule
import com.github.vase4kin.teamcityapp.navigation.IncomingDestinationResolver
import com.github.vase4kin.teamcityapp.navigation.MainActivity
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit
import org.junit.Assert.*
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito.*
import org.mockito.Spy
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.doSuspendableAnswer
import org.mockito.kotlin.verifyBlocking
import org.mockito.kotlin.whenever
import teamcityapp.features.test_details.repository.models.TestOccurrence

@HiltAndroidTest
@RunWith(AndroidJUnit4::class)
class TestDetailsActivityTest {
    @get:org.junit.Rule(order = 6)
    val phaseThreeEvidence = com.github.vase4kin.teamcityapp.helper.PhaseThreeEvidenceRule()

    @JvmField
    @Rule(order = 0)
    val hiltRule = HiltAndroidRule(this)

    @JvmField
    @Rule(order = 1)
    val mockitoRule = org.mockito.junit.MockitoJUnit.rule()
        .strictness(org.mockito.quality.Strictness.LENIENT)

    @JvmField
    @Rule(order = 2)
    val apiRule = HiltApiTestRule(hiltRule) { service }

    @JvmField
    @Rule(order = 3)
    val compose = createEmptyComposeRule()

    @Spy private val service: TeamCityService = FakeTeamCityServiceImpl()
    private val app get() = InstrumentationRegistry.getInstrumentation().targetContext.applicationContext as TeamCityApplicationBase
    private val text = "Test details <tag> & literal text"

    @Before fun setUp() {
        com.github.vase4kin.teamcityapp.helper.TestUtils.disableOnboarding()
        app.appInjector.sharedUserStorage().clearAll()
        app.appInjector.sharedUserStorage().saveGuestUserAccountAndSetItAsActive(Mocks.URL, false)
    }

    private fun launch(url: String = "/guestAuth/app/rest/testOccurrences/id:fixture") = ActivityScenario.launch<MainActivity>(Intent().setComponent(android.content.ComponentName(app.packageName, IncomingDestinationResolver.TEST_DETAILS_COMPONENT)).putExtra(TestDetailsViewModel.ARG_TEST_URL, url))
    private fun awaitText(text: String) {
        compose.waitUntil(5_000) { compose.onAllNodesWithText(text).fetchSemanticsNodes().isNotEmpty() }
        compose.onNodeWithText(text).assertIsDisplayed()
    }

    @Test fun contentIsLiteralAndSurvivesConfigurationChange() {
        doReturn(TestOccurrence(text)).whenever(service) { testOccurrence(anyString()) }
        launch().use { scenario ->
            awaitText(text)
            compose.onNodeWithText("Details").assertIsDisplayed()
            scenario.recreate()
            awaitText(text)
            verifyBlocking(service, times(1)) { testOccurrence(anyString()) }
        }
    }

    @Test fun emptyDetailsAreExplicit() {
        doReturn(TestOccurrence("")).whenever(service) { testOccurrence(anyString()) }
        launch().use { awaitText("No test details") }
    }

    @Test fun failureCanBeRetried() {
        java.util.concurrent.atomic.AtomicInteger().let { responseIndex ->
            doSuspendableAnswer { invocation ->
                when (responseIndex.getAndIncrement()) {
                    0 -> {
                        throw IllegalStateException("offline")
                    }

                    else -> {
                        TestOccurrence(text)
                    }
                }
            }.whenever(service) { testOccurrence(anyString()) }
        }
        launch().use {
            awaitText(app.getString(teamcityapp.libraries.theme.R.string.error_load_title))
            compose.onNodeWithText(app.getString(teamcityapp.libraries.theme.R.string.error_load_message)).assertIsDisplayed()
            compose.onNodeWithText(app.getString(teamcityapp.libraries.theme.R.string.action_retry)).performClick()
            awaitText(text)
            compose.onNodeWithText(app.getString(teamcityapp.libraries.theme.R.string.action_retry)).assertDoesNotExist()
            verifyBlocking(service, times(2)) { testOccurrence(anyString()) }
        }
    }

    @Test fun stoppingCancelsAndReturningRestartsPendingRequest() {
        val pending = PendingFixture<TestOccurrence>()
        val started = CountDownLatch(1)
        java.util.concurrent.atomic.AtomicInteger().let { responseIndex ->
            doSuspendableAnswer { invocation ->
                when (responseIndex.getAndIncrement()) {
                    0 -> {
                        started.countDown()
                        pending.await()
                    }

                    else -> {
                        TestOccurrence(text)
                    }
                }
            }.whenever(service) { testOccurrence(anyString()) }
        }
        launch().use { scenario ->
            compose.waitUntil(10_000) { started.count == 0L }
            scenario.moveToState(Lifecycle.State.CREATED)
            compose.waitUntil(5_000) { !pending.isAwaiting }
            pending.complete(TestOccurrence("late"))
            scenario.moveToState(Lifecycle.State.RESUMED)
            awaitText(text)
            compose.onNodeWithText("late").assertDoesNotExist()
        }
    }

    @Test fun recreatingPendingRequestCancelsAndReloads() {
        val pending = PendingFixture<TestOccurrence>()
        val started = CountDownLatch(1)
        java.util.concurrent.atomic.AtomicInteger().let { responseIndex ->
            doSuspendableAnswer { invocation ->
                when (responseIndex.getAndIncrement()) {
                    0 -> {
                        started.countDown()
                        pending.await()
                    }

                    else -> {
                        TestOccurrence(text)
                    }
                }
            }.whenever(service) { testOccurrence(anyString()) }
        }
        launch().use { scenario ->
            compose.waitUntil(10_000) { started.count == 0L }
            scenario.recreate()
            awaitText(text)
            compose.waitUntil(5_000) { !pending.isAwaiting }
        }
    }

    @Test fun closeReturnsToHome() {
        doReturn(TestOccurrence(text)).whenever(service) { testOccurrence(anyString()) }
        launch().use { scenario ->
            awaitText(text)
            compose.onNodeWithContentDescription("Back").performClick()
            compose.waitUntil(5_000) { compose.onAllNodesWithTag("home:tab:Projects").fetchSemanticsNodes().isNotEmpty() }
        }
    }

    @Test fun missingInputCanReturnHomeWithoutARequest() {
        launch("").use { scenario ->
            awaitText("Details")
            verifyBlocking(service, never()) { testOccurrence(anyString()) }
            compose.onNodeWithContentDescription("Back").performClick()
            compose.waitUntil(5_000) { compose.onAllNodesWithTag("home:tab:Projects").fetchSemanticsNodes().isNotEmpty() }
        }
    }
}
