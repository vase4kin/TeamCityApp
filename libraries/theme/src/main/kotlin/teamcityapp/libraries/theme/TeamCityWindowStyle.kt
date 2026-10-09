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

import android.view.Window
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.core.view.WindowCompat

/** The owning Activity or Dialog supplies its window; embedded fragments inherit their host. */
@Composable
fun TeamCitySystemBars(window: Window?, darkTheme: Boolean = isSystemInDarkTheme()) {
    DisposableEffect(window, darkTheme) {
        val controller = window?.let { WindowCompat.getInsetsController(it, it.decorView) }
        val statusWasLight = controller?.isAppearanceLightStatusBars
        val navigationWasLight = controller?.isAppearanceLightNavigationBars
        if (window != null) applyTeamCityWindowStyle(window, darkTheme)
        onDispose {
            if (statusWasLight != null) controller.isAppearanceLightStatusBars = statusWasLight
            if (navigationWasLight != null) controller.isAppearanceLightNavigationBars = navigationWasLight
        }
    }
}

/** Configure only the supplied owner, including a native bottom sheet's separate dialog window. */
fun applyTeamCityWindowStyle(window: Window, darkTheme: Boolean) {
    val controller = WindowCompat.getInsetsController(window, window.decorView)
    controller.isAppearanceLightStatusBars = !darkTheme
    controller.isAppearanceLightNavigationBars = !darkTheme
}
