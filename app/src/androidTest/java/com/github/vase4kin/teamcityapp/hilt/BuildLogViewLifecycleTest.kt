package com.github.vase4kin.teamcityapp.hilt

import android.content.Intent
import androidx.test.core.app.ActivityScenario
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.github.vase4kin.teamcityapp.TeamCityApplicationBase
import com.github.vase4kin.teamcityapp.base.extractor.BundleExtractorValues
import com.github.vase4kin.teamcityapp.build_details.view.BuildDetailsActivity
import com.github.vase4kin.teamcityapp.buildlog.view.BuildLogFragment
import com.github.vase4kin.teamcityapp.dagger.modules.Mocks
import com.github.vase4kin.teamcityapp.helper.HiltApiTestRule
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNotSame
import org.junit.Assert.assertNull
import org.junit.Assert.assertSame
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@HiltAndroidTest
@RunWith(AndroidJUnit4::class)
class BuildLogViewLifecycleTest {
    @JvmField @Rule(order = 0) val hiltRule = HiltAndroidRule(this)
    @JvmField @Rule(order = 1) val apiRule = HiltApiTestRule(hiltRule)

    private val app: TeamCityApplicationBase
        get() = InstrumentationRegistry.getInstrumentation().targetContext.applicationContext as TeamCityApplicationBase

    @Before
    fun setUp() {
        val storage = app.appInjector.sharedUserStorage()
        storage.clearAll()
        storage.saveGuestUserAccountAndSetItAsActive(Mocks.URL, false)
    }

    @Test
    fun sameFragmentCanCreateAndDestroyItsViewRepeatedly() {
        val intent = Intent(app, BuildDetailsActivity::class.java)
            .putExtra(BundleExtractorValues.BUILD, Mocks.successBuild())
        ActivityScenario.launch<BuildDetailsActivity>(intent).use { scenario ->
            scenario.onActivity { activity ->
                val fragment = BuildLogFragment.newInstance("123")
                val manager = activity.supportFragmentManager
                manager.beginTransaction().add(android.R.id.content, fragment).commitNow()
                val webClient = fragment.webClient
                repeat(2) {
                    val previousView = fragment.requireView()
                    manager.beginTransaction().detach(fragment).commitNow()
                    assertNull(fragment.view)
                    manager.beginTransaction().attach(fragment).commitNow()
                    assertNotNull(fragment.view)
                    assertNotSame(previousView, fragment.requireView())
                    assertSame(webClient, fragment.webClient)
                }
            }
        }
    }
}
