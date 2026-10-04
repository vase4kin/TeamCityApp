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

package teamcityapp.features.properties.impl

import androidx.lifecycle.SavedStateHandle
import org.junit.Assert.*
import org.junit.Test
import teamcityapp.features.properties.api.Property

class PropertiesViewModelTest {
    @Test fun absentAndEmptyArgumentsShowEmptyState() {
        assertEquals(PropertiesUiState.Empty, PropertiesViewModel(SavedStateHandle()).state.value)
        assertEquals(PropertiesUiState.Empty, model(emptyList()).state.value)
    }

    @Test fun keepsOrderDuplicateNamesAndEmptyValues() {
        val properties = listOf(Property("sdk", "24"), Property("sdk", ""), Property("secret", "•••"))
        assertEquals(PropertiesUiState.Content(properties), model(properties).state.value)
    }

    @Test fun restoringSavedArgumentsKeepsContent() {
        val properties = listOf(Property("env.CI", "true"))
        val saved = mapOf(
            PropertiesViewModel.ARG_NAMES to arrayListOf("env.CI"),
            PropertiesViewModel.ARG_VALUES to arrayListOf("true"))
        assertEquals(PropertiesUiState.Content(properties), PropertiesViewModel(SavedStateHandle(saved)).state.value)
    }

    @Test fun ownsSnapshotRatherThanMutableArgumentLists() {
        val names = arrayListOf("sdk")
        val values = arrayListOf("24")
        val model = PropertiesViewModel(SavedStateHandle(mapOf(
            PropertiesViewModel.ARG_NAMES to names, PropertiesViewModel.ARG_VALUES to values)))
        names.clear()
        values[0] = "changed"
        assertEquals(PropertiesUiState.Content(listOf(Property("sdk", "24"))), model.state.value)
    }

    private fun model(properties: List<Property>) = PropertiesViewModel(SavedStateHandle(mapOf(
        PropertiesViewModel.ARG_NAMES to ArrayList(properties.map { it.name }),
        PropertiesViewModel.ARG_VALUES to ArrayList(properties.map { it.value }))))
}
