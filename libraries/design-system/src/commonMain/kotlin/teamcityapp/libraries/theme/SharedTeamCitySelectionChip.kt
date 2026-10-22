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

package teamcityapp.libraries.theme

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.size
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.Preview

/** A single-choice filter whose persistent category icon keeps selection from shifting its neighbors. */
@Composable
fun SharedTeamCitySelectionChip(selected: Boolean, label: String, icon: Painter, onClick: () -> Unit, modifier: Modifier = Modifier, enabled: Boolean = true) {
    val defaults = FilterChipDefaults.filterChipColors()
    val container by animateColorAsState(if (selected) defaults.selectedContainerColor else defaults.containerColor, tween(200), label = "filter background")
    val textColor by animateColorAsState(if (selected) defaults.selectedLabelColor else defaults.labelColor, tween(200), label = "filter label")
    FilterChip(
        selected = selected,
        enabled = enabled,
        onClick = { if (!selected) onClick() },
        label = { Text(label) },
        colors = defaults.copy(containerColor = container, selectedContainerColor = container, labelColor = textColor, selectedLabelColor = textColor, leadingIconColor = textColor, selectedLeadingIconColor = textColor),
        border = FilterChipDefaults.filterChipBorder(enabled = enabled, selected = selected, selectedBorderColor = textColor, selectedBorderWidth = TeamCityDimensions.selectionChipBorderWidth),
        leadingIcon = {
            Icon(icon, null, Modifier.size(FilterChipDefaults.IconSize))
        },
        modifier = modifier.semantics { role = Role.RadioButton }
    )
}
