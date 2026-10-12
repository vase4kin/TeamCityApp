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

package teamcityapp.features.build_history.impl.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.modules.*
import org.junit.Assert.*
import org.junit.Test
import teamcityapp.features.build_history.api.navigation.BuildHistoryDestination
import teamcityapp.features.filter_builds.api.*
import teamcityapp.features.filter_builds.api.navigation.*
import teamcityapp.features.run_build.api.navigation.*
import teamcityapp.libraries.navigation.*

class BuildWorkflowNavigationTest {
    private val registry = DestinationRegistry(
        setOf(
            emptyDestination(BuildHistoryDestination::class, BuildHistoryDestination.serializer()),
            emptyDestination(RunBuildDestination::class, RunBuildDestination.serializer()),
            emptyDestination(FilterBuildsDestination::class, FilterBuildsDestination.serializer())
        )
    )
    private fun <T : NavKey> emptyDestination(type: kotlin.reflect.KClass<T>, serializer: kotlinx.serialization.KSerializer<T>) = object : FeatureDestination<T>(type, serializer) {
        override fun registerSerializers(builder: SerializersModuleBuilder) {
            super.registerSerializers(builder)
            if (type == RunBuildDestination::class) builder.polymorphic(NavigationResult::class) { subclass(QueuedBuildResult::class, QueuedBuildResult.serializer()) }
            if (type == FilterBuildsDestination::class) builder.polymorphic(NavigationResult::class) { subclass(BuildFilterResult::class, BuildFilterResult.serializer()) }
        }

        @Composable override fun Content(destination: T, navigator: Navigator, entryId: String) {}
    }
    private fun navigator(): Navigator {
        var next = 0
        return Navigator(mutableStateOf(NavigationSnapshot(listOf(NavigationEntry("first-history", BuildHistoryDestination("id", "Android"))))), nextId = { "visit-${++next}" })
    }

    @Test fun equalHistoryVisitsReceiveOnlyTheirOwnTypedResultsAndRestoreThemOnce() {
        val nav = navigator()
        val firstId = nav.current.id
        val secondId = nav.push(BuildHistoryDestination("id", "Android"))
        nav.push(RunBuildDestination("id", firstId))
        assertTrue(nav.finish(QueuedBuildResult("/queue/42"), firstId))
        assertNull(nav.consumeResult(secondId, QueuedBuildResult::class))
        val restored = Navigator(mutableStateOf(registry.decodeSnapshot(registry.encodeSnapshot(nav.snapshot))))
        assertEquals(QueuedBuildResult("/queue/42"), restored.consumeResult(firstId, QueuedBuildResult::class))
        assertNull(restored.consumeResult(firstId, QueuedBuildResult::class))
        assertEquals(secondId, restored.current.id)
    }

    @Test fun everyFilterSelectionAndCompletePayloadSurvivesResultRestoration() {
        BuildStatusFilter.entries.forEach { status ->
            val nav = navigator()
            val id = nav.current.id
            nav.push(FilterBuildsDestination("id", id))
            val filter = BuildFilter(status, "refs/heads/feature/a,b:(x)", personal = true, pinned = true)
            assertTrue(nav.finish(BuildFilterResult(filter), id))
            val restored = Navigator(mutableStateOf(registry.decodeSnapshot(registry.encodeSnapshot(nav.snapshot))))
            assertEquals(BuildFilterResult(filter), restored.consumeResult(id, BuildFilterResult::class))
            assertNull(restored.consumeResult(id, BuildFilterResult::class))
        }
    }

    @Test fun cancelledFormsDoNotSendResultsAndAccountResetDiscardsOldResults() {
        val nav = navigator()
        val id = nav.current.id
        nav.push(FilterBuildsDestination("id", id))
        assertTrue(nav.pop())
        assertTrue(nav.snapshot.results.isEmpty())
        nav.push(RunBuildDestination("id", id))
        assertTrue(nav.finish(QueuedBuildResult("queued"), id))
        nav.reset(BuildHistoryDestination("other", "New account"), "different-user-on-same-server")
        assertNull(nav.consumeResult(id, QueuedBuildResult::class))
        assertTrue(nav.snapshot.results.isEmpty())
    }

    @Test fun aResultCannotBeDeliveredToARemovedVisit() {
        val nav = navigator()
        val oldId = nav.push(BuildHistoryDestination("old", "Removed"))
        nav.pop()
        nav.push(RunBuildDestination("id", oldId))
        assertFalse(nav.finish(QueuedBuildResult("queued"), oldId))
        assertTrue(nav.current.destination is RunBuildDestination)
        assertTrue(nav.snapshot.results.isEmpty())
    }
}
