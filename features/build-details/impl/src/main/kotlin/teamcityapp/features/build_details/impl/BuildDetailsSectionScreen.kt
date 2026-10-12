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

package teamcityapp.features.build_details.impl

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import teamcityapp.features.build_details.api.BuildDetailsSection
import teamcityapp.libraries.theme.LocalTeamCityAppBarState
import teamcityapp.libraries.theme.teamCityTopAppBarColors

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun BuildDetailsSectionScreen(section: BuildDetailsSection, buildNumber: String, configurationName: String, onUp: () -> Unit, content: @Composable () -> Unit) {
    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()
    Scaffold(topBar = {
        TopAppBar(
            modifier = Modifier.testTag("screen:app-bar"),
            colors = teamCityTopAppBarColors(),
            scrollBehavior = scrollBehavior,
            title = {
                Column {
                    Text(stringResource(section.label()), maxLines = 1, overflow = TextOverflow.Ellipsis)
                    Text(stringResource(R.string.section_build_subtitle, buildNumber, configurationName), style = MaterialTheme.typography.bodyMedium, maxLines = 1, overflow = TextOverflow.Ellipsis)
                }
            },
            navigationIcon = { IconButton(onClick = onUp) { Icon(painterResource(R.drawable.ic_details_back), stringResource(R.string.navigate_up)) } }
        )
    }, modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection).testTag("details:section:${section.name}")) { padding -> Box(Modifier.fillMaxSize().padding(padding).consumeWindowInsets(padding)) { CompositionLocalProvider(LocalTeamCityAppBarState provides scrollBehavior.state) { content() } } }
}

@androidx.compose.ui.tooling.preview.Preview
@Composable
private fun BuildDetailsSectionPreview() {
    teamcityapp.libraries.theme.TeamCityTheme {
        BuildDetailsSectionScreen(BuildDetailsSection.Tests, "2458", "Build and test", {}) { Surface(Modifier.fillMaxSize()) {} }
    }
}
