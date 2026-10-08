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

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.v2.createEmptyComposeRule
import androidx.test.espresso.action.ViewActions.clearText
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.action.ViewActions.closeSoftKeyboard
import androidx.test.espresso.action.ViewActions.pressImeActionButton
import androidx.test.espresso.action.ViewActions.scrollTo
import androidx.test.espresso.action.ViewActions.typeText
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.github.vase4kin.teamcityapp.R
import com.github.vase4kin.teamcityapp.TeamCityApplicationBase
import com.github.vase4kin.teamcityapp.helper.CustomActivityTestRule
import com.github.vase4kin.teamcityapp.helper.HiltApiTestRule
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import org.hamcrest.Matchers.not
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import teamcityapp.features.login.impl.LoginActivity

/**
 * Validation tests for [LoginActivity]
 */
@HiltAndroidTest
@RunWith(AndroidJUnit4::class)
class LoginActivityValidationTest {
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
    val activityRule: CustomActivityTestRule<LoginActivity> =
        CustomActivityTestRule(LoginActivity::class.java)

    @Before
    fun setUp() {
        activityRule.launchActivity(null)
    }

    @Test
    fun testUserCanNotCreateAccountWithEmptyUrl() {
        compose.onNodeWithTag("auth:url").performTextClearance()
        compose.onNodeWithTag("auth:guest").performClick()
        compose.onNodeWithTag("login:submit").performClick()
        compose.onNodeWithText(context.getString(teamcityapp.libraries.authentication.R.string.server_cannot_be_empty)).assertIsDisplayed()
    }

    @Test
    fun testUserCanNotCreateAccountWithIncorrectProvidedUrl() {
        compose.onNodeWithTag("auth:url").performTextClearance()
        compose.onNodeWithTag("auth:url").performTextInput("google.com")
        compose.onNodeWithTag("auth:guest").performClick()
        compose.onNodeWithTag("login:submit").performClick()
        compose.onNodeWithText(context.getString(R.string.server_correct_url)).assertIsDisplayed()
    }

    @Test
    fun testUserCanSubmitDataByClickOnActionDone() {
        compose.onNodeWithTag("auth:guest").performClick()
        compose.onNodeWithTag("auth:url").performTextClearance()
        compose.onNodeWithTag("auth:url").performImeAction()
        compose.onNodeWithText(context.getString(teamcityapp.libraries.authentication.R.string.server_cannot_be_empty)).assertIsDisplayed()
    }

    @Test
    fun testUserSeeProtocolInUrlField() {
        compose.onNodeWithTag("auth:url").assert(hasText("https://"))
    }

    @Test
    fun testUserCanNotCreateAccountWithEmptyUserName() {
        compose.onNodeWithTag("login:submit").performScrollTo()
        compose.onNodeWithTag("login:submit").performClick()
        compose.onNodeWithText(context.getString(teamcityapp.libraries.authentication.R.string.server_user_name_cannot_be_empty)).assertIsDisplayed()
    }

    @Test
    fun testUserCanNotCreateAccountWithEmptyPasswordName() {
        compose.onNodeWithTag("auth:username").performTextInput("user")
        compose.onNodeWithTag("login:submit").performScrollTo()
        compose.onNodeWithTag("login:submit").performClick()
        compose.onNodeWithText(context.getString(teamcityapp.libraries.authentication.R.string.server_password_cannot_be_empty)).assertIsDisplayed()
    }

    @Test
    fun testUserWillNotSeeUserAndPasswordFieldsWhenGuestAccountIsOn() {
        compose.onNodeWithTag("auth:username").performTextInput("user")
        compose.onNodeWithTag("auth:username").performImeAction()
        compose.onNodeWithTag("auth:password").performTextInput("pass")
        // Enabling guest mode
        compose.onNodeWithTag("auth:guest").performClick()
        // Check input text layouts are not visible
        compose.onNodeWithTag("auth:username").assertDoesNotExist()
        compose.onNodeWithTag("auth:password").assertDoesNotExist()
        // Enabling user mode
        compose.onNodeWithTag("auth:guest").performClick()
        // Check input text layouts are visible
        compose.onNodeWithTag("auth:username").assertIsDisplayed()
        compose.onNodeWithTag("auth:password").assertIsDisplayed()
    }
}
