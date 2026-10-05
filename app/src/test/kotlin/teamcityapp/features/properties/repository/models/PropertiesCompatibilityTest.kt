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

package teamcityapp.features.properties.repository.models

import com.google.gson.Gson
import org.junit.Assert.*
import org.junit.Test

/** Moving the legacy DTO must preserve TeamCity JSON and existing cache class names. */
class PropertiesCompatibilityTest {
    @Test fun keepsJsonKeysEmptyValuesAndOwnership() {
        val gson = Gson()
        val json = """{"property":[{"name":"sdk","value":"24","own":true},{"name":"secret","value":"","own":false}]}"""
        val properties = gson.fromJson(json, Properties::class.java)
        assertEquals("sdk", properties.properties[0].name)
        assertEquals("24", properties.properties[0].value)
        assertTrue(properties.properties[0].isOwn)
        assertEquals("", properties.properties[1].value)
        assertFalse(properties.properties[1].isOwn)
        assertEquals(gson.fromJson(json, com.google.gson.JsonElement::class.java), gson.toJsonTree(properties))
        assertEquals("teamcityapp.features.properties.repository.models.Properties", Properties::class.java.name)
        assertEquals("teamcityapp.features.properties.repository.models.Properties\$Property", Properties.Property::class.java.name)
    }
}
