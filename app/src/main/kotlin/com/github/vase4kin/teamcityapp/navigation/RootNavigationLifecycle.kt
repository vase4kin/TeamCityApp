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
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LifecycleRegistry

/** Prevents retired content from resuming while an incoming navigation request is applied. */
internal class RootNavigationLifecycle(private val parent: Lifecycle) : LifecycleOwner {
    private val registry = LifecycleRegistry(this)
    private var held = false
    override val lifecycle: Lifecycle = object : Lifecycle() {
        // Compose can dispose before our parent observer receives ON_DESTROY. Navigation's
        // ViewModel provider must still see the destroyed Activity to retain stores on rotation.
        override val currentState: State get() = minOf(parent.currentState, registry.currentState)
        override fun addObserver(observer: LifecycleObserver) = registry.addObserver(observer)
        override fun removeObserver(observer: LifecycleObserver) = registry.removeObserver(observer)
    }
    init {
        parent.addObserver(LifecycleEventObserver { _, _ -> synchronize() })
        synchronize()
    }
    fun hold() {
        held = true
        synchronize()
    }
    fun applied() {
        held = false
        synchronize()
    }
    private fun synchronize() {
        val parentState = parent.currentState
        val state = if (held && parentState.isAtLeast(Lifecycle.State.CREATED)) Lifecycle.State.CREATED else parentState
        if (registry.currentState != state) registry.currentState = state
    }
}
