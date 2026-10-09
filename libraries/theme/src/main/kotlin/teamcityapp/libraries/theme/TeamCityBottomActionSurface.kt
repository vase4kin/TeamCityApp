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
import androidx.compose.foundation.layout.*
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp

/**
 * Fixed actions separate from content only while content remains below its viewport.
 * Pass canScrollForward from a normal vertical ScrollState/LazyListState. Reversed
 * layouts must instead supply whether content remains at the physical bottom.
 * Use in TeamCityScreen's bottomBar slot. The tone extends behind navigation bars;
 * insets keep actions safe and the content stays centered at the screen's default width.
 * Callers own action padding; changes in tone never change measurement.
 */
@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun TeamCityBottomActionSurface(
    contentCanScrollForward: Boolean,
    modifier: Modifier = Modifier,
    windowInsets: WindowInsets = WindowInsets.navigationBars,
    contentMaxWidth: Dp = TeamCityDimensions.screenContentMaxWidth,
    content: @Composable () -> Unit
) {
    val color by animateColorAsState(
        if (contentCanScrollForward) MaterialTheme.colorScheme.surfaceContainer else MaterialTheme.colorScheme.surface,
        animationSpec = MaterialTheme.motionScheme.defaultEffectsSpec(),
        label = "Bottom action color"
    )
    Surface(modifier.fillMaxWidth(), color = color) {
        Box(Modifier.windowInsetsPadding(windowInsets), contentAlignment = Alignment.TopCenter) {
            Box(Modifier.widthIn(max = contentMaxWidth).fillMaxWidth()) { content() }
        }
    }
}

@Preview
@Composable
private fun BottomActionsPreview() {
    TeamCityTheme {
        TeamCityBottomActionSurface(contentCanScrollForward = false) {
            androidx.compose.material3.Button({}, Modifier.fillMaxWidth().padding(TeamCityDimensions.contentPadding)) {
                androidx.compose.material3.Text(androidx.compose.ui.res.stringResource(android.R.string.ok))
            }
        }
    }
}
