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

package teamcityapp.shared

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import kotlinx.coroutines.awaitCancellation
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.stringResource
import teamcityapp.features.about.api.AboutRepository
import teamcityapp.features.about.api.AboutServerInfo
import teamcityapp.features.about.impl.SharedAboutRoute
import teamcityapp.libraries.theme.TeamCityTheme
import teamcityapp.shared.resources.*

/** Deterministic Phase 1 composition root; real account/network integration starts in Phase 2. */
enum class AboutFixture { Available, Unavailable, Loading }

internal class FixtureAboutRepository(private val fixture: AboutFixture) : AboutRepository {
    override suspend fun serverInfo(): AboutServerInfo = when (fixture) {
        AboutFixture.Available -> AboutServerInfo("2026.1", "https://teamcity.example")
        AboutFixture.Unavailable -> error("Deterministic unavailable server")
        AboutFixture.Loading -> awaitCancellation()
    }
}

@Composable
fun PhaseOneApp(fixture: AboutFixture = AboutFixture.Available, darkTheme: Boolean = false) {
    val repository = remember(fixture) { FixtureAboutRepository(fixture) }
    val snackbar = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val unavailable = stringResource(Res.string.preview_action_unavailable)
    val notify: () -> Unit = { scope.launch { snackbar.showSnackbar(unavailable) } }
    TeamCityTheme(darkTheme) {
        Box(Modifier.fillMaxSize()) {
            SharedAboutRoute(repository, APP_VERSION, { notify() }, { notify() }, notify)
            SnackbarHost(snackbar, Modifier.align(androidx.compose.ui.Alignment.BottomCenter))
        }
    }
}
