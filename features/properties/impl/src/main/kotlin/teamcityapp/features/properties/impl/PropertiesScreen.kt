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

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import teamcityapp.features.properties.api.Property
import teamcityapp.libraries.theme.TeamCityDimensions
import teamcityapp.libraries.theme.TeamCityTheme
import teamcityapp.libraries.theme.R as ThemeR

@Composable
fun PropertiesScreen(state: PropertiesUiState, onCopy: (Property) -> Unit, modifier: Modifier = Modifier) {
    Surface(modifier = modifier.fillMaxSize()) {
        when (state) {
            PropertiesUiState.Empty -> Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center,
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        painter = painterResource(ThemeR.drawable.ic_format_list_bulleted_black_24dp),
                        contentDescription = null,
                        modifier = Modifier.size(TeamCityDimensions.emptyStateIconSize),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Text(
                        text = stringResource(R.string.empty_list_message_parameters),
                        modifier = Modifier.padding(TeamCityDimensions.contentPadding),
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        style = MaterialTheme.typography.bodyLarge,
                    )
                }
            }
            is PropertiesUiState.Content -> LazyColumn(Modifier.fillMaxSize().testTag("properties:list")) {
                // Duplicate parameter names are valid; preserve their order and identities by index.
                itemsIndexed(state.properties) { index, property ->
                    PropertyRow(property, { onCopy(property) }, Modifier.testTag("properties:row:$index"))
                }
            }
        }
    }
}

@Composable
private fun PropertyRow(property: Property, onCopy: () -> Unit, modifier: Modifier = Modifier) {
    val empty = property.value.isEmpty()
    Column(
        modifier.fillMaxWidth().then(
            if (empty) Modifier else Modifier.clickable(role = Role.Button, onClick = onCopy)
        )
    ) {
        Column(Modifier.padding(TeamCityDimensions.contentPadding)) {
            Text(
                text = property.name,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                style = MaterialTheme.typography.bodyMedium,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                text = if (empty) stringResource(R.string.text_property_value_empty) else property.value,
                color = if (empty) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onSurface,
                style = MaterialTheme.typography.bodyLarge,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
        }
        HorizontalDivider()
    }
}

@Preview
@Composable
private fun PropertiesPreview() {
    TeamCityTheme {
        PropertiesScreen(PropertiesUiState.Content(listOf(Property("env.CI", "true"), Property("sdk", ""))), {})
    }
}

@Preview
@Composable
private fun EmptyPropertiesPreview() {
    TeamCityTheme { PropertiesScreen(PropertiesUiState.Empty, {}) }
}
