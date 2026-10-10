/*
 * Copyright 2020 Andrey Tolpeev
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

package com.github.vase4kin.teamcityapp.changes.view

import android.content.Intent
import android.os.Bundle
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.v2.createEmptyComposeRule
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.github.vase4kin.teamcityapp.R
import com.github.vase4kin.teamcityapp.TeamCityApplicationBase
import com.github.vase4kin.teamcityapp.api.TeamCityService
import com.github.vase4kin.teamcityapp.base.extractor.BundleExtractorValues
import com.github.vase4kin.teamcityapp.build_details.view.BuildDetailsActivity
import com.github.vase4kin.teamcityapp.changes.api.Changes
import com.github.vase4kin.teamcityapp.dagger.modules.FakeTeamCityServiceImpl
import com.github.vase4kin.teamcityapp.dagger.modules.Mocks
import com.github.vase4kin.teamcityapp.helper.CustomActivityTestRule
import com.github.vase4kin.teamcityapp.helper.HiltApiTestRule
import com.github.vase4kin.teamcityapp.helper.TestUtils
import com.google.android.material.tabs.TabLayout
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import io.reactivex.Single
import java.util.concurrent.atomic.AtomicBoolean
import org.junit.*
import org.junit.runner.RunWith
import org.mockito.ArgumentMatchers.anyString
import org.mockito.Mockito.`when`
import org.mockito.Spy
import teamcityapp.features.changes.impl.R as ChangesR

@HiltAndroidTest
@RunWith(AndroidJUnit4::class)
class ChangesFragmentTest {
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
    val activityRule = CustomActivityTestRule(BuildDetailsActivity::class.java)

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
    }

    @Test fun rowsOpenTheMigratedChangeDetailsScreen() {
        openChanges("Changes (1)")
        listOf("Do you believe?", "By john-117 on 30 Jul 16 00:36", "21312fsd1321").forEach(::assertTextVisible)
        compose.onNodeWithText("Do you believe?").performClick()
        listOf("Change details", "Do you believe?", "21312fsd1321", "By john-117 on 30 Jul 16 00:36", "Changed files (1)", "filename!", "Edited").forEach(::assertTextVisible)
    }

    @Test fun failedChangesCanBeRetriedWithinTheBuildTab() {
        `when`(teamCityService.listChanges(anyString())).thenReturn(Single.error(RuntimeException("offline")))
        openChanges("Changes (0)")
        assertTextVisible(text(R.string.error_view_error_text))
        val fake = FakeTeamCityServiceImpl()
        `when`(teamCityService.listChanges(anyString())).thenAnswer { fake.listChanges(it.getArgument(0)) }
        compose.onNodeWithText(text(teamcityapp.libraries.theme.R.string.action_retry)).performClick()
        assertTextVisible("Do you believe?")
    }

    @Test fun emptyChangesRetainTheCompatibilityTabCount() {
        `when`(teamCityService.listChanges(anyString())).thenReturn(Single.just(Changes(emptyList(), 0)))
        openChanges("Changes (0)")
        assertTextVisible(text(ChangesR.string.changes_empty))
    }

    @Test fun aFailedOptionalCountCanBeRetriedWhileChangesRemainVisible() {
        val countFails = AtomicBoolean(true)
        val fake = FakeTeamCityServiceImpl()
        `when`(teamCityService.listChanges(anyString())).thenAnswer {
            val url: String = it.getArgument(0)
            if (url.contains("fields=count") && countFails.get()) Single.error<Changes>(RuntimeException("count offline")) else fake.listChanges(url)
        }
        openChanges("Changes (0)")
        assertTextVisible("Do you believe?")
        assertTextVisible(text(ChangesR.string.changes_count_unavailable))
        countFails.set(false)
        compose.onNodeWithText(text(ChangesR.string.changes_retry_count)).performClick()
        awaitTab("Changes (1)")
        compose.onNodeWithText(text(ChangesR.string.changes_count_unavailable)).assertDoesNotExist()
        assertTextVisible("Do you believe?")
    }

    private fun openChanges(title: String) {
        activityRule.launchActivity(
            Intent().putExtras(
                Bundle().apply {
                    putSerializable(BundleExtractorValues.BUILD, Mocks.successBuild())
                    putString(BundleExtractorValues.NAME, "name")
                }
            )
        )
        awaitTab(title)
        onView(withText(title)).perform(click())
    }

    private fun awaitTab(title: String) {
        compose.waitUntil(10_000) {
            val found = AtomicBoolean()
            InstrumentationRegistry.getInstrumentation().runOnMainSync {
                val tabs = activityRule.activity.findViewById<TabLayout>(R.id.tabLayout)
                found.set((0 until tabs.tabCount).any { tabs.getTabAt(it)?.text.toString() == title })
            }
            found.get()
        }
    }

    private fun assertTextVisible(value: String) {
        compose.waitUntil(10_000) { compose.onAllNodesWithText(value).fetchSemanticsNodes().isNotEmpty() }
        compose.onNodeWithText(value).assertIsDisplayed()
    }
    private fun text(id: Int) = InstrumentationRegistry.getInstrumentation().targetContext.getString(id)
}
