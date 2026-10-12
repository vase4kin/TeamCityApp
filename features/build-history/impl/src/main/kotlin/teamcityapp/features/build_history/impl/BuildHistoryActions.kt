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

package teamcityapp.features.build_history.impl

import android.content.Context
import android.view.accessibility.AccessibilityManager
import androidx.compose.animation.*
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.layout.boundsInRoot
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import teamcityapp.features.build_history.api.BuildHistoryPrompt
import teamcityapp.libraries.theme.TeamCityDimensions
import teamcityapp.libraries.theme.TeamCityExtendedFloatingActionButton
import teamcityapp.libraries.theme.TeamCityTheme

/** The extended FAB is separate so its label and trailing position survive toolbar collapse. */
@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
internal fun BuildHistoryActions(
    controls: BuildHistoryControls,
    expanded: Boolean,
    showRunBuild: Boolean,
    onRunBuild: () -> Unit,
    onFilters: () -> Unit,
    onFavorite: () -> Unit,
    onFavoriteRetry: () -> Unit,
    onAnchor: (BuildHistoryPrompt, Rect) -> Unit,
    modifier: Modifier = Modifier
) {
    // Wrap at narrow widths or large text sizes instead of truncating the primary action.
    FlowRow(
        modifier.testTag("history:actions"),
        horizontalArrangement = Arrangement.End,
        verticalArrangement = Arrangement.spacedBy(TeamCityDimensions.smallSpacing)
    ) {
        AnimatedVisibility(
            visible = expanded,
            enter = expandHorizontally(MaterialTheme.motionScheme.defaultSpatialSpec(), expandFrom = Alignment.End) + fadeIn(MaterialTheme.motionScheme.defaultEffectsSpec()),
            exit = shrinkHorizontally(MaterialTheme.motionScheme.defaultSpatialSpec(), shrinkTowards = Alignment.End) + fadeOut(MaterialTheme.motionScheme.defaultEffectsSpec())
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                HorizontalFloatingToolbar(expanded = true, modifier = Modifier.testTag("history:toolbar")) {
                    IconButton(onClick = onFilters, modifier = Modifier.testTag("history:filter").onGloballyPositioned { onAnchor(BuildHistoryPrompt.Filter, it.boundsInRoot()) }) {
                        Icon(painterResource(R.drawable.ic_history_filter), stringResource(R.string.history_filter))
                    }
                    val favorite = controls.favorite
                    IconButton(
                        onClick = if (favorite == FavoriteState.Unavailable) onFavoriteRetry else onFavorite,
                        enabled = favorite != FavoriteState.Loading && (favorite !is FavoriteState.Available || !favorite.updating),
                        modifier = Modifier.testTag("history:favorite").onGloballyPositioned { onAnchor(BuildHistoryPrompt.Favorite, it.boundsInRoot()) }
                    ) {
                        if (favorite == FavoriteState.Loading || (favorite is FavoriteState.Available && favorite.updating)) {
                            CircularProgressIndicator(Modifier.size(TeamCityDimensions.compactProgressIndicatorSize), strokeWidth = TeamCityDimensions.progressStrokeWidth)
                        } else {
                            Icon(
                                painterResource(if (favorite is FavoriteState.Available && favorite.favorite) R.drawable.ic_history_favorite else R.drawable.ic_history_favorite_border),
                                stringResource(controls.favoriteActionLabelRes)
                            )
                        }
                    }
                }
                if (showRunBuild) Spacer(Modifier.width(TeamCityDimensions.mediumSpacing))
            }
        }
        if (showRunBuild) {
            TeamCityExtendedFloatingActionButton(
                onRunBuild,
                Modifier.heightIn(min = FloatingToolbarDefaults.ContainerSize).testTag("history:run").onGloballyPositioned { onAnchor(BuildHistoryPrompt.Run, it.boundsInRoot()) }
            ) {
                Icon(painterResource(R.drawable.ic_history_run), null)
                Spacer(Modifier.width(TeamCityDimensions.mediumSpacing))
                Text(stringResource(R.string.history_run))
            }
        }
    }
}

@Composable
internal fun rememberHistoryTouchExplorationEnabled(): Boolean {
    val context = LocalContext.current
    val manager = remember(context) { context.getSystemService(Context.ACCESSIBILITY_SERVICE) as AccessibilityManager }
    var enabled by remember(manager) { mutableStateOf(manager.isTouchExplorationEnabled) }
    DisposableEffect(manager) {
        val listener = AccessibilityManager.TouchExplorationStateChangeListener { enabled = it }
        manager.addTouchExplorationStateChangeListener(listener)
        enabled = manager.isTouchExplorationEnabled
        onDispose { manager.removeTouchExplorationStateChangeListener(listener) }
    }
    return enabled
}

@Preview
@Composable
private fun BuildHistoryActionsPreview() {
    TeamCityTheme {
        BuildHistoryActions(BuildHistoryControls(FavoriteState.Available(true), OnboardingState.Available()), true, true, {}, {}, {}, {}, { _, _ -> })
    }
}
