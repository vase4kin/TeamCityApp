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
import teamcityapp.features.change.view.ARG_BUNDLE_DATA
import teamcityapp.features.change.view.ChangeActivity
import teamcityapp.features.settings.view.SettingsActivity
import teamcityapp.features.test_details.view.TestDetailsActivity

@HiltAndroidTest
@RunWith(AndroidJUnit4::class)
class HiltMigrationSmokeTest {
    @JvmField @Rule(order = 0) val hiltRule = HiltAndroidRule(this)
    @JvmField @Rule(order = 1) val apiRule = HiltApiTestRule(hiltRule)

    private val app: TeamCityApplicationBase
        get() = InstrumentationRegistry.getInstrumentation().targetContext.applicationContext as TeamCityApplicationBase

    @Before
    fun setUp() {
        app.appInjector.sharedUserStorage().clearAll()
        app.appInjector.sharedUserStorage().saveGuestUserAccountAndSetItAsActive(Mocks.URL, false)
    }

    @Test
    fun changeDetailsInjectsAndRecreates() {
        val intent = Intent(app, ChangeActivity::class.java).putExtra(
            ARG_BUNDLE_DATA,
            ChangeActivity.Companion.BundleData("123", "Commit", "Developer", "01 Oct 2026", emptyList(), "abc123", Mocks.URL)
        )
        ActivityScenario.launch<ChangeActivity>(intent).use {
            assertEquals(Lifecycle.State.RESUMED, it.state)
            it.onActivity { activity -> assertEquals(2, activity.stateHolder.adapter.itemCount) }
            it.recreate()
            it.onActivity { activity -> assertEquals(2, activity.stateHolder.adapter.itemCount) }
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
            .putExtra(TestDetailsActivity.ARG_TEST_URL, "/app/rest/testOccurrences/id:123")
        ActivityScenario.launch<TestDetailsActivity>(intent).use {
            assertEquals(Lifecycle.State.RESUMED, it.state)
            it.recreate()
            assertEquals(Lifecycle.State.RESUMED, it.state)
        }
    }

    @Test
    fun settingsInjectsAndRecreates() {
        ActivityScenario.launch<SettingsActivity>(Intent(app, SettingsActivity::class.java)).use {
            it.onActivity { activity -> assertEquals(1, activity.supportFragmentManager.fragments.size) }
            it.recreate()
            assertEquals(Lifecycle.State.RESUMED, it.state)
        }
    }
}
