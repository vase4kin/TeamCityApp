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

package com.github.vase4kin.teamcityapp.navigation

import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LifecycleRegistry
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35], application = android.app.Application::class)
class RootNavigationLifecycleTest {
    @Test fun disposalSeesParentDestructionBeforeItsForwardingObserverRuns() {
        val parent = object : LifecycleOwner {
            val registry = LifecycleRegistry(this)
            override val lifecycle: Lifecycle get() = registry
        }
        lateinit var root: RootNavigationLifecycle
        var stateDuringDisposal: Lifecycle.State? = null
        // Downward events reach later observers first, as they do for Compose disposal.
        parent.registry.currentState = Lifecycle.State.RESUMED
        root = RootNavigationLifecycle(parent.lifecycle)
        parent.lifecycle.addObserver(
            LifecycleEventObserver { _, event ->
                if (event == Lifecycle.Event.ON_DESTROY) stateDuringDisposal = root.lifecycle.currentState
            }
        )
        parent.registry.currentState = Lifecycle.State.DESTROYED
        assertEquals(Lifecycle.State.DESTROYED, stateDuringDisposal)
    }

    @Test fun aPlatformResumeCannotRestartRetiredContentBeforeNavigationIsApplied() {
        val parent = object : LifecycleOwner {
            val registry = LifecycleRegistry(this)
            override val lifecycle: Lifecycle get() = registry
        }
        parent.registry.currentState = Lifecycle.State.RESUMED
        val root = RootNavigationLifecycle(parent.lifecycle)
        var resumes = 0
        root.lifecycle.addObserver(LifecycleEventObserver { _, event -> if (event == Lifecycle.Event.ON_RESUME) resumes++ })
        assertEquals(1, resumes)
        root.hold()
        assertEquals(Lifecycle.State.CREATED, root.lifecycle.currentState)
        parent.registry.currentState = Lifecycle.State.CREATED
        parent.registry.currentState = Lifecycle.State.RESUMED
        assertEquals(Lifecycle.State.CREATED, root.lifecycle.currentState)
        assertEquals(1, resumes)
        root.applied()
        assertEquals(Lifecycle.State.RESUMED, root.lifecycle.currentState)
        assertEquals(2, resumes)
        parent.registry.currentState = Lifecycle.State.DESTROYED
        root.applied()
        assertEquals(Lifecycle.State.DESTROYED, root.lifecycle.currentState)
        assertEquals(2, resumes)
    }
}
