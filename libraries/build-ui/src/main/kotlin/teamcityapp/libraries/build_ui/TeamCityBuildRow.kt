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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.Preview
import teamcityapp.libraries.builds.BuildLaunchData
import teamcityapp.libraries.list_ui.ListRowPosition
import teamcityapp.libraries.list_ui.TeamCityListLeadingIcon
import teamcityapp.libraries.list_ui.TeamCityListRow
import teamcityapp.libraries.theme.TeamCityDimensions
import teamcityapp.libraries.theme.TeamCityTheme
import teamcityapp.libraries.theme.teamCityStatusColors

@Composable
fun TeamCityBuildRow(
    build: BuildLaunchData,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    position: ListRowPosition = ListRowPosition.Single
) {
    val label = stringResource(
        when {
            build.isRunning -> R.string.build_running
            build.isQueued -> R.string.build_queued
            build.isFailed -> R.string.build_failed
            build.status == "ERROR" -> R.string.build_error
            build.status == "UNKNOWN" -> R.string.build_unknown
            else -> R.string.build_success
        }
    )
    val icon = when {
        build.isQueued -> R.drawable.ic_clock_fast
        build.isFailed -> R.drawable.ic_error_black_24dp
        build.status == "ERROR" -> R.drawable.ic_report_problem_black_24dp
        build.status == "UNKNOWN" -> R.drawable.ic_help_black_24dp
        else -> R.drawable.ic_check_circle_black_24dp
    }
    val statusColors = teamCityStatusColors()
    val statusText = if (build.isQueued) build.waitReason ?: stringResource(R.string.build_queued_fallback) else build.statusText.orEmpty()
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
                    CircularProgressIndicator(Modifier.size(TeamCityDimensions.iconSize).semantics { contentDescription = label }, color = contentColor, strokeWidth = TeamCityDimensions.progressStrokeWidth)
                } else {
                    Icon(painterResource(icon), null, Modifier.size(TeamCityDimensions.iconSize))
                }
            }
        }
    ) {
        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(TeamCityDimensions.extraSmallSpacing)) {
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                val number = build.number ?: stringResource(R.string.build_no_number)
                Text(stringResource(R.string.build_number, number), Modifier.weight(1f), style = MaterialTheme.typography.titleMedium)
                if (build.personal) Icon(painterResource(R.drawable.ic_person_black_24dp), stringResource(R.string.build_personal), Modifier.padding(start = TeamCityDimensions.smallSpacing).size(TeamCityDimensions.statusIconSize))
                if (build.pinned) Icon(painterResource(R.drawable.ic_pin), stringResource(R.string.build_pinned), Modifier.padding(start = TeamCityDimensions.smallSpacing).size(TeamCityDimensions.statusIconSize))
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
    TeamCityTheme { TeamCityBuildRow(BuildLaunchData("1", "/builds/id:1", number = "42", status = "SUCCESS", state = "finished", statusText = "Success", branchName = "main"), {}) }
}
