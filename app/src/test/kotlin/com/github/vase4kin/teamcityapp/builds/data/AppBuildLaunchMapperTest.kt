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

package com.github.vase4kin.teamcityapp.builds.data

import com.google.gson.GsonBuilder
import org.junit.Assert.*
import org.junit.Test
import teamcityapp.libraries.builds.*

class AppBuildLaunchMapperTest {
    private val mapper = AppBuildLaunchMapper()

    @Test fun completeLegacyPayloadMapsAllLaunchFieldsAndRawValues() {
        val actual = mapper.toLaunchData(legacyBuild(completeBuildJson))
        assertEquals(completeLaunch(), actual)
        assertFalse(actual.isFinished)
        assertFalse(actual.isFailed)
        assertFalse(actual.isSuccess)
    }

    @Test fun encodeUsesExactLegacyWireNamesWithoutMergingConfigurationIds() {
        val encoded = mapper.toLegacyBuild(completeLaunch())
        val json = GsonBuilder().serializeNulls().create().toJsonTree(encoded).asJsonObject
        assertEquals("top-level-id", json["buildTypeId"].asString)
        assertEquals("nested-id", json["buildType"].asJsonObject["id"].asString)
        assertEquals("trigger-id", json["triggered"].asJsonObject["buildType"].asJsonObject["id"].asString)
        assertEquals("/snapshots?next=2", json["snapshot-dependencies"].asJsonObject["nextHref"].asString)
        assertEquals(13, json["testOccurrences"].asJsonObject["count"].asInt)
        assertEquals(completeLaunch(), mapper.toLaunchData(encoded))
    }

    @Test fun minimalLegacyPayloadPreservesAbsentSectionsAndDefaultBooleans() {
        assertEquals(BuildLaunchData("1", "/builds/1"), mapper.toLaunchData(legacyBuild("""{"id":"1","href":"/builds/1"}""")))
        assertEquals(BuildLaunchData("1", "/builds/1"), mapper.toLaunchData(mapper.toLegacyBuild(BuildLaunchData("1", "/builds/1"))))
    }

    @Test fun presentPartialSectionsDoNotDisappearOrInventRowData() {
        val build = mapper.toLaunchData(legacyBuild("""{"id":"1","href":"/builds/1","buildType":{},"triggered":{},"agent":{},"canceledInfo":{},"changes":{},"artifacts":{},"testOccurrences":{},"snapshot-dependencies":{},"properties":{}}"""))
        val expected = BuildLaunchData(
            "1", "/builds/1", configuration = BuildConfigurationData(null),
            triggered = BuildTrigger(null), agent = BuildAgent(null, null), canceledInfo = BuildCancellation(null, null),
            changes = BuildCollectionLink(null, 0), artifacts = BuildCollectionLink(null), tests = BuildTests(null),
            snapshotDependencies = BuildCollectionLink(null, 0), properties = BuildProperties(null)
        )
        assertEquals(expected, build)
        assertEquals(expected, mapper.toLaunchData(mapper.toLegacyBuild(build)))
    }

    @Test fun absentNullAndEmptyPropertyListsRetainDifferentWrapperPresence() {
        val absent = BuildLaunchData("1", "/builds/1")
        val nullItems = absent.copy(properties = BuildProperties(null, "p", "/properties"))
        val emptyItems = nullItems.copy(properties = BuildProperties(emptyList(), "p", "/properties"))
        listOf(absent, nullItems, emptyItems).forEach { assertEquals(it, mapper.toLaunchData(mapper.toLegacyBuild(it))) }
        assertNull(mapper.toLegacyBuild(absent).properties)
        assertNotNull(mapper.toLegacyBuild(nullItems).properties)
        assertNull(mapper.toLegacyBuild(nullItems).properties!!.properties)
        assertTrue(mapper.toLegacyBuild(emptyItems).properties!!.properties.isEmpty())
    }

    @Test fun restartPropertiesRetainOwnFlagIdentityNullableNamesAndValues() {
        val properties = mapper.toLegacyBuild(completeLaunch()).properties!!
        assertEquals("properties-id", properties.id)
        assertEquals("/properties", properties.href)
        val first = properties.properties[0]
        assertTrue(first.isOwn)
        assertEquals("property-1", first.id)
        assertEquals("/properties/1", first.href)
        assertEquals("env", first.name)
        assertEquals("test", first.value)
        assertNull(properties.properties[1].name)
        assertNull(properties.properties[1].value)
        assertFalse(properties.properties[1].isOwn)
    }

    @Test fun mappingCopiesMutablePropertiesInsteadOfRetainingDtoList() {
        val source = legacyBuild(completeBuildJson)
        val mapped = mapper.toLaunchData(source)
        source.properties!!.properties.clear()
        assertEquals(2, mapped.properties!!.items!!.size)
        assertEquals(completeLaunch(), mapped)
    }

    @Test fun nullablePartialUserAndConfigurationMetadataSurviveRoundTrip() {
        val build = BuildLaunchData(
            "1",
            "/builds/1",
            configuration = BuildConfigurationData(null, href = "/config"),
            triggered = BuildTrigger("custom", user = BuildUser(null, null), configuration = BuildConfigurationData(null)),
            canceledInfo = BuildCancellation(null, BuildUser(null, null))
        )
        assertEquals(build, mapper.toLaunchData(mapper.toLegacyBuild(build)))
    }

    @Test(expected = IllegalArgumentException::class)
    fun missingIdIsRejected() {
        mapper.toLaunchData(legacyBuild("""{"href":"/builds/1"}"""))
    }

    @Test(expected = IllegalArgumentException::class)
    fun missingHrefIsRejected() {
        mapper.toLaunchData(legacyBuild("""{"id":"1"}"""))
    }

    @Test(expected = IllegalArgumentException::class)
    fun blankIdIsRejected() {
        mapper.toLaunchData(legacyBuild("""{"id":" ","href":"/builds/1"}"""))
    }

    @Test(expected = IllegalArgumentException::class)
    fun blankHrefIsRejected() {
        mapper.toLaunchData(legacyBuild("""{"id":"1","href":" "}"""))
    }
}
