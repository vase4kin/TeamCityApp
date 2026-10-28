/*
 * Copyright 2019 Andrey Tolpeev
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

package com.github.vase4kin.teamcityapp.account.create.view

import android.content.Intent
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.v2.createEmptyComposeRule
import androidx.test.espresso.matcher.ViewMatchers.assertThat
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.github.vase4kin.teamcityapp.R
import com.github.vase4kin.teamcityapp.TeamCityApplicationBase
import com.github.vase4kin.teamcityapp.api.TeamCityService
import com.github.vase4kin.teamcityapp.di.modules.FakeTeamCityServiceImpl
import com.github.vase4kin.teamcityapp.di.modules.Mocks.Companion.URL
import com.github.vase4kin.teamcityapp.di.modules.PendingFixture
import com.github.vase4kin.teamcityapp.helper.CustomIntentsTestRule
import com.github.vase4kin.teamcityapp.helper.HiltApiTestRule
import com.github.vase4kin.teamcityapp.helper.TestUtils
import com.github.vase4kin.teamcityapp.helper.awaitDisplayedText
import com.github.vase4kin.teamcityapp.navigation.IncomingDestinationResolver
import com.github.vase4kin.teamcityapp.navigation.MainActivity
import com.github.vase4kin.teamcityapp.storage.SharedUserStorage
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import org.hamcrest.CoreMatchers.`is`
import org.junit.Before
import org.junit.BeforeClass
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Spy
import org.mockito.kotlin.doSuspendableAnswer
import org.mockito.kotlin.verifyBlocking
import org.mockito.kotlin.whenever

/**
 * Tests for [MainActivity] with mocked internet connection
 */
@HiltAndroidTest
@RunWith(AndroidJUnit4::class)
class CreateAccountActivityTest {
    @JvmField
    @Rule(order = 4)
    val compose = createEmptyComposeRule()
    private val context get() = InstrumentationRegistry.getInstrumentation().targetContext

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
    val activityRule: CustomIntentsTestRule<MainActivity> =
        CustomIntentsTestRule(MainActivity::class.java, IncomingDestinationResolver.CREATE_ACCOUNT_COMPONENT)

    @Spy
    val teamCityService: TeamCityService = FakeTeamCityServiceImpl()

    private fun awaitHome() {
        compose.waitUntil(15_000) { compose.onAllNodesWithTag("home:tab:Projects").fetchSemanticsNodes().isNotEmpty() }
        compose.onNodeWithTag("home:tab:Projects").assertIsDisplayed()
        compose.onNodeWithTag("auth:password").assertDoesNotExist()
        org.junit.Assert.assertEquals(MainActivity::class.java, activityRule.activity.javaClass)
    }

    private val inputUrl = URL.replace("https://", "")

    companion object {
        @JvmStatic
        @BeforeClass
        fun disableOnboarding() {
            TestUtils.disableOnboarding()
        }
    }

    @Before
    fun setUp() {
        val app =
            InstrumentationRegistry.getInstrumentation().targetContext.applicationContext as TeamCityApplicationBase
        app.appInjector.sharedUserStorage().clearAll()
        app.appInjector.sharedUserStorage()
            .saveGuestUserAccountAndSetItAsActive("$URL/server", false)
        activityRule.launchActivity(null)
        compose.waitUntil(10_000) {
            runCatching { compose.onNodeWithTag("auth:url").assertIsDisplayed() }.isSuccess
        }
    }

    /**
     * Verifies that user can be logged in as guest user with correct account url
     */
    @Test
    fun testUserCanCreateGuestUserAccountWithCorrectUrl() {
        compose.onNodeWithTag("auth:url").performTextInput(inputUrl)
        compose.onNodeWithTag("auth:guest").performClick()
        androidx.test.espresso.Espresso.closeSoftKeyboard()
        compose.onNodeWithTag("create-account:submit").performClick()

        compose.waitForIdle()
        awaitHome()

        val storageUtils = SharedUserStorage.init(activityRule.activity, null)
        assertThat(storageUtils.hasGuestAccountWithUrl(URL), `is`(true))
        assertThat(storageUtils.activeUser.teamcityUrl, `is`(URL))
        assertThat(storageUtils.activeUser.isSslDisabled, `is`(false))
        val host = activityRule.activity
        androidx.test.espresso.Espresso.pressBackUnconditionally()
        compose.waitUntil(5_000) { host.isFinishing }
    }

    /**
     * Verifies that user can be logged in as guest user with correct account url ignoring ssl
     */
    @Test
    fun testUserCanCreateGuestUserAccountWithCorrectUrlIgnoringSsl() {
        compose.onNodeWithTag("auth:url").performTextInput(inputUrl)
        compose.onNodeWithTag("auth:guest").performClick()
        compose.onNodeWithTag("auth:ssl").performClick()
        compose.onNodeWithText(context.getString(R.string.warning_ssl_dialog_content)).assertIsDisplayed()
        compose.onNodeWithText(context.getString(R.string.dialog_ok_title).uppercase()).performClick()
        compose.onNodeWithTag("create-account:submit").performClick()

        compose.waitForIdle()
        awaitHome()

        val storageUtils = SharedUserStorage.init(activityRule.activity, null)
        assertThat(storageUtils.hasGuestAccountWithUrl(URL), `is`(true))
        assertThat(storageUtils.activeUser.teamcityUrl, `is`(URL))
        assertThat(storageUtils.activeUser.isSslDisabled, `is`(true))
    }

    /**
     * Verifies that user can be logged in with correct account url and credentials
     */
    @Test
    fun testUserCanCreateUserAccountWithCorrectUrlAndCredentials() {
        compose.onNodeWithTag("auth:url").performTextInput(inputUrl)
        compose.onNodeWithTag("auth:username").performTextInput("user")
        compose.onNodeWithTag("auth:username").performImeAction()
        compose.onNodeWithTag("auth:password").performTextInput("pass")
        compose.onNodeWithTag("auth:password").performImeAction()

        compose.waitForIdle()
        awaitHome()

        val app =
            InstrumentationRegistry.getInstrumentation().targetContext.applicationContext as TeamCityApplicationBase
        val storageUtils = app.appInjector.sharedUserStorage()
        assertThat(storageUtils.hasAccountWithUrl(URL, "user"), `is`(true))
        assertThat(storageUtils.activeUser.teamcityUrl, `is`(URL))
    }

    /**
     * Verifies that user can be notified with error message if servers returns smth bad
     */
    @Test
    fun testUserIsNotifiedIfServerReturnsBadResponse() {
        doSuspendableAnswer { throw com.github.vase4kin.teamcityapp.di.modules.MockHttpFailure(404) }.whenever(teamCityService) { serverInfo() }

        compose.onNodeWithTag("auth:url").performTextInput(inputUrl)
        compose.onNodeWithTag("auth:guest").performClick()
        compose.onNodeWithTag("create-account:submit").performClick()
        compose.awaitDisplayedText("404", substring = true)
    }

    @Test fun pendingGuestAuthenticationRetainsItsOwnerAcrossRootRecreation() {
        val pending = PendingFixture<teamcityapp.features.about.repository.models.ServerInfo>()
        doSuspendableAnswer { pending.await() }.whenever(teamCityService) { serverInfo() }
        compose.onNodeWithTag("auth:url").performTextInput(inputUrl)
        compose.onNodeWithTag("auth:guest").performClick()
        compose.onNodeWithTag("create-account:submit").performClick()
        compose.waitForIdle()
        compose.waitUntil(5_000) { pending.isAwaiting }
        InstrumentationRegistry.getInstrumentation().runOnMainSync { activityRule.activity.recreate() }
        compose.waitForIdle()
        compose.onNodeWithTag("create-account:submit").assertIsNotEnabled()
        verifyBlocking(teamCityService, org.mockito.Mockito.times(1)) { serverInfo() }
        pending.complete(teamcityapp.features.about.repository.models.ServerInfo("fixture", URL))
        awaitHome()
        val app = context.applicationContext as TeamCityApplicationBase
        assertThat(app.appInjector.sharedUserStorage().hasGuestAccountWithUrl(URL), `is`(true))
        verifyBlocking(teamCityService, org.mockito.Mockito.times(1)) { serverInfo() }
    }

    @Test fun replacingTheAccountStackCancelsPendingAuthenticationAndIgnoresLateSuccess() {
        val pending = PendingFixture<teamcityapp.features.about.repository.models.ServerInfo>()
        doSuspendableAnswer { pending.await() }.whenever(teamCityService) { serverInfo() }
        compose.onNodeWithTag("auth:url").performTextInput(inputUrl)
        compose.onNodeWithTag("auth:guest").performClick()
        compose.onNodeWithTag("create-account:submit").performClick()
        compose.waitForIdle()
        compose.waitUntil(5_000) { pending.isAwaiting }
        InstrumentationRegistry.getInstrumentation().runOnMainSync {
            activityRule.activity.startActivity(
                android.content.Intent().setClassName(context, IncomingDestinationResolver.HOME_COMPONENT).apply {
                    flags = android.content.Intent.FLAG_ACTIVITY_SINGLE_TOP
                    putExtra(com.github.vase4kin.teamcityapp.base.extractor.BundleExtractorValues.IS_REQUIRED_TO_RELOAD, true)
                }
            )
        }
        awaitHome()
        compose.waitUntil(5_000) { !pending.isAwaiting }
        pending.complete(teamcityapp.features.about.repository.models.ServerInfo("fixture", URL))
        compose.waitForIdle()
        val app = context.applicationContext as TeamCityApplicationBase
        assertThat(app.appInjector.sharedUserStorage().hasGuestAccountWithUrl(URL), `is`(false))
        assertThat(app.appInjector.sharedUserStorage().activeUser.teamcityUrl, `is`("$URL/server"))
        compose.onNodeWithTag("home:tab:Projects").assertIsDisplayed()
    }
}
