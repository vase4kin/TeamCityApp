/*
 * Copyright 2020 Andrey Tolpeev
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

package teamcityapp.features.properties.impl

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import teamcityapp.features.properties.api.Property
import teamcityapp.features.properties.impl.resources.*
import teamcityapp.libraries.design_system.SharedUiText

sealed interface PropertiesUiState {
    data object Empty : PropertiesUiState
    data class Content(val properties: List<Property>) : PropertiesUiState {
        val rows: List<PropertyRowUiState> = properties.map(::PropertyRowUiState)
    }
}

/** Only expansion/overflow state belongs to the row composable. */
data class PropertyRowUiState(val property: Property) {
    val valueText: SharedUiText = if (property.value.isEmpty()) SharedUiText.Resource(Res.string.text_property_value_empty) else SharedUiText.Dynamic(property.value)
}

open class PropertiesViewModel constructor(savedState: SavedStateHandle) : ViewModel() {
    // Arguments survive process recreation. No network work or UI callbacks belong here.
    private val properties = savedState.get<ArrayList<String>>(ARG_NAMES).orEmpty()
        .zip(savedState.get<ArrayList<String>>(ARG_VALUES).orEmpty()) { name, value -> Property(name, value) }
    val state = MutableStateFlow<PropertiesUiState>(
        if (properties.isEmpty()) PropertiesUiState.Empty else PropertiesUiState.Content(properties)
    ).asStateFlow()

    companion object {
        const val ARG_NAMES = "property_names"
        const val ARG_VALUES = "property_values"
    }
}
