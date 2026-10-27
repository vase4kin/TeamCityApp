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

import android.app.Application
import androidx.compose.runtime.*
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LifecycleRegistry
import androidx.lifecycle.compose.LocalLifecycleOwner
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35], application = Application::class)
class SharedScreenAnalyticsTest {
    @get:Rule val compose = createComposeRule()

    @Test fun visibleViewsAreNotRepeatedByRecompositionAndResumeUsesTheLatestCallback() {
        val owner = Owner()
        val revision = mutableIntStateOf(0)
        val events = mutableListOf<Int>()
        compose.runOnUiThread { owner.registry.currentState = Lifecycle.State.STARTED }
        compose.setContent {
            CompositionLocalProvider(LocalLifecycleOwner provides owner) {
                val value = revision.intValue
                SharedScreenAnalytics("visit") { events += value }
            }
        }
        compose.runOnIdle {
            assertTrue(events.isEmpty())
            owner.registry.currentState = Lifecycle.State.RESUMED
        }
        compose.runOnIdle {
            assertEquals(listOf(0), events)
            revision.intValue = 1
        }
        compose.runOnIdle {
            assertEquals(listOf(0), events)
            owner.registry.currentState = Lifecycle.State.STARTED
        }
        compose.runOnIdle { owner.registry.currentState = Lifecycle.State.RESUMED }
        compose.runOnIdle { assertEquals(listOf(0, 1), events) }
    }

    @Test fun aNewVisitOrAccountOwnerGetsItsOwnViewWithoutReplayingTheOldOwner() {
        val owner = Owner()
        val visit = mutableStateOf("alice")
        val events = mutableListOf<String>()
        compose.runOnUiThread { owner.registry.currentState = Lifecycle.State.RESUMED }
        compose.setContent {
            CompositionLocalProvider(LocalLifecycleOwner provides owner) {
                val identity = visit.value
                SharedScreenAnalytics(identity) { events += identity }
            }
        }
        compose.runOnIdle {
            assertEquals(listOf("alice"), events)
            visit.value = "bob"
        }
        compose.runOnIdle { assertEquals(listOf("alice", "bob"), events) }
    }

    private class Owner : LifecycleOwner {
        val registry = LifecycleRegistry(this)
        override val lifecycle: Lifecycle get() = registry
    }
}
