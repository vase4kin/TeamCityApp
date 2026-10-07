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

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.v2.createEmptyComposeRule
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
import com.github.vase4kin.teamcityapp.TeamCityApplicationBase
import com.github.vase4kin.teamcityapp.account.create.dagger.CreateAccountActivityValidationTestAppModule
import com.github.vase4kin.teamcityapp.dagger.modules.AppModule
import com.github.vase4kin.teamcityapp.dagger.modules.Mocks.Companion.URL
import com.github.vase4kin.teamcityapp.helper.CustomActivityTestRule
import com.github.vase4kin.teamcityapp.helper.HiltApiTestRule
import com.github.vase4kin.teamcityapp.storage.SharedUserStorage
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.android.testing.BindValue
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import dagger.hilt.android.testing.UninstallModules
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import org.hamcrest.Matchers.not
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.Mockito.`when`
import teamcityapp.features.create_account.impl.CreateAccountActivity

/**
 * Validation tests for [CreateAccountActivity]
 */
@UninstallModules(AppModule::class)
@HiltAndroidTest
@RunWith(AndroidJUnit4::class)
class CreateAccountActivityValidationTest {
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
    val activityRule: CustomActivityTestRule<CreateAccountActivity> =
        CustomActivityTestRule(CreateAccountActivity::class.java)

    @BindValue
    @Mock
    lateinit var storage: SharedUserStorage

    @BindValue
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
        compose.onNodeWithTag("auth:url").performTextClearance()
        compose.onNodeWithTag("auth:guest").performClick()
        compose.onNodeWithTag("create-account:submit").performClick()
        compose.onNodeWithText(context.getString(teamcityapp.libraries.authentication.R.string.server_cannot_be_empty)).assertIsDisplayed()
    }

    @Test
    fun testUserCanNotCreateUserAccountWithEmptyUrl() {
        compose.onNodeWithTag("auth:url").performTextClearance()
        compose.onNodeWithTag("auth:url").performImeAction()
        compose.onNodeWithTag("auth:username").performTextInput("user")
        compose.onNodeWithTag("auth:username").performImeAction()
        compose.onNodeWithTag("auth:password").performTextInput("password")
        compose.onNodeWithTag("auth:password").performImeAction()
        compose.onNodeWithText(context.getString(teamcityapp.libraries.authentication.R.string.server_cannot_be_empty)).assertIsDisplayed()
    }

    @Test
    fun testUserCanNotCreateAccountWithIncorrectProvidedUrl() {
        compose.onNodeWithTag("auth:url").performTextClearance()
        compose.onNodeWithTag("auth:url").performTextInput("google.com")
        compose.onNodeWithTag("auth:guest").performClick()
        compose.onNodeWithTag("create-account:submit").performClick()
        compose.onNodeWithText(context.getString(R.string.server_correct_url)).assertIsDisplayed()
    }

    @Test
    fun testUserCanNotCreateGuestUserAccountIfTheSameAccountExist() {
        compose.onNodeWithTag("auth:url").performTextInput(INPUT_URL)
        compose.onNodeWithTag("auth:guest").performClick()
        compose.onNodeWithTag("create-account:submit").performClick()
        compose.onNodeWithText(context.getString(teamcityapp.features.create_account.impl.R.string.add_new_account_dialog_account_exist_error_message)).assertIsDisplayed()
    }

    @Test
    fun testUserCanNotCreateUserAccountIfTheSameAccountExist() {
        compose.onNodeWithTag("auth:url").performTextInput(INPUT_URL)
        compose.onNodeWithTag("auth:url").performImeAction()
        compose.onNodeWithTag("auth:username").performTextInput("user")
        compose.onNodeWithTag("auth:username").performImeAction()
        compose.onNodeWithTag("auth:password").performTextInput("pass")
        compose.onNodeWithTag("auth:password").performImeAction()
        compose.onNodeWithText(context.getString(teamcityapp.features.create_account.impl.R.string.add_new_account_dialog_account_exist_error_message)).assertIsDisplayed()
    }

    @Test
    fun testUserSeesDiscardDialogOnCloseNavigationButtonClick() {
        compose.onNodeWithTag("auth:url").performTextInput("not empty")
        compose.onNodeWithContentDescription("Close").performClick()
        compose.onNodeWithText(context.getString(teamcityapp.features.create_account.impl.R.string.discard_dialog_content)).assertIsDisplayed()
    }

    @Test
    fun testUserSeesDiscardDialogOnBackButtonPressed() {
        compose.onNodeWithTag("auth:url").performTextInput("not empty")
        androidx.test.espresso.Espresso.closeSoftKeyboard()
        androidx.test.espresso.Espresso.pressBack()
        compose.onNodeWithText(context.getString(teamcityapp.features.create_account.impl.R.string.discard_dialog_content)).assertIsDisplayed()
    }

    @Test
    fun testUserCanSubmitDataByClickOnActionDone() {
        compose.onNodeWithTag("auth:guest").performScrollTo()
        compose.onNodeWithTag("auth:guest").performClick()
        compose.onNodeWithTag("auth:url").performScrollTo()
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
        compose.onNodeWithTag("create-account:submit").performClick()
        compose.onNodeWithText(context.getString(teamcityapp.libraries.authentication.R.string.server_user_name_cannot_be_empty)).assertIsDisplayed()
    }

    @Test
    fun testUserCanNotCreateAccountWithEmptyPasswordName() {
        compose.onNodeWithTag("auth:username").performTextInput("user")
        compose.onNodeWithTag("create-account:submit").performClick()
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

    @Module(includes = [CreateAccountActivityValidationTestAppModule::class])
    @InstallIn(SingletonComponent::class)
    object TestBindings
}
