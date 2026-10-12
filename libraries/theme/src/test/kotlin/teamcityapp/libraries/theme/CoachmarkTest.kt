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
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.unit.dp
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@OptIn(ExperimentalTestApi::class)
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35], application = Application::class)
class CoachmarkTest {
    @get:Rule val compose = createComposeRule()

    @Test fun nestedHostsTranslateRealControlBoundsAndDropDisposedTargets() {
        val anchors = CoachmarkAnchors()
        val inset = mutableStateOf(24.dp)
        val visible = mutableStateOf(true)
        compose.setContent {
            TeamCityTheme {
                Box(Modifier.padding(inset.value)) {
                    Box(Modifier.fillMaxSize().coachmarkHost(anchors).testTag("host")) {
                        CompositionLocalProvider(LocalCoachmarkAnchors provides anchors) {
                            if (visible.value) Box(Modifier.size(48.dp).coachmarkAnchor("target").testTag("target")) { IconButton({}) { Text("Target") } }
                        }
                    }
                }
            }
        }
        fun checkMeasuredBounds() {
            val host = compose.onNodeWithTag("host").fetchSemanticsNode().boundsInRoot
            val target = compose.onNodeWithTag("target").fetchSemanticsNode().boundsInRoot
            compose.runOnIdle { assertEquals(target.translate(-host.topLeft), anchors.bounds("target")) }
        }
        checkMeasuredBounds()
        compose.runOnIdle { inset.value = 48.dp }
        checkMeasuredBounds()
        compose.runOnIdle { visible.value = false }
        compose.waitForIdle()
        assertNull(anchors.bounds("target"))
    }
}
