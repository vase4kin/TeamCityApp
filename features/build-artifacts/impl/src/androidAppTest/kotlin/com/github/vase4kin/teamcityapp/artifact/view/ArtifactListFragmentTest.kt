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

package com.github.vase4kin.teamcityapp.artifact.view

import android.app.Activity
import android.app.Instrumentation.ActivityResult
import android.content.ComponentName
import android.content.Intent
import android.text.format.Formatter
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
import com.github.vase4kin.teamcityapp.TeamCityApplicationBase
import com.github.vase4kin.teamcityapp.api.TeamCityService
import com.github.vase4kin.teamcityapp.artifact.api.File
import com.github.vase4kin.teamcityapp.artifact.api.Files
import com.github.vase4kin.teamcityapp.base.extractor.BundleExtractorValues
import com.github.vase4kin.teamcityapp.buildlist.api.Build
import com.github.vase4kin.teamcityapp.di.modules.FakeTeamCityServiceImpl
import com.github.vase4kin.teamcityapp.di.modules.Mocks
import com.github.vase4kin.teamcityapp.di.modules.PendingFixture
import com.github.vase4kin.teamcityapp.helper.BuildComposeFixtures
import com.github.vase4kin.teamcityapp.helper.CustomActivityTestRule
import com.github.vase4kin.teamcityapp.helper.DeviceEvidence
import com.github.vase4kin.teamcityapp.helper.HiltApiTestRule
import com.github.vase4kin.teamcityapp.helper.TestUtils
import com.github.vase4kin.teamcityapp.helper.awaitDisplayedText
import com.github.vase4kin.teamcityapp.helper.currentRootSnapshot
import com.github.vase4kin.teamcityapp.helper.destinationIntent
import com.github.vase4kin.teamcityapp.helper.hasDestination
import com.github.vase4kin.teamcityapp.helper.tapBuildDetailsSection
import com.github.vase4kin.teamcityapp.navigation.IncomingDestinationResolver
import com.github.vase4kin.teamcityapp.navigation.LegacyArtifactsEntry
import com.github.vase4kin.teamcityapp.navigation.MainActivity
import com.google.gson.Gson
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import java.util.concurrent.atomic.AtomicInteger
import okhttp3.ResponseBody
import okhttp3.ResponseBody.Companion.toResponseBody
import okio.buffer
import org.hamcrest.core.AllOf.allOf
import org.junit.Assert.*
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.ArgumentMatchers.anyString
import org.mockito.Mockito.*
import org.mockito.Spy
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.doSuspendableAnswer
import org.mockito.kotlin.verifyBlocking
import org.mockito.kotlin.whenever
import teamcityapp.features.artifacts.api.navigation.ArtifactsDestination
import teamcityapp.features.artifacts.impl.R as ArtifactsR
import teamcityapp.features.bottom_sheet.impl.R as SheetR
import teamcityapp.features.build_history.api.navigation.BuildHistoryDestination
import teamcityapp.features.navigation.api.navigation.ProjectDestination

@HiltAndroidTest
@RunWith(AndroidJUnit4::class)
class ArtifactListFragmentTest {
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
    val activityRule = CustomActivityTestRule(MainActivity::class.java, IncomingDestinationResolver.BUILD_DETAILS_COMPONENT)

    @JvmField
    @Rule(order = 4)
    val compose = createEmptyComposeRule()

    @Spy private val teamCityService: TeamCityService = FakeTeamCityServiceImpl()
    private val context get() = InstrumentationRegistry.getInstrumentation().targetContext

    @Before fun setUp() {
        androidx.test.espresso.intent.Intents.init()
        TestUtils.disableOnboarding()
        val app = context.applicationContext as TeamCityApplicationBase
        app.appInjector.sharedUserStorage().clearAll()
        app.appInjector.sharedUserStorage().saveGuestUserAccountAndSetItAsActive(Mocks.URL, false)
    }
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
    private fun tab(title: String) = compose.tapBuildDetailsSection(title)
    private fun sheet(label: String) {
        awaitText(label)
        compose.onNodeWithText(label).performClick()
        compose.waitUntil(10_000) { compose.onAllNodesWithTag("sheet:content").fetchSemanticsNodes(atLeastOneRootRequired = false).isEmpty() }
    }

    @org.junit.After fun releaseIntents() {
        androidx.test.espresso.intent.Intents.release()
    }

    @Test fun filenamesAndPlatformFormattedSizesAreVisible() {
        openArtifacts(Mocks.artifacts())
        awaitText("res")
        awaitText("AndroidManifest.xml")
        awaitText(Formatter.formatFileSize(context, 7768L))
        awaitText("index.html")
        awaitText(Formatter.formatFileSize(context, 697840L))
        DeviceEvidence.capture("android_artifacts")
    }

    @Test fun emptyArtifactsHasExplicitEmptyState() {
        openArtifacts(Files(emptyList()))
        awaitText(text(ArtifactsR.string.artifacts_empty))
    }

    @Test fun initialFailureCanRetry() {
        doSuspendableAnswer { throw RuntimeException("offline") }.whenever(teamCityService) { listArtifacts(anyString(), anyString()) }
        start()
        awaitText(text(teamcityapp.libraries.theme.R.string.error_load_message))
        doReturn(Mocks.artifacts()).whenever(teamCityService) { listArtifacts(anyString(), anyString()) }
        val retry = compose.onAllNodesWithText(text(teamcityapp.libraries.theme.R.string.action_retry))
        val visibleRetry = retry.fetchSemanticsNodes().indices.single { index -> runCatching { retry[index].assertIsDisplayed() }.isSuccess }
        retry[visibleRetry].performClick()
        awaitText("AndroidManifest.xml")
    }

    @Test fun folderClickOpensInstalledAliasWithFullBuildAndExactChildrenUrl() {
        folders()
        clickFile("/metadata/res")
        assertRootDestination(ArtifactsDestination(BuildComposeFixtures.finished, "/children/res", "res"))
        awaitText("Nested file.txt")
        DeviceEvidence.capture("android_artifact_folder")
    }

    @Test fun folderLongClickOpenActionUsesSameNavigationPayload() {
        folders()
        compose.onNodeWithTag("artifacts:row:/metadata/res").performTouchInput { longClick() }
        sheet(text(SheetR.string.artifact_open))
        assertRootDestination(ArtifactsDestination(BuildComposeFixtures.finished, "/children/res", "res"))
        awaitText("Nested file.txt")
    }

    @Test fun archiveOffersDownloadAndOpenWithOpaqueArchiveChildren() {
        val archive = Gson().fromJson("""{"name":"bundle.zip","href":"/metadata/bundle.zip","size":2048,"content":{"href":"/content/bundle.zip"},"children":{"href":"/children/bundle.zip!"}}""", File::class.java)
        doReturn(Files(listOf(archive))).whenever(teamCityService) { listArtifacts(anyString(), anyString()) }
        doReturn(Files(listOf(file("Inner file.txt", "/metadata/inner")))).whenever(teamCityService) { listArtifacts("/children/bundle.zip!", "browseArchives:true") }
        start()
        clickFile("/metadata/bundle.zip")
        awaitText(text(SheetR.string.artifact_download))
        DeviceEvidence.capture("android_archive_actions")
        sheet(text(SheetR.string.artifact_open))
        assertRootDestination(ArtifactsDestination(BuildComposeFixtures.finished, "/children/bundle.zip!", "bundle.zip!"))
        awaitText("Inner file.txt")
        DeviceEvidence.capture("android_archive_folder")
    }

    @Test fun downloadedFileOpensWithProviderUriAndGrants() {
        intending(hasAction(Intent.ACTION_VIEW)).respondWith(ActivityResult(Activity.RESULT_OK, null))
        doReturn("downloaded bytes".toResponseBody()).whenever(teamCityService) { downloadFile(anyString()) }
        openArtifacts(Files(listOf(file("teamcity.unknown", "/metadata/download"))))
        download("/metadata/download")
        compose.waitUntil(10_000) { androidx.test.espresso.intent.Intents.getIntents().any { it.action == Intent.ACTION_VIEW } }
        intended(allOf(hasAction(Intent.ACTION_VIEW), hasType("*/*")))
        val opened = androidx.test.espresso.intent.Intents.getIntents().last { it.action == Intent.ACTION_VIEW }
        assertEquals("content", opened.data!!.scheme)
        assertEquals(context.packageName + ".provider", opened.data!!.authority)
        assertTrue(opened.flags and Intent.FLAG_GRANT_READ_URI_PERMISSION != 0)
        assertTrue(opened.flags and Intent.FLAG_GRANT_WRITE_URI_PERMISSION != 0)
        val saved = java.io.File(context.getExternalFilesDir(android.os.Environment.DIRECTORY_DOWNLOADS) ?: context.filesDir, "teamcity.unknown")
        assertEquals("downloaded bytes", saved.readText())
        saved.delete()
    }

    @Test fun htmlBrowserUsesFullBuildIdConfigurationAndMetadataPath() {
        intending(hasAction(Intent.ACTION_VIEW)).respondWith(ActivityResult(Activity.RESULT_OK, null))
        val html = file("index.html", "/guestAuth/app/rest/builds/id:42/artifacts/metadata/archive.zip!/index.html")
        openArtifacts(Files(listOf(html)))
        clickFile(html.href)
        sheet(text(SheetR.string.artifact_open_in_browser))
        intended(allOf(hasAction(Intent.ACTION_VIEW), hasData(Mocks.URL + "/repository/download/current-config/42:id/archive.zip!/index.html?guest=1")))
    }

    @Test fun downloadFailureCardOffersRetryThatCanSucceed() {
        intending(hasAction(Intent.ACTION_VIEW)).respondWith(ActivityResult(Activity.RESULT_OK, null))
        doSuspendableAnswer { throw RuntimeException("download offline") }.whenever(teamCityService) { downloadFile(anyString()) }
        openArtifacts(Files(listOf(file("retry.unknown", "/metadata/retry"))))
        download("/metadata/retry")
        awaitText(text(ArtifactsR.string.artifacts_download_failed))
        compose.awaitDisplayedText(InstrumentationRegistry.getInstrumentation().targetContext.getString(ArtifactsR.string.artifacts_download_failed))
        DeviceEvidence.capture("android_download_failure")
        doReturn("retried".toResponseBody()).whenever(teamCityService) { downloadFile(anyString()) }
        compose.onNodeWithText(text(ArtifactsR.string.artifacts_retry_download)).performClick()
        compose.waitUntil(10_000) { androidx.test.espresso.intent.Intents.getIntents().any { it.action == Intent.ACTION_VIEW } }
        compose.waitUntil(10_000) {
            compose.onAllNodesWithText(text(ArtifactsR.string.artifacts_download_failed))
                .fetchSemanticsNodes(atLeastOneRootRequired = false).isEmpty()
        }
    }

    @Test fun downloadReportsBytesAndCancellationClosesABlockedFileRead() {
        val closed = java.util.concurrent.CountDownLatch(1)
        val source = object : okio.Source {
            var first = true
            override fun read(sink: okio.Buffer, byteCount: Long): Long {
                if (first) {
                    first = false
                    sink.write(ByteArray(8192))
                    return 8192
                }
                check(closed.await(10, java.util.concurrent.TimeUnit.SECONDS))
                throw java.io.IOException("closed")
            }
            override fun close() {
                closed.countDown()
            }
            override fun timeout() = okio.Timeout.NONE
        }
        val body = object : ResponseBody() {
            val bytes = source.buffer()
            override fun contentType(): okhttp3.MediaType? = null
            override fun contentLength(): Long = 16384
            override fun source() = bytes
        }
        doReturn(body).whenever(teamCityService) { downloadFile(anyString()) }
        openArtifacts(Files(listOf(File("progress.txt", 16384L, File.Content("/content/progress.txt"), "/metadata/progress"))))
        download("/metadata/progress")
        awaitText("8192 bytes received")
        DeviceEvidence.capture("android_download_byte_progress")
        compose.onNodeWithText(text(ArtifactsR.string.artifacts_cancel)).performClick()
        compose.waitUntil(10_000) { closed.count == 0L }
        compose.onNodeWithTag("artifacts:download-card").assertDoesNotExist()
        val directory = context.getExternalFilesDir(android.os.Environment.DIRECTORY_DOWNLOADS) ?: context.filesDir
        compose.waitUntil(10_000) { directory.listFiles().orEmpty().none { it.name.endsWith(".part") } }
        DeviceEvidence.capture("android_download_cancelled")
    }

    @Test fun cancelDownloadDisposesTheNetworkRequestWithoutFailureNotification() {
        val pending = PendingFixture<ResponseBody>()
        val canceled = AtomicInteger()
        doSuspendableAnswer {
            try {
                pending.await()
            } catch (cancelled: kotlinx.coroutines.CancellationException) {
                canceled.incrementAndGet()
                throw cancelled
            }
        }.whenever(teamCityService) { downloadFile(anyString()) }
        openArtifacts(Files(listOf(file("cancel.unknown", "/metadata/cancel"))))
        download("/metadata/cancel")
        awaitText(text(ArtifactsR.string.artifacts_downloading))
        compose.waitUntil(10_000) { pending.isAwaiting }
        compose.onNodeWithText(text(ArtifactsR.string.artifacts_cancel)).performClick()
        compose.waitUntil(10_000) { canceled.get() == 1 }
        assertFalse(pending.isAwaiting)
        compose.onNodeWithText(text(ArtifactsR.string.artifacts_downloading)).assertDoesNotExist()
        compose.onNodeWithText(text(ArtifactsR.string.artifacts_download_failed)).assertDoesNotExist()
        compose.onAllNodesWithText(InstrumentationRegistry.getInstrumentation().targetContext.getString(ArtifactsR.string.artifacts_download_failed)).assertCountEquals(0)
    }

    @Test fun leavingArtifactsCancelsPendingDownloadAndReturningDoesNotResumeIt() {
        val pending = PendingFixture<ResponseBody>()
        doSuspendableAnswer { pending.await() }.whenever(teamCityService) { downloadFile(anyString()) }
        openArtifacts(Files(listOf(file("leave.unknown", "/metadata/leave"))))
        download("/metadata/leave")
        awaitText(text(ArtifactsR.string.artifacts_downloading))
        compose.waitUntil(10_000) { pending.isAwaiting }
        androidx.test.espresso.Espresso.pressBack()
        compose.waitUntil(10_000) { !pending.isAwaiting }
        tab("Artifacts")
        awaitText("leave.unknown")
        compose.onNodeWithTag("artifacts:download-card").assertDoesNotExist()
        compose.onNodeWithText(text(ArtifactsR.string.artifacts_download_failed)).assertDoesNotExist()
    }

    @Test fun backgroundingCancelsPendingDownloadWithoutAnErrorOrAutomaticResume() {
        val pending = PendingFixture<ResponseBody>()
        doSuspendableAnswer { pending.await() }.whenever(teamCityService) { downloadFile(anyString()) }
        doReturn(BuildComposeFixtures.legacy(BuildComposeFixtures.finished)).whenever(teamCityService) { build(anyString()) }
        doReturn(Files(listOf(file("background.unknown", "/metadata/background")))).whenever(teamCityService) { listArtifacts(anyString(), anyString()) }
        ActivityScenario.launch<MainActivity>(BuildComposeFixtures.intent(BuildComposeFixtures.finished)).use { scenario ->
            awaitTag("overview:list")
            tab("Artifacts")
            download("/metadata/background")
            awaitText(text(ArtifactsR.string.artifacts_downloading))
            compose.waitUntil(10_000) { pending.isAwaiting }
            scenario.moveToState(androidx.lifecycle.Lifecycle.State.CREATED)
            compose.waitUntil(10_000) { !pending.isAwaiting }
            scenario.moveToState(androidx.lifecycle.Lifecycle.State.RESUMED)
            awaitText("background.unknown")
            compose.onNodeWithTag("artifacts:download-card").assertDoesNotExist()
            compose.onNodeWithText(text(ArtifactsR.string.artifacts_download_failed)).assertDoesNotExist()
        }
    }

    @Test fun restoredArtifactsRetainsPagesAndPendingDownloadWithoutDuplicateWork() {
        val pending = PendingFixture<ResponseBody>()
        val lists = AtomicInteger()
        val downloads = AtomicInteger()
        doReturn(BuildComposeFixtures.legacy(BuildComposeFixtures.finished)).whenever(teamCityService) { build(anyString()) }
        doSuspendableAnswer {
            lists.incrementAndGet()
            Files(listOf(file("restore.unknown", "/metadata/restore")))
        }.whenever(teamCityService) { listArtifacts(anyString(), anyString()) }
        doSuspendableAnswer {
            downloads.incrementAndGet()
            pending.await()
        }.whenever(teamCityService) { downloadFile(anyString()) }
        ActivityScenario.launch<MainActivity>(BuildComposeFixtures.intent(BuildComposeFixtures.finished)).use { scenario ->
            awaitTag("overview:list")
            tab("Artifacts")
            download("/metadata/restore")
            awaitText(text(ArtifactsR.string.artifacts_downloading))
            val completedLists = lists.get()
            scenario.recreate()
            awaitText(text(ArtifactsR.string.artifacts_downloading))
            assertEquals(completedLists, lists.get())
            assertEquals(1, downloads.get())
            assertTrue(pending.isAwaiting)
            compose.onNodeWithText(text(ArtifactsR.string.artifacts_cancel)).performClick()
            compose.waitUntil(10_000) { !pending.isAwaiting }
            awaitText("restore.unknown")
            // The retained destination restores its loaded toolbar after recreation.
            compose.awaitDisplayedText("#latest-number · Current configuration")
        }
    }

    @Test fun apkDownloadUsesAppPrivateStorageWithoutRequestingInstallPermission() {
        val pending = PendingFixture<ResponseBody>()
        doSuspendableAnswer { pending.await() }.whenever(teamCityService) { downloadFile(anyString()) }
        openArtifacts(Files(listOf(file("app.apk", "/metadata/app.apk"))))
        download("/metadata/app.apk")
        awaitText(text(ArtifactsR.string.artifacts_downloading))
        compose.waitUntil(10_000) { pending.isAwaiting }
        compose.onNodeWithText(text(ArtifactsR.string.artifacts_permission_denied)).assertDoesNotExist()
        assertFalse(androidx.test.espresso.intent.Intents.getIntents().any { it.action == android.provider.Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES })
        compose.onNodeWithText(text(ArtifactsR.string.artifacts_cancel)).performClick()
        compose.waitUntil(10_000) { !pending.isAwaiting }
    }

    @Test fun hiddenBuildDetailsArtifactsCannotConsumeFolderActivityDownloadEvents() {
        intending(hasAction(Intent.ACTION_VIEW)).respondWith(ActivityResult(Activity.RESULT_OK, null))
        val downloads = AtomicInteger()
        doSuspendableAnswer {
            downloads.incrementAndGet()
            "one owner".toResponseBody()
        }.whenever(teamCityService) { downloadFile(anyString()) }
        folders()
        clickFile("/metadata/res")
        awaitText("Nested file.txt")
        download("/metadata/nested")
        compose.waitUntil(10_000) { androidx.test.espresso.intent.Intents.getIntents().any { it.action == Intent.ACTION_VIEW } }
        assertEquals(1, downloads.get())
        assertEquals(1, androidx.test.espresso.intent.Intents.getIntents().count { it.action == Intent.ACTION_VIEW })
    }

    @Test fun installedArtifactActivityAcceptsLegacyExtrasAndRestoresItsFolder() {
        val lists = AtomicInteger()
        doSuspendableAnswer {
            lists.incrementAndGet()
            Files(listOf(file("Alias file.txt", "/metadata/alias")))
        }.whenever(teamCityService) { listArtifacts(anyString(), anyString()) }
        listOf(LegacyArtifactsEntry.LEGACY_ACTIVITY, "teamcityapp.features.artifacts.impl.ArtifactsActivity").forEach { component ->
            val alias = Intent().setComponent(ComponentName(context.packageName, component))
                .putExtra(LegacyArtifactsEntry.BUILD, BuildComposeFixtures.legacy(BuildComposeFixtures.incoming))
                .putExtra(LegacyArtifactsEntry.NAME, "Folder title").putExtra(LegacyArtifactsEntry.URL, "/children/alias")
            ActivityScenario.launch<com.github.vase4kin.teamcityapp.navigation.MainActivity>(alias).use { scenario ->
                awaitText("Folder title")
                awaitText("Alias file.txt")
                scenario.onActivity {
                    assertEquals(ArtifactsDestination(BuildComposeFixtures.incoming, "/children/alias", "Folder title"), it.navigationSnapshot!!.entries.single().destination)
                    org.junit.Assert.assertFalse(it.intent.hasExtra(LegacyArtifactsEntry.BUILD))
                }
                val completedLists = lists.get()
                scenario.recreate()
                awaitText("Folder title")
                awaitText("Alias file.txt")
                assertEquals(completedLists, lists.get())
            }
        }
        verifyBlocking(teamCityService, times(2)) { listArtifacts("/children/alias", "browseArchives:true") }
    }
    private fun file(name: String, href: String) = File(name, 1024L, File.Content("/content/" + name), href)
    private fun start() {
        doReturn(BuildComposeFixtures.legacy(BuildComposeFixtures.finished)).whenever(teamCityService) { build(anyString()) }
        activityRule.launchActivity(BuildComposeFixtures.intent(BuildComposeFixtures.finished))
        tab("Artifacts")
    }
    private fun openArtifacts(files: Files) {
        doReturn(files).whenever(teamCityService) { listArtifacts(anyString(), anyString()) }
        start()
    }
    private fun folders() {
        val folder = File("res", File.Children("/children/res"), "/metadata/res")
        doReturn(Files(listOf(folder))).whenever(teamCityService) { listArtifacts(anyString(), anyString()) }
        doReturn(Files(listOf(file("Nested file.txt", "/metadata/nested")))).whenever(teamCityService) { listArtifacts("/children/res", "browseArchives:true") }
        start()
        awaitText("res")
    }
    private fun clickFile(href: String) {
        awaitTag("artifacts:row:$href")
        compose.onNodeWithTag("artifacts:row:$href").performClick()
    }
    private fun download(href: String) {
        clickFile(href)
        sheet(text(SheetR.string.artifact_download))
    }
    private fun assertRootDestination(expected: androidx.navigation3.runtime.NavKey) {
        compose.waitUntil(10_000) { currentRootSnapshot().entries.last().destination == expected }
        org.junit.Assert.assertEquals(expected, currentRootSnapshot().entries.last().destination)
    }
}
