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
import android.view.View
import android.view.ViewGroup
import android.webkit.WebView
import androidx.compose.ui.test.junit4.v2.createEmptyComposeRule
import androidx.test.core.app.ActivityScenario
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.github.vase4kin.teamcityapp.TeamCityApplicationBase
import com.github.vase4kin.teamcityapp.base.extractor.BundleExtractorValues
import com.github.vase4kin.teamcityapp.build_details.view.BuildDetailsActivity
import com.github.vase4kin.teamcityapp.dagger.modules.Mocks
import com.github.vase4kin.teamcityapp.helper.HiltApiTestRule
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNotSame
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import teamcityapp.features.build_log.impl.BuildLogFragment

@HiltAndroidTest
@RunWith(AndroidJUnit4::class)
class BuildLogViewLifecycleTest {
    @JvmField
    @Rule(order = 0)
    val hiltRule = HiltAndroidRule(this)

    @JvmField
    @Rule(order = 1)
    val apiRule = HiltApiTestRule(hiltRule)

    @JvmField
    @Rule(order = 2)
    val compose = createEmptyComposeRule()

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
            val fragment = BuildLogFragment.newInstance("123")
            scenario.onActivity { activity ->
                activity.supportFragmentManager.beginTransaction().add(android.R.id.content, fragment).commitNow()
            }
            compose.waitForIdle()
            repeat(2) {
                lateinit var previousView: View
                lateinit var previousWebView: WebView
                scenario.onActivity { activity ->
                    previousView = fragment.requireView()
                    previousWebView = requireNotNull(previousView.findWebView())
                    activity.supportFragmentManager.beginTransaction().detach(fragment).commitNow()
                    assertNull(fragment.view)
                    activity.supportFragmentManager.beginTransaction().attach(fragment).commitNow()
                }
                compose.waitForIdle()
                scenario.onActivity {
                    assertNotNull(fragment.view)
                    assertNotSame(previousView, fragment.requireView())
                    assertNotSame(previousWebView, fragment.requireView().findWebView())
                    assertNotNull(fragment.requireView().findWebView())
                }
            }
        }
    }

    private fun View.findWebView(): WebView? = when (this) {
        is WebView -> this
        is ViewGroup -> (0 until childCount).firstNotNullOfOrNull { getChildAt(it).findWebView() }
        else -> null
    }
}
