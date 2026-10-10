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

import android.app.Activity
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.FragmentManager
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
    fun downloadFailed()
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

internal class ArtifactsRouterDelegate(
    private val activity: AppCompatActivity,
    private val fragments: FragmentManager,
    private val navigation: ArtifactsNavigation,
    private val sheet: BottomSheetNavigation,
    private val app: ArtifactsAppRouter,
    private val events: ArtifactsEvents,
    private val up: () -> Unit
) : ArtifactsRouter {
    override val actions get() = events.actions
    override fun navigateUp() = up()
    override fun openFolder(name: String, build: BuildLaunchData, href: String) = navigation.open(activity, name, build, href)
    override fun openActions(file: Artifact) {
        val (menu, values) = artifactMenu(file)
        sheet.createBottomSheetDialog(file.name, values, menu).show(fragments, "Tag bottom sheet")
    }
    override fun permission(file: ArtifactDownload) = app.permission(activity, file)
    override fun openFile(file: DownloadedArtifact) = app.openDownloadedFile(activity, file)
    override fun openBrowser(build: BuildLaunchData, href: String) = app.openBrowser(activity, build, href)
    override fun downloadFailed() = events.downloadFailed()
    override fun dispose() = app.dispose(activity)
}
