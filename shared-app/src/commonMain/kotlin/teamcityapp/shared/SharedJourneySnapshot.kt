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

import androidx.compose.runtime.*
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import teamcityapp.features.home.api.HomeTab
import teamcityapp.libraries.navigation.*

@Serializable
internal data class SavedSharedHome(val selected: HomeTab = HomeTab.Projects, val activity: HomeTab = HomeTab.Running, val stacks: Map<String, String> = emptyMap())

@Serializable
internal data class SavedSharedJourney(val root: String, val homes: Map<String, SavedSharedHome> = emptyMap())
internal class SharedJourneyMemory {
    var homes by mutableStateOf(emptyMap<String, SavedSharedHome>())
}
internal val LocalSharedJourneyMemory = staticCompositionLocalOf<SharedJourneyMemory> { error("Native journey state is unavailable") }
internal val sharedJourneyJson = Json { ignoreUnknownKeys = true }
internal fun decodeSharedSnapshot(encoded: String?, registry: DestinationRegistry, sessionKey: String): Pair<NavigationSnapshot, Map<String, SavedSharedHome>>? = runCatching {
    require(encoded != null && encoded.length < 1_000_000)
    val envelope = sharedJourneyJson.decodeFromString<SavedSharedJourney>(encoded)
    val root = registry.decodeSnapshot(envelope.root.replace("\"teamcityapp.shared.NativeAboutDestination\"", "\"teamcityapp.features.about.api.navigation.AboutDestination\"").replace("\"teamcityapp.shared.AccountsDestination\"", "\"teamcityapp.features.drawer.api.navigation.DrawerDestination\""))
    require(root.sessionKey == sessionKey && root.entries.size <= 100)
    val homeIds = root.entries.filter { it.destination is teamcityapp.features.home.api.HomeDestination }.map { it.id }.toSet()
    require(envelope.homes.size <= homeIds.size && envelope.homes.keys.all { it in homeIds })
    root to envelope.homes
}.getOrNull()
