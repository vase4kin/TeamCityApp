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

import android.os.Bundle

/** The root owns Compose state; retired native hosts must never reconstruct their fragments. */
internal object RootActivitySavedState {
    const val ACCOUNT_KEY = "navigation.launch-account"
    const val LAUNCH_ACCOUNT_KEY = "navigation.intent-account"
    const val LAUNCH_DESTINATION = "navigation.saved-launch-destination"
    private const val FRAGMENTS_KEY = "android:support:fragments"
    private const val REGISTRY_KEY = "androidx.lifecycle.BundlableSavedStateRegistry.key"

    fun canRestoreLaunch(savedState: Bundle?, accountKey: String): Boolean = savedState == null || (savedState.getString(LAUNCH_ACCOUNT_KEY) ?: savedState.getString(ACCOUNT_KEY)) == accountKey

    data class RestoredLaunch(val payload: String?)

    fun restoredLaunch(savedState: Bundle?, accountKey: String): RestoredLaunch? = savedState?.takeIf { canRestoreLaunch(it, accountKey) && it.containsKey(LAUNCH_DESTINATION) }?.let {
        RestoredLaunch(it.getString(LAUNCH_DESTINATION))
    }

    fun accountChanged(savedState: Bundle?, accountKey: String): Boolean = savedState?.getString(ACCOUNT_KEY)?.let { it != accountKey } ?: false

    fun forRoot(savedState: Bundle?): Bundle? = savedState?.let { original ->
        Bundle(original).apply {
            remove(FRAGMENTS_KEY)
            original.getBundle(REGISTRY_KEY)?.let { components ->
                putBundle(REGISTRY_KEY, Bundle(components).apply { remove(FRAGMENTS_KEY) })
            }
        }
    }
}
