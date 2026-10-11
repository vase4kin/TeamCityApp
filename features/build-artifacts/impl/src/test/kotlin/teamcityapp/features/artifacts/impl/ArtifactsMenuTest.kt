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

package teamcityapp.features.artifacts.impl

import org.junit.Assert.*
import org.junit.Test
import teamcityapp.features.artifacts.api.Artifact
import teamcityapp.features.artifacts.impl.router.artifactMenu
import teamcityapp.features.bottom_sheet.api.SheetMenuType

class ArtifactsMenuTest {
    @Test fun folderUsesChildrenHref() {
        menu(Artifact("folder", "/folder", childrenHref = "/children"), SheetMenuType.ArtifactFolder, "/children")
    }

    @Test fun archiveProvidesDownloadThenBrowse() {
        menu(Artifact("build.zip", "/zip", 20, "/content", "/archive/children"), SheetMenuType.ArtifactFull, "/content", "/archive/children")
    }

    @Test fun htmlProvidesContentDownloadAndMetadataBrowserUrl() {
        menu(Artifact("index.html", "/metadata/index.html", 5, "/content/index.html"), SheetMenuType.ArtifactBrowser, "/content/index.html", "/metadata/index.html")
    }

    @Test fun ordinaryFileOnlyDownloads() {
        menu(Artifact("text.txt", "/metadata/text.txt", 20, "/content"), SheetMenuType.ArtifactDefault, "/content")
    }

    @Test fun zeroSizeWithoutChildrenStillOffersDownload() {
        menu(Artifact("empty", "/empty", contentHref = "/content/empty"), SheetMenuType.ArtifactDefault, "/content/empty")
    }
    private fun menu(file: Artifact, type: SheetMenuType, vararg urls: String) {
        val result = artifactMenu(file)
        assertEquals(type, result.first)
        assertArrayEquals(urls, result.second)
    }
}
