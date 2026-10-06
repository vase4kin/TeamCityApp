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

package teamcityapp.features.change_details.impl

import android.app.Activity
import android.app.Instrumentation.ActivityResult
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createEmptyComposeRule
import androidx.lifecycle.Lifecycle
import androidx.test.core.app.ActivityScenario
import androidx.test.espresso.intent.Intents
import androidx.test.espresso.intent.Intents.intended
import androidx.test.espresso.intent.Intents.intending
import androidx.test.espresso.intent.matcher.IntentMatchers.hasAction
import androidx.test.espresso.intent.matcher.IntentMatchers.hasData
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.github.vase4kin.teamcityapp.TeamCityApplicationBase
import com.github.vase4kin.teamcityapp.dagger.modules.Mocks
import com.github.vase4kin.teamcityapp.helper.HiltApiTestRule
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@HiltAndroidTest
@RunWith(AndroidJUnit4::class)
class ChangeDetailsActivityTest {
    @JvmField
    @Rule(order = 0)
    val hiltRule = HiltAndroidRule(this)

    @JvmField
    @Rule(order = 1)
    val apiRule = HiltApiTestRule(hiltRule)

    @JvmField
    @Rule(order = 2)
    val compose = createEmptyComposeRule()
    private val app get() = InstrumentationRegistry.getInstrumentation().targetContext.applicationContext as TeamCityApplicationBase
    private val comment = "Preserve the Compose screen appearance"
    private val file = "app/src/main/kotlin/example/Screen + Test.kt"
    private val webUrl = "https://teamcity.example/change/123"

    @Before fun setUp() {
        app.appInjector.sharedUserStorage().clearAll()
        app.appInjector.sharedUserStorage().saveGuestUserAccountAndSetItAsActive(Mocks.URL, false)
    }
    private fun intent(files: Boolean = true) = Intent(app, ChangeDetailsActivity::class.java).putExtras(
        Bundle().apply {
            putString("change:id", "123 & 456")
            putString("change:comment", "  $comment  ")
            putString("change:user", "Developer")
            putString("change:date", "01 Oct 2026")
            putStringArrayList("change:file_names", if (files) arrayListOf(file) else arrayListOf())
            putStringArrayList("change:file_types", if (files) arrayListOf("changed") else arrayListOf())
            putString("change:revision", "abc123")
            putString("change:web_url", webUrl)
        }
    )
    private fun awaitText(text: String) {
        compose.waitUntil(5_000) { compose.onAllNodesWithText(text).fetchSemanticsNodes().isNotEmpty() }
        compose.onNodeWithText(text).assertIsDisplayed()
    }

    @Test fun detailsSurviveActivityRecreationAndReturningFromBackground() {
        ActivityScenario.launch<ChangeDetailsActivity>(intent()).use { scenario ->
            awaitText(comment)
            compose.onNodeWithText("By Developer on 01 Oct 2026").assertIsDisplayed()
            compose.onNodeWithText(file).assertIsDisplayed()
            scenario.recreate()
            awaitText(comment)
            scenario.moveToState(Lifecycle.State.CREATED)
            scenario.moveToState(Lifecycle.State.RESUMED)
            awaitText(file)
        }
    }

    @Test fun noFilesHidesDiffHint() {
        ActivityScenario.launch<ChangeDetailsActivity>(intent(false)).use {
            awaitText("Changed files (0)")
            compose.onNodeWithText("Click on a file to view a diff").assertDoesNotExist()
        }
    }

    @Test fun moreDetailsOpensOriginalBrowserUrl() {
        Intents.init()
        try {
            intending(hasAction(Intent.ACTION_VIEW)).respondWith(ActivityResult(Activity.RESULT_OK, null))
            ActivityScenario.launch<ChangeDetailsActivity>(intent()).use {
                awaitText(comment)
                compose.onNodeWithText("MORE DETAILS").performClick()
                intended(hasData(webUrl))
            }
        } finally {
            Intents.release()
        }
    }

    @Test fun fileClickOpensEncodedDiffForActiveServer() {
        Intents.init()
        try {
            intending(hasAction(Intent.ACTION_VIEW)).respondWith(ActivityResult(Activity.RESULT_OK, null))
            ActivityScenario.launch<ChangeDetailsActivity>(intent()).use {
                awaitText(file)
                compose.onNodeWithTag("change_details:file:0").performClick()
                val expected = Uri.parse(Mocks.URL).buildUpon().appendPath("diffView.html")
                    .appendQueryParameter("id", "123 & 456").appendQueryParameter("vcsFileName", file).build()
                intended(hasData(expected))
            }
        } finally {
            Intents.release()
        }
    }

    @Test fun closeFinishesActivity() {
        ActivityScenario.launch<ChangeDetailsActivity>(intent()).use { scenario ->
            awaitText(comment)
            compose.onNodeWithContentDescription("Close").performClick()
            compose.waitUntil(5_000) { scenario.state == Lifecycle.State.DESTROYED }
        }
    }

    @Test fun missingInputClosesActivity() {
        ActivityScenario.launch<ChangeDetailsActivity>(Intent(app, ChangeDetailsActivity::class.java)).use { scenario ->
            compose.waitUntil(5_000) { scenario.state == Lifecycle.State.DESTROYED }
        }
    }
}
