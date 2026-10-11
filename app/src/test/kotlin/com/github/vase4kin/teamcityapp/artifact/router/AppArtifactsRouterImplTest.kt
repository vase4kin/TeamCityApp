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

package com.github.vase4kin.teamcityapp.artifact.router

import android.app.Activity
import android.app.Application
import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import com.github.vase4kin.teamcityapp.storage.SharedUserStorage
import java.io.File
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.kotlin.*
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import teamcityapp.features.artifacts.api.ArtifactDownload
import teamcityapp.features.artifacts.api.ArtifactPermission
import teamcityapp.features.artifacts.api.DownloadedArtifact
import teamcityapp.libraries.builds.BuildConfigurationData
import teamcityapp.libraries.builds.BuildLaunchData
import teamcityapp.libraries.chrome_tabs.ChromeCustomTabs
import teamcityapp.libraries.storage.models.UserAccount

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35], application = Application::class)
class AppArtifactsRouterImplTest {
    private val activity = mock<Activity>()
    private val storage = mock<SharedUserStorage>()
    private val tabs = mock<ChromeCustomTabs>()
    private val uri = Uri.parse("content://test.provider/downloaded_artifacts/report.txt")
    private val build = BuildLaunchData("42", "/builds/42", buildTypeId = "TeamCityApp_Build")
    private var createdTabs = 0
    private val requestedFiles = mutableListOf<File>()
    private fun router() = AppArtifactsRouterImpl(activity, storage, {
        createdTabs++
        tabs
    }) {
        requestedFiles.add(it)
        uri
    }
    private fun account(url: String = "https://teamcity.example") {
        val user = mock<UserAccount> { on { teamcityUrl } doReturn url }
        whenever(storage.activeUser).thenReturn(user)
    }

    @Test fun appPrivateStorageAndApkInstallChecksRemainAllowed() {
        val router = router()
        assertEquals(ArtifactPermission.Allowed, router.permission(activity, ArtifactDownload("report.txt", "href")))
        assertEquals(ArtifactPermission.Allowed, router.permission(activity, ArtifactDownload("app.apk", "href")))
        assertEquals(0, createdTabs)
    }

    @Test fun downloadedFileUsesProviderUriMimeAndBothTemporaryGrantFlags() {
        router().openDownloadedFile(activity, DownloadedArtifact("/downloads/report.txt"))
        val captured = argumentCaptor<Intent>()
        verify(activity).startActivity(captured.capture())
        assertEquals(Intent.ACTION_VIEW, captured.firstValue.action)
        assertEquals(uri, captured.firstValue.data)
        assertEquals("text/plain", captured.firstValue.type)
        assertEquals(Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION, captured.firstValue.flags)
        assertEquals(listOf(File("/downloads/report.txt")), requestedFiles)
    }

    @Test fun unknownFileExtensionUsesTheLegacyWildcardMimeType() {
        router().openDownloadedFile(activity, DownloadedArtifact("/downloads/file.unknownteamcity"))
        val captured = argumentCaptor<Intent>()
        verify(activity).startActivity(captured.capture())
        assertEquals("*/*", captured.firstValue.type)
    }

    @Test fun missingMimeHandlerRetriesTheSameUriWithWildcardMime() {
        val types = mutableListOf<String?>()
        doAnswer {
            types.add(it.getArgument<Intent>(0).type)
            if (types.size == 1) throw ActivityNotFoundException("no handler")
            null
        }.whenever(activity).startActivity(any())
        router().openDownloadedFile(activity, DownloadedArtifact("/downloads/report.txt"))
        assertEquals(listOf("text/plain", "*/*"), types)
        verify(activity, times(2)).startActivity(any())
        assertEquals(1, requestedFiles.size)
    }

    @Test fun noWildcardHandlerPropagatesForTheFeatureFileUnavailableDialog() {
        doThrow(ActivityNotFoundException("no browser")).whenever(activity).startActivity(any())
        assertTrue(runCatching { router().openDownloadedFile(activity, DownloadedArtifact("/downloads/report.txt")) }.exceptionOrNull() is ActivityNotFoundException)
        verify(activity, times(2)).startActivity(any())
    }

    @Test fun browserKeepsRawArchiveMetadataPathBuildIdentityAndGuestQuery() {
        account()
        val router = router()
        router.openBrowser(activity, build, "/guestAuth/app/rest/builds/42/artifacts/metadata/reports.zip!/folder/index.html")
        verify(tabs).initCustomsTabs()
        verify(tabs).launchUrl("https://teamcity.example/repository/download/TeamCityApp_Build/42:id/reports.zip!/folder/index.html?guest=1")
        assertEquals(1, createdTabs)
    }

    @Test fun browserUsesCurrentAccountAndSnapshotConfigurationFallback() {
        account("https://another-account.example")
        val snapshot = build.copy(buildTypeId = null, configuration = BuildConfigurationData("Fallback_Config"))
        router().openBrowser(activity, snapshot, "/metadata/report.html")
        verify(tabs).launchUrl("https://another-account.example/repository/download/Fallback_Config/42:id/report.html?guest=1")
    }

    @Test fun invalidBrowserMetadataIsExplicitAndDoesNotBindAService() {
        account()
        val router = router()
        assertTrue(runCatching { router.openBrowser(activity, build, "/content/report.html") }.exceptionOrNull() is IllegalArgumentException)
        assertTrue(runCatching { router.openBrowser(activity, build.copy(buildTypeId = null), "/metadata/report.html") }.exceptionOrNull() is IllegalArgumentException)
        assertEquals(0, createdTabs)
        verifyNoInteractions(tabs)
    }

    @Test fun browserFailurePropagatesForTheFeatureBrowserUnavailableDialog() {
        account()
        doThrow(ActivityNotFoundException("no browser")).whenever(tabs).launchUrl(any())
        assertTrue(runCatching { router().openBrowser(activity, build, "/metadata/report.html") }.exceptionOrNull() is ActivityNotFoundException)
    }

    @Test fun disposeUnbindsOnceAndTheNextViewReinitializesTheService() {
        account()
        val router = router()
        router.dispose(activity)
        verifyNoInteractions(tabs)
        router.openBrowser(activity, build, "/metadata/one.html")
        router.openBrowser(activity, build, "/metadata/two.html")
        assertEquals(1, createdTabs)
        router.dispose(activity)
        router.dispose(activity)
        verify(tabs, times(1)).unbindCustomsTabs()
        router.openBrowser(activity, build, "/metadata/three.html")
        assertEquals(2, createdTabs)
        verify(tabs, times(2)).initCustomsTabs()
    }

    @Test fun uiRouterRejectsAHostFromAnotherActivity() {
        val other = mock<Activity>()
        val router = router()
        assertTrue(runCatching { router.permission(other, ArtifactDownload("file", "href")) }.exceptionOrNull() is IllegalArgumentException)
        assertTrue(runCatching { router.openDownloadedFile(other, DownloadedArtifact("file")) }.exceptionOrNull() is IllegalArgumentException)
        assertTrue(runCatching { router.dispose(other) }.exceptionOrNull() is IllegalArgumentException)
        verifyNoInteractions(tabs, other)
    }
}
