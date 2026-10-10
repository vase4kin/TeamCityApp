/*
 * Copyright 2020 Andrey Tolpeev
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

package com.github.vase4kin.teamcityapp.overview.view

import android.app.Activity
import android.app.Instrumentation.ActivityResult
import android.content.Intent
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.v2.createEmptyComposeRule
import androidx.test.core.app.ActivityScenario
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.Espresso.openActionBarOverflowOrOptionsMenu
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.action.ViewActions.scrollTo
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.intent.Intents.intended
import androidx.test.espresso.intent.Intents.intending
import androidx.test.espresso.intent.matcher.IntentMatchers.*
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.github.vase4kin.teamcityapp.R
import com.github.vase4kin.teamcityapp.TeamCityApplicationBase
import com.github.vase4kin.teamcityapp.api.TeamCityService
import com.github.vase4kin.teamcityapp.base.extractor.BundleExtractorValues
import com.github.vase4kin.teamcityapp.build_details.api.BuildCancelRequest
import com.github.vase4kin.teamcityapp.build_details.view.BuildDetailsActivity
import com.github.vase4kin.teamcityapp.buildlist.api.Build
import com.github.vase4kin.teamcityapp.buildlist.api.CanceledInfo
import com.github.vase4kin.teamcityapp.buildlist.api.Triggered
import com.github.vase4kin.teamcityapp.buildlist.api.User
import com.github.vase4kin.teamcityapp.dagger.modules.FakeTeamCityServiceImpl
import com.github.vase4kin.teamcityapp.dagger.modules.Mocks
import com.github.vase4kin.teamcityapp.helper.BuildComposeFixtures
import com.github.vase4kin.teamcityapp.helper.CustomActivityTestRule
import com.github.vase4kin.teamcityapp.helper.HiltApiTestRule
import com.github.vase4kin.teamcityapp.helper.TestUtils
import com.github.vase4kin.teamcityapp.helper.TestUtils.Companion.matchToolbarSubTitle
import com.github.vase4kin.teamcityapp.helper.TestUtils.Companion.matchToolbarTitle
import com.github.vase4kin.teamcityapp.helper.tapNativePagerTab
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import io.reactivex.Single
import io.reactivex.subjects.SingleSubject
import java.util.concurrent.atomic.AtomicInteger
import okhttp3.ResponseBody.Companion.toResponseBody
import org.hamcrest.core.AllOf.allOf
import org.junit.Assert.*
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.ArgumentMatchers.anyString
import org.mockito.Mockito.*
import org.mockito.Spy
import retrofit2.HttpException
import retrofit2.Response
import teamcityapp.features.bottom_sheet.impl.R as SheetR
import teamcityapp.features.build_history.api.BuildHistoryNavigation
import teamcityapp.features.build_overview.impl.R as OverviewR
import teamcityapp.features.navigation.api.NavigationNavigation
import teamcityapp.libraries.builds.*

@HiltAndroidTest
@RunWith(AndroidJUnit4::class)
class OverviewFragmentTest {
    @JvmField
    @Rule(order = 0)
    val hiltRule = HiltAndroidRule(this)

    @JvmField
    @Rule(order = 1)
    val mockitoRule = org.mockito.junit.MockitoJUnit.rule().strictness(org.mockito.quality.Strictness.LENIENT)

    @JvmField
    @Rule(order = 2)
    val apiRule = HiltApiTestRule(hiltRule) { teamCityService }

    @JvmField
    @Rule(order = 3)
    val activityRule = CustomActivityTestRule(BuildDetailsActivity::class.java)

    @JvmField
    @Rule(order = 4)
    val compose = createEmptyComposeRule()

    @Spy private val teamCityService: TeamCityService = FakeTeamCityServiceImpl()
    private val context get() = InstrumentationRegistry.getInstrumentation().targetContext

    @Before fun setUp() {
        androidx.test.espresso.intent.Intents.init()
        TestUtils.disableOnboarding()
        val app = context.applicationContext as TeamCityApplicationBase
        app.appInjector.cacheManager().evictAllCache()
        app.appInjector.sharedUserStorage().clearAll()
        app.appInjector.sharedUserStorage().saveUserAccountAndSetItAsActive(
            Mocks.URL,
            "alice",
            "password",
            false,
            object : com.github.vase4kin.teamcityapp.storage.SharedUserStorage.OnStorageListener {
                override fun onSuccess() = Unit
                override fun onFail() {
                    fail("Could not save authenticated fixture account")
                }
            }
        )
    }
    private fun overviewText(value: String) = compose.onAllNodes(hasText(value) and hasAnyAncestor(hasTestTag("overview:screen")))
    private fun text(id: Int) = context.getString(id)
    private fun awaitText(value: String) {
        compose.waitUntil(10_000) {
            compose.onAllNodesWithText(value).fetchSemanticsNodes(atLeastOneRootRequired = false).indices.any { index ->
                runCatching { compose.onAllNodesWithText(value)[index].assertIsDisplayed() }.isSuccess
            }
        }
    }
    private fun awaitTag(value: String) {
        compose.waitUntil(10_000) { compose.onAllNodesWithTag(value).fetchSemanticsNodes(atLeastOneRootRequired = false).isNotEmpty() }
    }
    private fun tab(title: String) = compose.tapNativePagerTab(title)
    private fun sheet(label: String) {
        awaitText(label)
        compose.onNodeWithText(label).performClick()
        compose.waitUntil(10_000) { compose.onAllNodesWithTag("sheet:content").fetchSemanticsNodes(atLeastOneRootRequired = false).isEmpty() }
    }

    @org.junit.After fun releaseIntents() {
        androidx.test.espresso.intent.Intents.release()
    }

    @Test fun successRowsAndLoadedToolbarAreRendered() {
        openLegacy(Mocks.successBuild())
        row("Result", "Success")
        row("Time", "21 Jun 16 23:00 - 23:30 (30m:)")
        row("Branch", "refs/heads/master")
        row("Agent", "agent-love")
        row("TriggeredBy", "code-lover")
        row("Configuration", "build type name")
        row("Project", "project name")
        matchToolbarTitle("#2459")
        matchToolbarSubTitle("build type name")
    }

    @Test fun initialErrorRetriesIntoCurrentContent() {
        `when`(teamCityService.build(anyString())).thenReturn(Single.error(RuntimeException("offline")))
        activityRule.launchActivity(BuildComposeFixtures.intent())
        compose.waitUntil(10_000) { overviewText(text(teamcityapp.libraries.theme.R.string.error_load_message)).fetchSemanticsNodes().isNotEmpty() }
        overviewText(text(teamcityapp.libraries.theme.R.string.error_load_message)).onFirst().assertIsDisplayed()
        `when`(teamCityService.build(anyString())).thenReturn(Single.just(BuildComposeFixtures.legacy(BuildComposeFixtures.finished)))
        overviewText(text(teamcityapp.libraries.theme.R.string.action_retry)).onFirst().performClick()
        row("Result", "Current build succeeded")
    }

    @Test fun resultSheetCopiesTheResolvedValue() {
        openLegacy(Mocks.successBuild())
        clickRow("Result")
        sheet(text(SheetR.string.build_element_copy))
        onView(withText(R.string.build_element_copy_text)).check(matches(isDisplayed()))
        InstrumentationRegistry.getInstrumentation().runOnMainSync {
            val clipboard = context.getSystemService(android.content.Context.CLIPBOARD_SERVICE) as android.content.ClipboardManager
            assertEquals("Success", clipboard.primaryClip!!.getItemAt(0).text.toString())
        }
    }

    @Test fun canceledUserRealNameAndCancellationTime() {
        cancellation(User("user.name", "User name"), "User name")
    }

    @Test fun canceledUsernameFallback() {
        cancellation(User("user.name", null), "user.name")
    }

    @Test fun triggeredUserRealName() {
        trigger("user", User("john.117", "John one one seven"), "TriggeredBy", "John one one seven")
    }

    @Test fun triggeredUsernameFallback() {
        trigger("user", User("john.117", null), "TriggeredBy", "john.117")
    }

    @Test fun restartedUserRealName() {
        trigger("restarted", User("john.117", "John one one seven"), "RestartedBy", "John one one seven")
    }

    @Test fun restartedUsernameFallback() {
        trigger("restarted", User("john.117", null), "RestartedBy", "john.117")
    }

    @Test fun deletedTriggerUserIsExplicit() {
        trigger("user", null, "TriggeredBy", text(OverviewR.string.overview_deleted_user))
    }

    @Test fun deletedRestartUserIsExplicit() {
        trigger("restarted", null, "RestartedBy", text(OverviewR.string.overview_deleted_user))
    }

    @Test fun deletedTriggerConfigurationIsExplicit() {
        trigger("buildType", null, "TriggeredBy", text(OverviewR.string.overview_deleted_configuration))
    }

    @Test fun personalBuildShowsTheCurrentUser() {
        openLegacy(
            Mocks.successBuild().apply {
                isPersonal = true
                triggered = Triggered("user", null, User("user.name", "User name"))
            }
        )
        row("Personal", "User name")
    }

    @Test fun branchNavigationUsesLoadedBranchAndConfiguration() {
        openCurrent(BuildComposeFixtures.finished)
        clickRow("Branch")
        sheet(text(SheetR.string.build_element_show_all_builds_built_branch))
        intended(allOf(hasComponent(BuildHistoryNavigation.LEGACY_ACTIVITY), hasExtra(BundleExtractorValues.ID, "current-config")))
        verify(teamCityService).listBuilds("current-config", "state:any,canceled:any,failedToStart:any,branch:name:current-branch,personal:false,pinned:false,count:10")
    }

    @Test fun configurationNavigationUsesLoadedConfiguration() {
        openCurrent(BuildComposeFixtures.finished)
        clickRow("Configuration")
        sheet(text(SheetR.string.build_element_open_build_type))
        intended(allOf(hasComponent(BuildHistoryNavigation.LEGACY_ACTIVITY), hasExtra(BundleExtractorValues.ID, "current-config"), hasExtra(BundleExtractorValues.NAME, "Current configuration")))
    }

    @Test fun projectNavigationUsesLoadedProjectAndInstalledAlias() {
        openCurrent(BuildComposeFixtures.finished)
        clickRow("Project")
        sheet(text(SheetR.string.build_element_open_project))
        intended(allOf(hasComponent(NavigationNavigation.LEGACY_ACTIVITY), hasExtra(BundleExtractorValues.ID, "current-project"), hasExtra(BundleExtractorValues.NAME, "Current project")))
    }

    @Test fun shareUsesTheCurrentWebUrl() {
        intending(hasAction(Intent.ACTION_CHOOSER)).respondWith(ActivityResult(Activity.RESULT_OK, null))
        openCurrent(BuildComposeFixtures.finished)
        menu(text(OverviewR.string.overview_share))
        val chooser = androidx.test.espresso.intent.Intents.getIntents().last { it.action == Intent.ACTION_CHOOSER }

        @Suppress("DEPRECATION")
        val share = chooser.getParcelableExtra<Intent>(Intent.EXTRA_INTENT)!!
        assertEquals(Intent.ACTION_SEND, share.action)
        assertEquals("https://ci.example/current", share.getStringExtra(Intent.EXTRA_TEXT))
    }

    @Test fun browserUsesTheCurrentWebUrl() {
        intending(hasAction(Intent.ACTION_VIEW)).respondWith(ActivityResult(Activity.RESULT_OK, null))
        openCurrent(BuildComposeFixtures.finished)
        menu(text(OverviewR.string.overview_browser))
        intended(allOf(hasAction(Intent.ACTION_VIEW), hasData("https://ci.example/current")))
    }

    @Test fun runningLoadedAfterQueuedIncomingUsesStopAndLatestHref() {
        doReturn(Single.just(BuildComposeFixtures.legacy(BuildComposeFixtures.finished))).`when`(teamCityService).cancelBuild(anyString(), com.github.vase4kin.teamcityapp.helper.any())
        openCurrent(BuildComposeFixtures.loaded)
        menu(text(OverviewR.string.overview_stop))
        onView(withText(R.string.text_stop_the_build)).check(matches(isDisplayed()))
        onView(withText(R.string.text_stop_button)).perform(click())
        verify(teamCityService).cancelBuild("/builds/42", BuildCancelRequest(false))
        onView(withText(R.string.text_build_is_stopped)).check(matches(isDisplayed()))
    }

    @Test fun stoppingCanReAddTheCurrentBuild() {
        doReturn(Single.just(BuildComposeFixtures.legacy(BuildComposeFixtures.finished))).`when`(teamCityService).cancelBuild(anyString(), com.github.vase4kin.teamcityapp.helper.any())
        openCurrent(BuildComposeFixtures.loaded)
        menu(text(OverviewR.string.overview_stop))
        onView(withText(R.string.text_re_add_build)).perform(click())
        onView(withText(R.string.text_stop_button)).perform(click())
        verify(teamCityService).cancelBuild("/builds/42", BuildCancelRequest(true))
    }

    @Test fun queuedCurrentBuildUsesRemoveWithoutReAdd() {
        doReturn(Single.just(BuildComposeFixtures.legacy(BuildComposeFixtures.incoming))).`when`(teamCityService).cancelBuild(anyString(), com.github.vase4kin.teamcityapp.helper.any())
        openCurrent(BuildComposeFixtures.incoming)
        menu(text(OverviewR.string.overview_remove))
        onView(withText(R.string.text_re_add_build)).check(androidx.test.espresso.assertion.ViewAssertions.doesNotExist())
        onView(withText(R.string.text_remove_from_queue_button)).perform(click())
        verify(teamCityService).cancelBuild("/queue/42", BuildCancelRequest(false))
    }

    @Test fun stopForbiddenKeepsCurrentRows() {
        stopFailure(403, R.string.error_stop_build_forbidden_error)
    }

    @Test fun stopServerErrorKeepsCurrentRows() {
        stopFailure(500, R.string.error_base_stop_build_error)
    }

    @Test fun restartUsesCurrentBranchAndOwnProperties() {
        doReturn(Single.just(BuildComposeFixtures.legacy(BuildComposeFixtures.incoming))).`when`(teamCityService).queueBuild(com.github.vase4kin.teamcityapp.helper.any())
        openCurrent(BuildComposeFixtures.finished)
        menu(text(OverviewR.string.overview_restart))
        onView(withText(R.string.text_restart_button)).perform(click())
        val queued = org.mockito.kotlin.argumentCaptor<Build>()
        verify(teamCityService).queueBuild(queued.capture())
        assertEquals("current-config", queued.firstValue.buildType?.id)
        assertEquals("current-branch", queued.firstValue.branchName)
        assertEquals("env", queued.firstValue.properties!!.properties.single().name)
        assertEquals("production", queued.firstValue.properties!!.properties.single().value)
        onView(withText(R.string.text_build_is_restarted)).check(matches(isDisplayed()))
    }

    @Test fun restartForbiddenIsReported() {
        restartFailure(403, R.string.error_restart_build_forbidden_error)
    }

    @Test fun restartServerFailureIsReported() {
        restartFailure(500, R.string.error_base_restart_build_error)
    }

    @Test fun restoredOverviewRetainsLoadedStateAndNativeActions() {
        val calls = AtomicInteger()
        `when`(teamCityService.build(anyString())).thenAnswer {
            calls.incrementAndGet()
            Single.just(BuildComposeFixtures.legacy(BuildComposeFixtures.loaded))
        }
        ActivityScenario.launch<BuildDetailsActivity>(BuildComposeFixtures.intent()).use { scenario ->
            row("Result", "Running current build")
            val completedCalls = calls.get()
            scenario.recreate()
            row("Result", "Running current build")
            matchToolbarTitle("#latest-number")
            matchToolbarSubTitle("Current configuration")
            assertEquals(completedCalls, calls.get())
            menu(text(OverviewR.string.overview_stop))
            onView(withText(R.string.text_stop_the_build)).check(matches(isDisplayed()))
        }
    }

    @Test fun refreshFailureRetainsSuccessfulContentAndAllowsRetry() {
        openCurrent(BuildComposeFixtures.finished)
        `when`(teamCityService.build(anyString())).thenReturn(Single.error(RuntimeException("refresh offline")))
        compose.onNodeWithTag("overview:list").performTouchInput { swipeDown() }
        awaitText(text(teamcityapp.libraries.list_ui.R.string.list_refresh_failed))
        row("Result", "Current build succeeded")
        `when`(teamCityService.build(anyString())).thenReturn(Single.just(BuildComposeFixtures.legacy(BuildComposeFixtures.finished.copy(statusText = "Updated after retry"))))
        compose.onNodeWithText(text(teamcityapp.libraries.list_ui.R.string.list_action_retry)).performClick()
        row("Result", "Updated after retry")
    }

    @Test fun pendingOverviewIsCanceledOnPauseAndReloadedOnResume() {
        val pending = SingleSubject.create<Build>()
        val cancellations = AtomicInteger()
        `when`(teamCityService.build(anyString())).thenReturn(pending.doOnDispose { cancellations.incrementAndGet() })
        ActivityScenario.launch<BuildDetailsActivity>(BuildComposeFixtures.intent()).use { scenario ->
            compose.waitUntil(10_000) { pending.hasObservers() }
            scenario.moveToState(androidx.lifecycle.Lifecycle.State.CREATED)
            compose.waitUntil(10_000) { cancellations.get() == 1 }
            assertFalse(pending.hasObservers())
            `when`(teamCityService.build(anyString())).thenReturn(Single.just(BuildComposeFixtures.legacy(BuildComposeFixtures.finished)))
            scenario.moveToState(androidx.lifecycle.Lifecycle.State.RESUMED)
            row("Result", "Current build succeeded")
            overviewText(text(teamcityapp.libraries.theme.R.string.error_load_message)).assertCountEquals(0)
        }
    }

    @Test fun restartOnboardingIsDismissedWhenOverviewLosesVisibility() {
        TestUtils.enableOnboarding()
        `when`(teamCityService.listArtifacts(anyString(), anyString())).thenReturn(Single.just(Mocks.artifacts()))
        openLegacy(Mocks.successBuild(), BuildComposeFixtures.finished)
        com.azimolabs.conditionwatcher.ConditionWatcher.waitForCondition(object : com.azimolabs.conditionwatcher.Instruction() {
            override fun getDescription() = "Build overview onboarding is displayed"
            override fun checkCondition() = try {
                onView(withId(R.id.material_target_prompt_view)).check(matches(isDisplayed()))
                true
            } catch (ignored: Exception) {
                false
            }
        })
        // Select through the actual native host while the prompt overlay owns touch input.
        InstrumentationRegistry.getInstrumentation().runOnMainSync {
            val tabs = activityRule.activity.findViewById<com.google.android.material.tabs.TabLayout>(R.id.tabLayout)
            (0 until tabs.tabCount).mapNotNull(tabs::getTabAt).single { it.text.toString() == "Artifacts" }.select()
        }
        tab("Artifacts")
        awaitText("AndroidManifest.xml")
        onView(withId(R.id.material_target_prompt_view)).check(androidx.test.espresso.assertion.ViewAssertions.doesNotExist())
    }

    @Test fun newIntentReplacesQueuedTabsCancelsOldOverviewAndUsesNewLoadedConfiguration() {
        val queued = BuildComposeFixtures.incoming.copy(waitReason = "Previous queued build")
        val nextIncoming = BuildComposeFixtures.finished.copy(
            id = "replacement-43",
            href = "/builds/replacement-43",
            number = "replacement-incoming",
            statusText = "Replacement incoming snapshot",
            buildTypeId = "replacement-incoming-config",
            configuration = BuildConfigurationData("replacement-incoming-config", "Replacement incoming configuration", "replacement-project", "Replacement project")
        )
        val nextLoaded = nextIncoming.copy(
            number = "replacement-loaded",
            statusText = "Replacement build succeeded",
            buildTypeId = "replacement-loaded-config",
            configuration = BuildConfigurationData("replacement-loaded-config", "Replacement loaded configuration", "replacement-project", "Replacement project")
        )
        doReturn(Single.just(BuildComposeFixtures.legacy(queued))).`when`(teamCityService).build(queued.href)
        doReturn(Single.just(BuildComposeFixtures.legacy(nextLoaded))).`when`(teamCityService).build(nextIncoming.href)
        intending(hasComponent(BuildHistoryNavigation.LEGACY_ACTIVITY)).respondWith(ActivityResult(Activity.RESULT_CANCELED, null))
        val oldRefresh = SingleSubject.create<Build>()
        val oldCancellations = AtomicInteger()

        ActivityScenario.launch<BuildDetailsActivity>(BuildComposeFixtures.intent(queued)).use { scenario ->
            row("WaitReason", "Previous queued build")
            onView(withText(R.string.tab_artifacts)).check(androidx.test.espresso.assertion.ViewAssertions.doesNotExist())
            lateinit var oldOverview: teamcityapp.features.build_overview.impl.BuildOverviewFragment
            lateinit var oldOverviewLifecycle: androidx.lifecycle.Lifecycle
            scenario.onActivity { activity ->
                oldOverview = activity.supportFragmentManager.fragments
                    .filterIsInstance<teamcityapp.features.build_overview.impl.BuildOverviewFragment>().single()
                oldOverviewLifecycle = oldOverview.lifecycle
            }

            // Pending work makes disposal observable, beyond replacing the native presenter.
            doReturn(oldRefresh.doOnDispose { oldCancellations.incrementAndGet() }).`when`(teamCityService).build(queued.href)
            compose.onNodeWithTag("overview:list").performTouchInput { swipeDown() }
            compose.waitUntil(10_000) { oldRefresh.hasObservers() }
            scenario.onActivity { activity ->
                val replacement = BuildComposeFixtures.intent(nextIncoming)
                activity.javaClass.getDeclaredMethod("onNewIntent", Intent::class.java).apply {
                    isAccessible = true
                }.invoke(activity, replacement)
                assertTrue(BuildComposeFixtures.fullPayload(BundleExtractorValues.BUILD, nextIncoming).matches(activity.intent))
                assertEquals(androidx.lifecycle.Lifecycle.State.DESTROYED, oldOverviewLifecycle.currentState)
                assertFalse(oldOverview.isAdded)
            }
            compose.waitUntil(10_000) { oldCancellations.get() == 1 }
            assertFalse(oldRefresh.hasObservers())
            oldRefresh.onSuccess(BuildComposeFixtures.legacy(queued.copy(waitReason = "Stale queued response")))

            row("Result", "Replacement build succeeded")
            matchToolbarTitle("#replacement-loaded")
            matchToolbarSubTitle("Replacement loaded configuration")
            compose.onNodeWithText("Stale queued response").assertDoesNotExist()
            onView(withText(R.string.tab_artifacts)).perform(scrollTo()).check(matches(isDisplayed()))
            scenario.onActivity { activity ->
                val replacementOverview = activity.supportFragmentManager.fragments
                    .filterIsInstance<teamcityapp.features.build_overview.impl.BuildOverviewFragment>().single()
                assertNotSame(oldOverview, replacementOverview)
            }
            verify(teamCityService, times(1)).build(nextIncoming.href)

            clickRow("Configuration")
            sheet(text(SheetR.string.build_element_open_build_type))
            intended(
                allOf(
                    hasComponent(BuildHistoryNavigation.LEGACY_ACTIVITY),
                    hasExtra(BundleExtractorValues.ID, "replacement-loaded-config"),
                    hasExtra(BundleExtractorValues.NAME, "Replacement loaded configuration")
                )
            )
        }
    }

    private fun openLegacy(build: Build, incoming: BuildLaunchData = BuildComposeFixtures.incoming) {
        `when`(teamCityService.build(anyString())).thenReturn(Single.just(build))
        activityRule.launchActivity(BuildComposeFixtures.intent(incoming))
        awaitTag("overview:list")
    }
    private fun openCurrent(build: BuildLaunchData) = openLegacy(BuildComposeFixtures.legacy(build))
    private fun row(field: String, value: String) {
        awaitTag("overview:list")
        compose.onNodeWithTag("overview:list").performScrollToNode(hasTestTag("overview:row:$field"))
        compose.waitUntil(10_000) {
            compose.onAllNodes(hasTestTag("overview:row:$field").and(hasText(value, substring = true)))
                .fetchSemanticsNodes(atLeastOneRootRequired = false).isNotEmpty()
        }
        compose.onNodeWithTag("overview:row:$field").assertTextContains(value).assertIsDisplayed()
    }
    private fun clickRow(field: String) {
        awaitTag("overview:list")
        compose.onNodeWithTag("overview:list").performScrollToNode(hasTestTag("overview:row:$field"))
        compose.onNodeWithTag("overview:row:$field").performClick()
    }
    private fun menu(label: String) {
        openActionBarOverflowOrOptionsMenu(context)
        onView(withText(label)).perform(click())
    }
    private fun cancellation(user: User, expected: String) {
        openLegacy(Mocks.successBuild().apply { canceledInfo = CanceledInfo("20161223T151154+0300", user) })
        row("CancelledBy", expected)
        row("CancellationTime", "23 Dec 16 15:11")
    }
    private fun trigger(kind: String, user: User?, field: String, expected: String) {
        openLegacy(Mocks.successBuild().apply { triggered = Triggered(kind, null, user) })
        row(field, expected)
    }
    private fun stopFailure(code: Int, message: Int) {
        doReturn(Single.error<Build>(HttpException(Response.error<Build>(code, "offline".toResponseBody())))).`when`(teamCityService).cancelBuild(anyString(), com.github.vase4kin.teamcityapp.helper.any())
        openCurrent(BuildComposeFixtures.loaded)
        menu(text(OverviewR.string.overview_stop))
        onView(withText(R.string.text_stop_button)).perform(click())
        onView(withText(message)).check(matches(isDisplayed()))
        row("Result", "Running current build")
    }
    private fun restartFailure(code: Int, message: Int) {
        doReturn(Single.error<Build>(HttpException(Response.error<Build>(code, "offline".toResponseBody())))).`when`(teamCityService).queueBuild(com.github.vase4kin.teamcityapp.helper.any())
        openCurrent(BuildComposeFixtures.finished)
        menu(text(OverviewR.string.overview_restart))
        onView(withText(R.string.text_restart_button)).perform(click())
        onView(withText(message)).check(matches(isDisplayed()))
        row("Result", "Current build succeeded")
    }
}
