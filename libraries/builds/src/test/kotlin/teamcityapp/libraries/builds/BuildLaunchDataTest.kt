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

package teamcityapp.libraries.builds

import org.junit.Assert.*
import org.junit.Test

class BuildLaunchDataTest {
    @Test fun unknownServerStateStatusAndDatesRemainLossless() {
        val build = BuildLaunchData("1", "/builds/1", state = "future-state", status = "future-status", startDate = "20201010T012345+0700")
        assertEquals("future-state", build.state)
        assertEquals("future-status", build.status)
        assertEquals("20201010T012345+0700", build.startDate)
        assertFalse(build.isFinished)
        assertFalse(build.isRunning)
        assertFalse(build.isQueued)
    }

    @Test fun optionalSectionsRetainPresenceEvenWithNoUrlOrItems() {
        val absent = BuildLaunchData("1", "/builds/1")
        val present = absent.copy(changes = BuildCollectionLink(null), artifacts = BuildCollectionLink(null), tests = BuildTests(null), properties = BuildProperties(emptyList()), snapshotDependencies = BuildCollectionLink(null, 0))
        assertNull(absent.snapshotDependencies)
        assertNotNull(present.snapshotDependencies)
        assertNotNull(present.tests)
        assertNotNull(present.changes)
        assertNotNull(present.artifacts)
        assertEquals(emptyList<BuildProperty>(), present.properties?.items)
        assertNull(absent.properties)
    }

    @Test fun propertiesPreserveWrapperAndRestartMetadata() {
        val absent = BuildLaunchData("1", "/builds/1")
        val nullItems = absent.copy(properties = BuildProperties(null, "properties", "/properties"))
        val emptyItems = nullItems.copy(properties = nullItems.properties?.copy(items = emptyList()))
        val items = listOf(BuildProperty(null, null, true, "property", "/property"))
        val populated = nullItems.copy(properties = nullItems.properties?.copy(items = items))
        assertNull(absent.properties)
        assertNotNull(nullItems.properties)
        assertNull(nullItems.properties?.items)
        assertEquals(emptyList<BuildProperty>(), emptyItems.properties?.items)
        assertEquals(BuildProperties(items, "properties", "/properties"), populated.properties)
    }

    @Test fun collectionReferencesPreserveCountsAndContinuationUrls() {
        val build = BuildLaunchData("1", "/builds/1", changes = BuildCollectionLink("/changes", 5, "/next-changes"), tests = BuildTests("/tests", 1, 2, 3, 6, "/next-tests"))
        assertEquals("/next-changes", build.changes?.nextHref)
        assertEquals(5, build.changes?.count)
        assertEquals(6, build.tests?.count)
        assertEquals("/next-tests", build.tests?.nextHref)
    }

    @Test fun topLevelAndNestedConfigurationIdsAreDistinct() {
        val build = BuildLaunchData("1", "/builds/1", buildTypeId = "summary", configuration = BuildConfigurationData("detail", projectName = null))
        assertEquals("summary", build.buildTypeId)
        assertEquals("detail", build.configuration?.id)
        assertNull(build.configuration?.projectName)
    }

    @Test(expected = IllegalArgumentException::class)
    fun missingBuildIdCannotBeLaunched() {
        BuildLaunchData("", "/builds/1")
    }

    @Test(expected = IllegalArgumentException::class)
    fun missingDetailUrlCannotBeLaunched() {
        BuildLaunchData("1", " ")
    }
}
