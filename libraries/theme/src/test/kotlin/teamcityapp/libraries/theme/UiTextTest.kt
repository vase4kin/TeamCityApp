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

import android.app.Application
import android.content.res.Configuration
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Text
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalResources
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import java.util.Locale
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@OptIn(ExperimentalTestApi::class)
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35], application = Application::class, qualifiers = "en-rUS-w360dp-h800dp-mdpi")
class UiTextTest {
    @get:Rule val compose = createComposeRule()

    @Test fun retainedDescriptorResolvesAgainstTheCurrentLocaleAndPreservesServerText() {
        val message = UiText.Resource(android.R.string.cancel)
        val serverMessage = UiText.Dynamic("Server unavailable")
        val locale = mutableStateOf(Locale.US)
        compose.setContent {
            val context = LocalContext.current
            val configuration = Configuration(context.resources.configuration).apply { setLocale(locale.value) }
            val resources = context.createConfigurationContext(configuration).resources
            CompositionLocalProvider(LocalResources provides resources) {
                Column {
                    Text(message.resolve())
                    Text(serverMessage.resolve())
                }
            }
        }
        compose.onNodeWithText("Cancel").assertIsDisplayed()
        compose.onNodeWithText("Server unavailable").assertIsDisplayed()
        compose.runOnIdle { locale.value = Locale.FRANCE }
        compose.onNodeWithText("Annuler").assertIsDisplayed()
        compose.onNodeWithText("Server unavailable").assertIsDisplayed()
    }
}
