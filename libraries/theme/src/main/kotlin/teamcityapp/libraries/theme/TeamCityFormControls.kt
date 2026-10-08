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

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.selection.toggleable
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

/** Compact switch geometry matches the existing platform switches. */
@Composable
fun TeamCitySwitch(label: String, checked: Boolean, onCheckedChange: (Boolean) -> Unit, modifier: Modifier = Modifier, enabled: Boolean = true) {
    val colors = MaterialTheme.colorScheme
    val thumb = if (checked) colors.secondary else colors.surface
    val track = if (checked) colors.secondary.copy(alpha = .5f) else colors.onSurface.copy(alpha = .3f)
    Row(modifier.fillMaxWidth().heightIn(min = 48.dp).toggleable(checked, enabled = enabled, role = Role.Switch, onValueChange = onCheckedChange), verticalAlignment = Alignment.CenterVertically) {
        Text(label, modifier = Modifier.weight(1f).padding(end = 8.dp), style = MaterialTheme.typography.bodyLarge, color = colors.onSurface.copy(alpha = if (enabled) 1f else .38f))
        Canvas(Modifier.size(40.dp, 32.dp)) {
            drawRoundRect(track, topLeft = Offset(3.dp.toPx(), 9.dp.toPx()), size = Size(34.dp.toPx(), 14.dp.toPx()), cornerRadius = CornerRadius(7.dp.toPx()))
            val center = Offset((if (checked) 28 else 12).dp.toPx(), 16.dp.toPx())
            drawCircle(colors.onSurface.copy(alpha = .15f), 11.dp.toPx(), center.copy(y = center.y + 1.dp.toPx()))
            drawCircle(thumb.copy(alpha = if (enabled) 1f else .38f), 10.dp.toPx(), center)
        }
    }
}

@Preview @Composable
private fun SwitchPreview() {
    TeamCityTheme { TeamCitySwitch("Guest user", true, {}) }
}
