/*
 * Copyright 2020 Andrey Tolpeev
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

package com.github.vase4kin.teamcityapp.login.view

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.v2.createEmptyComposeRule
import androidx.test.espresso.action.ViewActions.clearText
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.action.ViewActions.closeSoftKeyboard
import androidx.test.espresso.action.ViewActions.pressImeActionButton
import androidx.test.espresso.action.ViewActions.scrollTo
import androidx.test.espresso.action.ViewActions.typeText
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.intent.Intents.assertNoUnverifiedIntents
import androidx.test.espresso.intent.matcher.BundleMatchers.hasEntry
import androidx.test.espresso.matcher.ViewMatchers.assertThat
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.github.vase4kin.teamcityapp.R
import com.github.vase4kin.teamcityapp.TeamCityApplicationBase
import com.github.vase4kin.teamcityapp.di.modules.AppModule
import com.github.vase4kin.teamcityapp.di.modules.Mocks.Companion.URL
import com.github.vase4kin.teamcityapp.di.modules.PendingFixture
import com.github.vase4kin.teamcityapp.helper.CustomIntentsTestRule
import com.github.vase4kin.teamcityapp.helper.HiltApiTestRule
import com.github.vase4kin.teamcityapp.helper.TestUtils
import com.github.vase4kin.teamcityapp.helper.awaitDisplayedText
import com.github.vase4kin.teamcityapp.helper.capture
import com.github.vase4kin.teamcityapp.login.dagger.LoginActivityTestAppModule
import com.github.vase4kin.teamcityapp.navigation.IncomingDestinationResolver
import com.github.vase4kin.teamcityapp.navigation.MainActivity
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.android.testing.BindValue
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import dagger.hilt.android.testing.UninstallModules
import dagger.hilt.components.SingletonComponent
import java.io.IOException
import org.hamcrest.CoreMatchers.containsString
import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.CoreMatchers.not
import org.junit.Before
import org.junit.Ignore
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.Mockito.doAnswer
import org.mockito.Mockito.mock
import org.mockito.Mockito.`when`
import org.mockito.kotlin.any
import org.mockito.kotlin.doSuspendableAnswer
import org.mockito.kotlin.verifyBlocking
import org.mockito.kotlin.whenever
import teamcityapp.libraries.security.CryptoManager

/**
 * Tests for [MainActivity] with mocked internet connection
 */
@UninstallModules(AppModule::class)
@HiltAndroidTest
@RunWith(AndroidJUnit4::class)
class LoginActivityTest {
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
        CustomIntentsTestRule(MainActivity::class.java, IncomingDestinationResolver.LOGIN_COMPONENT)

    @BindValue
    @Mock
    lateinit var cryptoManager: CryptoManager

    @Before
    fun setUp() {
        val app =
            InstrumentationRegistry.getInstrumentation().targetContext.applicationContext as TeamCityApplicationBase
        app.appInjector.sharedUserStorage().clearAll()
        // Authentication checks Home content; onboarding has its own root-flow tests.
        TestUtils.disableOnboarding()
    }

    private fun awaitHome() {
        compose.waitUntil(15_000) { compose.onAllNodesWithTag("home:tab:Projects").fetchSemanticsNodes().isNotEmpty() }
        compose.onNodeWithTag("home:tab:Projects").assertIsDisplayed()
        compose.onNodeWithTag("auth:password").assertDoesNotExist()
        org.junit.Assert.assertEquals(MainActivity::class.java, activityRule.activity.javaClass)
    }

    @org.mockito.Spy
    val teamCityService: com.github.vase4kin.teamcityapp.api.TeamCityService = com.github.vase4kin.teamcityapp.di.modules.FakeTeamCityServiceImpl()

    private val inputUrl = URL.replace("https://", "")
    private val messageEmpty = ""

    /**
     * Verifies that user can be logged in as guest user with correct account url
     */
    @Test
    @Throws(Throwable::class)
    fun testUserCanCreateGuestUserAccountWithCorrectUrl() {
        val urlWithPath = "https://teamcity.com/server"
        val savedUrl = "$urlWithPath/"

        activityRule.launchActivity(null)

        compose.onNodeWithTag("auth:url").performTextInput(urlWithPath.replace("https://", ""))
        compose.onNodeWithTag("auth:guest").performClick()
        compose.onNodeWithTag("login:submit").performClick()

        compose.waitForIdle()
        awaitHome()

        val app =
            InstrumentationRegistry.getInstrumentation().targetContext.applicationContext as TeamCityApplicationBase
        val storageUtils = app.appInjector.sharedUserStorage()
        assertThat(storageUtils.hasGuestAccountWithUrl(savedUrl), `is`(true))
        assertThat(storageUtils.activeUser.teamcityUrl, `is`(savedUrl))
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
        val urlWithPath = "https://teamcity.com/server"
        val savedUrl = "$urlWithPath/"

        activityRule.launchActivity(null)

        compose.onNodeWithTag("auth:url").performTextInput(urlWithPath.replace("https://", ""))
        compose.onNodeWithTag("auth:guest").performClick()
        compose.onNodeWithTag("auth:ssl").performClick()
        compose.onNodeWithText(context.getString(R.string.warning_ssl_dialog_content)).assertIsDisplayed()
        compose.onNodeWithText(context.getString(R.string.dialog_ok_title).uppercase()).performClick()
        compose.onNodeWithTag("login:submit").performClick()

        compose.waitForIdle()
        awaitHome()

        val app =
            InstrumentationRegistry.getInstrumentation().targetContext.applicationContext as TeamCityApplicationBase
        val storageUtils = app.appInjector.sharedUserStorage()
        assertThat(storageUtils.hasGuestAccountWithUrl(savedUrl), `is`(true))
        assertThat(storageUtils.activeUser.teamcityUrl, `is`(savedUrl))
        assertThat(storageUtils.activeUser.isSslDisabled, `is`(true))
    }

    /**
     * Verifies that user can be logged in as guest with correct account url
     */
    @Test
    @Throws(Throwable::class)
    fun testUserCanCreateAccountWithCorrectUrlByImeButton() {
        activityRule.launchActivity(null)

        compose.onNodeWithTag("auth:guest").performClick()
        compose.onNodeWithTag("auth:url").performTextInput(inputUrl)
        compose.onNodeWithTag("auth:url").performImeAction()

        compose.waitForIdle()
        awaitHome()

        val app =
            InstrumentationRegistry.getInstrumentation().targetContext.applicationContext as TeamCityApplicationBase
        val storageUtils = app.appInjector.sharedUserStorage()
        assertThat(storageUtils.hasGuestAccountWithUrl(URL), `is`(true))
        assertThat(storageUtils.activeUser.teamcityUrl, `is`(URL))
    }

    /**
     * Verifies that user can be logged in as guest with correct account url
     */
    @Test
    @Throws(Throwable::class)
    fun testUserCanCreateAccountWithCorrectUrlWhichContainsPathByImeButton() {
        activityRule.launchActivity(null)

        compose.onNodeWithTag("auth:guest").performClick()
        compose.onNodeWithTag("auth:url").performTextInput(inputUrl)
        compose.onNodeWithTag("auth:url").performImeAction()

        compose.waitForIdle()
        awaitHome()

        val app =
            InstrumentationRegistry.getInstrumentation().targetContext.applicationContext as TeamCityApplicationBase
        val storageUtils = app.appInjector.sharedUserStorage()
        assertThat(storageUtils.hasGuestAccountWithUrl(URL), `is`(true))
        assertThat(storageUtils.activeUser.teamcityUrl, `is`(URL))
    }

    /**
     * Verifies that user can be logged in with correct account url and credentials
     */
    @Test
    @Throws(Throwable::class)
    fun testUserCanCreateUserAccountWithCorrectUrlAndCredentials() {
        `when`(cryptoManager.encrypt("pass")).thenReturn(byteArrayOf(1, 2, 3))
        `when`(cryptoManager.decrypt(any<ByteArray>())).thenReturn("pass".toByteArray())

        activityRule.launchActivity(null)

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
    @Throws(IOException::class)
    fun testUserIsNotifiedIfServerReturnsBadResponse() {
        doSuspendableAnswer { throw com.github.vase4kin.teamcityapp.di.modules.MockHttpFailure(404) }.whenever(teamCityService) { serverInfo() }

        activityRule.launchActivity(null)

        compose.onNodeWithTag("auth:url").performTextInput(inputUrl)
        compose.onNodeWithTag("auth:guest").performClick()
        compose.onNodeWithTag("login:submit").performClick()
        compose.awaitDisplayedText("404", substring = true)
    }

    /**
     * Verifies that user can be notified with dialog info for 401 errors
     */
    @Test
    @Throws(IOException::class)
    fun testUserIsNotifiedIfServerReturns401Request() {
        doSuspendableAnswer { throw com.github.vase4kin.teamcityapp.di.modules.MockHttpFailure(401) }.whenever(teamCityService) { serverInfo() }

        activityRule.launchActivity(null)

        compose.onNodeWithTag("auth:url").performTextInput(inputUrl)
        compose.onNodeWithTag("auth:guest").performClick()
        compose.onNodeWithTag("login:submit").performClick()
        compose.onNodeWithText(context.getString(teamcityapp.features.login.impl.R.string.info_unauthorized_dialog_title)).assertIsDisplayed()
        compose.onNodeWithText(context.getString(teamcityapp.features.login.impl.R.string.info_unauthorized_dialog_content)).assertIsDisplayed()
    }

    /**
     * Verifies that user can be logged in as guest user with correct account url
     */
    @Test
    fun testUserCanCreateGuestUserAccountWithNotSecureUrl() {
        val urlWithPath = "http://teamcity.com/server"
        val savedUrl = "$urlWithPath/"

        activityRule.launchActivity(null)

        compose.onNodeWithTag("auth:url").performTextClearance()
        compose.onNodeWithTag("auth:url").performTextInput(urlWithPath)
        compose.onNodeWithTag("auth:guest").performClick()
        compose.onNodeWithTag("login:submit").performClick()

        compose.onNodeWithText(context.getString(R.string.warning_ssl_dialog_title)).assertIsDisplayed()
        compose.onNodeWithText(context.getString(teamcityapp.libraries.authentication.R.string.server_not_secure_http)).assertIsDisplayed()
        compose.onNodeWithText(context.getString(R.string.dialog_ok_title).uppercase()).performClick()

        compose.waitForIdle()
        awaitHome()

        val app =
            InstrumentationRegistry.getInstrumentation().targetContext.applicationContext as TeamCityApplicationBase
        val storageUtils = app.appInjector.sharedUserStorage()
        assertThat(storageUtils.hasGuestAccountWithUrl(savedUrl), `is`(true))
        assertThat(storageUtils.activeUser.teamcityUrl, `is`(savedUrl))
        assertThat(storageUtils.activeUser.isSslDisabled, `is`(false))
    }

    @Test
    fun testUserCanNotCreateAccountIfDataWasNotSaved() {
        `when`(cryptoManager.encrypt("pass")).thenReturn(byteArrayOf())
        `when`(cryptoManager.isFailed(any<ByteArray>())).thenReturn(true)

        activityRule.launchActivity(null)
        compose.onNodeWithTag("auth:url").performTextInput(inputUrl)
        compose.onNodeWithTag("auth:username").performTextInput("user")
        compose.onNodeWithTag("auth:password").performTextInput("pass")
        compose.onNodeWithTag("auth:password").performImeAction()
        compose.onNodeWithText(context.getString(teamcityapp.libraries.authentication.R.string.error_save_account)).assertIsDisplayed()
        val app = context.applicationContext as TeamCityApplicationBase
        assertThat(app.appInjector.sharedUserStorage().hasAccountWithUrl(URL, "user"), `is`(false))
        compose.onNodeWithTag("home:tab:Projects").assertDoesNotExist()
    }

    @Test
    fun pendingGuestAuthenticationSurvivesActivityRecreationWithoutAnotherRequest() {
        val pending = PendingFixture<teamcityapp.features.about.repository.models.ServerInfo>()
        doSuspendableAnswer { pending.await() }.whenever(teamCityService) { serverInfo() }
        activityRule.launchActivity(null)
        compose.onNodeWithTag("auth:url").performTextInput(inputUrl)
        compose.onNodeWithTag("auth:guest").performClick()
        compose.onNodeWithTag("login:submit").performClick()
        compose.waitForIdle()
        compose.waitUntil(5_000) { pending.isAwaiting }
        InstrumentationRegistry.getInstrumentation().runOnMainSync { activityRule.activity.recreate() }
        compose.waitForIdle()
        compose.onNodeWithTag("login:submit").assertIsNotEnabled()
        verifyBlocking(teamCityService, org.mockito.Mockito.times(1)) { serverInfo() }
        pending.complete(teamcityapp.features.about.repository.models.ServerInfo("fixture", URL))
        compose.waitForIdle()
        compose.waitForIdle()
        awaitHome()
        val app = context.applicationContext as TeamCityApplicationBase
        assertThat(app.appInjector.sharedUserStorage().hasGuestAccountWithUrl(URL), `is`(true))
    }

    @Test
    fun testUserAlwaysSeesTryItOut() {
        activityRule.launchActivity(null)

        compose.onNodeWithTag("login:demo").performScrollTo().assertIsDisplayed()
    }

    @Test
    fun testUserCanDeclineTryItOut() {
        activityRule.launchActivity(null)

        compose.onNodeWithTag("login:demo").performScrollTo()
            .assertIsDisplayed()
            .performClick()
        compose.onNodeWithText(context.getString(teamcityapp.libraries.authentication.R.string.warning_ssl_dialog_negative)).performClick()
        assertNoUnverifiedIntents()
    }

    @Test
    fun testUserCanSignInToDemoAsGuest() {
        val demoUrl = "https://teamcity.jetbrains.com"
        val savedUrl = demoUrl

        activityRule.launchActivity(null)

        compose.onNodeWithTag("login:demo").performScrollTo()
            .assertIsDisplayed().performClick()

        compose.onNodeWithText(context.getString(teamcityapp.features.login.impl.R.string.dialog_try_it_out_title)).performClick()

        compose.waitForIdle()
        awaitHome()

        val app =
            InstrumentationRegistry.getInstrumentation().targetContext.applicationContext as TeamCityApplicationBase
        val storageUtils = app.appInjector.sharedUserStorage()
        assertThat(storageUtils.hasGuestAccountWithUrl(savedUrl), `is`(true))
        assertThat(storageUtils.activeUser.teamcityUrl, `is`(savedUrl))
        assertThat(storageUtils.activeUser.isSslDisabled, `is`(false))
    }

    @Module(includes = [LoginActivityTestAppModule::class])
    @InstallIn(SingletonComponent::class)
    object TestBindings
}
