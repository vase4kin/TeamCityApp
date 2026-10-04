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

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.PlatformTextStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import teamcityapp.features.properties.api.Property
import teamcityapp.libraries.theme.TeamCityTheme
import teamcityapp.libraries.theme.R as ThemeR

@Composable
fun PropertiesScreen(state: PropertiesUiState, onCopy: (Property) -> Unit, modifier: Modifier = Modifier) {
    Surface(modifier.fillMaxSize(), color = colorResource(R.color.properties_background)) {
        when (state) {
            PropertiesUiState.Empty -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(painterResource(ThemeR.drawable.ic_format_list_bulleted_black_24dp), null,
                        Modifier.size(124.dp), tint = colorResource(R.color.properties_secondary))
                    Text(stringResource(R.string.empty_list_message_parameters), Modifier.padding(16.dp),
                        color = colorResource(R.color.properties_secondary), style = TextStyle(fontSize = 16.sp, letterSpacing = 0.5.sp, platformStyle = PlatformTextStyle(includeFontPadding = true)))
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
    Column(modifier.fillMaxWidth().background(colorResource(R.color.properties_surface)).then(if (empty) Modifier else Modifier.clickable(role = Role.Button, onClick = onCopy))) {
        Column(Modifier.padding(16.dp)) {
            Text(property.name, color = colorResource(R.color.properties_secondary),
                style = TextStyle(fontSize = 14.sp, letterSpacing = 0.25.sp, platformStyle = PlatformTextStyle(includeFontPadding = true)),
                maxLines = 2, overflow = TextOverflow.Ellipsis)
            Text(if (empty) stringResource(R.string.text_property_value_empty) else property.value,
                color = colorResource(if (empty) R.color.properties_disabled else R.color.properties_primary),
                style = TextStyle(fontSize = 16.sp, letterSpacing = 0.5.sp, platformStyle = PlatformTextStyle(includeFontPadding = true)),
                maxLines = 2, overflow = TextOverflow.Ellipsis)
        }
        HorizontalDivider(thickness = 1.dp, color = colorResource(R.color.properties_divider))
    }
}

@Preview
@Composable
private fun PropertiesPreview() {
    TeamCityTheme { PropertiesScreen(PropertiesUiState.Content(listOf(Property("env.CI", "true"), Property("sdk", ""))), {}) }
}

@Preview
@Composable
private fun EmptyPropertiesPreview() {
    TeamCityTheme { PropertiesScreen(PropertiesUiState.Empty, {}) }
}
