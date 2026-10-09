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

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.width
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

/** The app's standard rounded-square FAB, using Material 3's theme colors and elevation. */
@Composable
fun TeamCityFloatingActionButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    FloatingActionButton(onClick = onClick, modifier = modifier, shape = FloatingActionButtonDefaults.shape, content = content)
}

/** Labeled actions share the icon-only FAB's shape, colors, height, and elevation. */
@Composable
fun TeamCityExtendedFloatingActionButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable RowScope.() -> Unit
) {
    ExtendedFloatingActionButton(onClick = onClick, modifier = modifier, shape = FloatingActionButtonDefaults.shape, content = content)
}

@Preview
@Composable
private fun FloatingActionButtonsPreview() {
    TeamCityTheme {
        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            TeamCityFloatingActionButton({}) { Icon(painterResource(R.drawable.ic_add_black_24dp), null) }
            TeamCityExtendedFloatingActionButton({}) {
                Icon(painterResource(R.drawable.ic_add_black_24dp), null)
                Spacer(Modifier.width(12.dp))
                Text(stringResource(R.string.app_name))
            }
        }
    }
}
