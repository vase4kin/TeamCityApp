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
import com.github.vase4kin.teamcityapp.R
import com.github.vase4kin.teamcityapp.TeamCityApplicationBase
import com.github.vase4kin.teamcityapp.api.TeamCityService
import com.github.vase4kin.teamcityapp.base.extractor.BundleExtractorValues
import com.github.vase4kin.teamcityapp.build_details.view.BuildDetailsActivity
import com.github.vase4kin.teamcityapp.dagger.modules.FakeTeamCityServiceImpl
import com.github.vase4kin.teamcityapp.dagger.modules.Mocks
import com.github.vase4kin.teamcityapp.helper.CustomIntentsTestRule
import com.github.vase4kin.teamcityapp.helper.HiltApiTestRule
import com.github.vase4kin.teamcityapp.helper.TestUtils
import com.github.vase4kin.teamcityapp.tests.api.TestOccurrences
import com.google.android.material.tabs.TabLayout
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import io.reactivex.Single
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicInteger
import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.core.AllOf.allOf
import org.junit.*
import org.junit.runner.RunWith
import org.mockito.ArgumentMatchers.anyString
import org.mockito.Mockito.`when`
import org.mockito.Spy
import teamcityapp.features.test_details.impl.TestDetailsActivity
import teamcityapp.features.test_details.impl.TestDetailsViewModel
import teamcityapp.features.tests.impl.R as TestsR

private const val FAILED_URL = "/guestAuth/app/rest/testOccurrences?locator=build:(id:835695),status:FAILURE,count:10"
private const val PASSED_URL = "/guestAuth/app/rest/testOccurrences?locator=build:(id:835695),status:SUCCESS,count:10"
private const val IGNORED_URL = "/guestAuth/app/rest/testOccurrences?locator=build:(id:835695),status:UNKNOWN,count:10"

/** The Tests feature lives inside the retained legacy Build Details tab host. */
@HiltAndroidTest
@RunWith(AndroidJUnit4::class)
class TestOccurrencesFragmentTest {
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
    val activityRule = CustomIntentsTestRule(BuildDetailsActivity::class.java)

    @JvmField
    @Rule(order = 4)
    val compose = createEmptyComposeRule()

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
        `when`(teamCityService.build(anyString())).thenReturn(Single.just(Mocks.failedBuild()))
    }

    @Test fun failedFilterRetainsBuildCountsAndRows() {
        openTests()
        compose.onNodeWithTag("tests:filter:Failed").assertIsSelected()
        assertTextVisible(text(TestsR.string.tests_section_failed, 2))
        listOf("Test 1", "Test 6").forEach(::assertTextVisible)
        compose.onNodeWithText("Test 1").assertHasClickAction()
    }

    @Test fun failedRowOpensTestDetailsWithTheExistingOccurrenceUrl() {
        openTests()
        assertTextVisible("Test 1")
        compose.onNodeWithText("Test 1").performClick()
        intended(
            allOf(
                hasComponent(TestDetailsActivity::class.java.name),
                hasExtras(hasEntry(equalTo(TestDetailsViewModel.ARG_TEST_URL), equalTo("/guestAuth/app/rest/testOccurrences/id:4482,build:(id:835695)")))
            )
        )
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
        `when`(teamCityService.listTestOccurrences(FAILED_URL)).thenReturn(Single.just(TestOccurrences(0)))
        openTests()
        assertTextVisible(text(TestsR.string.tests_empty_failed))
    }

    @Test fun emptyPassedFilterUsesItsOwnMessage() {
        `when`(teamCityService.listTestOccurrences(PASSED_URL)).thenReturn(Single.just(TestOccurrences(0)))
        openTests()
        compose.onNodeWithTag("tests:filter:Passed").performClick()
        assertTextVisible(text(TestsR.string.tests_empty_passed))
    }

    @Test fun emptyIgnoredFilterUsesItsOwnMessage() {
        `when`(teamCityService.listTestOccurrences(IGNORED_URL)).thenReturn(Single.just(TestOccurrences(0)))
        openTests()
        compose.onNodeWithTag("tests:filter:Ignored").performClick()
        assertTextVisible(text(TestsR.string.tests_empty_ignored))
    }

    @Test fun initialListFailureCanBeRetriedWithinTheTestsTab() {
        `when`(teamCityService.listTestOccurrences(FAILED_URL)).thenReturn(Single.error(RuntimeException("offline")))
        openTests()
        assertTextVisible(text(teamcityapp.libraries.theme.R.string.error_load_message))
        val fake = FakeTeamCityServiceImpl()
        `when`(teamCityService.listTestOccurrences(FAILED_URL)).thenAnswer { fake.listTestOccurrences(FAILED_URL) }
        compose.onNodeWithText(text(teamcityapp.libraries.theme.R.string.action_retry)).performClick()
        assertTextVisible("Test 1")
        assertTextVisible("Test 6")
    }

    @Test fun optionalCountRetryUpdatesTheTabWithoutReloadingVisibleRows() {
        val fails = AtomicBoolean(true)
        val pageCalls = AtomicInteger()
        val fake = FakeTeamCityServiceImpl()
        `when`(teamCityService.listTestOccurrences(anyString())).thenAnswer {
            val url: String = it.getArgument(0)
            if (url.contains("fields=count")) {
                if (fails.get()) Single.error<TestOccurrences>(RuntimeException("count offline")) else fake.listTestOccurrences(url)
            } else {
                pageCalls.incrementAndGet()
                fake.listTestOccurrences(url)
            }
        }
        openTests("Tests (0)")
        assertTextVisible("Test 1")
        assertTextVisible(text(TestsR.string.tests_count_unavailable))
        val beforeRetry = pageCalls.get()
        fails.set(false)
        compose.onNodeWithText(text(TestsR.string.tests_retry_count)).performClick()
        awaitTab("Tests (16)")
        compose.onNodeWithText(text(TestsR.string.tests_count_unavailable)).assertDoesNotExist()
        assertTextVisible("Test 1")
        Assert.assertEquals(beforeRetry, pageCalls.get())
    }

    @Test fun configurationRecreationRetainsTheSelectedFilterAndCompletedPages() {
        val pageCalls = AtomicInteger()
        val fake = FakeTeamCityServiceImpl()
        `when`(teamCityService.listTestOccurrences(anyString())).thenAnswer {
            val url: String = it.getArgument(0)
            if (!url.contains("fields=count")) pageCalls.incrementAndGet()
            fake.listTestOccurrences(url)
        }
        ActivityScenario.launch<BuildDetailsActivity>(buildIntent()).use { scenario ->
            awaitTab("Tests (16)", scenario)
            onView(withText("Tests (16)")).perform(scrollTo(), click())
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
        onView(withText(title)).perform(scrollTo(), click())
    }

    private fun buildIntent() = Intent(InstrumentationRegistry.getInstrumentation().targetContext, BuildDetailsActivity::class.java).putExtras(
        Bundle().apply {
            putSerializable(BundleExtractorValues.BUILD, Mocks.failedBuild())
            putString(BundleExtractorValues.NAME, "name")
        }
    )

    private fun awaitTab(title: String, scenario: ActivityScenario<BuildDetailsActivity>? = null) {
        compose.waitUntil(10_000) {
            val found = AtomicBoolean()
            val inspect: (BuildDetailsActivity) -> Unit = { activity ->
                val tabs = activity.findViewById<TabLayout>(R.id.tabLayout)
                found.set((0 until tabs.tabCount).any { tabs.getTabAt(it)?.text.toString() == title })
            }
            if (scenario == null) {
                InstrumentationRegistry.getInstrumentation().runOnMainSync { inspect(activityRule.activity) }
            } else {
                scenario.onActivity { inspect(it) }
            }
            found.get()
        }
    }

    private fun assertTextVisible(value: String) {
        compose.waitUntil(10_000) { compose.onAllNodesWithText(value).fetchSemanticsNodes().isNotEmpty() }
        compose.onNodeWithText(value).assertIsDisplayed()
    }

    private fun text(id: Int, vararg args: Any) = InstrumentationRegistry.getInstrumentation().targetContext.getString(id, *args)
}
