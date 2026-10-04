/*
 * Copyright 2020 Andrey Tolpeev
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

package teamcityapp.features.about

import teamcityapp.features.about.impl.AboutActivity
import android.content.Intent
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createEmptyComposeRule
import androidx.lifecycle.Lifecycle
import androidx.test.core.app.ActivityScenario
import androidx.test.espresso.intent.Intents
import androidx.test.espresso.intent.matcher.IntentMatchers.*
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.github.vase4kin.teamcityapp.R
import com.github.vase4kin.teamcityapp.TeamCityApplicationBase
import com.github.vase4kin.teamcityapp.api.TeamCityService
import com.github.vase4kin.teamcityapp.dagger.modules.FakeTeamCityServiceImpl
import com.github.vase4kin.teamcityapp.dagger.modules.Mocks
import com.github.vase4kin.teamcityapp.helper.HiltApiTestRule
import com.google.android.gms.oss.licenses.OssLicensesMenuActivity
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import io.reactivex.Single
import io.reactivex.subjects.SingleSubject
import org.hamcrest.Matchers.allOf
import org.junit.Assert.*
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito.*
import org.mockito.Spy
import teamcityapp.features.about.repository.models.ServerInfo
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit

@HiltAndroidTest
@RunWith(AndroidJUnit4::class)
class AboutActivityTest {
    @JvmField @Rule(order = 0) val hiltRule = HiltAndroidRule(this)
    @JvmField @Rule(order = 1) val mockitoRule = org.mockito.junit.MockitoJUnit.rule()
        .strictness(org.mockito.quality.Strictness.LENIENT)
    @JvmField @Rule(order = 2) val apiRule = HiltApiTestRule(hiltRule) { teamCityService }
    @JvmField @Rule(order = 3) val compose = createEmptyComposeRule()
    @Spy private val teamCityService: TeamCityService = FakeTeamCityServiceImpl()
    private val app get() = InstrumentationRegistry.getInstrumentation().targetContext.applicationContext as TeamCityApplicationBase
    private val info = ServerInfo("xxx117", "https://www.server.xxx177.com")

    @Before fun setUp() {
        app.appInjector.providers()
        app.appInjector.cacheManager().evictAllCache()
        app.appInjector.sharedUserStorage().clearAll()
        app.appInjector.sharedUserStorage().saveGuestUserAccountAndSetItAsActive(Mocks.URL, false)
    }

    private fun launch() = ActivityScenario.launch<AboutActivity>(Intent(app, AboutActivity::class.java))
    private fun awaitText(text: String) {
        compose.waitUntil(5_000) { compose.onAllNodesWithText(text).fetchSemanticsNodes().isNotEmpty() }
        compose.onNodeWithText(text).assertIsDisplayed()
    }
    private fun scrollTo(text: String) {
        compose.onNode(hasScrollAction()).performScrollToNode(hasText(text))
        compose.onNodeWithText(text).assertIsDisplayed()
    }

    @Test fun contentSurvivesRecreationWithoutFragmentsOrAnotherRequest() {
        `when`(teamCityService.serverInfo()).thenReturn(Single.just(info))
        launch().use { scenario ->
            awaitText(info.version)
            compose.onNodeWithText(info.webUrl).assertIsDisplayed()
            compose.onNodeWithText(app.getString(R.string.drawer_item_about)).assertIsDisplayed()
            scenario.recreate()
            awaitText(info.version)
            compose.onNodeWithText(info.webUrl).assertIsDisplayed()
            scenario.onActivity { assertTrue(it.supportFragmentManager.fragments.isEmpty()) }
            verify(teamCityService, times(1)).serverInfo()
        }
    }

    @Test fun serverFailureStillShowsApplicationAndContactDetails() {
        `when`(teamCityService.serverInfo()).thenReturn(Single.error(IllegalStateException("offline")))
        launch().use {
            awaitText(app.getString(R.string.about_app_text_app))
            compose.onNodeWithText(app.getString(R.string.about_app_text_server_info)).assertDoesNotExist()
            scrollTo(app.getString(R.string.about_app_email))
            scrollTo(app.getString(R.string.about_app_text_privacy))
        }
    }

    @Test fun stoppingCancelsAndReturningLoadsFreshContent() {
        val request = SingleSubject.create<ServerInfo>()
        val subscribed = CountDownLatch(1)
        `when`(teamCityService.serverInfo()).thenReturn(
            request.doOnSubscribe { subscribed.countDown() }, Single.just(info))
        launch().use { scenario ->
            assertTrue(subscribed.await(5, TimeUnit.SECONDS))
            scenario.moveToState(Lifecycle.State.CREATED)
            compose.waitUntil(5_000) { !request.hasObservers() }
            request.onSuccess(ServerInfo("late result", "https://late.example"))
            scenario.moveToState(Lifecycle.State.RESUMED)
            awaitText(info.version)
            compose.onNodeWithText("late result").assertDoesNotExist()
        }
    }

    @Test fun recreatingWhileLoadingCancelsAndRestartsRequest() {
        val request = SingleSubject.create<ServerInfo>()
        val subscribed = CountDownLatch(1)
        `when`(teamCityService.serverInfo()).thenReturn(
            request.doOnSubscribe { subscribed.countDown() }, Single.just(info))
        launch().use { scenario ->
            assertTrue(subscribed.await(5, TimeUnit.SECONDS))
            scenario.recreate()
            awaitText(info.version)
            compose.waitUntil(5_000) { !request.hasObservers() }
            scenario.onActivity { assertTrue(it.supportFragmentManager.fragments.isEmpty()) }
        }
    }

    @Test fun linksEmailRateAndLicensesKeepTheirDestinations() {
        `when`(teamCityService.serverInfo()).thenReturn(Single.just(info))
        launch().use {
            awaitText(info.version)
            Intents.init()
            try {
                Intents.intending(org.hamcrest.Matchers.any(Intent::class.java))
                    .respondWith(android.app.Instrumentation.ActivityResult(android.app.Activity.RESULT_OK, null))
                compose.onNodeWithText(info.webUrl).performClick()
                Intents.intended(hasData(info.webUrl))
                scrollTo(app.getString(R.string.about_app_text_rate_app))
                compose.onNodeWithText(app.getString(R.string.about_app_text_rate_app)).performClick()
                Intents.intended(hasData("market://details?id=${app.packageName}"))
                val links = listOf(
                    R.string.about_app_text_found_issue to R.string.about_app_url_found_issue,
                    R.string.about_app_text_source_code to R.string.about_app_url_source_code,
                    R.string.about_app_text_web to R.string.about_app_url_web,
                    R.string.about_app_text_privacy to R.string.about_app_url_privacy)
                links.forEach { (label, url) ->
                    scrollTo(app.getString(label))
                    compose.onNodeWithText(app.getString(label)).performClick()
                    Intents.intended(hasData(app.getString(url)))
                }
                scrollTo(app.getString(R.string.about_app_text_email))
                compose.onNodeWithText(app.getString(R.string.about_app_text_email)).performClick()
                Intents.intended(allOf(hasAction(Intent.ACTION_CHOOSER), hasExtra(Intent.EXTRA_INTENT,
                    allOf(hasAction(Intent.ACTION_SENDTO), hasData("mailto:${app.getString(R.string.about_app_email)}"),
                        hasExtra(Intent.EXTRA_SUBJECT, app.getString(R.string.about_app_email_title))))))
                scrollTo(app.getString(R.string.about_app_text_libraries))
                compose.onNodeWithText(app.getString(R.string.about_app_text_libraries)).performClick()
                Intents.intended(hasComponent(OssLicensesMenuActivity::class.java.name))
            } finally { Intents.release() }
        }
    }

    @Test fun toolbarBackFinishesActivity() {
        `when`(teamCityService.serverInfo()).thenReturn(Single.just(info))
        launch().use { scenario ->
            awaitText(info.version)
            compose.onNodeWithContentDescription(app.getString(R.string.action_back)).performClick()
            compose.waitUntil(5_000) { scenario.state == Lifecycle.State.DESTROYED }
        }
    }
}
