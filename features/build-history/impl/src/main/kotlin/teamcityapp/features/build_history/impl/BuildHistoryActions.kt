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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import teamcityapp.features.build_history.api.BuildHistoryPrompt
import teamcityapp.libraries.theme.TeamCityDimensions
import teamcityapp.libraries.theme.TeamCityExtendedFloatingActionButton
import teamcityapp.libraries.theme.TeamCityFloatingToolbar
import teamcityapp.libraries.theme.TeamCityLoadingIndicator
import teamcityapp.libraries.theme.TeamCityTheme

/** Persistent secondary actions and a trailing primary action share one floating group. */
@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
internal fun BuildHistoryActions(
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
                    TeamCityLoadingIndicator(Modifier.size(TeamCityDimensions.compactProgressIndicatorSize))
                } else {
                    Icon(
                        painterResource(if (favorite is FavoriteState.Available && favorite.favorite) R.drawable.ic_history_favorite else R.drawable.ic_history_favorite_border),
                        stringResource(controls.favoriteActionLabelRes)
                    )
                }
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

@Preview
@Composable
private fun BuildHistoryActionsPreview() {
    TeamCityTheme {
        BuildHistoryActions(BuildHistoryControls(FavoriteState.Available(true), OnboardingState.Available()), true, {}, {}, {}, {}, { _, _ -> })
    }
}
