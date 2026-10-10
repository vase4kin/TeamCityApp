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
import com.github.vase4kin.teamcityapp.R
import com.github.vase4kin.teamcityapp.TeamCityApplicationBase
import com.github.vase4kin.teamcityapp.api.TeamCityService
import com.github.vase4kin.teamcityapp.artifact.api.File
import com.github.vase4kin.teamcityapp.artifact.api.Files
import com.github.vase4kin.teamcityapp.base.extractor.BundleExtractorValues
import com.github.vase4kin.teamcityapp.build_details.view.BuildDetailsActivity
import com.github.vase4kin.teamcityapp.buildlist.api.Build
import com.github.vase4kin.teamcityapp.dagger.modules.FakeTeamCityServiceImpl
import com.github.vase4kin.teamcityapp.dagger.modules.Mocks
import com.github.vase4kin.teamcityapp.helper.BuildComposeFixtures
import com.github.vase4kin.teamcityapp.helper.CustomActivityTestRule
import com.github.vase4kin.teamcityapp.helper.HiltApiTestRule
import com.github.vase4kin.teamcityapp.helper.TestUtils
import com.github.vase4kin.teamcityapp.helper.tapNativePagerTab
import com.google.gson.Gson
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import io.reactivex.Single
import io.reactivex.subjects.SingleSubject
import java.util.concurrent.atomic.AtomicInteger
import okhttp3.ResponseBody
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
import teamcityapp.features.artifacts.api.ArtifactsNavigation
import teamcityapp.features.artifacts.impl.R as ArtifactsR
import teamcityapp.features.bottom_sheet.impl.R as SheetR

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
    private fun tab(title: String) = compose.tapNativePagerTab(title)
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
    }

    @Test fun emptyArtifactsHasExplicitEmptyState() {
        openArtifacts(Files(emptyList()))
        awaitText(text(ArtifactsR.string.artifacts_empty))
    }

    @Test fun initialFailureCanRetry() {
        `when`(teamCityService.listArtifacts(anyString(), anyString())).thenReturn(Single.error(RuntimeException("offline")))
        start()
        awaitText(text(R.string.error_view_error_text))
        `when`(teamCityService.listArtifacts(anyString(), anyString())).thenReturn(Single.just(Mocks.artifacts()))
        val retry = compose.onAllNodesWithText(text(teamcityapp.libraries.theme.R.string.action_retry))
        val visibleRetry = retry.fetchSemanticsNodes().indices.single { index -> runCatching { retry[index].assertIsDisplayed() }.isSuccess }
        retry[visibleRetry].performClick()
        awaitText("AndroidManifest.xml")
    }

    @Test fun folderClickOpensInstalledAliasWithFullBuildAndExactChildrenUrl() {
        folders()
        clickFile("/metadata/res")
        intended(allOf(hasComponent(ArtifactsNavigation.LEGACY_ACTIVITY), hasExtra(ArtifactsNavigation.URL, "/children/res"), hasExtra(ArtifactsNavigation.NAME, "res"), BuildComposeFixtures.fullPayload(ArtifactsNavigation.BUILD, BuildComposeFixtures.finished)))
        awaitText("Nested file.txt")
    }

    @Test fun folderLongClickOpenActionUsesSameNavigationPayload() {
        folders()
        compose.onNodeWithTag("artifacts:row:/metadata/res").performTouchInput { longClick() }
        sheet(text(SheetR.string.artifact_open))
        intended(allOf(hasComponent(ArtifactsNavigation.LEGACY_ACTIVITY), hasExtra(ArtifactsNavigation.URL, "/children/res"), BuildComposeFixtures.fullPayload(ArtifactsNavigation.BUILD, BuildComposeFixtures.finished)))
        awaitText("Nested file.txt")
    }

    @Test fun archiveOffersDownloadAndOpenWithOpaqueArchiveChildren() {
        val archive = Gson().fromJson("""{"name":"bundle.zip","href":"/metadata/bundle.zip","size":2048,"content":{"href":"/content/bundle.zip"},"children":{"href":"/children/bundle.zip!"}}""", File::class.java)
        `when`(teamCityService.listArtifacts(anyString(), anyString())).thenReturn(Single.just(Files(listOf(archive))))
        `when`(teamCityService.listArtifacts("/children/bundle.zip!", "browseArchives:true")).thenReturn(Single.just(Files(listOf(file("Inner file.txt", "/metadata/inner")))))
        start()
        clickFile("/metadata/bundle.zip")
        awaitText(text(SheetR.string.artifact_download))
        sheet(text(SheetR.string.artifact_open))
        intended(allOf(hasComponent(ArtifactsNavigation.LEGACY_ACTIVITY), hasExtra(ArtifactsNavigation.URL, "/children/bundle.zip!"), BuildComposeFixtures.fullPayload(ArtifactsNavigation.BUILD, BuildComposeFixtures.finished)))
        awaitText("Inner file.txt")
    }

    @Test fun downloadedFileOpensWithProviderUriAndGrants() {
        intending(hasAction(Intent.ACTION_VIEW)).respondWith(ActivityResult(Activity.RESULT_OK, null))
        `when`(teamCityService.downloadFile(anyString())).thenReturn(Single.just("downloaded bytes".toResponseBody()))
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

    @Test fun downloadFailureNotifiesBuildDetailsAndFeatureRetryCanSucceed() {
        intending(hasAction(Intent.ACTION_VIEW)).respondWith(ActivityResult(Activity.RESULT_OK, null))
        `when`(teamCityService.downloadFile(anyString())).thenReturn(Single.error(RuntimeException("download offline")))
        openArtifacts(Files(listOf(file("retry.unknown", "/metadata/retry"))))
        download("/metadata/retry")
        awaitText(text(ArtifactsR.string.artifacts_download_failed))
        onView(withText(R.string.download_artifact_retry_snack_bar_text)).check(matches(isDisplayed()))
        `when`(teamCityService.downloadFile(anyString())).thenReturn(Single.just("retried".toResponseBody()))
        compose.onNodeWithText(text(ArtifactsR.string.artifacts_retry_download)).performClick()
        compose.waitUntil(10_000) { androidx.test.espresso.intent.Intents.getIntents().any { it.action == Intent.ACTION_VIEW } }
        compose.onNodeWithText(text(ArtifactsR.string.artifacts_download_failed)).assertDoesNotExist()
    }

    @Test fun cancelDownloadDisposesTheNetworkRequestWithoutFailureNotification() {
        val pending = SingleSubject.create<ResponseBody>()
        val canceled = AtomicInteger()
        `when`(teamCityService.downloadFile(anyString())).thenReturn(pending.doOnDispose { canceled.incrementAndGet() })
        openArtifacts(Files(listOf(file("cancel.unknown", "/metadata/cancel"))))
        download("/metadata/cancel")
        awaitText(text(ArtifactsR.string.artifacts_downloading_title))
        compose.waitUntil(10_000) { pending.hasObservers() }
        compose.onNodeWithText(text(ArtifactsR.string.artifacts_cancel)).performClick()
        compose.waitUntil(10_000) { canceled.get() == 1 }
        assertFalse(pending.hasObservers())
        compose.onNodeWithText(text(ArtifactsR.string.artifacts_downloading_title)).assertDoesNotExist()
        compose.onNodeWithText(text(ArtifactsR.string.artifacts_download_failed)).assertDoesNotExist()
        onView(withText(R.string.download_artifact_retry_snack_bar_text)).check(androidx.test.espresso.assertion.ViewAssertions.doesNotExist())
    }

    @Test fun restoredArtifactsRetainsPagesAndPendingDownloadWithoutDuplicateWork() {
        val pending = SingleSubject.create<ResponseBody>()
        val lists = AtomicInteger()
        val downloads = AtomicInteger()
        `when`(teamCityService.build(anyString())).thenReturn(Single.just(BuildComposeFixtures.legacy(BuildComposeFixtures.finished)))
        `when`(teamCityService.listArtifacts(anyString(), anyString())).thenAnswer {
            lists.incrementAndGet()
            Single.just(Files(listOf(file("restore.unknown", "/metadata/restore"))))
        }
        `when`(teamCityService.downloadFile(anyString())).thenAnswer {
            downloads.incrementAndGet()
            pending
        }
        ActivityScenario.launch<BuildDetailsActivity>(BuildComposeFixtures.intent(BuildComposeFixtures.finished)).use { scenario ->
            awaitTag("overview:list")
            tab("Artifacts")
            download("/metadata/restore")
            awaitText(text(ArtifactsR.string.artifacts_downloading_title))
            val completedLists = lists.get()
            scenario.recreate()
            awaitText(text(ArtifactsR.string.artifacts_downloading_title))
            assertEquals(completedLists, lists.get())
            assertEquals(1, downloads.get())
            assertTrue(pending.hasObservers())
            compose.onNodeWithText(text(ArtifactsR.string.artifacts_cancel)).performClick()
            compose.waitUntil(10_000) { !pending.hasObservers() }
            awaitText("restore.unknown")
            // After closing the download dialog, query the native host's restored toolbar.
            TestUtils.matchToolbarTitle("#latest-number")
            TestUtils.matchToolbarSubTitle("Current configuration")
        }
    }

    @Test fun apkDownloadUsesAppPrivateStorageWithoutRequestingInstallPermission() {
        val pending = SingleSubject.create<ResponseBody>()
        `when`(teamCityService.downloadFile(anyString())).thenReturn(pending)
        openArtifacts(Files(listOf(file("app.apk", "/metadata/app.apk"))))
        download("/metadata/app.apk")
        awaitText(text(ArtifactsR.string.artifacts_downloading_title))
        compose.waitUntil(10_000) { pending.hasObservers() }
        compose.onNodeWithText(text(ArtifactsR.string.artifacts_permission_denied)).assertDoesNotExist()
        assertFalse(androidx.test.espresso.intent.Intents.getIntents().any { it.action == android.provider.Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES })
        compose.onNodeWithText(text(ArtifactsR.string.artifacts_cancel)).performClick()
        compose.waitUntil(10_000) { !pending.hasObservers() }
    }

    @Test fun hiddenBuildDetailsArtifactsCannotConsumeFolderActivityDownloadEvents() {
        intending(hasAction(Intent.ACTION_VIEW)).respondWith(ActivityResult(Activity.RESULT_OK, null))
        val downloads = AtomicInteger()
        `when`(teamCityService.downloadFile(anyString())).thenAnswer {
            downloads.incrementAndGet()
            Single.just("one owner".toResponseBody())
        }
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
        `when`(teamCityService.listArtifacts(anyString(), anyString())).thenAnswer {
            lists.incrementAndGet()
            Single.just(Files(listOf(file("Alias file.txt", "/metadata/alias"))))
        }
        val alias = Intent().setComponent(ComponentName(context.packageName, ArtifactsNavigation.LEGACY_ACTIVITY))
            .putExtra(ArtifactsNavigation.BUILD, BuildComposeFixtures.legacy(BuildComposeFixtures.incoming))
            .putExtra(ArtifactsNavigation.NAME, "Folder title").putExtra(ArtifactsNavigation.URL, "/children/alias")
        ActivityScenario.launch<Activity>(alias).use { scenario ->
            awaitText("Folder title")
            awaitText("Alias file.txt")
            val completedLists = lists.get()
            scenario.recreate()
            awaitText("Folder title")
            awaitText("Alias file.txt")
            assertEquals(completedLists, lists.get())
        }
        verify(teamCityService).listArtifacts("/children/alias", "browseArchives:true")
    }
    private fun file(name: String, href: String) = File(name, 1024L, File.Content("/content/" + name), href)
    private fun start() {
        `when`(teamCityService.build(anyString())).thenReturn(Single.just(BuildComposeFixtures.legacy(BuildComposeFixtures.finished)))
        activityRule.launchActivity(BuildComposeFixtures.intent(BuildComposeFixtures.finished))
        tab("Artifacts")
    }
    private fun openArtifacts(files: Files) {
        `when`(teamCityService.listArtifacts(anyString(), anyString())).thenReturn(Single.just(files))
        start()
    }
    private fun folders() {
        val folder = File("res", File.Children("/children/res"), "/metadata/res")
        `when`(teamCityService.listArtifacts(anyString(), anyString())).thenReturn(Single.just(Files(listOf(folder))))
        `when`(teamCityService.listArtifacts("/children/res", "browseArchives:true")).thenReturn(Single.just(Files(listOf(file("Nested file.txt", "/metadata/nested")))))
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
}
