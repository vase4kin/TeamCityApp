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

package teamcityapp.features.splash.impl

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.*
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import teamcityapp.libraries.theme.ErrorContent
import teamcityapp.libraries.theme.TeamCityTheme

@Composable
fun SplashScreen(state: SplashUiState, modifier: Modifier = Modifier, onRetry: () -> Unit = {}) {
    Surface(modifier.fillMaxSize().testTag("splash:screen"), color = if (state == SplashUiState.Error) MaterialTheme.colorScheme.surface else MaterialTheme.colorScheme.primaryContainer, contentColor = MaterialTheme.colorScheme.onSurface) {
        Box(contentAlignment = Alignment.Center) {
            if (state == SplashUiState.Error) {
                ErrorContent(Modifier.fillMaxSize().testTag("splash:error"), onRetry, message = stringResource(R.string.splash_load_error))
            } else {
                Box(
                    Modifier.testTag(
                        when (state) {
                            SplashUiState.Loading -> "splash:loading"
                            is SplashUiState.Ready -> "splash:ready:${state.destination.name}"
                            else -> "splash:navigated"
                        }
                    )
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        SplashLogo()
                        if (state == SplashUiState.Loading) {
                            Spacer(Modifier.height(32.dp))
                            CircularProgressIndicator(Modifier.size(32.dp))
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SplashLogo() {
    Image(painterResource(teamcityapp.libraries.resources.R.drawable.ic_launcher), stringResource(R.string.splash_logo_description), Modifier.size(160.dp).testTag("splash:logo"))
}

@Preview(showBackground = true, widthDp = 360, heightDp = 800)
@Composable
private fun SplashPreview() {
    TeamCityTheme { SplashScreen(SplashUiState.Loading) }
}

@Preview(showBackground = true, widthDp = 360, heightDp = 800)
@Composable
private fun SplashErrorPreview() {
    TeamCityTheme { SplashScreen(SplashUiState.Error) }
}
