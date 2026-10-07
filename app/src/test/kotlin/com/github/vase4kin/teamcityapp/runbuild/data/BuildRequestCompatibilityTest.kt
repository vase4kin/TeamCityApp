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

package com.github.vase4kin.teamcityapp.runbuild.data

import com.google.gson.Gson
import org.junit.Assert.*
import org.junit.Test
import teamcityapp.features.run_build.api.*

/** TeamCity REST receives the same legacy wire fields and parameter ordering. */
class BuildRequestCompatibilityTest {
    @Test fun selectedOptionsAndDuplicateParameterNamesKeepTheirWireRepresentation() {
        val request = BuildRequest("bt1", "feature/compose", BuildAgent("42", "Linux"), personal = true, queueAtTop = true, cleanSources = false, parameters = listOf(BuildParameter("env.x", "one"), BuildParameter("env.x", "two"), BuildParameter("empty", "")))
        val json = Gson().toJsonTree(request.toLegacyBuild()).asJsonObject
        assertEquals("bt1", json.getAsJsonObject("buildType")["id"].asString)
        assertEquals("feature/compose", json["branchName"].asString)
        assertTrue(json["personal"].asBoolean)
        assertTrue(json["queueAtTop"].asBoolean)
        assertFalse(json["cleanSources"].asBoolean)
        assertEquals("42", json.getAsJsonObject("agent")["id"].asString)
        val parameters = json.getAsJsonObject("properties").getAsJsonArray("property")
        assertEquals(listOf("env.x", "env.x", "empty"), parameters.map { it.asJsonObject["name"].asString })
        assertEquals(listOf("one", "two", ""), parameters.map { it.asJsonObject["value"].asString })
    }

    @Test fun defaultRequestDoesNotAddAgentOrProperties() {
        val build = BuildRequest("bt1").toLegacyBuild()
        assertEquals("", build.branchName)
        assertNull(build.agent)
        assertNull(build.properties)
        assertFalse(build.isPersonal)
        assertFalse(build.isQueueAtTop)
        assertTrue(build.isCleanSources)
    }
}
