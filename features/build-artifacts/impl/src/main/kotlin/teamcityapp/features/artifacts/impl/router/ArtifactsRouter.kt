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

package teamcityapp.features.artifacts.impl.router

import kotlinx.coroutines.flow.Flow
import teamcityapp.features.artifacts.api.*
import teamcityapp.features.bottom_sheet.api.*
import teamcityapp.libraries.builds.BuildLaunchData

interface ArtifactsRouter {
    val actions: Flow<ArtifactAction>
    fun navigateUp()
    fun openFolder(name: String, build: BuildLaunchData, href: String)
    fun openActions(file: Artifact)
    fun permission(file: ArtifactDownload): ArtifactPermission
    fun openFile(file: DownloadedArtifact)
    fun openBrowser(build: BuildLaunchData, href: String)
    fun dispose()
}

/** Menu URL ordering preserves the installed action sheet's event contract. */
internal fun artifactMenu(file: Artifact): Pair<SheetMenuType, Array<String>> {
    val children = file.childrenHref
    return when {
        !file.isFolder && children != null -> SheetMenuType.ArtifactFull to arrayOf(file.contentHref.orEmpty(), children)
        file.isFolder && children != null -> SheetMenuType.ArtifactFolder to arrayOf(children)
        file.isBrowserFile -> SheetMenuType.ArtifactBrowser to arrayOf(file.contentHref.orEmpty(), file.href)
        else -> SheetMenuType.ArtifactDefault to arrayOf(file.contentHref.orEmpty())
    }
}
