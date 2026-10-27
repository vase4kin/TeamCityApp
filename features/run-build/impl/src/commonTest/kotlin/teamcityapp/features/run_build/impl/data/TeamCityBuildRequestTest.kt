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

package teamcityapp.features.run_build.impl.data

import kotlin.test.*
import kotlinx.serialization.json.*
import teamcityapp.features.run_build.api.*

class TeamCityBuildRequestTest {
    @Test fun requestPreservesOptionsDuplicateParametersEmptyValuesAndSafeIdentifiers() {
        val request = BuildRequest("Build", "release", BuildAgent("1", "Linux"), true, true, false, listOf(BuildParameter("env.X", ""), BuildParameter("env.X", "two")))
        val body = request.payload()
        assertEquals("Build", body["buildType"]!!.jsonObject["id"]!!.jsonPrimitive.content)
        assertEquals(true, body["personal"]!!.jsonPrimitive.boolean)
        assertEquals(false, body["triggeringOptions"]!!.jsonObject["cleanSources"]!!.jsonPrimitive.boolean)
        assertEquals(true, body["triggeringOptions"]!!.jsonObject["queueAtTop"]!!.jsonPrimitive.boolean)
        assertEquals(listOf("", "two"), body["properties"]!!.jsonObject["property"]!!.jsonArray.map { it.jsonObject["value"]!!.jsonPrimitive.content })
        assertFails { BuildRequest("../elsewhere").payload() }
    }
}
