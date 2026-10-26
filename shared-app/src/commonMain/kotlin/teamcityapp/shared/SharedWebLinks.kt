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

package teamcityapp.shared

import io.ktor.http.*
import teamcityapp.libraries.builds.BuildLaunchData
import teamcityapp.libraries.network.normalizeServerUrl

internal fun activeServerWebUrl(server: String, href: String): String {
    val base = Url(normalizeServerUrl(server))
    val root = base.toString().trimEnd('/')
    val candidate = when {
        href.startsWith("http://") || href.startsWith("https://") -> Url(href)
        href.startsWith('/') -> Url("${base.protocol.name}://${base.hostWithPort}$href")
        else -> Url("$root/$href")
    }
    val context = base.encodedPath.trimEnd('/') + "/"
    require(candidate.protocol == base.protocol && candidate.host == base.host && candidate.port == base.port && candidate.user.isNullOrEmpty() && candidate.password.isNullOrEmpty())
    require(candidate.encodedPath.startsWith(context) && candidate.encodedPath.split('/').flatMap { it.decodeURLPart().split('/', '\\') }.none { it in setOf(".", "..") })
    require(candidate.parameters.names().none { it.equals("password", true) || it.equals("token", true) || it.equals("access_token", true) })
    return candidate.toString()
}
internal fun buildLogUrl(server: String, build: BuildLaunchData, guest: Boolean): String {
    require(build.id.isNotBlank())
    return activeServerWebUrl(server, "downloadBuildLog.html?buildId=${build.id.encodeURLQueryComponent()}" + if (guest) "&guest=1" else "")
}
internal fun artifactBrowserUrl(server: String, build: BuildLaunchData, href: String, guest: Boolean): String {
    // Validate the opaque source before converting metadata to TeamCity's browser download route.
    activeServerWebUrl(server, href)
    val path = href.substringAfter("/metadata/", "")
    require(path.isNotBlank())
    val configuration = requireNotNull(build.buildTypeId ?: build.configuration?.id)
    return activeServerWebUrl(server, "repository/download/${configuration.encodeURLPathPart()}/${build.id.encodeURLPathPart()}:id/$path" + if (guest) "?guest=1" else "")
}
