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

package teamcityapp.features.build_overview.impl

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
import teamcityapp.libraries.list_state.ListUiState
import teamcityapp.libraries.theme.TeamCityTheme

@OptIn(ExperimentalTestApi::class)
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35], application = Application::class, qualifiers = "en-rUS-w360dp-h800dp-mdpi")
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class BuildOverviewScreenTest {
    @get:Rule val compose = createComposeRule()

    @Test fun branchCardDispatchesItsTypedValue() {
        val rows = overviewRows(BuildOverviewFixtures.finished)
        var clicked: OverviewRow? = null
        compose.setContent { TeamCityTheme { BuildOverviewScreen(BuildOverviewUiState(list = ListUiState.Content(rows)), {}, {}, { clicked = it }) } }
        compose.onNodeWithTag("overview:row:Branch").performClick()
        assertEquals(rows.single { it.field == OverviewField.Branch }, clicked)
    }

    @Test fun initialErrorOffersRetry() {
        var retries = 0
        compose.setContent { TeamCityTheme { BuildOverviewScreen(BuildOverviewUiState(list = ListUiState.Error), {}, { retries++ }, {}) } }
        compose.onNodeWithText("Try again").performClick()
        assertEquals(1, retries)
    }

    @Test fun retainedRefreshFailureKeepsCardsAndOffersRetry() {
        var retries = 0
        compose.setContent { TeamCityTheme { BuildOverviewScreen(BuildOverviewUiState(list = ListUiState.Content(overviewRows(BuildOverviewFixtures.finished), refreshFailed = true)), {}, { retries++ }, {}) } }
        compose.onNodeWithTag("overview:row:Result").assertIsDisplayed()
        compose.onNodeWithText("Retry").performClick()
        assertEquals(1, retries)
    }
}
