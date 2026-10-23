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
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.FloatingToolbarDefaults
import androidx.compose.material3.HorizontalFloatingToolbar
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.resources.stringResource
import teamcityapp.libraries.design_system.resources.*

/** Secondary actions use a tonal surface and a small shadow above scrolling content. */
@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun TeamCityFloatingToolbar(
    modifier: Modifier = Modifier,
    content: @Composable RowScope.() -> Unit
) {
    val description = stringResource(Res.string.action_toolbar_description)
    // This component is always expanded, with no collapsible side actions. The
    // Apple bridge exposes Material's internal expand/collapse parent as a blank
    // accessibility stop. Render the same static surface and controls directly.
    if (!supportsMaterialParentSemantics) {
        Surface(
            modifier = modifier.semantics { contentDescription = description },
            shape = FloatingToolbarDefaults.ContainerShape,
            color = MaterialTheme.colorScheme.secondaryContainer,
            contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
            shadowElevation = 3.dp
        ) {
            Row(
                Modifier.heightIn(min = FloatingToolbarDefaults.ContainerSize).padding(FloatingToolbarDefaults.ContentPadding),
                verticalAlignment = Alignment.CenterVertically,
                content = content
            )
        }
        return
    }
    HorizontalFloatingToolbar(
        expanded = true,
        modifier = modifier.semantics { contentDescription = description },
        colors = FloatingToolbarDefaults.standardFloatingToolbarColors(
            toolbarContainerColor = MaterialTheme.colorScheme.secondaryContainer,
            toolbarContentColor = MaterialTheme.colorScheme.onSecondaryContainer
        ),
        expandedShadowElevation = 3.dp,
        collapsedShadowElevation = 3.dp,
        content = content
    )
}
