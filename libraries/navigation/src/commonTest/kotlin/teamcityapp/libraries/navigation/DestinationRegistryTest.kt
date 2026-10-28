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

package teamcityapp.libraries.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.navigation3.runtime.NavKey
import kotlin.reflect.KClass
import kotlin.test.*
import kotlinx.serialization.KSerializer
import kotlinx.serialization.modules.SerializersModuleBuilder
import kotlinx.serialization.modules.polymorphic
import kotlinx.serialization.modules.subclass

internal open class EmptyDestination<T : NavKey>(type: KClass<T>, serializer: KSerializer<T>) : FeatureDestination<T>(type, serializer) {
    @Composable override fun Content(destination: T, navigator: Navigator, entryId: String) {}
}

internal fun testRegistry(): DestinationRegistry = DestinationRegistry(
    setOf(
        EmptyDestination(Root::class, Root.serializer()),
        object : EmptyDestination<Detail>(Detail::class, Detail.serializer()) {
            override fun registerSerializers(builder: SerializersModuleBuilder) {
                super.registerSerializers(builder)
                builder.polymorphic(NavigationResult::class) {
                    subclass(Selected::class, Selected.serializer())
                    subclass(Queued::class, Queued.serializer())
                }
            }
        }
    )
)

class DestinationRegistryTest {
    @Test fun featureKeysRoundTripWithoutAppKnowingTheirImplementation() {
        val registry = testRegistry()
        assertEquals(Root, registry.decodeDestination(registry.encodeDestination(Root)))
        assertEquals(Detail("same/server/project"), registry.decodeDestination(registry.encodeDestination(Detail("same/server/project"))))
    }

    @Test fun restorationPreservesRepeatedVisitsAndUnconsumedTypedResults() {
        val registry = testRegistry()
        val original = NavigationSnapshot(
            listOf(NavigationEntry("root", Root), NavigationEntry("first", Detail("build")), NavigationEntry("second", Detail("build"))),
            listOf(PendingNavigationResult("first", Selected("branch")), PendingNavigationResult("second", Queued("build/321"))),
            sessionKey = "account-one"
        )
        val restored = registry.decodeSnapshot(registry.encodeSnapshot(original))
        assertEquals(original, restored)
        val navigator = Navigator(mutableStateOf(restored))
        assertEquals(Selected("branch"), navigator.consumeResult("first", Selected::class))
        assertEquals(Queued("build/321"), navigator.consumeResult("second", Queued::class))
    }

    @Test fun entryContentIdentityUsesVisitIdRatherThanDestinationEquality() {
        val registry = testRegistry()
        val nav = Navigator(mutableStateOf(NavigationSnapshot(listOf(NavigationEntry("root", Root)))))
        val first = registry.entry(NavigationEntry("first", Detail("same")), nav)
        val second = registry.entry(NavigationEntry("second", Detail("same")), nav)
        assertEquals("first", first.contentKey)
        assertEquals("second", second.contentKey)
    }

    @Test
    fun duplicateRegistrationsAreRejected() {
        assertFailsWith<IllegalArgumentException> {
            DestinationRegistry(setOf(EmptyDestination(Root::class, Root.serializer()), EmptyDestination(Root::class, Root.serializer())))
        }
    }

    @Test
    fun missingDestinationFailsAtRegistrationBoundary() {
        assertFailsWith<IllegalStateException> {
            val registry = DestinationRegistry(emptySet())
            val visit = NavigationEntry("root", Root)
            registry.entry(visit, Navigator(mutableStateOf(NavigationSnapshot(listOf(visit)))))
        }
    }
}
