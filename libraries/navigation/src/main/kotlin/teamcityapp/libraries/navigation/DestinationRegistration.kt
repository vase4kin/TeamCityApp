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
import androidx.navigation3.runtime.NavEntry
import androidx.navigation3.runtime.NavKey
import kotlin.reflect.KClass
import kotlinx.serialization.KSerializer
import kotlinx.serialization.modules.SerializersModuleBuilder
import kotlinx.serialization.modules.polymorphic
import kotlinx.serialization.modules.subclass

/** Implementations contribute entries; the app combines them without knowing their UI. */
interface DestinationRegistration {
    val keyType: KClass<out NavKey>
    fun registerSerializers(builder: SerializersModuleBuilder)
    fun entry(visit: NavigationEntry, navigator: Navigator): NavEntry<NavigationEntry>
}

abstract class FeatureDestination<T : NavKey>(
    final override val keyType: KClass<T>,
    private val serializer: KSerializer<T>
) : DestinationRegistration {
    protected open val presentation: DestinationPresentation = DestinationPresentation.Screen

    override fun registerSerializers(builder: SerializersModuleBuilder) {
        builder.polymorphic(NavKey::class) { subclass(keyType, serializer) }
    }

    final override fun entry(visit: NavigationEntry, navigator: Navigator): NavEntry<NavigationEntry> = NavEntry(visit, contentKey = visit.id, metadata = presentation.metadata) {
        NavigationContentLifecycle { Content(keyType.java.cast(visit.destination), navigator, visit.id) }
    }

    @Composable
    protected abstract fun Content(destination: T, navigator: Navigator, entryId: String)
}
