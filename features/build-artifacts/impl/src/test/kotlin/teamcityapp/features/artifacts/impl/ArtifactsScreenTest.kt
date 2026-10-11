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

package teamcityapp.features.artifacts.impl

import android.app.Application
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.v2.createComposeRule
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode
import teamcityapp.features.artifacts.api.*
import teamcityapp.libraries.list_state.ListUiState
import teamcityapp.libraries.theme.TeamCityTheme

@OptIn(ExperimentalTestApi::class)
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35], application = Application::class, qualifiers = "en-rUS-w360dp-h800dp-mdpi")
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class ArtifactsScreenTest {
    @get:Rule val compose = createComposeRule()
    private val folder = Artifact("folder", "/folder", childrenHref = "/children")
    private val file = Artifact("archive.zip", "/zip", 2048, "/content", "/zip/children")
    private val rows = ListUiState.Content(listOf(folder, file))

    @Test fun rowsDispatchClickAndLongClickWithTheirExactMetadata() {
        var clicked: Artifact? = null
        var longClicked: Artifact? = null
        compose.setContent { TeamCityTheme { ArtifactsScreen(ArtifactsUiState("Artifacts", rows), true, null, {}, {}, {}, { clicked = it }, { longClicked = it }, {}, {}, {}) } }
        compose.onNodeWithTag("artifacts:row:/folder").performClick()
        assertEquals(folder, clicked)
        compose.onNodeWithTag("artifacts:row:/zip").performTouchInput { longClick() }
        assertEquals(file, longClicked)
    }

    @Test fun downloadFailureKeepsRowsAndOffersRetry() {
        var retries = 0
        compose.setContent { TeamCityTheme { ArtifactsScreen(ArtifactsUiState(list = rows, download = ArtifactDownloadState.Failed(ArtifactDownload("archive.zip", "/content"))), false, null, {}, {}, {}, {}, {}, { retries++ }, {}, {}) } }
        compose.onNodeWithTag("artifacts:row:/folder").assertIsDisplayed()
        compose.onNodeWithText("Retry download").performClick()
        assertEquals(1, retries)
    }

    @Test fun pendingDownloadHasAnExplicitCancel() {
        var cancels = 0
        compose.setContent { TeamCityTheme { ArtifactsScreen(ArtifactsUiState(list = rows, download = ArtifactDownloadState.Downloading(ArtifactDownload("archive.zip", "/content"))), false, null, {}, {}, {}, {}, {}, {}, { cancels++ }, {}) } }
        compose.onNodeWithText("Cancel").performClick()
        assertEquals(1, cancels)
    }

    @Test fun platformErrorCanBeDismissed() {
        var dismisses = 0
        compose.setContent { TeamCityTheme { ArtifactsScreen(ArtifactsUiState(list = rows), false, ArtifactPlatformError.FileUnavailable, {}, {}, {}, {}, {}, {}, {}, { dismisses++ }) } }
        compose.onNodeWithText("No installed app could open this file.").assertIsDisplayed()
        compose.onNodeWithText("OK").performClick()
        assertEquals(1, dismisses)
    }
}
