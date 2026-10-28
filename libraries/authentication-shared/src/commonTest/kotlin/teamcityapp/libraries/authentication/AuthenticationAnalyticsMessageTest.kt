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

package teamcityapp.libraries.authentication

import kotlin.test.*

class AuthenticationAnalyticsMessageTest {
    @Test fun preservesExplicitLegacyFailureMessages() {
        assertEquals("Unauthorized", AuthenticationResult.Failure(401, "Unauthorized", AuthenticationError.Http(401)).analyticsMessage())
    }

    @Test fun typedFailuresRemainInformativeWithoutServerOrCredentialData() {
        assertEquals("HTTP 401", AuthenticationResult.Failure(401, "", AuthenticationError.Http(401)).analyticsMessage())
        assertEquals("HTTP 500", AuthenticationResult.Failure(500, "").analyticsMessage())
        assertEquals("Connection failed", AuthenticationResult.Failure(0, "", AuthenticationError.Connection).analyticsMessage())
        assertEquals("Invalid server URL", AuthenticationResult.Failure(0, "", AuthenticationError.InvalidUrl).analyticsMessage())
        assertEquals("Server is not secure (HTTP)", AuthenticationResult.Failure(-1, "").analyticsMessage())
        assertEquals("", AuthenticationResult.Failure(0, "").analyticsMessage())
    }
}
