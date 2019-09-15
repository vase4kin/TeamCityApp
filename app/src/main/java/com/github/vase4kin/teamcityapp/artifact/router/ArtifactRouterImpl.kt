/*
 * Copyright 2019 Andrey Tolpeev
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

import android.content.Intent
import android.webkit.MimeTypeMap
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import com.github.vase4kin.teamcityapp.BuildConfig
import com.github.vase4kin.teamcityapp.artifact.view.ArtifactListActivity
import com.github.vase4kin.teamcityapp.custom_tabs.ChromeCustomTabs
import com.github.vase4kin.teamcityapp.overview.data.BuildDetails
import com.github.vase4kin.teamcityapp.storage.SharedUserStorage
import java.io.File

/**
 * Impl of [ArtifactRouter]
 */
class ArtifactRouterImpl(
    private val sharedUserStorage: SharedUserStorage,
    private val activity: AppCompatActivity,
    private val chromeCustomTabs: ChromeCustomTabs
) : ArtifactRouter {

    init {
        chromeCustomTabs.initCustomsTabs()
    }

    /**
     * {@inheritDoc}
     */
    override fun startFileActivity(file: File) {
        val map = MimeTypeMap.getSingleton()
        val ext = MimeTypeMap.getFileExtensionFromUrl(file.name)
        var type = map.getMimeTypeFromExtension(ext)

        if (type == null) {
            type = ALL_FILES_TYPE
        }

        val intent = Intent(Intent.ACTION_VIEW)
        val data = FileProvider.getUriForFile(
            activity,
            BuildConfig.APPLICATION_ID + ".provider", file
        )
        intent.setDataAndType(data, type)
        intent.flags =
            Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION

        // User couldn't have app with type intent
        try {
            activity.startActivity(intent)
        } catch (e: android.content.ActivityNotFoundException) {
            intent.setDataAndType(data, ALL_FILES_TYPE)
            activity.startActivity(intent)
        }
    }

    /**
     * {@inheritDoc}
     */
    override fun openArtifactFile(
        artifactName: String,
        buildDetails: BuildDetails,
        href: String
    ) {
        ArtifactListActivity.start(artifactName, buildDetails.toBuild(), href, activity)
    }

    /**
     * {@inheritDoc}
     */
    override fun unbindCustomsTabs() {
        chromeCustomTabs.unbindCustomsTabs()
    }

    /**
     * {@inheritDoc}
     */
    override fun startBrowser(buildDetails: BuildDetails, href: String) {
        val pathToFile =
            href.split("/metadata/".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()[1]
        val url = String.format(
            FILE_URL_PATTERN,
            sharedUserStorage.activeUser.teamcityUrl,
            buildDetails.buildTypeId,
            buildDetails.id,
            pathToFile
        )
        chromeCustomTabs.launchUrl(url)
    }

    companion object {

        /**
         * Pattern to open artifacts in the browser
         */
        private const val FILE_URL_PATTERN = "%s/repository/download/%s/%s:id/%s?guest=1"
        /**
         * All file types
         */
        private const val ALL_FILES_TYPE = "*/*"
    }
}
