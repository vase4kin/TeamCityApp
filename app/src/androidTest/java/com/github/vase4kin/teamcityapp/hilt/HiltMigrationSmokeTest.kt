package com.github.vase4kin.teamcityapp.hilt

import android.content.Intent
import androidx.lifecycle.Lifecycle
import androidx.test.core.app.ActivityScenario
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.github.vase4kin.teamcityapp.TeamCityApplicationBase
import com.github.vase4kin.teamcityapp.dagger.modules.Mocks
import com.github.vase4kin.teamcityapp.helper.HiltApiTestRule
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import teamcityapp.features.about.impl.AboutActivity
import teamcityapp.features.change_details.impl.ChangeDetailsActivity
import teamcityapp.features.settings.impl.SettingsActivity
import teamcityapp.features.test_details.impl.TestDetailsActivity
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
        val intent = Intent(app, ChangeDetailsActivity::class.java).putExtras(
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
        ActivityScenario.launch<ChangeDetailsActivity>(intent).use {
            assertEquals(Lifecycle.State.RESUMED, it.state)
            it.recreate()
            assertEquals(Lifecycle.State.RESUMED, it.state)
        }
    }

    @Test
    fun aboutInjectsAndRecreates() {
        ActivityScenario.launch<AboutActivity>(Intent(app, AboutActivity::class.java)).use {
            assertEquals(Lifecycle.State.RESUMED, it.state)
            it.recreate()
            assertEquals(Lifecycle.State.RESUMED, it.state)
        }
    }

    @Test
    fun testDetailsInjectsAndRecreates() {
        val intent = Intent(app, TestDetailsActivity::class.java)
            .putExtra(TestDetailsViewModel.ARG_TEST_URL, "/app/rest/testOccurrences/id:123")
        ActivityScenario.launch<TestDetailsActivity>(intent).use {
            assertEquals(Lifecycle.State.RESUMED, it.state)
            it.recreate()
            assertEquals(Lifecycle.State.RESUMED, it.state)
        }
    }

    @Test
    fun settingsInjectsAndRecreates() {
        ActivityScenario.launch<SettingsActivity>(Intent(app, SettingsActivity::class.java)).use {
            assertEquals(Lifecycle.State.RESUMED, it.state)
            it.recreate()
            assertEquals(Lifecycle.State.RESUMED, it.state)
        }
    }
}
