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
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.layout.boundsInRoot
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.tooling.preview.Preview
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import teamcityapp.features.build_history.api.BuildHistoryPrompt
import teamcityapp.features.build_history.presentation.resources.*
import teamcityapp.libraries.theme.TeamCityDimensions
import teamcityapp.libraries.theme.TeamCityExtendedFloatingActionButton
import teamcityapp.libraries.theme.TeamCityFloatingToolbar
import teamcityapp.libraries.theme.TeamCityLoadingIndicator
import teamcityapp.libraries.theme.TeamCityTheme
import teamcityapp.libraries.theme.TeamCityTooltip

/** Persistent secondary actions and a trailing primary action share one floating group. */
@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun BuildHistoryActions(
    controls: BuildHistoryControls,
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
        horizontalArrangement = Arrangement.spacedBy(TeamCityDimensions.mediumSpacing, Alignment.End),
        verticalArrangement = Arrangement.spacedBy(TeamCityDimensions.smallSpacing)
    ) {
        TeamCityFloatingToolbar(modifier = Modifier.testTag("history:toolbar")) {
            TeamCityTooltip(stringResource(Res.string.history_filter)) {
                IconButton(onClick = onFilters, modifier = Modifier.testTag("history:filter").onGloballyPositioned { onAnchor(BuildHistoryPrompt.Filter, it.boundsInRoot()) }) {
                    Icon(painterResource(Res.drawable.ic_history_filter), stringResource(Res.string.history_filter))
                }
            }
            val favorite = controls.favorite
            val favoriteLabel = stringResource(controls.favoriteActionLabelRes)
            val favoriteState = controls.favoriteStateDescriptionRes?.let { stringResource(it) }
            TeamCityTooltip(favoriteLabel) {
                IconButton(
                    onClick = if (favorite == FavoriteState.Unavailable) onFavoriteRetry else onFavorite,
                    enabled = favorite != FavoriteState.Loading && (favorite !is FavoriteState.Available || !favorite.updating),
                    modifier = Modifier.testTag("history:favorite").semantics {
                        contentDescription = favoriteLabel
                        favoriteState?.let { stateDescription = it }
                    }.onGloballyPositioned { onAnchor(BuildHistoryPrompt.Favorite, it.boundsInRoot()) }
                ) {
                    if (favorite == FavoriteState.Loading || (favorite is FavoriteState.Available && favorite.updating)) {
                        TeamCityLoadingIndicator(Modifier.size(TeamCityDimensions.compactProgressIndicatorSize))
                    } else {
                        Icon(
                            painterResource(if (favorite is FavoriteState.Available && favorite.favorite) Res.drawable.ic_history_favorite else Res.drawable.ic_history_favorite_border),
                            null
                        )
                    }
                }
            }
        }
        if (showRunBuild) {
            TeamCityTooltip(stringResource(Res.string.history_run)) {
                TeamCityExtendedFloatingActionButton(
                    onRunBuild,
                    Modifier.heightIn(min = FloatingToolbarDefaults.ContainerSize).testTag("history:run").onGloballyPositioned { onAnchor(BuildHistoryPrompt.Run, it.boundsInRoot()) }
                ) {
                    Icon(painterResource(Res.drawable.ic_history_run), null)
                    Spacer(Modifier.width(TeamCityDimensions.mediumSpacing))
                    Text(stringResource(Res.string.history_run))
                }
            }
        }
    }
}

@Preview
@Composable
private fun BuildHistoryActionsPreview() {
    TeamCityTheme {
        BuildHistoryActions(BuildHistoryControls(FavoriteState.Available(true), OnboardingState.Available()), true, {}, {}, {}, {}, { _, _ -> })
    }
}
