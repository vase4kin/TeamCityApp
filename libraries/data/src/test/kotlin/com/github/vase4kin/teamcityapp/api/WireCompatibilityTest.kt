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

package com.github.vase4kin.teamcityapp.api

import com.github.vase4kin.teamcityapp.buildlist.api.Build
import com.google.gson.Gson
import org.junit.Assert.*
import org.junit.Test
import teamcityapp.features.about.repository.models.ServerInfo
import teamcityapp.features.properties.repository.models.Properties
import teamcityapp.features.test_details.repository.models.TestOccurrence

class WireCompatibilityTest {
    @Test fun movedWireModelsKeepTheirPersistedCacheClassNamesAndWireFields() {
        assertEquals("com.github.vase4kin.teamcityapp.buildlist.api.Build", Build::class.java.name)
        assertEquals("teamcityapp.features.about.repository.models.ServerInfo", ServerInfo::class.java.name)
        assertEquals("teamcityapp.features.properties.repository.models.Properties", Properties::class.java.name)
        assertEquals("teamcityapp.features.test_details.repository.models.TestOccurrence", TestOccurrence::class.java.name)
        val gson = Gson()
        val build = gson.fromJson("""{"id":"42","href":"/builds/42","number":"2459","state":"finished","buildType":{"id":"android","name":"Android"}}""", Build::class.java)
        val wire = gson.toJsonTree(build).asJsonObject
        assertEquals("42", wire["id"].asString)
        assertEquals("/builds/42", wire["href"].asString)
        assertEquals("2459", wire["number"].asString)
        assertEquals("android", wire["buildType"].asJsonObject["id"].asString)
    }
}
