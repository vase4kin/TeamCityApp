/*
 * Copyright 2019 Andrey Tolpeev
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

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import teamcityapp.libraries.builds.BuildConfigurationData
import teamcityapp.libraries.builds.BuildLaunchData
import teamcityapp.libraries.list_ui.TeamCityListSectionHeader
import teamcityapp.libraries.theme.TeamCityTheme

/** Legacy sections compare displayed titles and use the top-level configuration ID fallback. */
fun buildConfigurationTitle(build: BuildLaunchData): String {
    val configuration = build.configuration
    return if (configuration?.projectName != null) {
        "${configuration.projectName} - ${configuration.name}"
    } else {
        build.buildTypeId.orEmpty()
    }
}

@Composable
fun TeamCityBuildConfigurationHeader(
    build: BuildLaunchData,
    onClick: (configurationId: String, configurationName: String) -> Unit,
    modifier: Modifier = Modifier
) {
    TeamCityListSectionHeader(buildConfigurationTitle(build), modifier, onClick = {
        onClick(build.buildTypeId.orEmpty(), build.configuration?.name.orEmpty())
    })
}

@Preview
@Composable
private fun BuildConfigurationHeaderPreview() {
    TeamCityTheme {
        TeamCityBuildConfigurationHeader(BuildLaunchData("1", "/builds/1", buildTypeId = "Android_Debug", configuration = BuildConfigurationData("Android_Debug", "Debug", "Mobile", "Mobile")), { _, _ -> })
    }
}
