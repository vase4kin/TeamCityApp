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

package com.github.vase4kin.teamcityapp.navigation

import android.app.Activity
import android.content.Intent
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.v2.createEmptyComposeRule
import androidx.test.core.app.ActivityScenario
import androidx.test.espresso.Espresso.pressBack
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.github.vase4kin.teamcityapp.TeamCityApplicationBase
import com.github.vase4kin.teamcityapp.api.TeamCityService
import com.github.vase4kin.teamcityapp.build_details.api.BuildCancelRequest
import com.github.vase4kin.teamcityapp.buildlist.filter.BuildListFilterImpl
import com.github.vase4kin.teamcityapp.di.modules.FakeTeamCityServiceImpl
import com.github.vase4kin.teamcityapp.di.modules.Mocks
import com.github.vase4kin.teamcityapp.helper.HiltApiTestRule
import com.github.vase4kin.teamcityapp.helper.RootDestinationLauncher
import com.github.vase4kin.teamcityapp.helper.TestUtils
import com.github.vase4kin.teamcityapp.helper.tapBuildDetailsSection
import dagger.hilt.android.testing.*
import java.io.File
import javax.inject.Inject
import okio.buffer
import org.junit.Assert.*
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito.*
import org.mockito.Spy
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.doSuspendableAnswer
import org.mockito.kotlin.verifyBlocking
import org.mockito.kotlin.whenever
import teamcityapp.features.artifacts.api.*
import teamcityapp.features.build_details.api.*
import teamcityapp.libraries.builds.*
import teamcityapp.libraries.data.builds.BuildLaunchMapper

@OptIn(ExperimentalTestApi::class)
@HiltAndroidTest
@RunWith(AndroidJUnit4::class)
class BuildDetailsNavigationTest {
    @get:Rule(order = 99)
    val phaseFourEvidence = com.github.vase4kin.teamcityapp.helper.PhaseFourEvidenceRule()

    @JvmField
    @Rule(order = 0)
    val hilt = HiltAndroidRule(this)

    @JvmField
    @Rule(order = 1)
    val mockito = org.mockito.junit.MockitoJUnit.rule().strictness(org.mockito.quality.Strictness.LENIENT)

    @JvmField
    @Rule(order = 2)
    val api = HiltApiTestRule(hilt) { service }

    @JvmField
    @Rule(order = 3)
    val compose = createEmptyComposeRule()

    @Spy private val service: TeamCityService = FakeTeamCityServiceImpl()

    @Inject lateinit var launcher: RootDestinationLauncher

    private val mapper = BuildLaunchMapper()

    @get:Rule(order = 4)
    val exportIntents = object : org.junit.rules.ExternalResource() {
        override fun before() {
            androidx.test.espresso.intent.Intents.init()
            androidx.test.espresso.intent.Intents.intending(androidx.test.espresso.intent.matcher.IntentMatchers.hasAction(Intent.ACTION_CHOOSER))
                .respondWith(android.app.Instrumentation.ActivityResult(Activity.RESULT_OK, null))
        }
        override fun after() {
            androidx.test.espresso.intent.Intents.release()
        }
    }
    private fun exportedFiles(): List<android.net.Uri> = androidx.test.espresso.intent.Intents.getIntents().mapNotNull { chooser ->
        if (chooser.action != Intent.ACTION_CHOOSER) return@mapNotNull null
        val send = androidx.core.content.IntentCompat.getParcelableExtra(chooser, Intent.EXTRA_INTENT, Intent::class.java) ?: return@mapNotNull null
        androidx.core.content.IntentCompat.getParcelableExtra(send, Intent.EXTRA_STREAM, android.net.Uri::class.java)
    }
    private val app get() = InstrumentationRegistry.getInstrumentation().targetContext.applicationContext as TeamCityApplicationBase

    @Before fun before() {
        TestUtils.disableOnboarding()
        val storage = app.appInjector.sharedUserStorage()
        storage.clearAll()
        storage.saveGuestUserAccountAndSetItAsActive(Mocks.URL, false)
    }
    private fun launch() = ActivityScenario.launch<MainActivity>(Intent(app, MainActivity::class.java))
    private fun awaitTag(tag: String) {
        compose.waitUntil(15_000) { compose.onAllNodesWithTag(tag).fetchSemanticsNodes().isNotEmpty() }
        compose.onNodeWithTag(tag).assertIsDisplayed()
    }
    private fun awaitText(text: String) {
        compose.waitUntil(15_000) { compose.onAllNodesWithText(text).fetchSemanticsNodes().isNotEmpty() }
    }
    private fun open(scenario: ActivityScenario<MainActivity>, build: BuildLaunchData) {
        awaitTag("navigation:row:project:id")
        scenario.onActivity { launcher.open(it, BuildDetailsDestination(build, "Fallback configuration")) }
        awaitTag("overview:list")
    }
    private fun tab(name: String) {
        compose.tapBuildDetailsSection(name)
    }

    @Test fun loadedSnapshotOwnsParametersAndSectionAvailabilityAcrossRecreation() {
        val incoming = mapper.toLaunchData(Mocks.failedBuild()).copy(number = "incoming", properties = BuildProperties(listOf(BuildProperty("initial.parameter", "initial-value"))), snapshotDependencies = BuildCollectionLink(null))
        val loaded = incoming.copy(number = "latest", changes = null, tests = null, artifacts = null, snapshotDependencies = null, properties = BuildProperties(listOf(BuildProperty("loaded.parameter", "loaded-value"))))
        doReturn(mapper.toLegacyBuild(loaded)).whenever(service) { build(incoming.href) }
        launch().use { scenario ->
            open(scenario, incoming)
            awaitText("#latest")
            tab("Parameters")
            awaitTag("properties:list")
            compose.onNodeWithText("loaded.parameter").assertIsDisplayed()
            compose.onNodeWithText("initial.parameter").assertDoesNotExist()
            scenario.recreate()
            awaitTag("properties:list")
            assertEquals(BuildDetailsSection.Parameters, (com.github.vase4kin.teamcityapp.helper.currentRootSnapshot().entries.last().destination as BuildDetailsSectionDestination).section)
            compose.onNodeWithText("loaded.parameter").assertIsDisplayed()
            tab("Overview")
            awaitTag("overview:list")
            listOf("Changes", "Tests", "Artifacts", "Snapshots").forEach {
                compose.onNodeWithTag("overview:section:$it").assertDoesNotExist()
            }
            scenario.onActivity { assertTrue(it.supportFragmentManager.fragments.isEmpty()) }
            pressBack()
            awaitTag("navigation:row:project:id")
        }
    }

    @Test fun branchSheetRestoresAndNavigatesUsingTheLoadedConfigurationAndExactBranch() {
        val incoming = mapper.toLaunchData(Mocks.successBuild())
        val loaded = incoming.copy(buildTypeId = "latest-configuration", branchName = "refs/heads/opaque:branch", configuration = incoming.configuration?.copy(name = "Latest configuration"))
        doReturn(mapper.toLegacyBuild(loaded)).whenever(service) { build(incoming.href) }
        launch().use { scenario ->
            open(scenario, incoming)
            compose.onNodeWithTag("overview:row:Branch").performClick()
            awaitTag("sheet:Branch")
            scenario.recreate()
            awaitTag("sheet:Branch")
            compose.onNodeWithTag("sheet:Branch").performClick()
            awaitTag("history:list")
            val locator = BuildListFilterImpl().apply { setBranch(loaded.branchName.orEmpty()) }.toLocator()
            compose.waitUntil(10_000) { runCatching { verifyBlocking(service) { listBuilds("latest-configuration", locator) } }.isSuccess }
            pressBack()
            awaitTag("overview:list")
            compose.onNodeWithTag("sheet:content").assertDoesNotExist()
            scenario.onActivity { assertTrue(it.supportFragmentManager.fragments.isEmpty()) }
        }
    }

    @Test fun artifactFolderAndDownloadSheetKeepTheRightOwnerThroughRecreation() {
        val build = mapper.toLaunchData(Mocks.successBuild())
        val folderHref = "/guestAuth/app/rest/builds/id:92912/artifacts/children/TCity.apk!/res"
        val fileHref = "/guestAuth/app/rest/builds/id:92912/artifacts/metadata/TCity.apk!/AndroidManifest.xml"
        doReturn(Mocks.artifacts()).whenever(service) { listArtifacts(folderHref, "browseArchives:true") }
        launch().use { scenario ->
            open(scenario, build)
            tab("Artifacts")
            awaitTag("artifacts:list")
            var originVisit: String? = null
            scenario.onActivity { originVisit = it.navigationSnapshot?.entries?.last()?.id }
            compose.onNodeWithTag("artifacts:row:/guestAuth/app/rest/builds/id:92912/artifacts/metadata/TCity.apk!/res").performClick()
            awaitTag("artifacts:list")
            awaitText("res")
            scenario.recreate()
            awaitTag("artifacts:list")
            compose.onNodeWithTag("artifacts:row:$fileHref").performClick()
            awaitTag("sheet:ArtifactDownload")
            scenario.recreate()
            awaitTag("sheet:ArtifactDownload")
            compose.onNodeWithTag("sheet:ArtifactDownload").performClick()
            compose.waitUntil(15_000) { exportedFiles().size == 1 }
            val exported = exportedFiles().single()
            assertEquals("text", app.contentResolver.openInputStream(exported)!!.bufferedReader().use { it.readText() })
            scenario.recreate()
            awaitTag("artifacts:list")
            assertEquals(1, exportedFiles().size)
            pressBack()
            awaitTag("artifacts:list")
            scenario.onActivity {
                assertEquals(originVisit, it.navigationSnapshot?.entries?.last()?.id)
                assertTrue(it.supportFragmentManager.fragments.isEmpty())
            }
        }
    }

    @Test fun stoppingBuildSurvivesConfirmationRecreationAndReplacesTheOldTabOwners() {
        val running = mapper.toLaunchData(Mocks.runningBuild())
        val cancelled = running.copy(state = "finished", number = "cancelled", artifacts = BuildCollectionLink("/guestAuth/app/rest/builds/id:92912/artifacts/children/"))
        var current = running
        doSuspendableAnswer { mapper.toLegacyBuild(current) }.whenever(service) { build(running.href) }
        doSuspendableAnswer {
            current = cancelled
            mapper.toLegacyBuild(cancelled)
        }.whenever(service) { cancelBuild(running.href, BuildCancelRequest(true)) }
        launch().use { scenario ->
            open(scenario, running)
            compose.onNodeWithTag("details:action:Cancel").performClick()
            awaitTag("details:confirm")
            scenario.recreate()
            awaitTag("details:confirm")
            compose.onNodeWithTag("details:readd").performClick()
            compose.onNodeWithTag("details:confirm").performClick()
            awaitText("#cancelled")
            compose.onNodeWithTag("overview:screen").assertIsDisplayed()
            tab("Artifacts")
            awaitTag("artifacts:list")
            verifyBlocking(service, times(1)) { cancelBuild(running.href, BuildCancelRequest(true)) }
            scenario.onActivity { assertTrue(it.supportFragmentManager.fragments.isEmpty()) }
        }
    }

    @Test fun restartOpensQueuedVisitAndBackRestoresTheFinishedBuild() {
        val build = mapper.toLaunchData(Mocks.successBuild())
        launch().use { scenario ->
            open(scenario, build)
            compose.onNodeWithTag("details:action:Restart").performClick()
            awaitTag("details:confirm")
            scenario.recreate()
            awaitTag("details:confirm")
            compose.onNodeWithTag("details:confirm").performClick()
            awaitText("Build is restarted successfully")
            compose.onNodeWithText("Show").performClick()
            awaitTag("overview:row:WaitReason")
            compose.onNodeWithTag("overview:log").assertDoesNotExist()
            compose.onNodeWithTag("overview:section:Artifacts").assertDoesNotExist()
            scenario.recreate()
            awaitTag("overview:row:WaitReason")
            verifyBlocking(service, times(1)) { queueBuild(org.mockito.kotlin.any()) }
            pressBack()
            compose.waitUntil(10_000) {
                (com.github.vase4kin.teamcityapp.helper.currentRootSnapshot().entries.last().destination as? BuildDetailsDestination)?.build?.id == build.id
            }
            awaitTag("overview:list")
            pressBack()
            awaitTag("navigation:row:project:id")
        }
    }

    @Test fun repeatedBuildVisitsHaveIndependentSelectedTabsAndBackRestoresTheFirst() {
        val build = mapper.toLaunchData(Mocks.failedBuild())
        launch().use { scenario ->
            open(scenario, build)
            tab("Tests")
            awaitTag("tests:list")
            scenario.onActivity { launcher.open(it, BuildDetailsDestination(build)) }
            awaitTag("overview:list")
            compose.onNodeWithTag("overview:screen").assertIsDisplayed()
            scenario.recreate()
            awaitTag("overview:list")
            pressBack()
            awaitTag("tests:list")
            assertEquals(BuildDetailsSection.Tests, (com.github.vase4kin.teamcityapp.helper.currentRootSnapshot().entries.last().destination as BuildDetailsSectionDestination).section)
            pressBack()
            awaitTag("overview:list")
            pressBack()
            awaitTag("navigation:row:project:id")
        }
    }

    @Test fun hubCountsRetainTheirResultsAndRetryOnlyTheFailedSection() {
        val build = mapper.toLaunchData(Mocks.failedBuild())
        val testsHref = "${build.tests?.href},count:2147483647&fields=count,passed,failed,ignored"
        val changesHref = "${build.changes?.href},count:2147483647&fields=count"
        var failed = true
        doSuspendableAnswer { if (failed) throw IllegalStateException("offline count") else com.github.vase4kin.teamcityapp.tests.api.TestOccurrences(16) }.whenever(service) { listTestOccurrences(testsHref) }
        launch().use { scenario ->
            open(scenario, build)
            awaitTag("overview:retry-count:Tests")
            verifyBlocking(service, times(1)) { listTestOccurrences(testsHref) }
            verifyBlocking(service, times(1)) { listChanges(changesHref) }
            scenario.recreate()
            awaitTag("overview:retry-count:Tests")
            failed = false
            compose.onNodeWithTag("overview:retry-count:Tests").performClick()
            compose.waitUntil(10_000) { compose.onAllNodesWithTag("overview:retry-count:Tests").fetchSemanticsNodes(atLeastOneRootRequired = false).isEmpty() }
            compose.onNodeWithTag("overview:section:Tests").assertTextContains("16")
            scenario.recreate()
            compose.onNodeWithTag("overview:section:Tests").assertTextContains("16")
            verifyBlocking(service, times(2)) { listTestOccurrences(testsHref) }
            verifyBlocking(service, times(1)) { listChanges(changesHref) }
        }
    }

    @Test fun eachActionTipSurvivesRecreationAndPersistsItsOwnPreferenceOnDismissal() {
        val rootState = dagger.hilt.EntryPoints.get(app, com.github.vase4kin.teamcityapp.helper.SharedRootTestState::class.java)
        val accounts = rootState.accounts() as teamcityapp.libraries.accounts.AccountPreferencesStorage
        val active = app.appInjector.sharedUserStorage().activeUser
        val key = teamcityapp.libraries.accounts.AccountIdentity(active.teamcityUrl, active.userName, active.isGuestUser, active.isSslDisabled).key
        fun shown(prompt: BuildDetailsPrompt): Boolean = kotlinx.coroutines.runBlocking {
            "details.${prompt.name}" in accounts.preferences(key).shownPrompts
        }
        val cases = listOf(
            Triple(Mocks.runningBuild(), BuildDetailsPrompt.Stop, "Stop this running build. You can also add it back to the queue."),
            Triple(Mocks.queuedBuild1(), BuildDetailsPrompt.Remove, "Remove this build from the queue before it starts."),
            Triple(Mocks.successBuild(), BuildDetailsPrompt.Restart, "Run this build again with the same parameters."),
            Triple(Mocks.successBuild(), BuildDetailsPrompt.Share, "Share a link to this build with others."),
            Triple(Mocks.successBuild(), BuildDetailsPrompt.Browser, "Open this build’s page in a browser.")
        )
        cases.forEach { (legacy, prompt, copy) ->
            // Each activation reads its durable account-scoped preferences once.
            // Host recreation below must retain that live preference state.
            kotlinx.coroutines.runBlocking {
                rootState.navigation().write("")
                accounts.updatePreferences(key) {
                    it.copy(shownPrompts = it.shownPrompts + BuildDetailsPrompt.entries.map { "details.${it.name}" } - "details.${prompt.name}")
                }
            }
            launch().use { scenario ->
                val build = mapper.toLaunchData(legacy)
                awaitTag("navigation:row:project:id")
                scenario.onActivity { launcher.open(it, BuildDetailsDestination(build)) }
                awaitTag("details:dismiss-prompt")
                compose.onNodeWithText(copy).assertIsDisplayed()
                assertFalse(shown(prompt))
                scenario.recreate()
                awaitTag("details:dismiss-prompt")
                compose.onNodeWithText(copy).assertIsDisplayed()
                compose.onNodeWithTag("details:dismiss-prompt").performClick()
                compose.waitUntil(10_000) { shown(prompt) }
                awaitTag("overview:list")
                pressBack()
                awaitTag("navigation:row:project:id")
                scenario.onActivity { launcher.open(it, BuildDetailsDestination(build)) }
                awaitTag("overview:list")
                compose.onNodeWithTag("details:dismiss-prompt").assertDoesNotExist()
                pressBack()
                awaitTag("navigation:row:project:id")
            }
        }
    }

    @Test fun accountReplacementClosesBlockedArtifactReadAndPreservesTheCompletedFile() {
        val entered = java.util.concurrent.CountDownLatch(1)
        val closed = java.util.concurrent.CountDownLatch(1)
        val source = object : okio.Source {
            override fun timeout() = okio.Timeout.NONE
            override fun read(sink: okio.Buffer, byteCount: Long): Long {
                entered.countDown()
                check(closed.await(15, java.util.concurrent.TimeUnit.SECONDS)) { "Download read was not cancelled" }
                return -1L
            }
            override fun close() {
                closed.countDown()
            }
        }
        val body = object : okhttp3.ResponseBody() {
            private val buffered = source.buffer()
            override fun contentType(): okhttp3.MediaType? = null
            override fun contentLength() = -1L
            override fun source() = buffered
        }
        doReturn(body).whenever(service) { downloadFile(org.mockito.kotlin.any()) }
        val directory = app.getExternalFilesDir(android.os.Environment.DIRECTORY_DOWNLOADS) ?: app.filesDir
        directory.mkdirs()
        val completed = File(directory, "AndroidManifest.xml").apply { writeText("completed download") }
        try {
            launch().use { scenario ->
                open(scenario, mapper.toLaunchData(Mocks.successBuild()))
                tab("Artifacts")
                awaitTag("artifacts:list")
                compose.onNodeWithTag("artifacts:row:/guestAuth/app/rest/builds/id:92912/artifacts/metadata/TCity.apk!/AndroidManifest.xml").performClick()
                awaitTag("sheet:ArtifactDownload")
                compose.onNodeWithTag("sheet:ArtifactDownload").performClick()
                compose.waitUntil(10_000) { entered.count == 0L }
                scenario.recreate()
                assertEquals(1L, closed.count)
                scenario.onActivity {
                    it.startActivity(
                        Intent().setClassName(it, IncomingDestinationResolver.HOME_COMPONENT).apply {
                            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP
                            putExtra(com.github.vase4kin.teamcityapp.base.extractor.BundleExtractorValues.IS_REQUIRED_TO_RELOAD, true)
                        }
                    )
                }
                awaitTag("navigation:row:project:id")
                compose.waitUntil(10_000) { closed.count == 0L }
                assertEquals("completed download", completed.readText())
                assertTrue(exportedFiles().isEmpty())
                verifyBlocking(service, times(1)) { downloadFile(org.mockito.kotlin.any()) }
            }
        } finally {
            closed.countDown()
        }
    }
}
