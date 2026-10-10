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

/** Metadata URLs are opaque TeamCity paths, including archive entries. */
data class Artifact(
    val name: String,
    val href: String,
    val size: Long = 0,
    val contentHref: String? = null,
    val childrenHref: String? = null
) {
    // Preserve the server/legacy size-based distinction, including zero-byte entries.
    val isFolder: Boolean get() = size == 0L
    val isBrowserFile: Boolean get() = href.contains(".html") || href.contains(".htm")
}
data class DownloadedArtifact(val path: String)
data class ArtifactDownload(val name: String, val href: String)
sealed interface ArtifactAction {
    data class Download(val file: ArtifactDownload) : ArtifactAction
    data class Open(val name: String, val href: String) : ArtifactAction
    data class Browser(val href: String) : ArtifactAction
}
enum class ArtifactPermission { Allowed, Storage, InstallPackages }
