/*
 * Copyright 2020 Andrey Tolpeev
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

package teamcityapp.libraries.build_ui
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.Preview
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import teamcityapp.libraries.build_presentation.resources.*
import teamcityapp.libraries.builds.BuildLaunchData
import teamcityapp.libraries.design_system.resolve
import teamcityapp.libraries.list_ui.ListRowPosition
import teamcityapp.libraries.list_ui.TeamCityListLeadingIcon
import teamcityapp.libraries.list_ui.TeamCityListRow
import teamcityapp.libraries.theme.TeamCityDimensions
import teamcityapp.libraries.theme.TeamCityLoadingIndicator
import teamcityapp.libraries.theme.TeamCityTheme
import teamcityapp.libraries.theme.teamCityStatusColors

@Composable
fun TeamCityBuildRow(
    row: BuildRowUiState,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    position: ListRowPosition = ListRowPosition.Single
) {
    val build = row.build
    val label = stringResource(row.statusLabel)
    val icon = when {
        build.isQueued -> Res.drawable.ic_clock_fast
        build.isFailed -> Res.drawable.ic_error_black_24dp
        build.status == "ERROR" -> Res.drawable.ic_report_problem_black_24dp
        build.status == "UNKNOWN" -> Res.drawable.ic_help_black_24dp
        else -> Res.drawable.ic_check_circle_black_24dp
    }
    val statusColors = teamCityStatusColors()
    val statusText = row.statusText.resolve()
    val containerColor = when {
        build.isRunning -> statusColors.info.container
        build.isQueued -> statusColors.warning.container
        build.isFailed || build.status == "ERROR" -> MaterialTheme.colorScheme.errorContainer
        build.isSuccess -> statusColors.success.container
        else -> MaterialTheme.colorScheme.surfaceContainerHighest
    }
    val contentColor = when {
        build.isRunning -> statusColors.info.onContainer
        build.isQueued -> statusColors.warning.onContainer
        build.isFailed || build.status == "ERROR" -> MaterialTheme.colorScheme.onErrorContainer
        build.isSuccess -> statusColors.success.onContainer
        else -> MaterialTheme.colorScheme.onSurfaceVariant
    }
    TeamCityListRow(
        onClick = onClick,
        modifier = modifier,
        position = position,
        leadingContent = {
            TeamCityListLeadingIcon(containerColor = containerColor, contentColor = contentColor) {
                if (build.isRunning) {
                    TeamCityLoadingIndicator(Modifier.size(TeamCityDimensions.iconSize).semantics { contentDescription = label }, color = contentColor)
                } else {
                    Icon(painterResource(icon), null, Modifier.size(TeamCityDimensions.iconSize))
                }
            }
        }
    ) {
        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(TeamCityDimensions.extraSmallSpacing)) {
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                val number = row.number.resolve()
                Text(stringResource(Res.string.build_number, number), Modifier.weight(1f), style = MaterialTheme.typography.titleMedium)
                if (build.personal) Icon(painterResource(Res.drawable.ic_person_black_24dp), stringResource(Res.string.build_personal), Modifier.padding(start = TeamCityDimensions.smallSpacing).size(TeamCityDimensions.statusIconSize))
                if (build.pinned) Icon(painterResource(Res.drawable.ic_pin), stringResource(Res.string.build_pinned), Modifier.padding(start = TeamCityDimensions.smallSpacing).size(TeamCityDimensions.statusIconSize))
            }
            Text(label, style = MaterialTheme.typography.labelMedium, color = contentColor)
            if (statusText.isNotBlank() && statusText != label) {
                Text(statusText, style = MaterialTheme.typography.bodyMedium)
            }
            build.branchName?.takeIf { it.isNotEmpty() }?.let {
                Text(it, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}

@Preview
@Composable
private fun TeamCityBuildRowPreview() {
    TeamCityTheme { TeamCityBuildRow(BuildRowUiState(BuildLaunchData("1", "/builds/id:1", number = "42", status = "SUCCESS", state = "finished", statusText = "Success", branchName = "main")), {}) }
}
