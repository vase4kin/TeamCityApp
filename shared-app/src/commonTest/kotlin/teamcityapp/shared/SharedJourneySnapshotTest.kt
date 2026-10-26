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
import teamcityapp.features.about.api.navigation.AboutDestination
import teamcityapp.features.home.api.*
import teamcityapp.libraries.navigation.*

class SharedJourneySnapshotTest {
    private val registry = DestinationRegistry(setOf(SharedEntry(HomeDestination::class, HomeDestination.serializer()) { _, _, _ -> }))
    private val root = NavigationSnapshot(listOf(NavigationEntry("home", HomeDestination())), sessionKey = "alice")

    @Test fun retainedHomeStacksAndActivityAreBoundToTheSavedAccount() {
        val saved = SavedSharedJourney(registry.encodeSnapshot(root), mapOf("home" to SavedSharedHome(HomeTab.Queue, HomeTab.Queue)))
        val encoded = sharedJourneyJson.encodeToString(saved)
        assertEquals(HomeTab.Queue, decodeSharedSnapshot(encoded, registry, "alice")?.second?.get("home")?.selected)
        assertNull(decodeSharedSnapshot(encoded, registry, "bob"))
    }

    @Test fun staleOwnersMalformedOversizedAndLegacyPayloadsResetSafely() {
        assertNull(decodeSharedSnapshot(sharedJourneyJson.encodeToString(SavedSharedJourney(registry.encodeSnapshot(root), mapOf("old-home" to SavedSharedHome()))), registry, "alice"))
        assertNull(decodeSharedSnapshot("{invalid", registry, "alice"))
        assertNull(decodeSharedSnapshot("x".repeat(1_000_001), registry, "alice"))
        assertNull(decodeSharedSnapshot(registry.encodeSnapshot(root), registry, "alice"))
    }

    @Test fun previousNativeAccountAndAboutKeysMigrateToTheSharedPublicDestinations() {
        val public = DestinationRegistry(
            setOf(
                SharedEntry(AccountsDestination::class, AccountsDestination.serializer()) { _, _, _ -> },
                SharedEntry(AboutDestination::class, AboutDestination.serializer()) { _, _, _ -> }
            )
        )
        val snapshot = NavigationSnapshot(listOf(NavigationEntry("account", AccountsDestination), NavigationEntry("about", AboutDestination)), sessionKey = "alice")
        val legacy = public.encodeSnapshot(snapshot)
            .replace("teamcityapp.features.drawer.api.navigation.DrawerDestination", "teamcityapp.shared.AccountsDestination")
            .replace("teamcityapp.features.about.api.navigation.AboutDestination", "teamcityapp.shared.NativeAboutDestination")
        val encoded = sharedJourneyJson.encodeToString(SavedSharedJourney(legacy))
        assertEquals(snapshot, decodeSharedSnapshot(encoded, public, "alice")?.first)
        assertNull(decodeSharedSnapshot(encoded, public, "bob"))
    }

    @Test fun installedLicenseDestinationIdentifiersRemainReadableAfterSharedFileRenames() {
        val public = DestinationRegistry(
            setOf(
                SharedEntry(NativeLicensesDestination::class, NativeLicensesDestination.serializer()) { _, _, _ -> },
                SharedEntry(NativeLicenseDestination::class, NativeLicenseDestination.serializer()) { _, _, _ -> }
            )
        )
        val snapshot = NavigationSnapshot(listOf(NavigationEntry("catalog", NativeLicensesDestination), NavigationEntry("reader", NativeLicenseDestination("kotlin"))), sessionKey = "alice")
        val encoded = public.encodeSnapshot(snapshot)
        assertTrue(encoded.contains("teamcityapp.shared.NativeLicenseDestination"))
        val envelope = sharedJourneyJson.encodeToString(SavedSharedJourney(encoded))
        assertEquals(snapshot, decodeSharedSnapshot(envelope, public, "alice")?.first)
    }
}
