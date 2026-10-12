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
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.v2.createEmptyComposeRule
import androidx.test.core.app.ActivityScenario
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.github.vase4kin.teamcityapp.TeamCityApplicationBase
import com.github.vase4kin.teamcityapp.base.extractor.BundleExtractorValues
import com.github.vase4kin.teamcityapp.di.modules.Mocks
import com.github.vase4kin.teamcityapp.helper.HiltApiTestRule
import com.github.vase4kin.teamcityapp.helper.TestUtils
import com.github.vase4kin.teamcityapp.navigation.IncomingDestinationResolver
import com.github.vase4kin.teamcityapp.navigation.MainActivity
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotSame
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@HiltAndroidTest
@RunWith(AndroidJUnit4::class)
class BuildLogViewLifecycleTest {
    @javax.inject.Inject lateinit var consent: teamcityapp.features.build_log.api.BuildLogConsentStorage

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
        TestUtils.disableOnboarding()
        kotlinx.coroutines.runBlocking { consent.setAcknowledged(true) }
    }

    @Test
    fun retainedBuildLogCreatesAndDisposesItsWebViewOnEachTabVisit() {
        val intent = Intent(app, MainActivity::class.java).setClassName(app, IncomingDestinationResolver.BUILD_DETAILS_COMPONENT)
            .putExtra(BundleExtractorValues.BUILD, Mocks.successBuild())
        ActivityScenario.launch<MainActivity>(intent).use { scenario ->
            compose.waitUntil(10_000) { compose.onAllNodesWithTag("details:tab:BuildLog").fetchSemanticsNodes(atLeastOneRootRequired = false).isNotEmpty() }
            compose.onNodeWithTag("details:tab:BuildLog").performScrollTo().performClick()
            var webView: WebView? = null
            compose.waitUntil(10_000) {
                scenario.onActivity { webView = it.window.decorView.findWebView() }
                webView != null
            }
            repeat(2) {
                val previous = requireNotNull(webView)
                compose.onNodeWithTag("details:tab:Parameters").performScrollTo().performClick()
                compose.waitUntil(10_000) { compose.onAllNodesWithTag("properties:list").fetchSemanticsNodes(atLeastOneRootRequired = false).isNotEmpty() }
                scenario.onActivity { assertFalse(previous.isAttachedToWindow) }
                compose.onNodeWithTag("details:tab:BuildLog").performScrollTo().performClick()
                webView = null
                compose.waitUntil(10_000) {
                    scenario.onActivity { webView = it.window.decorView.findWebView() }
                    webView != null
                }
                assertNotSame(previous, webView)
            }
            scenario.onActivity { assertTrue(it.supportFragmentManager.fragments.isEmpty()) }
        }
    }

    private fun View.findWebView(): WebView? = when (this) {
        is WebView -> this
        is ViewGroup -> (0 until childCount).firstNotNullOfOrNull { getChildAt(it).findWebView() }
        else -> null
    }
}
