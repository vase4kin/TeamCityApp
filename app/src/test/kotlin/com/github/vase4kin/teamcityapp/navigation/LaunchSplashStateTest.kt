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

import org.junit.Assert.*
import org.junit.Test

class LaunchSplashStateTest {
    @Test fun holdsInitialPreparationAndReleasesAsSoonAsContentIsReady() {
        var now = 0L
        val splash = LaunchSplashState { now }
        assertTrue(splash.keepOnScreen())
        now = 50L
        splash.release()
        assertFalse(splash.keepOnScreen())
    }

    @Test fun slowPreparationGivesWayToOrdinaryLoadingAfterOneSecond() {
        var now = 100L
        val splash = LaunchSplashState { now }
        now = 1_099L
        assertTrue(splash.keepOnScreen())
        now = 1_100L
        assertFalse(splash.keepOnScreen())
    }

    @Test fun releasedSplashCannotReturnDuringRetryOrAccountSwitching() {
        val splash = LaunchSplashState { 0L }
        splash.release()
        repeat(3) { assertFalse(splash.keepOnScreen()) }
        // Each new activity owns a fresh launch policy; no app-wide state is retained.
        assertTrue(LaunchSplashState { 0L }.keepOnScreen())
    }
}
