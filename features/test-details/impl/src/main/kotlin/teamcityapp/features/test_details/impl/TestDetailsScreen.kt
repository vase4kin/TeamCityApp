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

/* Updated for Compose in 2026. */

package teamcityapp.features.test_details.impl

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.PlatformTextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import teamcityapp.libraries.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TestDetailsScreen(state: TestDetailsUiState, onRetry: () -> Unit, onClose: () -> Unit) {
    val appBarHeight = dimensionResource(androidx.appcompat.R.dimen.abc_action_bar_default_height_material)
    val barColor = colorResource(R.color.test_details_toolbar)
    TeamCityScreen(
        title = stringResource(R.string.test_details_title),
        onClose = onClose,
        appBarHeight = appBarHeight,
        titleStartPadding = 16.dp,
        titleStyle = TextStyle(fontSize = 20.sp, fontWeight = FontWeight.Medium, platformStyle = PlatformTextStyle(includeFontPadding = true)),
        appBarColors = TopAppBarDefaults.topAppBarColors(
            containerColor = barColor, scrolledContainerColor = barColor,
            titleContentColor = colorResource(R.color.test_details_toolbar_title), navigationIconContentColor = Color.White),
        containerColor = colorResource(R.color.test_details_background),
    ) { modifier ->
        when (state) {
            TestDetailsUiState.Loading -> LoadingContent(modifier.offset(y = -appBarHeight / 2), color = colorResource(R.color.test_details_progress))
            TestDetailsUiState.Empty -> Box(modifier.offset(y = -appBarHeight / 2), contentAlignment = Alignment.Center) {
                Text(stringResource(R.string.text_empty_test_details), color = colorResource(R.color.test_details_primary),
                    style = TextStyle(fontSize = 16.sp, letterSpacing = 0.5.sp,
                        platformStyle = PlatformTextStyle(includeFontPadding = true)))
            }
            TestDetailsUiState.Error -> ErrorContent(modifier, onRetry)
            TestDetailsUiState.InvalidInput -> Unit
            is TestDetailsUiState.Content -> SelectionContainer(modifier.background(Color.White)) {
                Text(
                    text = remember(state.details) { formatTestDetails(state.details) },
                    color = Color.Black,
                    // Match the old escaped HTML body: default sans-serif text and an 8px body margin.
                    style = TextStyle(fontFamily = FontFamily.SansSerif, fontSize = 16.sp, lineHeight = 19.sp),
                    modifier = Modifier.fillMaxSize().testTag("test_details:text")
                        .verticalScroll(rememberScrollState()).padding(8.dp),
                )
            }
        }
    }
}

@Preview
@Composable
private fun DetailsPreview() {
    TeamCityTheme { TestDetailsScreen(TestDetailsUiState.Content("java.lang.AssertionError: expected <true> but was <false>\n    at example.Test.test(Test.kt:42)"), {}, {}) }
}

@Preview
@Composable
private fun EmptyPreview() { TeamCityTheme { TestDetailsScreen(TestDetailsUiState.Empty, {}, {}) } }

@Preview
@Composable
private fun ErrorPreview() { TeamCityTheme { TestDetailsScreen(TestDetailsUiState.Error, {}, {}) } }
