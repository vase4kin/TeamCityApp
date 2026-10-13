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

import androidx.compose.foundation.layout.Box
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview

/** The anchor keeps its own label, role and action; the popup only explains it. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TeamCityTooltip(
    label: String,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    content: @Composable () -> Unit
) {
    // Material applies its modifier to the inner anchor. Own the outer layout so RowScope
    // weights and minimum constraints still apply to the item in its actual parent.
    Box(modifier, propagateMinConstraints = true) {
        TooltipBox(
            positionProvider = TooltipDefaults.rememberTooltipPositionProvider(TooltipAnchorPosition.Above),
            tooltip = { PlainTooltip { Text(label) } },
            state = rememberTooltipState(),
            enableUserInput = enabled,
            content = content
        )
    }
}

@Preview
@Composable
private fun TooltipPreview() {
    TeamCityTheme {
        TeamCityTooltip(stringResource(R.string.action_close)) {
            IconButton(onClick = {}) {
                Icon(painterResource(R.drawable.ic_close_black_24dp), stringResource(R.string.action_close))
            }
        }
    }
}
