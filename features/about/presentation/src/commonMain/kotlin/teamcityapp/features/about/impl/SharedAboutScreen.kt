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
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalWindowInfo
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import teamcityapp.features.about.presentation.resources.*
import teamcityapp.libraries.design_system.SharedOptionalErrorNotice
import teamcityapp.libraries.design_system.resolve
import teamcityapp.libraries.theme.LoadingContent
import teamcityapp.libraries.theme.SharedTeamCityScreen
import teamcityapp.libraries.theme.TeamCityDimensions
import teamcityapp.libraries.theme.TeamCityTheme

enum class AboutAction { Rate, Issue, Source, Libraries, Website, Email, Privacy }

@OptIn(ExperimentalMaterial3WindowSizeClassApi::class)
@Composable
fun SharedAboutScreen(
    state: AboutUiState,
    onOpenUrl: (String) -> Unit,
    onAction: (AboutAction) -> Unit,
    onClose: () -> Unit,
    appVersion: String
) {
    val versionDescription = stringResource(Res.string.about_application_version_description, appVersion)
    val size = LocalWindowInfo.current.containerSize
    val window = with(LocalDensity.current) { WindowSizeClass.calculateFromSize(DpSize(size.width.toDp(), size.height.toDp())) }
    SharedTeamCityScreen(stringResource(Res.string.drawer_item_about), onClose, contentMaxWidth = TeamCityDimensions.aboutContentMaxWidth) { modifier ->
        when (state) {
            AboutUiState.Loading -> LoadingContent(modifier)

            is AboutUiState.Content -> Box(modifier, contentAlignment = Alignment.TopCenter) {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(if (window.widthSizeClass == WindowWidthSizeClass.Expanded) 2 else 1),
                    modifier = Modifier.widthIn(max = TeamCityDimensions.aboutContentMaxWidth).fillMaxWidth().fillMaxHeight(),
                    contentPadding = PaddingValues(TeamCityDimensions.contentPadding),
                    horizontalArrangement = Arrangement.spacedBy(TeamCityDimensions.contentPadding),
                    verticalArrangement = Arrangement.spacedBy(TeamCityDimensions.contentPadding)
                ) {
                    item(span = { GridItemSpan(maxLineSpan) }) {
                        Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)) {
                            Column(Modifier.padding(TeamCityDimensions.sectionSpacing)) {
                                Text(stringResource(Res.string.about_app_name), style = MaterialTheme.typography.headlineLarge)
                                Text(appVersion, Modifier.padding(top = TeamCityDimensions.smallSpacing).semantics { contentDescription = versionDescription }, style = MaterialTheme.typography.titleMedium)
                            }
                        }
                    }
                    item {
                        AboutSection(stringResource(Res.string.about_app_text_server_info)) {
                            val info = (state.serverDetails as? ServerDetailsUiState.Available)?.info
                            if (info != null) {
                                AboutRow(stringResource(Res.string.about_version), Res.drawable.ic_info_outline_black_24dp, info.version)
                                AboutRow(stringResource(Res.string.about_app_text_server_url), Res.drawable.ic_web_black_24dp, info.webUrl) { onOpenUrl(info.webUrl) }
                            } else {
                                SharedOptionalErrorNotice(ServerDetailsUiState.Unavailable.message.resolve(), modifier = Modifier.padding(TeamCityDimensions.contentPadding).testTag("about:server-unavailable"))
                            }
                        }
                    }
                    item {
                        AboutSection(stringResource(Res.string.about_app_text_app)) {
                            AboutRow(stringResource(Res.string.about_app_text_rate_app), Res.drawable.ic_star_border_black_24dp) {
                                onAction(AboutAction.Rate)
                            }
                            AboutRow(
                                stringResource(Res.string.about_app_text_found_issue),
                                Res.drawable.ic_question_answer_black_24dp,
                                stringResource(Res.string.about_app_subtext_found_issue)
                            ) { onAction(AboutAction.Issue) }
                        }
                    }
                    item {
                        AboutSection(stringResource(Res.string.about_app_text_dev)) {
                            AboutRow(stringResource(Res.string.about_app_text_source_code), Res.drawable.ic_github_circle) {
                                onAction(AboutAction.Source)
                            }
                            AboutRow(stringResource(Res.string.about_app_text_libraries), Res.drawable.ic_github_circle) {
                                onAction(AboutAction.Libraries)
                            }
                        }
                    }
                    item {
                        AboutSection(stringResource(Res.string.about_app_text_contacts)) {
                            AboutRow(
                                stringResource(Res.string.about_app_text_web),
                                Res.drawable.ic_web_black_24dp,
                                stringResource(Res.string.about_app_url_web)
                            ) { onAction(AboutAction.Website) }
                            AboutRow(
                                stringResource(Res.string.about_app_text_email),
                                Res.drawable.ic_email_black_24dp,
                                stringResource(Res.string.about_app_email)
                            ) { onAction(AboutAction.Email) }
                            AboutRow(stringResource(Res.string.about_app_text_privacy), Res.drawable.ic_web_black_24dp) {
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
        Text(title, Modifier.padding(TeamCityDimensions.largeContentPadding), style = MaterialTheme.typography.titleLarge)
        content()
    }
}

@Composable
private fun AboutRow(text: String, icon: DrawableResource, detail: String? = null, onClick: (() -> Unit)? = null) {
    ListItem(
        headlineContent = { Text(text) },
        supportingContent = detail?.let { { Text(it) } },
        leadingContent = { Icon(painterResource(icon), contentDescription = null) },
        colors = ListItemDefaults.colors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
        modifier = if (onClick == null) {
            Modifier
        } else {
            Modifier.clickable(role = Role.Button, onClick = onClick).semantics {
                contentDescription = listOfNotNull(text, detail).joinToString(", ")
            }
        }
    )
}
