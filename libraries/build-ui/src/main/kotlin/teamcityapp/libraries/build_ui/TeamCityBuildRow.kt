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

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import teamcityapp.libraries.builds.BuildLaunchData
import teamcityapp.libraries.theme.TeamCityTheme
import teamcityapp.libraries.theme.teamCityStatusColors

@Composable
fun TeamCityBuildRow(build: BuildLaunchData, onClick: () -> Unit, modifier: Modifier = Modifier) {
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
    CompositionLocalProvider(LocalContentColor provides MaterialTheme.colorScheme.onSurface) {
        Column(modifier.fillMaxWidth().clickable(role = Role.Button, onClick = onClick)) {
            Row(Modifier.fillMaxWidth().padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                if (build.isRunning) {
                    CircularProgressIndicator(Modifier.size(24.dp).semantics { contentDescription = label }, strokeWidth = 2.dp)
                } else {
                    Icon(
                        painterResource(icon),
                        label,
                        Modifier.size(24.dp),
                        tint = when {
                            build.isFailed -> MaterialTheme.colorScheme.error
                            build.isSuccess -> statusColors.success.onContainer
                            else -> MaterialTheme.colorScheme.onSurfaceVariant
                        }
                    )
                }
                Column(Modifier.weight(1f).padding(start = 16.dp)) {
                    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                        val number = build.number ?: stringResource(R.string.build_no_number)
                        Text(stringResource(R.string.build_number, number), Modifier.weight(1f), style = MaterialTheme.typography.bodyMedium)
                        if (build.personal) Icon(painterResource(R.drawable.ic_person_black_24dp), stringResource(R.string.build_personal), Modifier.padding(start = 8.dp).size(16.dp))
                        if (build.pinned) Icon(painterResource(R.drawable.ic_pin), stringResource(R.string.build_pinned), Modifier.padding(start = 8.dp).size(16.dp))
                    }
                    Text(statusText, style = MaterialTheme.typography.bodyLarge)
                    build.branchName?.takeIf { it.isNotEmpty() }?.let { Text(it, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant) }
                }
            }
            HorizontalDivider(Modifier.padding(start = 56.dp))
        }
    }
}

@Preview
@Composable
private fun TeamCityBuildRowPreview() {
    TeamCityTheme { TeamCityBuildRow(BuildLaunchData("1", "/builds/id:1", number = "42", status = "SUCCESS", state = "finished", statusText = "Success", branchName = "main"), {}) }
}
