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

package teamcityapp.libraries.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import teamcityapp.libraries.design_system.resources.*

internal val LocalDarkTheme = staticCompositionLocalOf { false }

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun TeamCityTheme(darkTheme: Boolean = isSystemInDarkTheme(), content: @Composable () -> Unit) {
    CompositionLocalProvider(LocalDarkTheme provides darkTheme) {
        MaterialExpressiveTheme(
            colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme,
            typography = TeamCityTypography,
            shapes = TeamCityShapes,
            motionScheme = MotionScheme.expressive(),
            content = content
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SharedTeamCityScreen(
    title: String,
    onClose: () -> Unit,
    appBarHeight: Dp = TopAppBarDefaults.TopAppBarExpandedHeight,
    titleStyle: TextStyle = MaterialTheme.typography.titleLarge,
    titleStartPadding: Dp = TeamCityDimensions.noSpacing,
    appBarColors: TopAppBarColors? = null,
    containerColor: Color = MaterialTheme.colorScheme.background,
    contentMaxWidth: Dp = TeamCityDimensions.screenContentMaxWidth,
    actions: @Composable androidx.compose.foundation.layout.RowScope.() -> Unit = {},
    bottomBar: (@Composable () -> Unit)? = null,
    snackbarHost: @Composable () -> Unit = {},
    content: @Composable (Modifier) -> Unit
) = TeamCityScreenFrame(
    title, onClose, stringResource(Res.string.action_back), painterResource(Res.drawable.ic_arrow_back_24dp),
    appBarHeight, titleStyle, titleStartPadding, appBarColors, containerColor, contentMaxWidth,
    actions, bottomBar, snackbarHost, content
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TeamCityScreenFrame(
    title: String,
    onClose: () -> Unit,
    backLabel: String,
    backIcon: androidx.compose.ui.graphics.painter.Painter,
    appBarHeight: Dp = TopAppBarDefaults.TopAppBarExpandedHeight,
    titleStyle: TextStyle = MaterialTheme.typography.titleLarge,
    titleStartPadding: Dp = TeamCityDimensions.noSpacing,
    appBarColors: TopAppBarColors? = null,
    containerColor: Color = MaterialTheme.colorScheme.background,
    contentMaxWidth: Dp = TeamCityDimensions.screenContentMaxWidth,
    actions: @Composable androidx.compose.foundation.layout.RowScope.() -> Unit = {},
    bottomBar: (@Composable () -> Unit)? = null,
    snackbarHost: @Composable () -> Unit = {},
    content: @Composable (Modifier) -> Unit
) {
    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()
    val colors = appBarColors ?: teamCityTopAppBarColors()
    val description = backLabel
    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection).then(if (bottomBar != null) Modifier.imePadding() else Modifier),
        containerColor = containerColor,
        bottomBar = { bottomBar?.invoke() },
        snackbarHost = snackbarHost,
        topBar = {
            Box {
                TopAppBar(
                    modifier = Modifier.testTag("screen:app-bar"),
                    title = { Text(title, modifier = Modifier.padding(start = titleStartPadding), style = titleStyle, maxLines = 1, overflow = TextOverflow.Ellipsis) },
                    expandedHeight = appBarHeight,
                    navigationIcon = {
                        TeamCityTooltip(description) {
                            IconButton(onClick = onClose) {
                                Icon(
                                    backIcon,
                                    contentDescription = description
                                )
                            }
                        }
                    },
                    actions = actions,
                    colors = colors,
                    scrollBehavior = scrollBehavior
                )
            }
        }
    ) { padding ->
        Box(Modifier.fillMaxSize().padding(padding).consumeWindowInsets(padding), contentAlignment = Alignment.TopCenter) {
            CompositionLocalProvider(LocalTeamCityAppBarState provides scrollBehavior.state) {
                content(Modifier.widthIn(max = contentMaxWidth).fillMaxWidth().fillMaxHeight())
            }
        }
    }
}

@Composable
fun LoadingContent(modifier: Modifier = Modifier, color: Color = MaterialTheme.colorScheme.primary) {
    Box(modifier, contentAlignment = Alignment.Center) {
        Surface(shape = MaterialTheme.shapes.extraLarge, color = MaterialTheme.colorScheme.surfaceContainerHigh) {
            TeamCityLoadingIndicator(Modifier.padding(TeamCityDimensions.sectionSpacing), color = color)
        }
    }
}

@Composable
fun MessageContent(message: String, modifier: Modifier = Modifier, action: (@Composable () -> Unit)? = null) {
    Box(modifier.padding(TeamCityDimensions.sectionSpacing), contentAlignment = Alignment.Center) {
        Card(Modifier.widthIn(max = TeamCityDimensions.messageMaxWidth), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow)) {
            Column(Modifier.padding(TeamCityDimensions.sectionSpacing), horizontalAlignment = Alignment.CenterHorizontally) {
                Text(message, style = MaterialTheme.typography.titleMedium, textAlign = androidx.compose.ui.text.style.TextAlign.Center)
                action?.invoke()
            }
        }
    }
}

@Preview
@Composable
private fun ScreenPreview() {
    TeamCityTheme { SharedTeamCityScreen("TeamCity", {}) { MessageContent("Content", it) } }
}

@Preview
@Composable
private fun LoadingPreview() {
    TeamCityTheme { LoadingContent(Modifier.fillMaxSize()) }
}
