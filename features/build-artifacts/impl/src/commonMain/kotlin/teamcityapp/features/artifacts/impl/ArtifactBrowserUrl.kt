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

import io.ktor.http.*
import teamcityapp.libraries.builds.BuildLaunchData
import teamcityapp.libraries.network.activeServerWebUrl

fun artifactBrowserUrl(server: String, build: BuildLaunchData, href: String, guest: Boolean): String {
    // Validate the opaque source before converting metadata to TeamCity's browser download route.
    activeServerWebUrl(server, href)
    val path = href.substringAfter("/metadata/", "")
    require(path.isNotBlank())
    val configuration = requireNotNull(build.buildTypeId ?: build.configuration?.id)
    return activeServerWebUrl(server, "repository/download/${configuration.encodeURLPathPart()}/${build.id.encodeURLPathPart()}:id/$path" + if (guest) "?guest=1" else "")
}
