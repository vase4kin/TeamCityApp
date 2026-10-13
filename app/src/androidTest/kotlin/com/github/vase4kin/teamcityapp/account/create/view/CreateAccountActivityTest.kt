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
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.action.ViewActions.closeSoftKeyboard
import androidx.test.espresso.action.ViewActions.pressImeActionButton
import androidx.test.espresso.action.ViewActions.typeText
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.intent.matcher.BundleMatchers.hasEntry
import androidx.test.espresso.matcher.ViewMatchers.assertThat
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.github.vase4kin.teamcityapp.R
import com.github.vase4kin.teamcityapp.TeamCityApplicationBase
import com.github.vase4kin.teamcityapp.account.create.dagger.CreateAccountActivityTestAppModule
import com.github.vase4kin.teamcityapp.api.TeamCityService
import com.github.vase4kin.teamcityapp.di.modules.AppModule
import com.github.vase4kin.teamcityapp.di.modules.AppModule.CLIENT_AUTH
import com.github.vase4kin.teamcityapp.di.modules.AppModule.CLIENT_BASE
import com.github.vase4kin.teamcityapp.di.modules.AppModule.CLIENT_BASE_UNSAFE
import com.github.vase4kin.teamcityapp.di.modules.FakeTeamCityServiceImpl
import com.github.vase4kin.teamcityapp.di.modules.Mocks.Companion.URL
import com.github.vase4kin.teamcityapp.helper.CustomIntentsTestRule
import com.github.vase4kin.teamcityapp.helper.HiltApiTestRule
import com.github.vase4kin.teamcityapp.helper.TestUtils
import com.github.vase4kin.teamcityapp.helper.capture
import com.github.vase4kin.teamcityapp.navigation.IncomingDestinationResolver
import com.github.vase4kin.teamcityapp.navigation.MainActivity
import com.github.vase4kin.teamcityapp.storage.SharedUserStorage
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.android.testing.BindValue
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import dagger.hilt.android.testing.UninstallModules
import dagger.hilt.components.SingletonComponent
import javax.inject.Named
import okhttp3.Authenticator
import okhttp3.Call
import okhttp3.Callback
import okhttp3.OkHttpClient
import okhttp3.Protocol
import okhttp3.Request
import okhttp3.Response
import okhttp3.ResponseBody.Companion.toResponseBody
import org.hamcrest.CoreMatchers.containsString
import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.CoreMatchers.`is`
import org.junit.Before
import org.junit.BeforeClass
import org.junit.Ignore
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.ArgumentCaptor
import org.mockito.Captor
import org.mockito.Mock
import org.mockito.Mockito.doAnswer
import org.mockito.Mockito.mock
import org.mockito.Mockito.`when`
import org.mockito.Spy
import org.mockito.kotlin.any

/**
 * Tests for [MainActivity] with mocked internet connection
 */
@UninstallModules(AppModule::class)
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

    @Captor
    lateinit var callbackArgumentCaptor: ArgumentCaptor<Callback>

    @field:Named(CLIENT_BASE)
    @BindValue
    @Mock
    lateinit var clientBase: OkHttpClient

    @field:Named(CLIENT_BASE_UNSAFE)
    @BindValue
    @Mock
    lateinit var unsafeOkHttpClient: OkHttpClient

    @field:Named(CLIENT_AUTH)
    @BindValue
    @Mock
    lateinit var clientAuth: OkHttpClient

    @Mock
    lateinit var call: Call

    @javax.inject.Inject lateinit var accounts: com.github.vase4kin.teamcityapp.navigation.AppAccountFlowNavigation

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
        for (client in listOf(clientBase, unsafeOkHttpClient)) {
            val builder = mock(OkHttpClient.Builder::class.java)
            `when`(client.newBuilder()).thenReturn(builder)
            `when`(builder.authenticator(any<Authenticator>())).thenReturn(builder)
            `when`(builder.build()).thenReturn(client)
        }

        val app =
            InstrumentationRegistry.getInstrumentation().targetContext.applicationContext as TeamCityApplicationBase
        app.appInjector.sharedUserStorage().clearAll()
        app.appInjector.sharedUserStorage()
            .saveGuestUserAccountAndSetItAsActive("$URL/server", false)
        `when`(clientBase.newCall(any<Request>())).thenReturn(call)
        `when`(unsafeOkHttpClient.newCall(any<Request>())).thenReturn(call)
        activityRule.launchActivity(null)
    }

    /**
     * Verifies that user can be logged in as guest user with correct account url
     */
    @Test
    fun testUserCanCreateGuestUserAccountWithCorrectUrl() {
        doAnswer {
            callbackArgumentCaptor.value.onResponse(
                call,
                Response.Builder()
                    .request(Request.Builder().url(URL).build())
                    .protocol(Protocol.HTTP_1_0)
                    .message("OK")
                    .code(200)
                    .body("".toResponseBody())
                    .message("")
                    .build()
            )
            null
        }.`when`(call).enqueue(capture(callbackArgumentCaptor))

        compose.onNodeWithTag("auth:url").performTextInput(inputUrl)
        compose.onNodeWithTag("auth:guest").performClick()
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
        doAnswer {
            callbackArgumentCaptor.value.onResponse(
                call,
                Response.Builder()
                    .request(Request.Builder().url(URL).build())
                    .protocol(Protocol.HTTP_1_0)
                    .message("OK")
                    .code(200)
                    .body("".toResponseBody())
                    .message("")
                    .build()
            )
            null
        }.`when`(call).enqueue(capture(callbackArgumentCaptor))

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
        doAnswer {
            callbackArgumentCaptor.value.onResponse(
                call,
                Response.Builder()
                    .request(Request.Builder().url(URL).build())
                    .protocol(Protocol.HTTP_1_0)
                    .message("OK")
                    .code(200)
                    .body("".toResponseBody())
                    .build()
            )
            null
        }.`when`(call).enqueue(capture(callbackArgumentCaptor))

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
        doAnswer {
            callbackArgumentCaptor.value.onResponse(
                call,
                Response.Builder()
                    .request(Request.Builder().url(URL).build())
                    .protocol(Protocol.HTTP_1_0)
                    .message("OK")
                    .code(404)
                    .body("".toResponseBody())
                    .message("Client Error")
                    .build()
            )
            null
        }.`when`(call).enqueue(capture(callbackArgumentCaptor))

        compose.onNodeWithTag("auth:url").performTextInput(inputUrl)
        compose.onNodeWithTag("auth:guest").performClick()
        compose.onNodeWithTag("create-account:submit").performClick()
        compose.onNodeWithText("Client Error", substring = true).assertIsDisplayed()
    }

    @Test fun pendingGuestAuthenticationRetainsItsOwnerAcrossRootRecreation() {
        compose.onNodeWithTag("auth:url").performTextInput(inputUrl)
        compose.onNodeWithTag("auth:guest").performClick()
        compose.onNodeWithTag("create-account:submit").performClick()
        compose.waitForIdle()
        org.mockito.Mockito.verify(call, org.mockito.Mockito.timeout(5_000)).enqueue(capture(callbackArgumentCaptor))
        InstrumentationRegistry.getInstrumentation().runOnMainSync { activityRule.activity.recreate() }
        compose.waitForIdle()
        compose.onNodeWithTag("create-account:submit").assertIsNotEnabled()
        org.mockito.Mockito.verify(call, org.mockito.Mockito.times(1)).enqueue(any())
        callbackArgumentCaptor.value.onResponse(call, Response.Builder().request(Request.Builder().url(URL).build()).protocol(Protocol.HTTP_1_1).code(200).message("OK").body("".toResponseBody()).build())
        awaitHome()
        val app = context.applicationContext as TeamCityApplicationBase
        assertThat(app.appInjector.sharedUserStorage().hasGuestAccountWithUrl(URL), `is`(true))
        org.mockito.Mockito.verify(call, org.mockito.Mockito.times(1)).enqueue(any())
    }

    @Test fun replacingTheAccountStackCancelsPendingAuthenticationAndIgnoresLateSuccess() {
        compose.onNodeWithTag("auth:url").performTextInput(inputUrl)
        compose.onNodeWithTag("auth:guest").performClick()
        compose.onNodeWithTag("create-account:submit").performClick()
        compose.waitForIdle()
        org.mockito.Mockito.verify(call, org.mockito.Mockito.timeout(5_000)).enqueue(capture(callbackArgumentCaptor))
        accounts.home(resetSession = true)
        awaitHome()
        org.mockito.Mockito.verify(call, org.mockito.Mockito.timeout(5_000)).cancel()
        callbackArgumentCaptor.value.onResponse(call, Response.Builder().request(Request.Builder().url(URL).build()).protocol(Protocol.HTTP_1_1).code(200).message("OK").body("".toResponseBody()).build())
        compose.waitForIdle()
        val app = context.applicationContext as TeamCityApplicationBase
        assertThat(app.appInjector.sharedUserStorage().hasGuestAccountWithUrl(URL), `is`(false))
        assertThat(app.appInjector.sharedUserStorage().activeUser.teamcityUrl, `is`("$URL/server"))
        compose.onNodeWithTag("home:tab:Projects").assertIsDisplayed()
    }

    @Module(includes = [CreateAccountActivityTestAppModule::class])
    @InstallIn(SingletonComponent::class)
    object TestBindings
}
