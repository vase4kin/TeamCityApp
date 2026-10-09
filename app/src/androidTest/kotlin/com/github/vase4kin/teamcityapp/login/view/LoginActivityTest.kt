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
import androidx.test.espresso.intent.Intents.intended
import androidx.test.espresso.intent.matcher.BundleMatchers.hasEntry
import androidx.test.espresso.intent.matcher.IntentMatchers.hasComponent
import androidx.test.espresso.intent.matcher.IntentMatchers.hasExtras
import androidx.test.espresso.matcher.ViewMatchers.assertThat
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.github.vase4kin.teamcityapp.R
import com.github.vase4kin.teamcityapp.TeamCityApplicationBase
import com.github.vase4kin.teamcityapp.base.extractor.BundleExtractorValues
import com.github.vase4kin.teamcityapp.dagger.modules.AppModule
import com.github.vase4kin.teamcityapp.dagger.modules.AppModule.CLIENT_AUTH
import com.github.vase4kin.teamcityapp.dagger.modules.AppModule.CLIENT_BASE
import com.github.vase4kin.teamcityapp.dagger.modules.AppModule.CLIENT_BASE_UNSAFE
import com.github.vase4kin.teamcityapp.dagger.modules.Mocks.Companion.URL
import com.github.vase4kin.teamcityapp.helper.CustomIntentsTestRule
import com.github.vase4kin.teamcityapp.helper.HiltApiTestRule
import com.github.vase4kin.teamcityapp.helper.capture
import com.github.vase4kin.teamcityapp.home.view.HomeActivity
import com.github.vase4kin.teamcityapp.login.dagger.LoginActivityTestAppModule
import com.github.vase4kin.teamcityapp.remote.RemoteServiceImpl
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.android.testing.BindValue
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import dagger.hilt.android.testing.UninstallModules
import dagger.hilt.components.SingletonComponent
import java.io.IOException
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
import org.hamcrest.CoreMatchers.not
import org.hamcrest.core.AllOf.allOf
import org.junit.Before
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
import org.mockito.kotlin.any
import teamcityapp.features.login.impl.LoginActivity
import teamcityapp.libraries.security.CryptoManager

/**
 * Tests for [LoginActivity] with mocked internet connection
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
    val apiRule = HiltApiTestRule(hiltRule)

    @JvmField
    @Rule(order = 3)
    val activityRule: CustomIntentsTestRule<LoginActivity> =
        CustomIntentsTestRule(LoginActivity::class.java)

    @Captor
    lateinit var callbackArgumentCaptor: ArgumentCaptor<Callback>

    @field:Named(CLIENT_BASE)
    @BindValue
    @Mock
    lateinit var okHttpClient: OkHttpClient

    @field:Named(CLIENT_BASE_UNSAFE)
    @BindValue
    @Mock
    lateinit var unsafeOkHttpClient: OkHttpClient

    @field:Named(CLIENT_AUTH)
    @BindValue
    @Mock
    lateinit var clientAuth: OkHttpClient

    @BindValue
    @Mock
    lateinit var cryptoManager: CryptoManager

    @Mock
    lateinit var call: Call

    @Before
    fun setUp() {
        for (client in listOf(okHttpClient, unsafeOkHttpClient)) {
            val builder = mock(OkHttpClient.Builder::class.java)
            `when`(client.newBuilder()).thenReturn(builder)
            `when`(builder.authenticator(any<Authenticator>())).thenReturn(builder)
            `when`(builder.build()).thenReturn(client)
        }

        val app =
            InstrumentationRegistry.getInstrumentation().targetContext.applicationContext as TeamCityApplicationBase
        app.appInjector.sharedUserStorage().clearAll()
        `when`(okHttpClient.newCall(any<Request>())).thenReturn(call)
        `when`(unsafeOkHttpClient.newCall(any<Request>())).thenReturn(call)
    }

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
        doAnswer {
            callbackArgumentCaptor.value.onResponse(
                call,
                Response.Builder()
                    .request(Request.Builder().url(urlWithPath).build())
                    .protocol(Protocol.HTTP_1_0)
                    .message(messageEmpty)
                    .code(200)
                    .body("".toResponseBody())
                    .build()
            )
            null
        }.`when`(call).enqueue(capture(callbackArgumentCaptor))

        activityRule.launchActivity(null)

        compose.onNodeWithTag("auth:url").performTextInput(urlWithPath.replace("https://", ""))
        compose.onNodeWithTag("auth:guest").performClick()
        compose.onNodeWithTag("login:submit").performClick()

        compose.waitForIdle()
        intended(
            allOf(
                hasComponent(HomeActivity::class.java.name),
                hasExtras(
                    hasEntry(
                        equalTo(BundleExtractorValues.IS_NEW_ACCOUNT_CREATED),
                        equalTo(true)
                    )
                )
            )
        )

        val app =
            InstrumentationRegistry.getInstrumentation().targetContext.applicationContext as TeamCityApplicationBase
        val storageUtils = app.appInjector.sharedUserStorage()
        assertThat(storageUtils.hasGuestAccountWithUrl(savedUrl), `is`(true))
        assertThat(storageUtils.activeUser.teamcityUrl, `is`(savedUrl))
        assertThat(storageUtils.activeUser.isSslDisabled, `is`(false))
    }

    /**
     * Verifies that user can be logged in as guest user with correct account url ignoring ssl
     */
    @Test
    fun testUserCanCreateGuestUserAccountWithCorrectUrlIgnoringSsl() {
        val urlWithPath = "https://teamcity.com/server"
        val savedUrl = "$urlWithPath/"
        doAnswer {
            callbackArgumentCaptor.value.onResponse(
                call,
                Response.Builder()
                    .request(Request.Builder().url(urlWithPath).build())
                    .protocol(Protocol.HTTP_1_0)
                    .message(messageEmpty)
                    .code(200)
                    .body("".toResponseBody())
                    .build()
            )
            null
        }.`when`(call).enqueue(capture(callbackArgumentCaptor))

        activityRule.launchActivity(null)

        compose.onNodeWithTag("auth:url").performTextInput(urlWithPath.replace("https://", ""))
        compose.onNodeWithTag("auth:guest").performClick()
        compose.onNodeWithTag("auth:ssl").performClick()
        compose.onNodeWithText(context.getString(R.string.warning_ssl_dialog_content)).assertIsDisplayed()
        compose.onNodeWithText(context.getString(R.string.dialog_ok_title).uppercase()).performClick()
        compose.onNodeWithTag("login:submit").performClick()

        compose.waitForIdle()
        intended(
            allOf(
                hasComponent(HomeActivity::class.java.name),
                hasExtras(
                    hasEntry(
                        equalTo(BundleExtractorValues.IS_NEW_ACCOUNT_CREATED),
                        equalTo(true)
                    )
                )
            )
        )

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
        doAnswer {
            callbackArgumentCaptor.value.onResponse(
                call,
                Response.Builder()
                    .request(Request.Builder().url(URL).build())
                    .protocol(Protocol.HTTP_1_0)
                    .message(messageEmpty)
                    .code(200)
                    .body("".toResponseBody())
                    .build()
            )
            null
        }.`when`(call).enqueue(capture(callbackArgumentCaptor))

        activityRule.launchActivity(null)

        compose.onNodeWithTag("auth:guest").performClick()
        compose.onNodeWithTag("auth:url").performTextInput(inputUrl)
        compose.onNodeWithTag("auth:url").performImeAction()

        compose.waitForIdle()
        intended(
            allOf(
                hasComponent(HomeActivity::class.java.name),
                hasExtras(
                    hasEntry(
                        equalTo(BundleExtractorValues.IS_NEW_ACCOUNT_CREATED),
                        equalTo(true)
                    )
                )
            )
        )

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
        doAnswer {
            callbackArgumentCaptor.value.onResponse(
                call,
                Response.Builder()
                    .request(Request.Builder().url(URL).build())
                    .protocol(Protocol.HTTP_1_0)
                    .message(messageEmpty)
                    .code(200)
                    .body("".toResponseBody())
                    .build()
            )
            null
        }.`when`(call).enqueue(capture(callbackArgumentCaptor))

        activityRule.launchActivity(null)

        compose.onNodeWithTag("auth:guest").performClick()
        compose.onNodeWithTag("auth:url").performTextInput(inputUrl)
        compose.onNodeWithTag("auth:url").performImeAction()

        compose.waitForIdle()
        intended(
            allOf(
                hasComponent(HomeActivity::class.java.name),
                hasExtras(
                    hasEntry(
                        equalTo(BundleExtractorValues.IS_NEW_ACCOUNT_CREATED),
                        equalTo(true)
                    )
                )
            )
        )

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

        activityRule.launchActivity(null)

        compose.onNodeWithTag("auth:url").performTextInput(inputUrl)
        compose.onNodeWithTag("auth:username").performTextInput("user")
        compose.onNodeWithTag("auth:username").performImeAction()
        compose.onNodeWithTag("auth:password").performTextInput("pass")
        compose.onNodeWithTag("auth:password").performImeAction()

        compose.waitForIdle()
        intended(
            allOf(
                hasComponent(HomeActivity::class.java.name),
                hasExtras(
                    hasEntry(
                        equalTo(BundleExtractorValues.IS_NEW_ACCOUNT_CREATED),
                        equalTo(true)
                    )
                )
            )
        )

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

        activityRule.launchActivity(null)

        compose.onNodeWithTag("auth:url").performTextInput(inputUrl)
        compose.onNodeWithTag("auth:guest").performClick()
        compose.onNodeWithTag("login:submit").performClick()
        compose.onNodeWithText("Client Error", substring = true).assertIsDisplayed()
    }

    /**
     * Verifies that user can be notified with dialog info for 401 errors
     */
    @Test
    @Throws(IOException::class)
    fun testUserIsNotifiedIfServerReturns401Request() {
        doAnswer {
            callbackArgumentCaptor.value.onResponse(
                call,
                Response.Builder()
                    .request(Request.Builder().url(URL).build())
                    .protocol(Protocol.HTTP_1_0)
                    .message("OK")
                    .code(401)
                    .body("".toResponseBody())
                    .message("Unauthorized")
                    .build()
            )
            null
        }.`when`(call).enqueue(capture(callbackArgumentCaptor))

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
        doAnswer {
            callbackArgumentCaptor.value.onResponse(
                call,
                Response.Builder()
                    .request(Request.Builder().url(urlWithPath).build())
                    .protocol(Protocol.HTTP_1_0)
                    .message(messageEmpty)
                    .code(200)
                    .body("".toResponseBody())
                    .build()
            )
            null
        }.`when`(call).enqueue(capture(callbackArgumentCaptor))

        activityRule.launchActivity(null)

        compose.onNodeWithTag("auth:url").performTextClearance()
        compose.onNodeWithTag("auth:url").performTextInput(urlWithPath)
        compose.onNodeWithTag("auth:guest").performClick()
        compose.onNodeWithTag("login:submit").performClick()

        compose.onNodeWithText(context.getString(R.string.warning_ssl_dialog_title)).assertIsDisplayed()
        compose.onNodeWithText(context.getString(R.string.server_not_secure_http)).assertIsDisplayed()
        compose.onNodeWithText(context.getString(R.string.dialog_ok_title).uppercase()).performClick()

        compose.waitForIdle()
        intended(
            allOf(
                hasComponent(HomeActivity::class.java.name),
                hasExtras(
                    hasEntry(
                        equalTo(BundleExtractorValues.IS_NEW_ACCOUNT_CREATED),
                        equalTo(true)
                    )
                )
            )
        )

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
        doAnswer {
            callbackArgumentCaptor.value.onResponse(call, Response.Builder().request(Request.Builder().url(URL).build()).protocol(Protocol.HTTP_1_1).code(200).message("OK").body("".toResponseBody()).build())
            null
        }.`when`(call).enqueue(capture(callbackArgumentCaptor))
        activityRule.launchActivity(null)
        compose.onNodeWithTag("auth:url").performTextInput(inputUrl)
        compose.onNodeWithTag("auth:username").performTextInput("user")
        compose.onNodeWithTag("auth:password").performTextInput("pass")
        compose.onNodeWithTag("auth:password").performImeAction()
        compose.onNodeWithText(context.getString(teamcityapp.libraries.authentication.R.string.error_save_account)).assertIsDisplayed()
        val app = context.applicationContext as TeamCityApplicationBase
        assertThat(app.appInjector.sharedUserStorage().hasAccountWithUrl(URL, "user"), `is`(false))
        org.junit.Assert.assertEquals(0, androidx.test.espresso.intent.Intents.getIntents().count { it.component?.className == HomeActivity::class.java.name })
    }

    @Test
    fun pendingGuestAuthenticationSurvivesActivityRecreationWithoutAnotherRequest() {
        activityRule.launchActivity(null)
        compose.onNodeWithTag("auth:url").performTextInput(inputUrl)
        compose.onNodeWithTag("auth:guest").performClick()
        compose.onNodeWithTag("login:submit").performClick()
        compose.waitForIdle()
        org.mockito.Mockito.verify(call).enqueue(capture(callbackArgumentCaptor))
        InstrumentationRegistry.getInstrumentation().runOnMainSync { activityRule.activity.recreate() }
        compose.waitForIdle()
        compose.onNodeWithTag("auth:progress").assertExists()
        org.mockito.Mockito.verify(call, org.mockito.Mockito.times(1)).enqueue(any())
        callbackArgumentCaptor.value.onResponse(call, Response.Builder().request(Request.Builder().url(URL).build()).protocol(Protocol.HTTP_1_1).code(200).message("OK").body("".toResponseBody()).build())
        compose.waitForIdle()
        compose.waitForIdle()
        intended(hasComponent(HomeActivity::class.java.name))
        val app = context.applicationContext as TeamCityApplicationBase
        assertThat(app.appInjector.sharedUserStorage().hasGuestAccountWithUrl(URL), `is`(true))
    }

    @Test
    fun testUserCannotSeeTryItOutIfItIsNotEnabled() {
        setTryItOutValue(false)

        activityRule.launchActivity(null)

        compose.onNodeWithTag("login:demo-card")
            .assertDoesNotExist()
    }

    @Test
    fun testUserCanSeeDeclinesItOutIfItIsEnabled() {
        val urlWithPath = "https://test.com/test"
        setTryItOutValue(true)
        setTryItOutValueUrl(urlWithPath)

        activityRule.launchActivity(null)

        compose.onNodeWithTag("login:demo").performScrollTo()
            .assertIsDisplayed()
            .performClick()
        compose.onNodeWithText(context.getString(teamcityapp.libraries.authentication.R.string.warning_ssl_dialog_negative)).performClick()
        assertNoUnverifiedIntents()
    }

    @Test
    fun testUserCanSeeTryItOutIfItIsEnabled() {
        val urlWithPath = "https://test.com/test"
        val savedUrl = "$urlWithPath/"
        doAnswer {
            callbackArgumentCaptor.value.onResponse(
                call,
                Response.Builder()
                    .request(Request.Builder().url(urlWithPath).build())
                    .protocol(Protocol.HTTP_1_0)
                    .message(messageEmpty)
                    .code(200)
                    .body("".toResponseBody())
                    .build()
            )
            null
        }.`when`(call).enqueue(capture(callbackArgumentCaptor))

        setTryItOutValue(true)
        setTryItOutValueUrl(urlWithPath)

        activityRule.launchActivity(null)

        compose.onNodeWithTag("login:demo").performScrollTo()
            .assertIsDisplayed().performClick()

        compose.onNodeWithText(context.getString(teamcityapp.features.login.impl.R.string.dialog_try_it_out_title)).performClick()

        compose.waitForIdle()
        intended(
            allOf(
                hasComponent(HomeActivity::class.java.name),
                hasExtras(
                    hasEntry(
                        equalTo(BundleExtractorValues.IS_NEW_ACCOUNT_CREATED),
                        equalTo(true)
                    )
                )
            )
        )

        val app =
            InstrumentationRegistry.getInstrumentation().targetContext.applicationContext as TeamCityApplicationBase
        val storageUtils = app.appInjector.sharedUserStorage()
        assertThat(storageUtils.hasGuestAccountWithUrl(savedUrl), `is`(true))
        assertThat(storageUtils.activeUser.teamcityUrl, `is`(savedUrl))
        assertThat(storageUtils.activeUser.isSslDisabled, `is`(false))
    }

    private fun setTryItOutValue(value: Boolean) {
        val app =
            InstrumentationRegistry.getInstrumentation().targetContext.applicationContext as TeamCityApplicationBase
        val remoteService = app.appInjector.remoteService() as RemoteServiceImpl
        remoteService.showTryItOut = value
    }

    private fun setTryItOutValueUrl(value: String) {
        val app =
            InstrumentationRegistry.getInstrumentation().targetContext.applicationContext as TeamCityApplicationBase
        val remoteService = app.appInjector.remoteService() as RemoteServiceImpl
        remoteService.showTryItOutUrl = value
    }

    @Module(includes = [LoginActivityTestAppModule::class])
    @InstallIn(SingletonComponent::class)
    object TestBindings
}
