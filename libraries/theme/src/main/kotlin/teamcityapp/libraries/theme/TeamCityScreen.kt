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

import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.Dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TeamCityScreen(
    title: String,
    onClose: () -> Unit,
    appBarHeight: Dp = TopAppBarDefaults.TopAppBarExpandedHeight,
    titleStyle: TextStyle = MaterialTheme.typography.titleLarge,
    titleStartPadding: Dp = TeamCityDimensions.noSpacing,
    appBarColors: TopAppBarColors? = null,
    containerColor: Color = MaterialTheme.colorScheme.background,
    contentMaxWidth: Dp = TeamCityDimensions.screenContentMaxWidth,
    actions: @Composable androidx.compose.foundation.layout.RowScope.() -> Unit = {},
    bottomBar: (@Composable () -> Unit)? = null,
    snackbarHost: @Composable () -> Unit = {},
    content: @Composable (Modifier) -> Unit
) = TeamCityScreenFrame(
    title, onClose, stringResource(R.string.action_back), painterResource(R.drawable.ic_arrow_back_24dp),
    appBarHeight, titleStyle, titleStartPadding, appBarColors, containerColor, contentMaxWidth,
    actions, bottomBar, snackbarHost, content
)
