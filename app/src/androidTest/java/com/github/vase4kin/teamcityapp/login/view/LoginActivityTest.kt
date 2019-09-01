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

package com.github.vase4kin.teamcityapp.login.view

import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.clearText
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.action.ViewActions.closeSoftKeyboard
import androidx.test.espresso.action.ViewActions.pressImeActionButton
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
import com.github.vase4kin.teamcityapp.TeamCityApplication
import com.github.vase4kin.teamcityapp.base.extractor.BundleExtractorValues
import com.github.vase4kin.teamcityapp.crypto.CryptoManager
import com.github.vase4kin.teamcityapp.dagger.components.AppComponent
import com.github.vase4kin.teamcityapp.dagger.modules.AppModule
import com.github.vase4kin.teamcityapp.dagger.modules.AppModule.CLIENT_AUTH
import com.github.vase4kin.teamcityapp.dagger.modules.AppModule.CLIENT_BASE
import com.github.vase4kin.teamcityapp.dagger.modules.AppModule.CLIENT_BASE_UNSAFE
import com.github.vase4kin.teamcityapp.dagger.modules.Mocks.URL
import com.github.vase4kin.teamcityapp.helper.CustomIntentsTestRule
import com.github.vase4kin.teamcityapp.home.view.HomeActivity
import com.github.vase4kin.teamcityapp.remote.RemoteServiceImpl
import it.cosenonjaviste.daggermock.DaggerMockRule
import okhttp3.Call
import okhttp3.Callback
import okhttp3.OkHttpClient
import okhttp3.Protocol
import okhttp3.Request
import okhttp3.Response
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.CoreMatchers.containsString
import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.CoreMatchers.not
import org.hamcrest.core.AllOf.allOf
import org.junit.Before
import org.junit.Ignore
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.ArgumentCaptor
import org.mockito.Captor
import org.mockito.Matchers
import org.mockito.Mock
import org.mockito.Mockito.`when`
import org.mockito.Mockito.doAnswer
import java.io.IOException
import javax.inject.Named

/**
 * Tests for [LoginActivity] with mocked internet connection
 */
@RunWith(AndroidJUnit4::class)
class LoginActivityTest {

    @JvmField
    @Rule
    val daggerRule: DaggerMockRule<AppComponent> = DaggerMockRule(
        AppComponent::class.java,
        AppModule(InstrumentationRegistry.getInstrumentation().targetContext.applicationContext as TeamCityApplication)
    )
        .set { appComponent ->
            val app =
                InstrumentationRegistry.getInstrumentation().targetContext.applicationContext as TeamCityApplication
            app.setAppInjector(appComponent)
        }

    @JvmField
    @Rule
    val activityRule: CustomIntentsTestRule<LoginActivity> =
        CustomIntentsTestRule(LoginActivity::class.java)

    @Captor
    lateinit var callbackArgumentCaptor: ArgumentCaptor<Callback>

    @field:Named(CLIENT_BASE)
    @Mock
    lateinit var okHttpClient: OkHttpClient

    @field:Named(CLIENT_BASE_UNSAFE)
    @Mock
    lateinit var unsafeOkHttpClient: OkHttpClient

    @field:Named(CLIENT_AUTH)
    @Mock
    lateinit var clientAuth: OkHttpClient

    @Mock
    lateinit var cryptoManager: CryptoManager

    @Mock
    lateinit var call: Call

    @Before
    fun setUp() {
        val app =
            InstrumentationRegistry.getInstrumentation().targetContext.applicationContext as TeamCityApplication
        app.appInjector.sharedUserStorage().clearAll()
        `when`(okHttpClient.newCall(Matchers.any(Request::class.java))).thenReturn(call)
        `when`(unsafeOkHttpClient.newCall(Matchers.any(Request::class.java))).thenReturn(call)
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
                    .build()
            )
            null
        }.`when`(call).enqueue(callbackArgumentCaptor.capture())

        activityRule.launchActivity(null)

        onView(withId(R.id.teamcity_url)).perform(
            typeText(urlWithPath.replace("https://", "")),
            closeSoftKeyboard()
        )
        onView(withId(R.id.guest_user_switch)).perform(click())
        onView(withId(R.id.btn_login)).perform(click())

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
            InstrumentationRegistry.getInstrumentation().targetContext.applicationContext as TeamCityApplication
        val storageUtils = app.restApiInjector.sharedUserStorage()
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
                    .build()
            )
            null
        }.`when`(call).enqueue(callbackArgumentCaptor.capture())

        activityRule.launchActivity(null)

        onView(withId(R.id.teamcity_url)).perform(
            typeText(urlWithPath.replace("https://", "")),
            closeSoftKeyboard()
        )
        onView(withId(R.id.guest_user_switch)).perform(click())
        onView(withId(R.id.disable_ssl_switch)).perform(click())
        onView(withText(R.string.warning_ssl_dialog_content)).check(matches(isDisplayed()))
        onView(withText(R.string.dialog_ok_title)).perform(click())
        onView(withId(R.id.btn_login)).perform(click())

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
            InstrumentationRegistry.getInstrumentation().targetContext.applicationContext as TeamCityApplication
        val storageUtils = app.restApiInjector.sharedUserStorage()
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
                    .build()
            )
            null
        }.`when`(call).enqueue(callbackArgumentCaptor.capture())

        activityRule.launchActivity(null)

        onView(withId(R.id.guest_user_switch)).perform(click())
        onView(withId(R.id.teamcity_url)).perform(typeText(inputUrl), pressImeActionButton())

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
            InstrumentationRegistry.getInstrumentation().targetContext.applicationContext as TeamCityApplication
        val storageUtils = app.restApiInjector.sharedUserStorage()
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
                    .build()
            )
            null
        }.`when`(call).enqueue(callbackArgumentCaptor.capture())

        activityRule.launchActivity(null)

        onView(withId(R.id.guest_user_switch)).perform(click())
        onView(withId(R.id.teamcity_url)).perform(typeText(inputUrl), pressImeActionButton())

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
            InstrumentationRegistry.getInstrumentation().targetContext.applicationContext as TeamCityApplication
        val storageUtils = app.restApiInjector.sharedUserStorage()
        assertThat(storageUtils.hasGuestAccountWithUrl(URL), `is`(true))
        assertThat(storageUtils.activeUser.teamcityUrl, `is`(URL))
    }

    /**
     * Verifies that user can be logged in with correct account url and credentials
     */
    @Ignore
    @Test
    @Throws(Throwable::class)
    fun testUserCanCreateUserAccountWithCorrectUrlAndCredentials() {
        doAnswer {
            callbackArgumentCaptor.value.onResponse(
                call,
                Response.Builder()
                    .request(Request.Builder().url(URL).build())
                    .protocol(Protocol.HTTP_1_0)
                    .code(200)
                    .build()
            )
            null
        }.`when`(call).enqueue(callbackArgumentCaptor.capture())

        activityRule.launchActivity(null)

        onView(withId(R.id.teamcity_url)).perform(typeText(inputUrl), closeSoftKeyboard())
        onView(withId(R.id.user_name)).perform(typeText("user"), pressImeActionButton())
        onView(withId(R.id.password)).perform(typeText("pass"), pressImeActionButton())

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
            InstrumentationRegistry.getInstrumentation().targetContext.applicationContext as TeamCityApplication
        val storageUtils = app.restApiInjector.sharedUserStorage()
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
                    .code(404)
                    .message("Client Error")
                    .build()
            )
            null
        }.`when`(call).enqueue(callbackArgumentCaptor.capture())

        activityRule.launchActivity(null)

        onView(withId(R.id.teamcity_url)).perform(typeText(inputUrl), closeSoftKeyboard())
        onView(withId(R.id.guest_user_switch)).perform(click())
        onView(withId(R.id.btn_login)).perform(click())
        onView(withText(containsString("Client Error"))).check(matches(isDisplayed()))
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
                    .code(401)
                    .message("Unauthorized")
                    .build()
            )
            null
        }.`when`(call).enqueue(callbackArgumentCaptor.capture())

        activityRule.launchActivity(null)

        onView(withId(R.id.teamcity_url)).perform(typeText(inputUrl), closeSoftKeyboard())
        onView(withId(R.id.guest_user_switch)).perform(click())
        onView(withId(R.id.btn_login)).perform(click())
        onView(withText(R.string.info_unauthorized_dialog_title)).check(matches(isDisplayed()))
        onView(withText(R.string.info_unauthorized_dialog_content)).check(matches(isDisplayed()))
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
                    .build()
            )
            null
        }.`when`(call).enqueue(callbackArgumentCaptor.capture())

        activityRule.launchActivity(null)

        onView(withId(R.id.teamcity_url)).perform(
            clearText(),
            typeText(urlWithPath),
            closeSoftKeyboard()
        )
        onView(withId(R.id.guest_user_switch)).perform(click())
        onView(withId(R.id.btn_login)).perform(click())

        onView(withText(R.string.warning_ssl_dialog_title)).check(matches(isDisplayed()))
        onView(withText(R.string.server_not_secure_http)).check(matches(isDisplayed()))
        onView(withText(R.string.dialog_ok_title)).perform(click())

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
            InstrumentationRegistry.getInstrumentation().targetContext.applicationContext as TeamCityApplication
        val storageUtils = app.restApiInjector.sharedUserStorage()
        assertThat(storageUtils.hasGuestAccountWithUrl(savedUrl), `is`(true))
        assertThat(storageUtils.activeUser.teamcityUrl, `is`(savedUrl))
        assertThat(storageUtils.activeUser.isSslDisabled, `is`(false))
    }

    @Ignore
    @Test
    @Throws(Throwable::class)
    fun testUserCanNotCreateAccountIfDataWasNotSaved() {
        // You know what to do
    }

    @Test
    fun testUserCannotSeeTryItOutIfItIsNotEnabled() {
        setTryItOutValue(false)

        activityRule.launchActivity(null)

        onView(withId(R.id.give_it_a_try)).check(matches(not(isDisplayed())))
    }

    @Test
    fun testUserCanSeeDeclinesItOutIfItIsEnabled() {
        val urlWithPath = "https://test.com/test"
        setTryItOutValue(true)
        setTryItOutValueUrl(urlWithPath)

        activityRule.launchActivity(null)

        onView(withId(R.id.give_it_a_try)).check(matches(isDisplayed())).perform(click())
        onView(withText(R.string.warning_ssl_dialog_negative)).perform(click())
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
                    .build()
            )
            null
        }.`when`(call).enqueue(callbackArgumentCaptor.capture())

        setTryItOutValue(true)
        setTryItOutValueUrl(urlWithPath)

        activityRule.launchActivity(null)

        onView(withId(R.id.give_it_a_try)).check(matches(isDisplayed())).perform(click())

        onView(withText(R.string.text_try_it_out_button)).perform(click())

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
            InstrumentationRegistry.getInstrumentation().targetContext.applicationContext as TeamCityApplication
        val storageUtils = app.restApiInjector.sharedUserStorage()
        assertThat(storageUtils.hasGuestAccountWithUrl(savedUrl), `is`(true))
        assertThat(storageUtils.activeUser.teamcityUrl, `is`(savedUrl))
        assertThat(storageUtils.activeUser.isSslDisabled, `is`(false))
    }

    private fun setTryItOutValue(value: Boolean) {
        val app =
            InstrumentationRegistry.getInstrumentation().targetContext.applicationContext as TeamCityApplication
        val remoteService = app.appInjector.remoteService() as RemoteServiceImpl
        remoteService.showTryItOut = value
    }

    private fun setTryItOutValueUrl(value: String) {
        val app =
            InstrumentationRegistry.getInstrumentation().targetContext.applicationContext as TeamCityApplication
        val remoteService = app.appInjector.remoteService() as RemoteServiceImpl
        remoteService.showTryItOutUrl = value
    }
}
