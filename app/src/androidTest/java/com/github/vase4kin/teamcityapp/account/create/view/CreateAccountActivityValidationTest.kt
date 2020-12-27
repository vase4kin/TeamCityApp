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

package com.github.vase4kin.teamcityapp.account.create.view

import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.clearText
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.action.ViewActions.closeSoftKeyboard
import androidx.test.espresso.action.ViewActions.pressBack
import androidx.test.espresso.action.ViewActions.pressImeActionButton
import androidx.test.espresso.action.ViewActions.scrollTo
import androidx.test.espresso.action.ViewActions.typeText
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withContentDescription
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.github.vase4kin.teamcityapp.R
import com.github.vase4kin.teamcityapp.TeamCityApplication
import com.github.vase4kin.teamcityapp.dagger.components.AppComponent
import com.github.vase4kin.teamcityapp.dagger.components.RestApiComponent
import com.github.vase4kin.teamcityapp.dagger.modules.AppModule
import com.github.vase4kin.teamcityapp.dagger.modules.Mocks.Companion.URL
import com.github.vase4kin.teamcityapp.dagger.modules.RestApiModule
import com.github.vase4kin.teamcityapp.helper.CustomActivityTestRule
import com.github.vase4kin.teamcityapp.storage.SharedUserStorage
import it.cosenonjaviste.daggermock.DaggerMockRule
import okhttp3.OkHttpClient
import org.hamcrest.Matchers.not
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.Mockito.`when`

/**
 * Validation tests for [CreateAccountActivity]
 */
@RunWith(AndroidJUnit4::class)
class CreateAccountActivityValidationTest {

    @JvmField
    @Rule
    val daggerRule: DaggerMockRule<RestApiComponent> =
        DaggerMockRule(RestApiComponent::class.java, RestApiModule(URL))
            .addComponentDependency(
                AppComponent::class.java,
                AppModule(InstrumentationRegistry.getInstrumentation().targetContext.applicationContext as TeamCityApplication)
            )
            .set { restApiComponent ->
                val app =
                    InstrumentationRegistry.getInstrumentation().targetContext.applicationContext as TeamCityApplication
                app.setRestApiInjector(restApiComponent)
            }

    @JvmField
    @Rule
    val activityRule: CustomActivityTestRule<CreateAccountActivity> =
        CustomActivityTestRule(CreateAccountActivity::class.java)

    @Mock
    lateinit var storage: SharedUserStorage

    @Mock
    lateinit var client: OkHttpClient

    private val INPUT_URL = URL.replace("https://", "")

    @Before
    fun setUp() {
        `when`(storage.hasGuestAccountWithUrl(URL)).thenReturn(true)
        `when`(storage.hasAccountWithUrl(URL, "user")).thenReturn(true)
        activityRule.launchActivity(null)
    }

    @Test
    fun testUserCanNotCreateGuestUserAccountWithEmptyUrl() {
        onView(withId(R.id.teamcity_url)).perform(clearText(), closeSoftKeyboard())
        onView(withId(R.id.guest_user_switch)).perform(click())
        onView(withId(R.id.btn_login)).perform(click())
        onView(withText(R.string.server_cannot_be_empty)).check(matches(isDisplayed()))
    }

    @Test
    fun testUserCanNotCreateUserAccountWithEmptyUrl() {
        onView(withId(R.id.teamcity_url)).perform(clearText(), pressImeActionButton())
        onView(withId(R.id.user_name)).perform(typeText("user"), pressImeActionButton())
        onView(withId(R.id.password)).perform(typeText("password"), pressImeActionButton())
        onView(withText(R.string.server_cannot_be_empty)).check(matches(isDisplayed()))
    }

    @Test
    fun testUserCanNotCreateAccountWithIncorrectProvidedUrl() {
        onView(withId(R.id.teamcity_url)).perform(
            clearText(),
            typeText("google.com"),
            closeSoftKeyboard()
        )
        onView(withId(R.id.guest_user_switch)).perform(click())
        onView(withId(R.id.btn_login)).perform(click())
        onView(withText(R.string.server_correct_url)).check(matches(isDisplayed()))
    }

    @Test
    fun testUserCanNotCreateGuestUserAccountIfTheSameAccountExist() {
        onView(withId(R.id.teamcity_url)).perform(typeText(INPUT_URL), closeSoftKeyboard())
        onView(withId(R.id.guest_user_switch)).perform(click())
        onView(withId(R.id.btn_login)).perform(click())
        onView(withText(R.string.add_new_account_dialog_account_exist_error_message)).check(
            matches(
                isDisplayed()
            )
        )
    }

    @Test
    fun testUserCanNotCreateUserAccountIfTheSameAccountExist() {
        onView(withId(R.id.teamcity_url)).perform(typeText(INPUT_URL), pressImeActionButton())
        onView(withId(R.id.user_name)).perform(typeText("user"), pressImeActionButton())
        onView(withId(R.id.password)).perform(typeText("pass"), pressImeActionButton())
        onView(withText(R.string.add_new_account_dialog_account_exist_error_message)).check(
            matches(
                isDisplayed()
            )
        )
    }

    @Test
    fun testUserSeesDiscardDialogOnCloseNavigationButtonClick() {
        onView(withId(R.id.teamcity_url)).perform(typeText("not empty"))
        onView(withContentDescription(activityRule.activity.getString(R.string.navigate_up))).perform(
            click()
        )
        onView(withText(R.string.discard_dialog_content)).check(matches(isDisplayed()))
    }

    @Test
    fun testUserSeesDiscardDialogOnBackButtonPressed() {
        onView(withId(R.id.teamcity_url)).perform(
            typeText("not empty"),
            closeSoftKeyboard(),
            pressBack()
        )
        onView(withText(R.string.discard_dialog_content)).check(matches(isDisplayed()))
    }

    @Test
    fun testUserCanSubmitDataByClickOnActionDone() {
        onView(withId(R.id.guest_user_switch)).perform(scrollTo(), click())
        onView(withId(R.id.teamcity_url)).perform(scrollTo(), clearText(), pressImeActionButton())
        onView(withText(R.string.server_cannot_be_empty)).check(matches(isDisplayed()))
    }

    @Test
    fun testUserSeeProtocolInUrlField() {
        onView(withId(R.id.teamcity_url)).check(matches(withText("https://")))
    }

    @Test
    fun testUserCanNotCreateAccountWithEmptyUserName() {
        onView(withId(R.id.btn_login)).perform(click())
        onView(withText(R.string.server_user_name_cannot_be_empty)).check(matches(isDisplayed()))
    }

    @Test
    fun testUserCanNotCreateAccountWithEmptyPasswordName() {
        onView(withId(R.id.user_name)).perform(typeText("user"), closeSoftKeyboard())
        onView(withId(R.id.btn_login)).perform(click())
        onView(withText(R.string.server_password_cannot_be_empty)).check(matches(isDisplayed()))
    }

    @Test
    fun testUserWillNotSeeUserAndPasswordFieldsWhenGuestAccountIsOn() {
        onView(withId(R.id.user_name)).perform(typeText("user"), pressImeActionButton())
        onView(withId(R.id.password)).perform(typeText("pass"), closeSoftKeyboard())
        // Enabling guest mode
        onView(withId(R.id.guest_user_switch)).perform(click())
        // Check input text layouts are not visible
        onView(withId(R.id.user_field_wrapper)).check(matches(not(isDisplayed())))
        onView(withId(R.id.password_field_wrapper)).check(matches(not(isDisplayed())))
        // Enabling user mode
        onView(withId(R.id.guest_user_switch)).perform(click())
        // Check input text layouts are visible
        onView(withId(R.id.user_field_wrapper)).check(matches(isDisplayed()))
        onView(withId(R.id.password_field_wrapper)).check(matches(isDisplayed()))
    }
}
