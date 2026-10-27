/*
 * Copyright 2019 Andrey Tolpeev
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

package com.github.vase4kin.teamcityapp.tests.view

import android.content.Intent
import android.os.Bundle
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.v2.createEmptyComposeRule
import androidx.test.core.app.ActivityScenario
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.Espresso.pressBack
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.action.ViewActions.scrollTo
import androidx.test.espresso.intent.Intents.assertNoUnverifiedIntents
import androidx.test.espresso.intent.Intents.intended
import androidx.test.espresso.intent.matcher.BundleMatchers.hasEntry
import androidx.test.espresso.intent.matcher.IntentMatchers.hasComponent
import androidx.test.espresso.intent.matcher.IntentMatchers.hasExtras
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.github.vase4kin.teamcityapp.TeamCityApplicationBase
import com.github.vase4kin.teamcityapp.api.TeamCityService
import com.github.vase4kin.teamcityapp.base.extractor.BundleExtractorValues
import com.github.vase4kin.teamcityapp.di.modules.FakeTeamCityServiceImpl
import com.github.vase4kin.teamcityapp.di.modules.Mocks
import com.github.vase4kin.teamcityapp.helper.CustomIntentsTestRule
import com.github.vase4kin.teamcityapp.helper.HiltApiTestRule
import com.github.vase4kin.teamcityapp.helper.TestUtils
import com.github.vase4kin.teamcityapp.helper.currentRootSnapshot
import com.github.vase4kin.teamcityapp.helper.tapBuildDetailsSection
import com.github.vase4kin.teamcityapp.navigation.IncomingDestinationResolver
import com.github.vase4kin.teamcityapp.navigation.MainActivity
import com.github.vase4kin.teamcityapp.tests.api.TestOccurrences
import com.google.android.material.tabs.TabLayout
import com.google.gson.Gson
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicInteger
import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.core.AllOf.allOf
import org.junit.*
import org.junit.runner.RunWith
import org.mockito.ArgumentMatchers.anyString
import org.mockito.Mockito.`when`
import org.mockito.Spy
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.doSuspendableAnswer
import org.mockito.kotlin.whenever
import teamcityapp.features.test_details.api.navigation.TestDetailsDestination
import teamcityapp.features.test_details.impl.TestDetailsViewModel
import teamcityapp.features.tests.impl.R as TestsR
import teamcityapp.libraries.navigation.DestinationRegistry

private const val FAILED_URL = "/guestAuth/app/rest/testOccurrences?locator=build:(id:835695),status:FAILURE,count:10"
private const val PASSED_URL = "/guestAuth/app/rest/testOccurrences?locator=build:(id:835695),status:SUCCESS,count:10"
private const val IGNORED_URL = "/guestAuth/app/rest/testOccurrences?locator=build:(id:835695),status:UNKNOWN,count:10"
private const val SUMMARY_URL = "/guestAuth/app/rest/testOccurrences?locator=build:(id:835695),count:2147483647&fields=count,passed,failed,ignored"

/** Tests is a separate section visit that returns fresh metadata to Build Details. */
@HiltAndroidTest
@RunWith(AndroidJUnit4::class)
class TestOccurrencesFragmentTest {
    @get:org.junit.Rule(order = 6)
    val phaseThreeEvidence = com.github.vase4kin.teamcityapp.helper.PhaseThreeEvidenceRule()

    @JvmField
    @Rule(order = 0)
    val hiltRule = HiltAndroidRule(this)

    @JvmField
    @Rule(order = 1)
    val mockitoRule = org.mockito.junit.MockitoJUnit.rule().strictness(org.mockito.quality.Strictness.LENIENT)

    @JvmField
    @Rule(order = 2)
    val apiRule = HiltApiTestRule(hiltRule) { teamCityService }

    @JvmField
    @Rule(order = 3)
    val activityRule = CustomIntentsTestRule(MainActivity::class.java, IncomingDestinationResolver.BUILD_DETAILS_COMPONENT)

    @JvmField
    @Rule(order = 4)
    val compose = createEmptyComposeRule()

    @javax.inject.Inject lateinit var destinationRegistry: DestinationRegistry

    @Spy private val teamCityService: TeamCityService = FakeTeamCityServiceImpl()

    companion object {
        @JvmStatic @BeforeClass
        fun disableOnboarding() = TestUtils.disableOnboarding()
    }

    @Before fun setUp() {
        val app = InstrumentationRegistry.getInstrumentation().targetContext.applicationContext as TeamCityApplicationBase
        app.appInjector.sharedUserStorage().apply {
            clearAll()
            saveGuestUserAccountAndSetItAsActive(Mocks.URL, false)
        }
        doReturn(Mocks.failedBuild()).whenever(teamCityService) { build(anyString()) }
    }

    @Test fun failedFilterRetainsBuildCountsAndRows() {
        openTests()
        compose.onNodeWithTag("tests:filter:Failed").assertIsSelected()
        assertTextVisible(text(TestsR.string.tests_section_failed, 2))
        listOf("Test 1", "Test 6").forEach(::assertTextVisible)
        compose.onNodeWithText("Test 1").assertHasClickAction()
    }

    @Test fun freshCountsReplaceOpeningSnapshotAndReturnToOverviewOnSystemBack() {
        val summary = Gson().fromJson("""{"count":21,"passed":16,"failed":3,"ignored":2}""", TestOccurrences::class.java)
        doReturn(summary).whenever(teamCityService) { listTestOccurrences(SUMMARY_URL) }
        openTests()
        assertTextVisible(text(TestsR.string.tests_total, 21))
        assertTextVisible(text(TestsR.string.tests_section_failed, 3))
        pressBack()
        compose.waitUntil(10_000) {
            compose.onAllNodes(hasTestTag("overview:section:Tests") and hasText("21"))
                .fetchSemanticsNodes(atLeastOneRootRequired = false).isNotEmpty()
        }
        compose.onNode(hasTestTag("overview:section:Tests") and hasText("21")).assertIsDisplayed()
    }

    @Test fun failedRowOpensTestDetailsWithTheExistingOccurrenceUrl() {
        openTests()
        assertTextVisible("Test 1")
        compose.onNodeWithText("Test 1").performClick()
        val expected = TestDetailsDestination("/guestAuth/app/rest/testOccurrences/id:4482,build:(id:835695)")
        compose.waitUntil(10_000) { currentRootSnapshot().entries.last().destination == expected }
        org.junit.Assert.assertEquals(expected, currentRootSnapshot().entries.last().destination)
    }

    @Test fun passedFilterRetainsBuildCountsAndRowsWithoutLaunchingDetails() {
        openTests()
        selectFilter("Passed", "Test 5")
        assertTextVisible(text(TestsR.string.tests_section_passed, 10))
        assertTextVisible("Test 2")
        compose.onNodeWithText("Test 5").assertHasNoClickAction().performTouchInput { click() }
        assertNoUnverifiedIntents()
    }

    @Test fun ignoredFilterRetainsBuildCountsAndRowsWithoutLaunchingDetails() {
        openTests()
        selectFilter("Ignored", "Test 4")
        assertTextVisible(text(TestsR.string.tests_section_ignored, 4))
        assertTextVisible("Test 9")
        compose.onNodeWithText("Test 4").assertHasNoClickAction().performTouchInput { click() }
        assertNoUnverifiedIntents()
    }

    @Test fun emptyFailedFilterUsesItsOwnMessage() {
        doReturn(TestOccurrences(0)).whenever(teamCityService) { listTestOccurrences(FAILED_URL) }
        openTests()
        assertTextVisible(text(TestsR.string.tests_empty_failed))
    }

    @Test fun emptyPassedFilterUsesItsOwnMessage() {
        doReturn(TestOccurrences(0)).whenever(teamCityService) { listTestOccurrences(PASSED_URL) }
        openTests()
        compose.onNodeWithTag("tests:filter:Passed").performClick()
        assertTextVisible(text(TestsR.string.tests_empty_passed))
    }

    @Test fun emptyIgnoredFilterUsesItsOwnMessage() {
        doReturn(TestOccurrences(0)).whenever(teamCityService) { listTestOccurrences(IGNORED_URL) }
        openTests()
        compose.onNodeWithTag("tests:filter:Ignored").performClick()
        assertTextVisible(text(TestsR.string.tests_empty_ignored))
    }

    @Test fun initialListFailureCanBeRetriedWithinTheTestsTab() {
        doSuspendableAnswer { throw RuntimeException("offline") }.whenever(teamCityService) { listTestOccurrences(FAILED_URL) }
        openTests()
        assertTextVisible(text(teamcityapp.libraries.theme.R.string.error_load_message))
        val fake = FakeTeamCityServiceImpl()
        doSuspendableAnswer { fake.listTestOccurrences(FAILED_URL) }.whenever(teamCityService) { listTestOccurrences(FAILED_URL) }
        compose.onNodeWithText(text(teamcityapp.libraries.theme.R.string.action_retry)).performClick()
        assertTextVisible("Test 1")
        assertTextVisible("Test 6")
    }

    @Test fun optionalCountRetryUpdatesTheTabWithoutReloadingVisibleRows() {
        val fails = AtomicBoolean(true)
        val pageCalls = AtomicInteger()
        val fake = FakeTeamCityServiceImpl()
        doSuspendableAnswer {
            val url: String = it.getArgument(0)
            if (url.contains("fields=count")) {
                if (fails.get()) throw RuntimeException("count offline") else fake.listTestOccurrences(url)
            } else {
                pageCalls.incrementAndGet()
                fake.listTestOccurrences(url)
            }
        }.whenever(teamCityService) { listTestOccurrences(anyString()) }
        openTests("Tests (?)")
        assertTextVisible("Test 1")
        assertTextVisible(text(TestsR.string.tests_count_unavailable))
        val beforeRetry = pageCalls.get()
        fails.set(false)
        compose.onNode(hasText(text(TestsR.string.tests_retry_count)) and hasAnyAncestor(hasTestTag("retry:snackbar"))).performClick()
        compose.waitUntil(10_000) { compose.onAllNodesWithText(text(TestsR.string.tests_count_unavailable)).fetchSemanticsNodes(atLeastOneRootRequired = false).isEmpty() }
        compose.onNodeWithText(text(TestsR.string.tests_count_unavailable)).assertDoesNotExist()
        assertTextVisible("Test 1")
        Assert.assertEquals(beforeRetry, pageCalls.get())
    }

    @Test fun configurationRecreationRetainsTheSelectedFilterAndCompletedPages() {
        val pageCalls = AtomicInteger()
        val fake = FakeTeamCityServiceImpl()
        doSuspendableAnswer {
            val url: String = it.getArgument(0)
            if (!url.contains("fields=count")) pageCalls.incrementAndGet()
            fake.listTestOccurrences(url)
        }.whenever(teamCityService) { listTestOccurrences(anyString()) }
        ActivityScenario.launch<MainActivity>(buildIntent()).use { scenario ->
            awaitTab("Tests (16)", scenario)
            compose.tapBuildDetailsSection("Tests")
            selectFilter("Passed", "Test 5")
            val beforeRecreation = pageCalls.get()
            scenario.recreate()
            assertTextVisible("Test 5")
            compose.onNodeWithTag("tests:filter:Passed").assertIsSelected()
            compose.onNodeWithTag("tests:filter:Failed").assertIsNotSelected()
            assertTextVisible(text(TestsR.string.tests_section_passed, 10))
            Assert.assertEquals(beforeRecreation, pageCalls.get())
        }
    }

    private fun selectFilter(filter: String, row: String) {
        compose.onNodeWithTag("tests:filter:$filter").performClick()
        assertTextVisible(row)
        compose.onNodeWithTag("tests:filter:$filter").assertIsSelected()
    }

    private fun openTests(title: String = "Tests (16)") {
        activityRule.launchActivity(buildIntent())
        awaitTab(title)
        compose.tapBuildDetailsSection(title)
    }

    private fun buildIntent() = Intent(InstrumentationRegistry.getInstrumentation().targetContext, MainActivity::class.java)
        .setClassName(InstrumentationRegistry.getInstrumentation().targetContext, IncomingDestinationResolver.BUILD_DETAILS_COMPONENT)
        .putExtras(
            Bundle().apply {
                putSerializable(BundleExtractorValues.BUILD, Mocks.failedBuild())
                putString(BundleExtractorValues.NAME, "name")
            }
        )

    private fun awaitTab(title: String, scenario: ActivityScenario<MainActivity>? = null) {
        compose.waitUntil(10_000) {
            compose.onAllNodes(hasTestTag("overview:section:Tests") and hasText(title.substringBefore(" (")))
                .fetchSemanticsNodes(atLeastOneRootRequired = false).isNotEmpty()
        }
    }

    private fun assertTextVisible(value: String) {
        compose.waitUntil(10_000) { compose.onAllNodesWithText(value).fetchSemanticsNodes().isNotEmpty() }
        compose.onNodeWithText(value).assertIsDisplayed()
    }

    private fun text(id: Int, vararg args: Any) = InstrumentationRegistry.getInstrumentation().targetContext.getString(id, *args)
}
