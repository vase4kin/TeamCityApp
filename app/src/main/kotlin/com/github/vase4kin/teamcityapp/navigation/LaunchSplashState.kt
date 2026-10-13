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

import android.os.SystemClock

/** An activity's initial splash ends once content is ready, or gives way to ordinary loading UI. */
internal class LaunchSplashState(private val uptimeMillis: () -> Long = SystemClock::uptimeMillis) {
    private val startedAt = uptimeMillis()
    private var released = false

    fun release() {
        released = true
    }

    fun keepOnScreen(): Boolean {
        if (uptimeMillis() - startedAt >= MAX_WAIT_MILLIS) release()
        return !released
    }

    private companion object {
        const val MAX_WAIT_MILLIS = 1_000L
    }
}
