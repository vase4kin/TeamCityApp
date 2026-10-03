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
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.action.ViewActions.closeSoftKeyboard
import androidx.test.espresso.action.ViewActions.pressImeActionButton
import androidx.test.espresso.action.ViewActions.typeText
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.intent.Intents.intended
import androidx.test.espresso.intent.matcher.BundleMatchers.hasEntry
import androidx.test.espresso.intent.matcher.IntentMatchers.hasComponent
import androidx.test.espresso.intent.matcher.IntentMatchers.hasExtras
import androidx.test.espresso.intent.matcher.IntentMatchers.hasFlags
import androidx.test.espresso.intent.matcher.IntentMatchers.toPackage
import androidx.test.espresso.matcher.ViewMatchers.assertThat
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.github.vase4kin.teamcityapp.TeamCityApplicationBase
import com.github.vase4kin.teamcityapp.R
import com.github.vase4kin.teamcityapp.api.TeamCityService
import com.github.vase4kin.teamcityapp.base.extractor.BundleExtractorValues
import com.github.vase4kin.teamcityapp.dagger.modules.AppModule
import com.github.vase4kin.teamcityapp.dagger.modules.AppModule.CLIENT_AUTH
import com.github.vase4kin.teamcityapp.dagger.modules.AppModule.CLIENT_BASE
import com.github.vase4kin.teamcityapp.dagger.modules.AppModule.CLIENT_BASE_UNSAFE
import com.github.vase4kin.teamcityapp.dagger.modules.FakeTeamCityServiceImpl
import com.github.vase4kin.teamcityapp.dagger.modules.Mocks.Companion.URL
import com.github.vase4kin.teamcityapp.helper.CustomIntentsTestRule
import com.github.vase4kin.teamcityapp.helper.TestUtils
import com.github.vase4kin.teamcityapp.helper.capture
import com.github.vase4kin.teamcityapp.home.view.HomeActivity
import com.github.vase4kin.teamcityapp.storage.SharedUserStorage
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import com.github.vase4kin.teamcityapp.helper.HiltApiTestRule
import okhttp3.Call
import okhttp3.Callback
import okhttp3.OkHttpClient
import okhttp3.Protocol
import okhttp3.Request
import okhttp3.Response
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.CoreMatchers.containsString
import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.core.AllOf.allOf
import dagger.hilt.android.testing.BindValue
import dagger.hilt.android.testing.UninstallModules
import com.github.vase4kin.teamcityapp.account.create.dagger.CreateAccountActivityTestAppModule
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import org.junit.Before
import org.junit.BeforeClass
import org.junit.Ignore
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.ArgumentCaptor
import org.mockito.Captor
import org.mockito.Mock
import org.mockito.Mockito.`when`
import org.mockito.Mockito.doAnswer
import org.mockito.Spy
import org.mockito.kotlin.any
import javax.inject.Named

/**
 * Tests for [CreateAccountActivity] with mocked internet connection
 */
@UninstallModules(AppModule::class)
@HiltAndroidTest
@RunWith(AndroidJUnit4::class)
class CreateAccountActivityTest {

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
    val activityRule: CustomIntentsTestRule<CreateAccountActivity> =
        CustomIntentsTestRule(CreateAccountActivity::class.java)

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

    @Spy
    val teamCityService: TeamCityService = FakeTeamCityServiceImpl()

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
                    .code(200)
                    .message("")
                    .build()
            )
            null
        }.`when`(call).enqueue(capture(callbackArgumentCaptor))

        onView(withId(R.id.teamcity_url)).perform(typeText(inputUrl), closeSoftKeyboard())
        onView(withId(R.id.guest_user_switch)).perform(click())
        onView(withId(R.id.action_create)).perform(click())

        intended(
            allOf(
                hasComponent(HomeActivity::class.java.name),
                hasFlags(
                    Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
                        or Intent.FLAG_ACTIVITY_SINGLE_TOP
                ),
                hasExtras(
                    hasEntry(
                        equalTo(BundleExtractorValues.IS_REQUIRED_TO_RELOAD),
                        equalTo(true)
                    )
                ),
                toPackage("com.github.vase4kin.teamcityapp.mock.debug")
            )
        )

        val storageUtils = SharedUserStorage.init(activityRule.activity, null)
        assertThat(storageUtils.hasGuestAccountWithUrl(URL), `is`(true))
        assertThat(storageUtils.activeUser.teamcityUrl, `is`(URL))
        assertThat(storageUtils.activeUser.isSslDisabled, `is`(false))
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
                    .code(200)
                    .message("")
                    .build()
            )
            null
        }.`when`(call).enqueue(capture(callbackArgumentCaptor))

        onView(withId(R.id.teamcity_url)).perform(typeText(inputUrl), closeSoftKeyboard())
        onView(withId(R.id.guest_user_switch)).perform(click())
        onView(withId(R.id.disable_ssl_switch)).perform(click())
        onView(withText(R.string.warning_ssl_dialog_content)).check(matches(isDisplayed()))
        onView(withText(R.string.dialog_ok_title)).perform(click())
        onView(withId(R.id.action_create)).perform(click())

        intended(
            allOf(
                hasComponent(HomeActivity::class.java.name),
                hasFlags(
                    Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
                        or Intent.FLAG_ACTIVITY_SINGLE_TOP
                ),
                hasExtras(
                    hasEntry(
                        equalTo(BundleExtractorValues.IS_REQUIRED_TO_RELOAD),
                        equalTo(true)
                    )
                ),
                toPackage("com.github.vase4kin.teamcityapp.mock.debug")
            )
        )

        val storageUtils = SharedUserStorage.init(activityRule.activity, null)
        assertThat(storageUtils.hasGuestAccountWithUrl(URL), `is`(true))
        assertThat(storageUtils.activeUser.teamcityUrl, `is`(URL))
        assertThat(storageUtils.activeUser.isSslDisabled, `is`(true))
    }

    /**
     * Verifies that user can be logged in with correct account url and credentials
     */
    @Ignore
    @Test
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
        }.`when`(call).enqueue(capture(callbackArgumentCaptor))

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
                    .code(404)
                    .message("Client Error")
                    .build()
            )
            null
        }.`when`(call).enqueue(capture(callbackArgumentCaptor))

        onView(withId(R.id.teamcity_url)).perform(typeText(inputUrl), closeSoftKeyboard())
        onView(withId(R.id.guest_user_switch)).perform(click())
        onView(withId(R.id.action_create)).perform(click())
        onView(withText(containsString("Client Error"))).check(matches(isDisplayed()))
    }

    @Ignore
    @Test
    fun testUserCanNotCreateAccountIfDataWasNotSaved() {
        // You know what to do
    }

    @Module(includes = [CreateAccountActivityTestAppModule::class])
    @InstallIn(SingletonComponent::class)
    object TestBindings
}
