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
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import teamcityapp.features.properties.api.Property
import teamcityapp.libraries.theme.R as ThemeR
import teamcityapp.libraries.theme.TeamCityDimensions
import teamcityapp.libraries.theme.TeamCityTheme

@Composable
fun PropertiesScreen(state: PropertiesUiState, onCopy: (Property) -> Unit, modifier: Modifier = Modifier) {
    Surface(modifier = modifier.fillMaxSize()) {
        when (state) {
            PropertiesUiState.Empty -> Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        painter = painterResource(ThemeR.drawable.ic_format_list_bulleted_black_24dp),
                        contentDescription = null,
                        modifier = Modifier.size(TeamCityDimensions.emptyStateIconSize),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = stringResource(R.string.empty_list_message_parameters),
                        modifier = Modifier.padding(TeamCityDimensions.contentPadding),
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
            }

            is PropertiesUiState.Content -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.TopCenter) {
                LazyColumn(Modifier.widthIn(max = TeamCityDimensions.screenContentMaxWidth).fillMaxSize().testTag("properties:list"), contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(3.dp)) {
                    // Duplicate parameter names are valid; preserve their order and identities by index.
                    itemsIndexed(state.properties) { index, property ->
                        PropertyRow(property, { onCopy(property) }, index, state.properties.lastIndex)
                    }
                }
            }
        }
    }
}

@Composable
private fun PropertyRow(property: Property, onCopy: () -> Unit, index: Int, lastIndex: Int) {
    val empty = property.value.isEmpty()
    var expanded by rememberSaveable(property.name, property.value) { mutableStateOf(false) }
    var overflows by remember(property.value) { mutableStateOf(false) }
    val copyLabel = stringResource(R.string.copy_property, property.name)
    val expandLabel = stringResource(if (expanded) R.string.hide_full_property_value else R.string.show_full_property_value)
    val expandDescription = stringResource(if (expanded) R.string.hide_full_property_value_description else R.string.show_full_property_value_description, property.name)
    val shape = RoundedCornerShape(
        topStart = if (index == 0) MaterialTheme.shapes.medium.topStart else MaterialTheme.shapes.extraSmall.topStart,
        topEnd = if (index == 0) MaterialTheme.shapes.medium.topEnd else MaterialTheme.shapes.extraSmall.topEnd,
        bottomStart = if (index == lastIndex) MaterialTheme.shapes.medium.bottomStart else MaterialTheme.shapes.extraSmall.bottomStart,
        bottomEnd = if (index == lastIndex) MaterialTheme.shapes.medium.bottomEnd else MaterialTheme.shapes.extraSmall.bottomEnd
    )
    Surface(shape = shape, color = MaterialTheme.colorScheme.surfaceContainer) {
        Column {
            Row(
                Modifier.fillMaxWidth().testTag("properties:row:$index").then(
                    if (empty) Modifier else Modifier.clickable(role = Role.Button, onClickLabel = copyLabel, onClick = onCopy)
                ).padding(horizontal = 16.dp, vertical = 18.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(property.name, color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodyMedium)
                    if (!expanded) {
                        SelectionContainer {
                            Text(
                                text = if (empty) stringResource(R.string.text_property_value_empty) else property.value,
                                color = if (empty) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onSurface,
                                style = MaterialTheme.typography.bodyLarge,
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis,
                                onTextLayout = { overflows = it.hasVisualOverflow }
                            )
                        }
                    }
                }
                if (!empty) {
                    Icon(
                        painter = painterResource(R.drawable.ic_content_copy_black_24dp),
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
            if (expanded || overflows) {
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                Row(
                    Modifier.fillMaxWidth().heightIn(min = TeamCityDimensions.minimumTouchTarget).testTag("properties:expand:$index")
                        .clickable(role = Role.Button, onClickLabel = expandDescription) { expanded = !expanded }
                        .semantics {
                            contentDescription = expandDescription
                            stateDescription = expandLabel
                        }.padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(expandLabel, color = MaterialTheme.colorScheme.primary, style = MaterialTheme.typography.labelLarge)
                    Icon(painterResource(if (expanded) R.drawable.ic_expand_less_24dp else R.drawable.ic_expand_more_24dp), null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
                }
            }
            if (expanded) {
                SelectionContainer {
                    Text(
                        property.value,
                        Modifier.fillMaxWidth().testTag("properties:full:$index").padding(start = 16.dp, end = 16.dp, bottom = 16.dp),
                        color = MaterialTheme.colorScheme.onSurface,
                        style = MaterialTheme.typography.bodyLarge.copy(fontFamily = FontFamily.Monospace)
                    )
                }
            }
        }
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
