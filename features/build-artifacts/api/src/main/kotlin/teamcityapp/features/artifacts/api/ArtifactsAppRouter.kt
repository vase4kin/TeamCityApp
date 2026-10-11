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

package teamcityapp.features.artifacts.api

import android.app.Activity
import teamcityapp.libraries.builds.BuildLaunchData

/** Permission, file-provider, authenticated browser, and download event compatibility. */
interface ArtifactsAppRouter {
    fun permission(activity: Activity, file: ArtifactDownload): ArtifactPermission
    fun openDownloadedFile(activity: Activity, file: DownloadedArtifact)
    fun openBrowser(activity: Activity, build: BuildLaunchData, href: String)

    /** Releases any view-owned custom-tab bindings. Safe even if no browser was opened. */
    fun dispose(activity: Activity)
}
