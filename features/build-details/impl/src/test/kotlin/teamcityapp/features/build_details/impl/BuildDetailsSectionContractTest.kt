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

import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.KSerializer
import kotlinx.serialization.modules.*
import org.junit.Assert.*
import org.junit.Test
import teamcityapp.features.artifacts.api.navigation.ArtifactsDestination
import teamcityapp.features.bottom_sheet.api.*
import teamcityapp.features.bottom_sheet.api.navigation.*
import teamcityapp.features.build_details.api.*
import teamcityapp.features.build_overview.api.navigation.BuildOverviewDestination
import teamcityapp.features.changes.api.navigation.ChangesDestination
import teamcityapp.features.properties.api.Property
import teamcityapp.features.properties.api.navigation.PropertiesDestination
import teamcityapp.features.snapshot_dependencies.api.navigation.SnapshotDependenciesDestination
import teamcityapp.features.tests.api.navigation.TestsDestination
import teamcityapp.libraries.builds.*
import teamcityapp.libraries.navigation.*

class BuildDetailsSectionContractTest {
    private val build = BuildLaunchData("42", "/opaque/build:42&fields=all", state = "finished", tests = BuildTests("/opaque/tests", 10, 20, 30, 60), properties = BuildProperties(null), snapshotDependencies = BuildCollectionLink(null))
    private val registry = DestinationRegistry(
        setOf(
            entry(BuildDetailsDestination::class, BuildDetailsDestination.serializer()),
            entry(BuildDetailsSectionDestination::class, BuildDetailsSectionDestination.serializer()),
            entry(BuildOverviewDestination::class, BuildOverviewDestination.serializer()),
            entry(ChangesDestination::class, ChangesDestination.serializer()),
            entry(TestsDestination::class, TestsDestination.serializer()),
            entry(PropertiesDestination::class, PropertiesDestination.serializer()),
            entry(ArtifactsDestination::class, ArtifactsDestination.serializer()),
            entry(SnapshotDependenciesDestination::class, SnapshotDependenciesDestination.serializer()),
            entry(ActionSheetDestination::class, ActionSheetDestination.serializer())
        )
    )
    private fun <T : NavKey> entry(type: kotlin.reflect.KClass<T>, serializer: KSerializer<T>) = object : FeatureDestination<T>(type, serializer) {
        override fun registerSerializers(builder: SerializersModuleBuilder) {
            super.registerSerializers(builder)
            if (type == ActionSheetDestination::class) builder.polymorphic(NavigationResult::class) { subclass(ActionSheetResult::class, ActionSheetResult.serializer()) }
        }

        @Composable override fun Content(destination: T, navigator: Navigator, entryId: String) {}
    }
    private fun navigator(): Navigator {
        var next = 0
        return Navigator(mutableStateOf(NavigationSnapshot(listOf(NavigationEntry("parent", BuildDetailsDestination(build)))))) { "entry-${++next}" }
    }

    @Test fun sectionContentKeysPreserveTheirCompleteArgumentsOnRestoration() {
        val keys = listOf(BuildOverviewDestination(build), ChangesDestination("/changes?opaque"), TestsDestination("/tests?opaque", 10, 20, 30), PropertiesDestination(listOf(Property("empty", ""), Property("secret.name", "literal:\$value"))), ArtifactsDestination(build, "/artifacts!/archive", "Archive", false), SnapshotDependenciesDestination("42", "Configuration"))
        keys.forEach { assertEquals(it, registry.decodeDestination(registry.encodeDestination(it))) }
    }

    @Test fun sectionsAreSeparateVisitsAndKeepTheirCompleteSnapshotOnRestoration() {
        val incoming = build.copy(changes = BuildCollectionLink("/changes"), artifacts = BuildCollectionLink("/artifacts"))
        incoming.detailsSections().forEach { section ->
            val destination = BuildDetailsSectionDestination(incoming, section, "Build configuration", "latest-number")
            assertEquals(destination, registry.decodeDestination(registry.encodeDestination(destination)))
        }
        val nav = navigator()
        val first = nav.push(BuildDetailsSectionDestination(incoming, BuildDetailsSection.Tests))
        val second = nav.push(BuildDetailsSectionDestination(incoming, BuildDetailsSection.Tests))
        assertNotEquals(first, second)
        nav.pop()
        assertEquals(first, nav.current.id)
        nav.pop()
        assertEquals("parent", nav.current.id)
    }

    @Test fun actionSheetResultReturnsToExactRetainedChildAfterRestoration() {
        val nav = navigator()
        val child = "parent/Overview"
        nav.push(ActionSheetDestination("Branch", listOf("refs/heads/opaque:branch"), SheetMenuType.Branch, "parent", child))
        nav.finish(ActionSheetResult(child, SheetItem(SheetAction.Branch, "refs/heads/opaque:branch")), "parent")
        val restored = Navigator(mutableStateOf(registry.decodeSnapshot(registry.encodeSnapshot(nav.snapshot))))
        assertNull(restored.consumeResult("parent", ActionSheetResult::class) { it.consumerId == "parent/Artifacts" })
        assertEquals(ActionSheetResult(child, SheetItem(SheetAction.Branch, "refs/heads/opaque:branch")), restored.consumeResult("parent", ActionSheetResult::class) { it.consumerId == child })
        assertNull(restored.consumeResult("parent", ActionSheetResult::class))
    }

    @Test fun equalArtifactFoldersHaveIndependentResultRecipientsAndPopDropsRemovedResults() {
        val nav = navigator()
        val folder = ArtifactsDestination(build, "/same-folder", "Folder")
        val first = nav.push(folder)
        val second = nav.push(folder)
        nav.push(ActionSheetDestination("File", listOf("/content/file.zip"), SheetMenuType.ArtifactDefault, first, first))
        nav.finish(ActionSheetResult(first, SheetItem(SheetAction.ArtifactDownload, "/content/file.zip")), first)
        assertNull(nav.consumeResult(second, ActionSheetResult::class))
        assertTrue(nav.pop())
        assertEquals(first, nav.current.id)
        assertEquals("/content/file.zip", nav.consumeResult(first, ActionSheetResult::class)?.item?.description)
        nav.push(ActionSheetDestination("File", listOf("/content/file.zip"), SheetMenuType.ArtifactDefault, first, first))
        nav.finish(ActionSheetResult(first, SheetItem(SheetAction.ArtifactDownload, "/content/file.zip")), first)
        nav.pop()
        assertTrue(nav.snapshot.results.isEmpty())
    }
}
