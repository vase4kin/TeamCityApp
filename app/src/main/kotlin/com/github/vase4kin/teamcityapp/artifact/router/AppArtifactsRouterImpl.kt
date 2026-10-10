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
import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.webkit.MimeTypeMap
import androidx.core.content.FileProvider
import com.github.vase4kin.teamcityapp.BuildConfig
import com.github.vase4kin.teamcityapp.storage.SharedUserStorage
import dagger.hilt.android.scopes.ActivityScoped
import java.io.File
import javax.inject.Inject
import teamcityapp.features.artifacts.api.ArtifactDownload
import teamcityapp.features.artifacts.api.ArtifactPermission
import teamcityapp.features.artifacts.api.ArtifactsAppRouter
import teamcityapp.features.artifacts.api.DownloadedArtifact
import teamcityapp.libraries.builds.BuildLaunchData
import teamcityapp.libraries.chrome_tabs.ChromeCustomTabs
import teamcityapp.libraries.chrome_tabs.ChromeCustomTabsImpl

@ActivityScoped
class AppArtifactsRouterImpl internal constructor(
    private val owner: Activity,
    private val storage: SharedUserStorage,
    private val tabsFactory: (Activity) -> ChromeCustomTabs,
    private val fileUri: (File) -> Uri
) : ArtifactsAppRouter {
    @Inject constructor(activity: Activity, storage: SharedUserStorage) : this(
        activity,
        storage,
        { ChromeCustomTabsImpl(it) },
        { FileProvider.getUriForFile(activity, BuildConfig.APPLICATION_ID + ".provider", it) }
    )
    private var customTabs: ChromeCustomTabs? = null

    override fun permission(activity: Activity, file: ArtifactDownload): ArtifactPermission {
        requireOwner(activity)
        // PermissionManager's storage and APK-install checks both return true for app-private files.
        return ArtifactPermission.Allowed
    }

    override fun openDownloadedFile(activity: Activity, file: DownloadedArtifact) {
        requireOwner(activity)
        val downloaded = File(file.path)
        val extension = MimeTypeMap.getFileExtensionFromUrl(downloaded.name)
        val type = MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension) ?: ALL_FILES_TYPE
        val data = fileUri(downloaded)
        val intent = Intent(Intent.ACTION_VIEW).setDataAndType(data, type)
            .addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION)
        try {
            activity.startActivity(intent)
        } catch (_: ActivityNotFoundException) {
            intent.setDataAndType(data, ALL_FILES_TYPE)
            activity.startActivity(intent)
        }
    }

    override fun openBrowser(activity: Activity, build: BuildLaunchData, href: String) {
        requireOwner(activity)
        val path = href.substringAfter("/metadata/", "")
        require(path.isNotEmpty()) { "Artifact metadata URL has no file path" }
        val configurationId = build.buildTypeId ?: build.configuration?.id
        require(!configurationId.isNullOrEmpty()) { "Artifact build has no configuration ID" }
        val url = "${storage.activeUser.teamcityUrl}/repository/download/$configurationId/${build.id}:id/$path?guest=1"
        val tabs = customTabs ?: tabsFactory(activity).also {
            it.initCustomsTabs()
            customTabs = it
        }
        tabs.launchUrl(url)
    }

    override fun dispose(activity: Activity) {
        requireOwner(activity)
        val tabs = customTabs ?: return
        customTabs = null
        tabs.unbindCustomsTabs()
    }

    private fun requireOwner(activity: Activity) = require(activity === owner) { "Artifacts router must use its owning activity" }
    private companion object {
        const val ALL_FILES_TYPE = "*/*"
    }
}
