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

import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.windowInsetsTopHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import teamcityapp.libraries.resources.R as SharedR

private val LocalDarkTheme = staticCompositionLocalOf { false }

enum class ScreenNavigation { Back, Close }

@Composable
fun TeamCityTheme(darkTheme: Boolean = isSystemInDarkTheme(), content: @Composable () -> Unit) {
    val colors = if (darkTheme) DarkColorScheme else LightColorScheme
    CompositionLocalProvider(LocalDarkTheme provides darkTheme) {
        MaterialTheme(colorScheme = colors, typography = TeamCityTypography, content = content)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TeamCityScreen(
    title: String,
    onClose: () -> Unit,
    navigation: ScreenNavigation = ScreenNavigation.Close,
    scrollToolbarWithContent: Boolean = false,
    appBarHeight: Dp = TopAppBarDefaults.TopAppBarExpandedHeight,
    titleStyle: TextStyle = MaterialTheme.typography.titleLarge,
    titleStartPadding: Dp = 0.dp,
    appBarColors: TopAppBarColors? = null,
    containerColor: Color = MaterialTheme.colorScheme.background,
    content: @Composable (Modifier) -> Unit
) {
    val scrollBehavior = if (scrollToolbarWithContent) TopAppBarDefaults.enterAlwaysScrollBehavior() else TopAppBarDefaults.pinnedScrollBehavior()
    val colors = appBarColors ?: if (LocalDarkTheme.current) TopAppBarDefaults.topAppBarColors()
    else TopAppBarDefaults.topAppBarColors(
        containerColor = MaterialTheme.colorScheme.primary,
        scrolledContainerColor = MaterialTheme.colorScheme.primary,
        navigationIconContentColor = MaterialTheme.colorScheme.onPrimary,
        titleContentColor = MaterialTheme.colorScheme.onPrimary,
        actionIconContentColor = MaterialTheme.colorScheme.onPrimary
    )
    val description = stringResource(
        if (navigation == ScreenNavigation.Back) R.string.action_back else R.string.action_close
    )
    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        containerColor = containerColor,
        topBar = {
            Box {
                TopAppBar(
                    modifier = if (LocalDarkTheme.current) Modifier else Modifier.shadow(4.dp, clip = false),
                    title = { Text(title, modifier = Modifier.padding(start = titleStartPadding), style = titleStyle, maxLines = 1, overflow = TextOverflow.Ellipsis) },
                    expandedHeight = appBarHeight,
                    navigationIcon = {
                        TooltipBox(
                            positionProvider = TooltipDefaults.rememberTooltipPositionProvider(TooltipAnchorPosition.Above),
                            tooltip = { PlainTooltip { Text(description) } },
                            state = rememberTooltipState()
                        ) {
                            IconButton(onClick = onClose) {
                                Icon(
                                    painterResource(
                                        if (navigation == ScreenNavigation.Back) R.drawable.ic_arrow_back_24dp
                                        else R.drawable.ic_close_black_24dp
                                    ),
                                    contentDescription = description
                                )
                            }
                        }
                    },
                    colors = colors,
                    scrollBehavior = scrollBehavior
                )
                // Match the legacy Activity theme in the system-bar area under edge-to-edge.
                Box(Modifier.fillMaxWidth().windowInsetsTopHeight(WindowInsets.statusBars)
                    .background(if (LocalDarkTheme.current) colorResource(SharedR.color.black_800) else colors.containerColor))
            }
        }
    ) { padding -> content(Modifier.fillMaxSize().padding(padding)) }
}

@Composable
fun LoadingContent(modifier: Modifier = Modifier, color: Color = MaterialTheme.colorScheme.primary) {
    Box(modifier, contentAlignment = Alignment.Center) { CircularProgressIndicator(color = color) }
}

@Composable
fun MessageContent(message: String, modifier: Modifier = Modifier, action: (@Composable () -> Unit)? = null) {
    Box(modifier.padding(24.dp), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(message, style = MaterialTheme.typography.bodyLarge)
            action?.invoke()
        }
    }
}

@Preview
@Composable
private fun ScreenPreview() {
    TeamCityTheme { TeamCityScreen("TeamCity", {}) { MessageContent("Content", it) } }
}

@Preview
@Composable
private fun LoadingPreview() {
    TeamCityTheme { LoadingContent(Modifier.fillMaxSize()) }
}
