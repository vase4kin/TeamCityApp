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

package teamcityapp.libraries.network

import kotlin.test.*

class TeamCityWebUrlTest {
    @Test fun webLinksKeepTheActiveServerContext() {
        assertEquals("https://teamcity.example/context/viewLog.html?buildId=1", activeServerWebUrl("https://teamcity.example/context/", "viewLog.html?buildId=1"))
    }

    @Test fun externalCredentialTraversalAndWrongContextLinksAreRejected() {
        val server = "https://teamcity.example/context/"
        for (href in listOf("https://evil.example/context/file", "http://teamcity.example/context/file", "https://alice:secret@teamcity.example/context/file", "/elsewhere/file", "/context/%2e%2e/file", "/context/%2e%2e%2foutside", "/context/file?password=secret", "/context/file?access_token=secret")) assertFails { activeServerWebUrl(server, href) }
    }
}
