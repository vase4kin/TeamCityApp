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

package teamcityapp.features.settings.impl

import android.app.Application
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LifecycleRegistry
import androidx.lifecycle.ViewModelStore
import androidx.lifecycle.compose.LocalLifecycleOwner
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito.*
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode
import teamcityapp.libraries.theme.TeamCityTheme
import kotlinx.coroutines.flow.MutableStateFlow
import teamcityapp.libraries.app_theme.ThemeMode
import teamcityapp.libraries.app_theme.ThemeOptions
import teamcityapp.libraries.app_theme.ThemePreferencesRepository
import teamcityapp.features.settings.impl.router.SettingsRouter
import teamcityapp.features.settings.impl.tracker.SettingsTracker

@OptIn(ExperimentalTestApi::class)
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35], application = Application::class, qualifiers = "en-rUS-w360dp-h800dp-mdpi")
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class SettingsRouteTest {
    @get:Rule val compose = createComposeRule()
    private val store = ViewModelStore()
    private class Owner : LifecycleOwner {
        override val lifecycle = LifecycleRegistry.createUnsafe(this).apply { currentState = Lifecycle.State.RESUMED }
    }
    @After fun after() { compose.runOnIdle { store.clear() } }
    private fun model(tracker: SettingsTracker) = SettingsViewModel(
        object : ThemePreferencesRepository {
            override val theme = MutableStateFlow(ThemeMode.System)
            override suspend fun setTheme(mode: ThemeMode) = error("Unexpected theme change")
        }, ThemeOptions.forSdk(35), tracker,
    ).also { store.put("settings", it) }
    private fun router(onClose: () -> Unit) = object : SettingsRouter {
        override fun close() = onClose()
    }

    @Test fun screenEventsFollowResumeAndReentryWithoutRetrackingOnRouterRecomposition() {
        val owner = Owner()
        val tracker = mock(SettingsTracker::class.java)
        val model = model(tracker)
        var closes = 0
        val currentRouter = mutableStateOf(router { closes++ })
        val visible = mutableStateOf(true)
        compose.setContent {
            CompositionLocalProvider(LocalLifecycleOwner provides owner) {
                TeamCityTheme { if (visible.value) SettingsRoute(currentRouter.value, model) }
            }
        }
        compose.waitForIdle()
        verify(tracker).trackView()
        compose.runOnIdle { currentRouter.value = router { closes++ } }
        compose.waitForIdle()
        verify(tracker, times(1)).trackView()
        compose.onNodeWithContentDescription("Back").performClick()
        assertEquals(1, closes)
        compose.runOnIdle { owner.lifecycle.currentState = Lifecycle.State.CREATED }
        compose.waitForIdle()
        verify(tracker, times(1)).trackView()
        compose.runOnIdle { owner.lifecycle.currentState = Lifecycle.State.RESUMED }
        compose.waitForIdle()
        verify(tracker, times(2)).trackView()
        compose.runOnIdle { visible.value = false }
        compose.runOnIdle { visible.value = true }
        compose.waitForIdle()
        verify(tracker, times(3)).trackView()
    }
}
