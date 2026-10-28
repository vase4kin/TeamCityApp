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
import androidx.compose.runtime.*
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.lifecycle.*
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@OptIn(ExperimentalTestApi::class)
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35], application = Application::class)
class ArtifactDownloadLifetimeTest {
    @get:Rule val compose = createComposeRule()
    private val owner = object : LifecycleOwner {
        val registry = LifecycleRegistry.createUnsafe(this).apply { currentState = Lifecycle.State.RESUMED }
        override val lifecycle: Lifecycle = registry
    }

    @Test fun pausingForASheetKeepsTransferButNavigationAwayCancelsIt() {
        val active = mutableStateOf(true)
        var cancellations = 0
        compose.setContent { ArtifactDownloadLifetime(owner.lifecycle, active.value, { false }, { cancellations++ }) }
        compose.runOnIdle { owner.registry.currentState = Lifecycle.State.STARTED }
        compose.waitForIdle()
        assertEquals(0, cancellations)
        compose.runOnIdle { active.value = false }
        compose.waitForIdle()
        assertEquals(1, cancellations)
    }

    @Test fun backgroundingCancelsButConfigurationStopAndDisposalDoNot() {
        var rotating = true
        var cancellations = 0
        val present = mutableStateOf(true)
        compose.setContent { if (present.value) ArtifactDownloadLifetime(owner.lifecycle, true, { rotating }, { cancellations++ }) }
        compose.runOnIdle {
            owner.registry.currentState = Lifecycle.State.CREATED
            present.value = false
        }
        compose.waitForIdle()
        assertEquals(0, cancellations)
        compose.runOnIdle {
            owner.registry.currentState = Lifecycle.State.RESUMED
            present.value = true
            rotating = false
        }
        compose.waitForIdle()
        compose.runOnIdle { owner.registry.currentState = Lifecycle.State.CREATED }
        compose.waitForIdle()
        assertEquals(1, cancellations)
    }

    @Test fun removingScreenCancelsRetainedWork() {
        val present = mutableStateOf(true)
        var cancellations = 0
        compose.setContent { if (present.value) ArtifactDownloadLifetime(owner.lifecycle, true, { false }, { cancellations++ }) }
        compose.runOnIdle { present.value = false }
        compose.waitForIdle()
        assertEquals(1, cancellations)
    }
}
