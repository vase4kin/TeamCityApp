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

package teamcityapp.features.build_details.impl

import kotlinx.serialization.json.Json
import org.junit.Assert.*
import org.junit.Test
import teamcityapp.features.build_details.api.*
import teamcityapp.libraries.builds.*

class BuildDetailsDestinationTest {
    private val base = BuildLaunchData("1", "/builds/1", changes = BuildCollectionLink("/changes"), tests = BuildTests("/tests"), artifacts = BuildCollectionLink("/artifacts"), snapshotDependencies = BuildCollectionLink(null))

    @Test fun finishedSnapshotCreatesFiveSectionsInInstalledOrder() {
        assertEquals(BuildDetailsSection.entries, base.copy(state = "finished").detailsSections())
    }

    @Test fun runningAndQueuedSnapshotsExcludeUnavailableArtifacts() {
        assertEquals(BuildDetailsSection.entries - BuildDetailsSection.Artifacts, base.copy(state = "running").detailsSections())
        assertEquals(BuildDetailsSection.entries - setOf(BuildDetailsSection.Artifacts), base.copy(state = "queued").detailsSections())
    }

    @Test fun absentUrlsOmitChangesTestsAndArtifactsButSnapshotWrapperPresenceRemainsMeaningful() {
        val snapshot = base.copy(changes = BuildCollectionLink(null), tests = BuildTests(null), artifacts = BuildCollectionLink(null))
        assertEquals(listOf(BuildDetailsSection.Parameters, BuildDetailsSection.Snapshots), snapshot.detailsSections())
        assertFalse(snapshot.copy(snapshotDependencies = null).detailsSections().contains(BuildDetailsSection.Snapshots))
    }

    @Test fun destinationSerializationPreservesCompleteSnapshotAndFallbackName() {
        val destination = BuildDetailsDestination(base.copy(branchName = "feature:opaque,branch", tests = BuildTests("/tests", 7, 8, 9, 24), properties = BuildProperties(null, "wrapper", "/properties")), "Fallback", completion = BuildDetailsCompletion.Stopped)
        assertEquals(destination, Json.decodeFromString<BuildDetailsDestination>(Json.encodeToString(destination)))
    }
}
