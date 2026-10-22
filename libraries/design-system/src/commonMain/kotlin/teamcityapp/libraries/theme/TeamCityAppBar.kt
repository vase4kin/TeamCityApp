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
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

/** The shared flat and scrolled colors for pinned screen headers. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun teamCityTopAppBarColors(): TopAppBarColors = TopAppBarDefaults.topAppBarColors(
    containerColor = MaterialTheme.colorScheme.surface,
    scrolledContainerColor = MaterialTheme.colorScheme.surfaceContainer,
    navigationIconContentColor = MaterialTheme.colorScheme.onSurface,
    titleContentColor = MaterialTheme.colorScheme.onSurface,
    actionIconContentColor = MaterialTheme.colorScheme.onSurface
)

/** Colors the entire header when its title or other content sits outside the TopAppBar. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TeamCityAppBarSurface(scrollBehavior: TopAppBarScrollBehavior, modifier: Modifier = Modifier, content: @Composable ColumnScope.() -> Unit) {
    val colors = teamCityTopAppBarColors()
    val color by animateColorAsState(
        if (scrollBehavior.state.overlappedFraction > 0.01f) colors.scrolledContainerColor else colors.containerColor,
        label = "app bar color"
    )
    Surface(modifier, color = color) { Column(content = content) }
}

/** A screen header's UI state, shared only with the content composed under that header. */
@OptIn(ExperimentalMaterial3Api::class)
val LocalTeamCityAppBarState = staticCompositionLocalOf<TopAppBarState?> { null }

/** Tracks the real list position, including restored/programmatic scrolls and pull-to-refresh. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BindTeamCityAppBarToList(listState: LazyListState, appBarState: TopAppBarState? = LocalTeamCityAppBarState.current) {
    LaunchedEffect(listState, appBarState) {
        if (appBarState != null) {
            snapshotFlow {
                (if (listState.canScrollBackward) appBarState.heightOffsetLimit else 0f) to appBarState.contentOffset
            }.collect { (expectedOffset, actualOffset) ->
                if (actualOffset != expectedOffset) appBarState.contentOffset = expectedOffset
            }
        }
    }
}

/** Retains the list position and keeps the enclosing screen header in sync with it. */
@Composable
fun rememberTeamCityLazyListState(): LazyListState = rememberLazyListState().also { BindTeamCityAppBarToList(it) }

@OptIn(ExperimentalMaterial3Api::class)
@Preview
@Composable
private fun TeamCityAppBarSurfacePreview() {
    TeamCityTheme {
        TeamCityAppBarSurface(TopAppBarDefaults.pinnedScrollBehavior()) {
            Text("TeamCity", Modifier.padding(16.dp), style = MaterialTheme.typography.titleLarge)
        }
    }
}
