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

    @Test fun finishedSnapshotCreatesAllSevenTabsInInstalledOrder() {
        assertEquals(BuildDetailsTab.entries, base.copy(state = "finished").detailsTabs())
    }

    @Test fun runningAndQueuedSnapshotsExcludeUnavailableLogAndArtifactTabs() {
        assertEquals(BuildDetailsTab.entries - BuildDetailsTab.Artifacts, base.copy(state = "running").detailsTabs())
        assertEquals(BuildDetailsTab.entries - setOf(BuildDetailsTab.BuildLog, BuildDetailsTab.Artifacts), base.copy(state = "queued").detailsTabs())
    }

    @Test fun absentUrlsOmitChangesTestsAndArtifactsButSnapshotWrapperPresenceRemainsMeaningful() {
        val snapshot = base.copy(changes = BuildCollectionLink(null), tests = BuildTests(null), artifacts = BuildCollectionLink(null))
        assertEquals(listOf(BuildDetailsTab.Overview, BuildDetailsTab.BuildLog, BuildDetailsTab.Parameters, BuildDetailsTab.Snapshots), snapshot.detailsTabs())
        assertFalse(snapshot.copy(snapshotDependencies = null).detailsTabs().contains(BuildDetailsTab.Snapshots))
    }

    @Test fun destinationSerializationPreservesCompleteSnapshotAndFallbackName() {
        val destination = BuildDetailsDestination(base.copy(branchName = "feature:opaque,branch", tests = BuildTests("/tests", 7, 8, 9, 24), properties = BuildProperties(null, "wrapper", "/properties")), "Fallback", completion = BuildDetailsCompletion.Stopped)
        assertEquals(destination, Json.decodeFromString<BuildDetailsDestination>(Json.encodeToString(destination)))
    }
}
