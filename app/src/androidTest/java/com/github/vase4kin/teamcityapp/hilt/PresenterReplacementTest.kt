package com.github.vase4kin.teamcityapp.hilt

import android.content.Intent
import androidx.test.core.app.ActivityScenario
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.github.vase4kin.teamcityapp.TeamCityApplicationBase
import com.github.vase4kin.teamcityapp.base.extractor.BundleExtractorValues
import com.github.vase4kin.teamcityapp.build_details.view.BuildDetailsActivity
import com.github.vase4kin.teamcityapp.dagger.modules.Mocks
import com.github.vase4kin.teamcityapp.helper.HiltApiTestRule
import com.github.vase4kin.teamcityapp.home.view.HomeActivity
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import org.junit.Assert.assertNotSame
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito.inOrder
import org.mockito.Mockito.spy

@HiltAndroidTest
@RunWith(AndroidJUnit4::class)
class PresenterReplacementTest {
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
    fun homeDisposesOldPresenterBeforeAccountReload() {
        ActivityScenario.launch<HomeActivity>(Intent(app, HomeActivity::class.java)).use { scenario ->
            scenario.onActivity { activity ->
                val previous = spy(activity.presenter)
                activity.presenter = previous
                dispatchNewIntent(activity, Intent(activity, HomeActivity::class.java)
                    .putExtra(BundleExtractorValues.IS_REQUIRED_TO_RELOAD, true))
                assertNotSame(previous, activity.presenter)
                inOrder(previous).apply {
                    verify(previous).onPause()
                    verify(previous).onDestroy()
                }
            }
        }
    }

    @Test
    fun buildDetailsDisposesOldPresenterBeforeNewBuild() {
        val intent = Intent(app, BuildDetailsActivity::class.java)
            .putExtra(BundleExtractorValues.BUILD, Mocks.successBuild())
        ActivityScenario.launch<BuildDetailsActivity>(intent).use { scenario ->
            scenario.onActivity { activity ->
                val previous = spy(activity.presenter)
                activity.presenter = previous
                dispatchNewIntent(activity, Intent(intent))
                assertNotSame(previous, activity.presenter)
                inOrder(previous).apply {
                    verify(previous).onPause()
                    verify(previous).onViewsDestroyed()
                }
            }
        }
    }
    private fun dispatchNewIntent(activity: android.app.Activity, intent: Intent) {
        activity.javaClass.getDeclaredMethod("onNewIntent", Intent::class.java).apply {
            isAccessible = true
        }.invoke(activity, intent)
    }

}
