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

package teamcityapp.features.properties.impl

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
import teamcityapp.features.properties.api.Property
import teamcityapp.libraries.theme.TeamCityTheme

@OptIn(ExperimentalTestApi::class)
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35], application = Application::class, qualifiers = "en-rUS-w360dp-h800dp-mdpi")
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class PropertiesScreenTest {
    @get:Rule val compose = createComposeRule()

    @Test fun nonEmptyValueOpensCopyWithOriginalNameAndValue() {
        val property = Property("sdk", "24")
        var copied: Property? = null
        compose.setContent { TeamCityTheme { PropertiesScreen(PropertiesUiState.Content(listOf(property)), { copied = it }) } }
        compose.onNodeWithTag("properties:row:0").performClick()
        assertEquals(property, copied)
    }

    @Test fun emptyValueIsLabeledAndCannotOpenCopy() {
        compose.setContent { TeamCityTheme { PropertiesScreen(PropertiesUiState.Content(listOf(Property("sdk", ""))), { fail("Empty value must not open copy") }) } }
        compose.onNodeWithText("Empty").assertIsDisplayed()
        compose.onNodeWithTag("properties:row:0").assertHasNoClickAction()
    }

    @Test fun longListScrollsToLastParameterAndKeepsItsAction() {
        val properties = (0..50).map { Property("name$it", "value$it") }
        var copied: Property? = null
        compose.setContent { TeamCityTheme { PropertiesScreen(PropertiesUiState.Content(properties), { copied = it }) } }
        compose.onNodeWithTag("properties:list").performScrollToIndex(50)
        compose.onNodeWithTag("properties:row:50").performClick()
        assertEquals(properties.last(), copied)
    }
}
