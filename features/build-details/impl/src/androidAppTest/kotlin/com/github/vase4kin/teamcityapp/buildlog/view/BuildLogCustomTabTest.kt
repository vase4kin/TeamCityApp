/*
 * Copyright 2019 Andrey Tolpeev
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

package com.github.vase4kin.teamcityapp.buildlog.view

import android.app.Activity
import android.app.Instrumentation
import android.content.Intent
import android.os.Bundle
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.v2.createEmptyComposeRule
import androidx.test.espresso.intent.Intents.*
import androidx.test.espresso.intent.matcher.IntentMatchers.*
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.github.vase4kin.teamcityapp.TeamCityApplicationBase
import com.github.vase4kin.teamcityapp.api.TeamCityService
import com.github.vase4kin.teamcityapp.base.extractor.BundleExtractorValues
import com.github.vase4kin.teamcityapp.buildlist.api.Build
import com.github.vase4kin.teamcityapp.di.modules.*
import com.github.vase4kin.teamcityapp.helper.*
import com.github.vase4kin.teamcityapp.navigation.*
import dagger.hilt.android.testing.*
import org.hamcrest.Matchers.allOf
import org.junit.*
import org.junit.runner.RunWith

@OptIn(ExperimentalTestApi::class)
@HiltAndroidTest
@RunWith(AndroidJUnit4::class)
class BuildLogCustomTabTest {
    @JvmField
    @Rule(order = 0)
    val hilt = HiltAndroidRule(this)
    private val service: TeamCityService = FakeTeamCityServiceImpl()

    @JvmField
    @Rule(order = 1)
    val api = HiltApiTestRule(hilt) { service }

    @JvmField
    @Rule(order = 2)
    val activity = CustomIntentsTestRule(MainActivity::class.java, IncomingDestinationResolver.BUILD_DETAILS_COMPONENT)

    @JvmField
    @Rule(order = 3)
    val compose = createEmptyComposeRule()

    @Before fun setup() {
        TestUtils.disableOnboarding()
        val app = InstrumentationRegistry.getInstrumentation().targetContext.applicationContext as TeamCityApplicationBase
        app.appInjector.sharedUserStorage().apply {
            clearAll()
            saveGuestUserAccountAndSetItAsActive(Mocks.URL, false)
        }
    }

    @Test fun viewLogOpensTheActiveServersLogInACustomTab() {
        assertLogOpensInBrowser(Mocks.successBuild())
    }

    @Test fun runningBuildLogOpensTheViewerInACustomTab() {
        assertLogOpensInBrowser(Mocks.runningBuild())
    }

    @Test fun failedBuildLogOpensTheViewerInACustomTab() {
        assertLogOpensInBrowser(Mocks.failedBuild())
    }

    private fun assertLogOpensInBrowser(build: Build) {
        activity.launchActivity(Intent().putExtras(Bundle().apply { putSerializable(BundleExtractorValues.BUILD, build) }))
        intending(hasAction(Intent.ACTION_VIEW)).respondWith(Instrumentation.ActivityResult(Activity.RESULT_OK, null))
        compose.waitUntil(10_000) { compose.onAllNodesWithTag("overview:log").fetchSemanticsNodes(atLeastOneRootRequired = false).isNotEmpty() }
        compose.onNodeWithTag("overview:log").performClick()
        intended(allOf(hasAction(Intent.ACTION_VIEW), hasData(Mocks.URL.trimEnd('/') + "/viewLog.html?buildId=${build.id}&tab=buildLog&guest=1"), hasExtraWithKey("android.support.customtabs.extra.SESSION")))
        compose.onNodeWithTag("build-log:screen").assertDoesNotExist()
        compose.onNodeWithTag("overview:screen").assertIsDisplayed()
    }
}
