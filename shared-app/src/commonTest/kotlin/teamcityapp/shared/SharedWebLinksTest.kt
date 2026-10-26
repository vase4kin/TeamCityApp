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
import kotlin.test.*
import teamcityapp.libraries.builds.BuildLaunchData
class SharedWebLinksTest {
    private val server = "https://teamcity.example/context/"
    private val build = BuildLaunchData("1", "/context/app/rest/builds/id:1", buildTypeId = "Demo_Build")

    @Test fun webLinksKeepContextAndNeverInjectApiCredentials() {
        assertEquals("https://teamcity.example/context/viewLog.html?buildId=1", activeServerWebUrl(server, "viewLog.html?buildId=1"))
        assertEquals("https://teamcity.example/context/downloadBuildLog.html?buildId=1&guest=1", buildLogUrl(server, build, true))
        assertFalse(buildLogUrl(server, build, false).contains("guest"))
        assertEquals("https://teamcity.example/context/repository/download/Demo_Build/1:id/report/index.html?guest=1", artifactBrowserUrl(server, build, "/context/app/rest/builds/id:1/artifacts/metadata/report/index.html", true))
    }

    @Test fun privateArtifactsUseTheBrowsersLoginRatherThanForcingGuest() {
        val href = "/context/app/rest/builds/id:1/artifacts/metadata/archive.zip!/report.html"
        assertEquals("https://teamcity.example/context/repository/download/Demo_Build/1:id/archive.zip!/report.html", artifactBrowserUrl(server, build, href, false))
        assertEquals(artifactBrowserUrl(server, build, href, false) + "?guest=1", artifactBrowserUrl(server, build, href, true))
    }

    @Test fun externalCredentialTraversalAndWrongContextLinksAreRejected() {
        for (href in listOf("https://evil.example/context/file", "http://teamcity.example/context/file", "https://alice:secret@teamcity.example/context/file", "/elsewhere/file", "/context/%2e%2e/file", "/context/%2e%2e%2foutside", "/context/file?password=secret", "/context/file?access_token=secret")) assertFails { activeServerWebUrl(server, href) }
        assertFails { artifactBrowserUrl(server, build, "/context/not-artifact", true) }
    }
}
