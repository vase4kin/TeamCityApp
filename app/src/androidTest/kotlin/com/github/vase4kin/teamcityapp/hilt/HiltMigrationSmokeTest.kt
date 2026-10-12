/*
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

package com.github.vase4kin.teamcityapp.hilt

import android.content.Intent
import androidx.lifecycle.Lifecycle
import androidx.test.core.app.ActivityScenario
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.github.vase4kin.teamcityapp.TeamCityApplicationBase
import com.github.vase4kin.teamcityapp.di.modules.Mocks
import com.github.vase4kin.teamcityapp.helper.HiltApiTestRule
import com.github.vase4kin.teamcityapp.navigation.IncomingDestinationResolver
import com.github.vase4kin.teamcityapp.navigation.MainActivity
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import teamcityapp.features.test_details.impl.TestDetailsViewModel

@HiltAndroidTest
@RunWith(AndroidJUnit4::class)
class HiltMigrationSmokeTest {
    @JvmField
    @Rule(order = 0)
    val hiltRule = HiltAndroidRule(this)

    @JvmField
    @Rule(order = 1)
    val apiRule = HiltApiTestRule(hiltRule)

    private val app: TeamCityApplicationBase
        get() = InstrumentationRegistry.getInstrumentation().targetContext.applicationContext as TeamCityApplicationBase

    @Before
    fun setUp() {
        app.appInjector.sharedUserStorage().clearAll()
        app.appInjector.sharedUserStorage().saveGuestUserAccountAndSetItAsActive(Mocks.URL, false)
    }

    @Test
    fun changeDetailsInjectsAndRecreates() {
        val intent = Intent().setComponent(android.content.ComponentName(app.packageName, IncomingDestinationResolver.CHANGE_DETAILS_COMPONENT)).putExtras(
            android.os.Bundle().apply {
                putString("change:id", "123")
                putString("change:comment", "Commit")
                putString("change:user", "Developer")
                putString("change:date", "01 Oct 2026")
                putStringArrayList("change:file_names", arrayListOf())
                putStringArrayList("change:file_types", arrayListOf())
                putString("change:revision", "abc123")
                putString("change:web_url", Mocks.URL)
            }
        )
        ActivityScenario.launch<MainActivity>(intent).use {
            assertEquals(Lifecycle.State.RESUMED, it.state)
            it.recreate()
            assertEquals(Lifecycle.State.RESUMED, it.state)
        }
    }

    @Test
    fun aboutInjectsAndRecreates() {
        ActivityScenario.launch<MainActivity>(Intent().setClassName(app, IncomingDestinationResolver.ABOUT_COMPONENT)).use {
            assertEquals(Lifecycle.State.RESUMED, it.state)
            it.recreate()
            assertEquals(Lifecycle.State.RESUMED, it.state)
        }
    }

    @Test
    fun testDetailsInjectsAndRecreates() {
        val intent = Intent().setComponent(android.content.ComponentName(app.packageName, IncomingDestinationResolver.TEST_DETAILS_COMPONENT))
            .putExtra(TestDetailsViewModel.ARG_TEST_URL, "/app/rest/testOccurrences/id:123")
        ActivityScenario.launch<MainActivity>(intent).use {
            assertEquals(Lifecycle.State.RESUMED, it.state)
            it.recreate()
            assertEquals(Lifecycle.State.RESUMED, it.state)
        }
    }

    @Test
    fun settingsInjectsAndRecreates() {
        ActivityScenario.launch<MainActivity>(Intent().setClassName(app, IncomingDestinationResolver.SETTINGS_COMPONENT)).use {
            assertEquals(Lifecycle.State.RESUMED, it.state)
            it.recreate()
            assertEquals(Lifecycle.State.RESUMED, it.state)
        }
    }

    @Test
    fun manageAccountsInjectsAndRecreates() {
        ActivityScenario.launch<MainActivity>(Intent().setComponent(android.content.ComponentName(app.packageName, IncomingDestinationResolver.MANAGE_ACCOUNTS_COMPONENT))).use {
            assertEquals(Lifecycle.State.RESUMED, it.state)
            it.recreate()
            assertEquals(Lifecycle.State.RESUMED, it.state)
        }
    }
}
