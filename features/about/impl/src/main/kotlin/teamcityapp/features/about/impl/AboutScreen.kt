/*
 * Copyright 2020 Andrey Tolpeev
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

package teamcityapp.features.about.impl

import androidx.annotation.DrawableRes
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.material3.*
import androidx.compose.material3.windowsizeclass.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import teamcityapp.libraries.resources.R as SharedR
import teamcityapp.libraries.theme.ErrorNotice
import teamcityapp.libraries.theme.LoadingContent
import teamcityapp.libraries.theme.R as ThemeR
import teamcityapp.libraries.theme.ScreenNavigation
import teamcityapp.libraries.theme.TeamCityScreen
import teamcityapp.libraries.theme.TeamCityTheme

enum class AboutAction { Rate, Issue, Source, Libraries, Website, Email, Privacy }

@OptIn(ExperimentalMaterial3WindowSizeClassApi::class)
@Composable
fun AboutScreen(
    state: AboutUiState,
    onOpenUrl: (String) -> Unit,
    onAction: (AboutAction) -> Unit,
    onClose: () -> Unit,
    appVersion: String = BuildConfig.VERSION
) {
    val configuration = LocalConfiguration.current
    val window = WindowSizeClass.calculateFromSize(DpSize(configuration.screenWidthDp.dp, configuration.screenHeightDp.dp))
    TeamCityScreen(stringResource(SharedR.string.drawer_item_about), onClose, ScreenNavigation.Back, contentMaxWidth = 840.dp) { modifier ->
        when (state) {
            AboutUiState.Loading -> LoadingContent(modifier)

            is AboutUiState.Content -> Box(modifier, contentAlignment = Alignment.TopCenter) {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(if (window.widthSizeClass == WindowWidthSizeClass.Expanded) 2 else 1),
                    modifier = Modifier.widthIn(max = 840.dp).fillMaxWidth().fillMaxHeight(),
                    contentPadding = PaddingValues(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    item(span = { GridItemSpan(maxLineSpan) }) {
                        Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)) {
                            Column(Modifier.padding(24.dp)) {
                                Text(stringResource(R.string.about_app_name), style = MaterialTheme.typography.headlineLarge)
                                Text(appVersion, Modifier.padding(top = 8.dp), style = MaterialTheme.typography.titleMedium)
                            }
                        }
                    }
                    item {
                        AboutSection(stringResource(R.string.about_app_text_server_info)) {
                            val info = (state.serverDetails as? ServerDetailsUiState.Available)?.info
                            if (info != null) {
                                AboutRow(stringResource(R.string.about_version), ThemeR.drawable.ic_info_outline_black_24dp, info.version)
                                AboutRow(stringResource(R.string.about_app_text_server_url), R.drawable.ic_web_black_24dp, info.webUrl) { onOpenUrl(info.webUrl) }
                            } else {
                                ErrorNotice(stringResource(R.string.server_unavailable), modifier = Modifier.padding(16.dp).testTag("about:server-unavailable"))
                            }
                        }
                    }
                    item {
                        AboutSection(stringResource(R.string.about_app_text_app)) {
                            AboutRow(stringResource(R.string.about_app_text_rate_app), R.drawable.ic_star_border_black_24dp) {
                                onAction(AboutAction.Rate)
                            }
                            AboutRow(
                                stringResource(R.string.about_app_text_found_issue),
                                R.drawable.ic_question_answer_black_24dp,
                                stringResource(R.string.about_app_subtext_found_issue)
                            ) { onAction(AboutAction.Issue) }
                        }
                    }
                    item {
                        AboutSection(stringResource(R.string.about_app_text_dev)) {
                            AboutRow(stringResource(R.string.about_app_text_source_code), R.drawable.ic_github_circle) {
                                onAction(AboutAction.Source)
                            }
                            AboutRow(stringResource(R.string.about_app_text_libraries), R.drawable.ic_github_circle) {
                                onAction(AboutAction.Libraries)
                            }
                        }
                    }
                    item {
                        AboutSection(stringResource(R.string.about_app_text_contacts)) {
                            AboutRow(
                                stringResource(R.string.about_app_text_web),
                                R.drawable.ic_web_black_24dp,
                                stringResource(R.string.about_app_url_web)
                            ) { onAction(AboutAction.Website) }
                            AboutRow(
                                stringResource(R.string.about_app_text_email),
                                R.drawable.ic_email_black_24dp,
                                stringResource(R.string.about_app_email)
                            ) { onAction(AboutAction.Email) }
                            AboutRow(stringResource(SharedR.string.about_app_text_privacy), R.drawable.ic_web_black_24dp) {
                                onAction(AboutAction.Privacy)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun AboutSection(title: String, content: @Composable ColumnScope.() -> Unit) {
    Card(Modifier.fillMaxWidth().testTag("about:section"), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow)) {
        Text(title, Modifier.padding(20.dp), style = MaterialTheme.typography.titleLarge)
        content()
    }
}

@Composable
private fun AboutRow(text: String, @DrawableRes icon: Int, detail: String? = null, onClick: (() -> Unit)? = null) {
    ListItem(
        headlineContent = { Text(text) },
        supportingContent = detail?.let { { Text(it) } },
        leadingContent = { Icon(painterResource(icon), contentDescription = null) },
        colors = ListItemDefaults.colors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
        modifier = if (onClick == null) Modifier else Modifier.clickable(role = Role.Button, onClick = onClick)
    )
}

@Preview
@Composable
private fun AboutContentPreview() {
    TeamCityTheme { AboutScreen(AboutUiState.Content(ServerDetailsUiState.Available(ServerDetailsUiModel("2026.1", "https://teamcity.example"))), {}, {}, {}) }
}

@Preview(uiMode = android.content.res.Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun AboutOfflinePreview() {
    TeamCityTheme { AboutScreen(AboutUiState.Content(ServerDetailsUiState.Unavailable), {}, {}, {}) }
}

@Preview
@Composable
private fun AboutLoadingPreview() {
    TeamCityTheme { AboutScreen(AboutUiState.Loading, {}, {}, {}) }
}

@Preview(widthDp = 1000, heightDp = 700)
@Composable
private fun AboutWidePreview() {
    TeamCityTheme { AboutScreen(AboutUiState.Content(ServerDetailsUiState.Unavailable), {}, {}, {}) }
}
