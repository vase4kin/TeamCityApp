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

import kotlin.test.*
import teamcityapp.libraries.builds.BuildLaunchData

class ArtifactBrowserUrlTest {
    private val server = "https://teamcity.example/context/"
    private val build = BuildLaunchData("1", "/context/app/rest/builds/id:1", buildTypeId = "Demo_Build")

    @Test fun privateArtifactsUseTheBrowsersLoginRatherThanForcingGuest() {
        val href = "/context/app/rest/builds/id:1/artifacts/metadata/archive.zip!/report.html"
        assertEquals("https://teamcity.example/context/repository/download/Demo_Build/1:id/archive.zip!/report.html", artifactBrowserUrl(server, build, href, false))
        assertEquals(artifactBrowserUrl(server, build, href, false) + "?guest=1", artifactBrowserUrl(server, build, href, true))
    }

    @Test fun guestArtifactsKeepTheActiveServerContext() {
        assertEquals("https://teamcity.example/context/repository/download/Demo_Build/1:id/report/index.html?guest=1", artifactBrowserUrl(server, build, "/context/app/rest/builds/id:1/artifacts/metadata/report/index.html", true))
    }

    @Test fun invalidAndExternalMetadataIsRejectedBeforeBuildingTheDownloadUrl() {
        assertFails { artifactBrowserUrl(server, build, "/context/not-artifact", true) }
        assertFails { artifactBrowserUrl(server, build, "https://evil.example/context/metadata/report", false) }
    }
}
