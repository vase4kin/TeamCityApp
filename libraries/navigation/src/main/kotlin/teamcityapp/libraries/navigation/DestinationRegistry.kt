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

import androidx.navigation3.runtime.NavEntry
import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.PolymorphicSerializer
import kotlinx.serialization.json.Json
import kotlinx.serialization.modules.SerializersModule

/** One explicit serializer registry serves restoration and incoming compatibility requests. */
class DestinationRegistry(registrations: Set<DestinationRegistration>) {
    private val destinations = registrations.associateBy { it.keyType }
    init {
        require(destinations.size == registrations.size) { "Each destination must have exactly one registration" }
    }

    val serializers = SerializersModule { registrations.forEach { it.registerSerializers(this) } }
    private val json = Json { serializersModule = serializers }

    fun entry(visit: NavigationEntry, navigator: Navigator): NavEntry<NavigationEntry> = checkNotNull(destinations[visit.destination::class]) {
        "No entry registered for ${visit.destination::class}"
    }.entry(visit, navigator)

    fun encodeDestination(destination: NavKey): String = json.encodeToString(PolymorphicSerializer(NavKey::class), destination)
    fun decodeDestination(value: String): NavKey = json.decodeFromString(PolymorphicSerializer(NavKey::class), value)

    fun encodeSnapshot(snapshot: NavigationSnapshot): String = json.encodeToString(NavigationSnapshot.serializer(), snapshot)
    fun decodeSnapshot(value: String): NavigationSnapshot = json.decodeFromString(NavigationSnapshot.serializer(), value)
}
