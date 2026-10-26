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

package teamcityapp.shared

import androidx.compose.runtime.mutableStateOf
import kotlin.test.*
import teamcityapp.features.build_details.api.*
import teamcityapp.features.build_history.api.navigation.BuildHistoryDestination
import teamcityapp.libraries.builds.*
import teamcityapp.libraries.navigation.*

class SharedBuildNavigationTest {
    @Test fun historyOpensOneCompleteVisitAndBackReturnsToTheSameHistoryOwner() {
        val history = NavigationEntry("history", BuildHistoryDestination("config", "Build Android", "branch:main"))
        val state = mutableStateOf(NavigationSnapshot(listOf(history), sessionKey = "alice"))
        var newOwners = 0
        val navigator = Navigator(state, nextId = { "build-${++newOwners}" })
        val build = BuildLaunchData(
            "42", "/context/app/rest/builds/id:42", state = "finished", status = "FAILURE",
            configuration = BuildConfigurationData("config", "Build Android"),
            changes = BuildCollectionLink("/changes", 3), tests = BuildTests("/tests", failed = 1),
            properties = BuildProperties(listOf(BuildProperty("key", "value"))),
            artifacts = BuildCollectionLink("/artifacts"), snapshotDependencies = BuildCollectionLink("/snapshots")
        )

        navigator.openHistoryBuild(history.id, build, "Build Android")
        // A delayed callback from the outgoing History entry must not add another visit.
        navigator.openHistoryBuild(history.id, build, "Build Android")

        assertEquals(1, newOwners)
        assertEquals(2, state.value.entries.size)
        assertEquals(history, state.value.entries.first())
        assertEquals(BuildDetailsDestination(build, "Build Android"), navigator.current.destination)
        assertTrue(navigator.pop())
        assertEquals(history, navigator.current)
        assertFalse(navigator.pop())
        navigator.openHistoryBuild(history.id, build, "Build Android")
        assertEquals(2, newOwners)
        assertTrue(navigator.pop())
        assertEquals(history, navigator.current)
    }

    @Test fun queuedShowReferencesRemainSeparateFromCompleteHistorySnapshots() {
        val state = mutableStateOf(NavigationSnapshot(listOf(NavigationEntry("history", BuildHistoryDestination("config", "Build Android")))))
        val navigator = Navigator(state)
        val reference = BuildDetailsReferenceDestination("/context/app/rest/buildQueue/id:7", "Build Android")
        navigator.push(reference)
        assertEquals(reference, navigator.current.destination)
        navigator.replace(BuildDetailsDestination(BuildLaunchData("7", reference.href, state = "queued"), reference.configurationName))
        assertEquals(2, navigator.snapshot.entries.size)
        assertTrue(navigator.pop())
        assertIs<BuildHistoryDestination>(navigator.current.destination)
    }

    @Test fun refreshedSectionsUseOneCompleteSnapshotAndKeepTheExactOrigin() {
        val incoming = BuildLaunchData("42", "/context/app/rest/builds/id:42", state = "running")
        val fresh = incoming.copy(state = "finished", changes = BuildCollectionLink("/changes", 2), tests = BuildTests("/tests", failed = 1), artifacts = BuildCollectionLink("/artifacts"), properties = BuildProperties(listOf(BuildProperty("fresh", "value"))))
        val origin = NavigationEntry("build-visit", BuildDetailsDestination(incoming, "Build"))
        val navigator = Navigator(mutableStateOf(NavigationSnapshot(listOf(origin), sessionKey = "alice")))
        for (section in listOf(BuildDetailsSection.Changes, BuildDetailsSection.Tests, BuildDetailsSection.Parameters, BuildDetailsSection.Artifacts)) {
            val destination = assertNotNull(buildSectionDestination(incoming.id, fresh, section, "Build", origin.id))
            navigator.openBuildSection(origin.id, incoming.id, fresh, section, "Build")
            navigator.openBuildSection(origin.id, incoming.id, fresh, section, "Build")
            assertEquals(2, navigator.snapshot.entries.size)
            assertEquals(destination, navigator.current.destination)
            when (destination) {
                is BuildDetailsSectionDestination -> {
                    assertEquals(fresh, destination.build)
                    assertEquals(origin.id, destination.originEntryId)
                    assertTrue(navigator.finish(BuildDetailsSectionCountResult(fresh.id, section, 2), origin.id))
                    assertNotNull(navigator.consumeResult(origin.id, BuildDetailsSectionCountResult::class))
                    assertNull(navigator.consumeResult(origin.id, BuildDetailsSectionCountResult::class))
                }

                is teamcityapp.features.artifacts.api.navigation.ArtifactsDestination -> {
                    assertEquals(fresh, destination.build)
                    assertEquals("/artifacts", destination.href)
                    assertTrue(navigator.pop())
                }

                else -> fail("Unexpected section destination")
            }
            assertEquals(origin, navigator.current)
        }
    }

    @Test fun absentOrWrongBuildCannotLaunchARefreshedSection() {
        val incoming = BuildLaunchData("42", "/build/42", state = "running")
        assertNull(buildSectionDestination(incoming.id, null, BuildDetailsSection.Tests, null, "origin"))
        assertNull(buildSectionDestination(incoming.id, incoming, BuildDetailsSection.Tests, null, "origin"))
        assertNull(buildSectionDestination("other", incoming, BuildDetailsSection.Parameters, null, "origin"))
    }
}
