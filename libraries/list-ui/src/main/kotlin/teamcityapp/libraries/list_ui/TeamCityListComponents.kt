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

package teamcityapp.libraries.list_ui

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import teamcityapp.libraries.theme.ErrorNotice
import teamcityapp.libraries.theme.MessageContent
import teamcityapp.libraries.theme.TeamCityTheme

/** Only adjacent rows in the same logical section share their inside corners. */
enum class ListRowPosition { Single, First, Middle, Last }

fun listRowPosition(index: Int, count: Int): ListRowPosition = listRowPosition(index > 0, index < count - 1)

fun listRowPosition(hasPrevious: Boolean, hasNext: Boolean): ListRowPosition = when {
    hasPrevious && hasNext -> ListRowPosition.Middle
    hasPrevious -> ListRowPosition.Last
    hasNext -> ListRowPosition.First
    else -> ListRowPosition.Single
}

/** A grouped surface without loading, domain, or navigation behavior. Passive rows stay passive. */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun TeamCityListRow(
    onClick: (() -> Unit)? = null,
    modifier: Modifier = Modifier,
    position: ListRowPosition = ListRowPosition.Single,
    leadingContent: (@Composable () -> Unit)? = null,
    onLongClick: (() -> Unit)? = null,
    content: @Composable RowScope.() -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val pressed by interactionSource.collectIsPressedAsState()
    val shape = listRowShape(position, pressed)
    val interaction = if (onClick == null && onLongClick == null) {
        Modifier
    } else {
        Modifier.combinedClickable(
            interactionSource = interactionSource,
            indication = androidx.compose.foundation.LocalIndication.current,
            role = Role.Button,
            onClick = { onClick?.invoke() },
            onLongClick = onLongClick
        )
    }
    Surface(
        modifier = modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 1.dp).clip(shape).then(interaction),
        shape = shape,
        color = MaterialTheme.colorScheme.surfaceContainerLow,
        contentColor = MaterialTheme.colorScheme.onSurface
    ) {
        Row(
            Modifier.fillMaxWidth().heightIn(min = 72.dp).padding(horizontal = 12.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            leadingContent?.invoke()
            content()
        }
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun listRowShape(position: ListRowPosition, pressed: Boolean): Shape {
    val first = position == ListRowPosition.Single || position == ListRowPosition.First
    val last = position == ListRowPosition.Single || position == ListRowPosition.Last
    return RoundedCornerShape(topStart = animateListCorner(first, pressed), topEnd = animateListCorner(first, pressed), bottomStart = animateListCorner(last, pressed), bottomEnd = animateListCorner(last, pressed))
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun animateListCorner(outer: Boolean, pressed: Boolean): Dp {
    val value by animateDpAsState(
        targetValue = if (pressed) {
            16.dp
        } else if (outer) {
            24.dp
        } else {
            4.dp
        },
        animationSpec = MaterialTheme.motionScheme.fastSpatialSpec(),
        label = "list corner"
    )
    return value
}

@Composable
fun TeamCityListLeadingIcon(
    modifier: Modifier = Modifier,
    containerColor: Color = MaterialTheme.colorScheme.surfaceContainerHighest,
    contentColor: Color = MaterialTheme.colorScheme.onSurfaceVariant,
    content: @Composable () -> Unit
) {
    Surface(modifier = modifier.size(40.dp), shape = RoundedCornerShape(14.dp), color = containerColor, contentColor = contentColor) {
        Box(contentAlignment = Alignment.Center) { content() }
    }
}

/** The owning feature resolves the message from its resources and current selection. */
@Composable
fun TeamCityListEmpty(message: String, modifier: Modifier = Modifier) {
    MessageContent(message, modifier.fillMaxSize())
}

@Composable
fun TeamCityListSectionHeader(title: String, modifier: Modifier = Modifier, onClick: (() -> Unit)? = null) {
    val interaction = if (onClick == null) Modifier else Modifier.clickable(onClick = onClick)
    Text(
        title,
        modifier.fillMaxWidth().heightIn(min = 48.dp).then(interaction).semantics { heading() }.padding(horizontal = 20.dp, vertical = 12.dp),
        style = MaterialTheme.typography.titleSmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant
    )
}

/** Skeletons share row geometry but never enter the feature's domain collection. */
@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun TeamCityListLoading(modifier: Modifier = Modifier) {
    val description = stringResource(R.string.list_loading)
    Column(modifier.fillMaxSize().semantics { stateDescription = description }) {
        Row(Modifier.padding(horizontal = 20.dp, vertical = 16.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            LoadingIndicator(Modifier.size(40.dp))
            Text(description, style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary)
        }
        repeat(6) { TeamCityListLoadingRow(position = listRowPosition(it, 6)) }
    }
}

@Composable
fun TeamCityListLoadingRow(modifier: Modifier = Modifier, position: ListRowPosition = ListRowPosition.Single) {
    TeamCityListRow(modifier = modifier, position = position, leadingContent = {
        Spacer(Modifier.size(40.dp).clip(RoundedCornerShape(14.dp)).background(MaterialTheme.colorScheme.surfaceContainerHighest))
    }) {
        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Spacer(Modifier.fillMaxWidth(.45f).height(16.dp).clip(MaterialTheme.shapes.small).background(MaterialTheme.colorScheme.surfaceContainerHighest))
            Spacer(Modifier.fillMaxWidth(.95f).height(12.dp).clip(MaterialTheme.shapes.small).background(MaterialTheme.colorScheme.surfaceContainerHighest))
            Spacer(Modifier.fillMaxWidth(.62f).height(12.dp).clip(MaterialTheme.shapes.small).background(MaterialTheme.colorScheme.surfaceContainer))
        }
    }
}

/** A feature can place this footer in its lazy list while a page is being appended. */
@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun TeamCityListAppendLoading(modifier: Modifier = Modifier) {
    val description = stringResource(R.string.list_loading_more)
    Box(modifier.fillMaxWidth().padding(24.dp).semantics { stateDescription = description }, contentAlignment = Alignment.Center) {
        LoadingIndicator(Modifier.size(40.dp))
    }
}

@Composable
fun TeamCityListAppendRetry(onRetry: () -> Unit, modifier: Modifier = Modifier) {
    ErrorNotice(
        message = stringResource(R.string.list_append_failed),
        onRetry = onRetry,
        modifier = modifier.fillMaxWidth().padding(16.dp),
        actionLabel = stringResource(R.string.list_action_retry)
    )
}

@Preview
@Composable
private fun ListComponentsPreview() {
    TeamCityTheme {
        Surface {
            Column {
                TeamCityListSectionHeader("Project")
                TeamCityListRow({}, position = ListRowPosition.First) { Text("Linux agent", style = MaterialTheme.typography.titleMedium) }
                TeamCityListRow({}, position = ListRowPosition.Last) { Text("Windows agent", style = MaterialTheme.typography.titleMedium) }
                TeamCityListLoadingRow()
                TeamCityListAppendLoading()
                TeamCityListAppendRetry({})
            }
        }
    }
}
