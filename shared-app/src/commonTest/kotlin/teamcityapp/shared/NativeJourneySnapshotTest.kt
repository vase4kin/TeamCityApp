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

import kotlin.test.*
import teamcityapp.features.home.api.*
import teamcityapp.libraries.navigation.*

class NativeJourneySnapshotTest {
    private val registry = DestinationRegistry(setOf(SharedEntry(HomeDestination::class, HomeDestination.serializer()) { _, _, _ -> }))
    private val root = NavigationSnapshot(listOf(NavigationEntry("home", HomeDestination())), sessionKey = "alice")

    @Test fun retainedHomeStacksAndActivityAreBoundToTheSavedAccount() {
        val saved = SavedNativeJourney(registry.encodeSnapshot(root), mapOf("home" to SavedNativeHome(HomeTab.Queue, HomeTab.Queue)))
        val encoded = nativeJourneyJson.encodeToString(saved)
        assertEquals(HomeTab.Queue, decodeNativeSnapshot(encoded, registry, "alice")?.second?.get("home")?.selected)
        assertNull(decodeNativeSnapshot(encoded, registry, "bob"))
    }

    @Test fun staleOwnersMalformedOversizedAndLegacyPayloadsResetSafely() {
        assertNull(decodeNativeSnapshot(nativeJourneyJson.encodeToString(SavedNativeJourney(registry.encodeSnapshot(root), mapOf("old-home" to SavedNativeHome()))), registry, "alice"))
        assertNull(decodeNativeSnapshot("{invalid", registry, "alice"))
        assertNull(decodeNativeSnapshot("x".repeat(1_000_001), registry, "alice"))
        assertNull(decodeNativeSnapshot(registry.encodeSnapshot(root), registry, "alice"))
    }
}
