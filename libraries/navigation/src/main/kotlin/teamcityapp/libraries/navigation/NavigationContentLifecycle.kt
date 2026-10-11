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

package teamcityapp.libraries.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LifecycleRegistry
import androidx.lifecycle.compose.LocalLifecycleOwner

/** Temporarily caps destination UI while the host applies an incoming account/navigation request. */
val LocalNavigationContentLifecycle = staticCompositionLocalOf<Lifecycle?> { null }

// Entry decorators must continue to use Navigation 3's original lifecycle for retained stores.
internal val LocalNavigationEntryLifecycleOwner = staticCompositionLocalOf<LifecycleOwner?> { null }

@Composable
internal fun NavigationContentLifecycle(content: @Composable () -> Unit) {
    val entryOwner = LocalLifecycleOwner.current
    val cap = LocalNavigationContentLifecycle.current
    if (cap == null) {
        content()
        return
    }
    val owner = remember(entryOwner, cap) { CappedNavigationLifecycle(entryOwner.lifecycle, cap) }
    DisposableEffect(owner) {
        owner.observe()
        onDispose { owner.dispose() }
    }
    CompositionLocalProvider(
        LocalNavigationEntryLifecycleOwner provides entryOwner,
        LocalLifecycleOwner provides owner,
        content = content
    )
}

internal class CappedNavigationLifecycle(private val entry: Lifecycle, private val cap: Lifecycle) : LifecycleOwner {
    private val registry = LifecycleRegistry(this)
    override val lifecycle: Lifecycle get() = registry
    private val observer = LifecycleEventObserver { _, _ -> synchronize() }

    fun observe() {
        entry.addObserver(observer)
        cap.addObserver(observer)
        synchronize()
    }

    fun dispose() {
        entry.removeObserver(observer)
        cap.removeObserver(observer)
        if (registry.currentState.isAtLeast(Lifecycle.State.CREATED)) registry.currentState = Lifecycle.State.DESTROYED
    }

    private fun synchronize() {
        val state = minOf(entry.currentState, cap.currentState)
        if (registry.currentState != state) registry.currentState = state
    }
}
