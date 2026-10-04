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

package teamcityapp.features.change_details.impl

import android.app.Application
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LifecycleRegistry
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.compose.LocalLifecycleOwner
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test
import org.mockito.Mockito.*
import teamcityapp.features.change_details.impl.tracker.ChangeDetailsTracker
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode
import teamcityapp.features.change_details.impl.router.ChangeDetailsRouter
import teamcityapp.libraries.theme.TeamCityTheme

@OptIn(ExperimentalTestApi::class)
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35], application = Application::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class ChangeDetailsRouteTest {
    @get:Rule val compose = createComposeRule()
    private class Owner : LifecycleOwner {
        override val lifecycle = LifecycleRegistry.createUnsafe(this).apply { currentState = Lifecycle.State.RESUMED }
    }
    private class Router : ChangeDetailsRouter {
        var starts = 0; var stops = 0; var closes = 0
        val urls = mutableListOf<String>()
        val diffs = mutableListOf<Pair<String, String>>()
        override fun start() { starts++ }
        override fun stop() { stops++ }
        override fun close() { closes++ }
        override fun openUrl(url: String) { urls += url }
        override fun openDiff(id: String, fileName: String) { diffs += id to fileName }
    }
    @Test fun browserConnectionAndScreenEventsFollowVisibleLifecycle() {
        val owner = Owner(); val router = Router()
        val tracker = mock(ChangeDetailsTracker::class.java)
        val model = ChangeDetailsViewModel(SavedStateHandle(mapOf(ChangeDetailsArguments.ID to "123",
            ChangeDetailsArguments.FILE_NAMES to arrayListOf<String>(), ChangeDetailsArguments.FILE_TYPES to arrayListOf<String>())), tracker)
        compose.setContent { CompositionLocalProvider(LocalLifecycleOwner provides owner) { TeamCityTheme { ChangeDetailsRoute(router, model) } } }
        compose.waitForIdle()
        assertEquals(1, router.starts); verify(tracker).trackView()
        compose.runOnIdle { owner.lifecycle.currentState = Lifecycle.State.CREATED }
        compose.waitForIdle()
        assertEquals(1, router.stops)
        compose.runOnIdle { owner.lifecycle.currentState = Lifecycle.State.RESUMED }
        compose.waitForIdle()
        assertEquals(2, router.starts); verify(tracker, times(2)).trackView()
        compose.runOnIdle { owner.lifecycle.currentState = Lifecycle.State.DESTROYED }
        compose.waitForIdle()
        assertEquals(2, router.stops)
    }
    @Test fun browserActionsTrackThroughViewModelAndKeepTheirNavigationArguments() {
        val router = Router()
        val tracker = mock(ChangeDetailsTracker::class.java)
        val fileName = fixture.files.first().name
        val model = ChangeDetailsViewModel(SavedStateHandle(mapOf(
            ChangeDetailsArguments.ID to fixture.id,
            ChangeDetailsArguments.WEB_URL to fixture.webUrl,
            ChangeDetailsArguments.FILE_NAMES to arrayListOf(fileName),
            ChangeDetailsArguments.FILE_TYPES to arrayListOf("changed"),
        )), tracker)
        compose.setContent { TeamCityTheme { ChangeDetailsRoute(router, model) } }
        compose.onNodeWithText("MORE DETAILS").performClick()
        compose.onNodeWithTag("change_details:file:0").performScrollTo().performClick()
        assertEquals(listOf(fixture.webUrl), router.urls)
        assertEquals(listOf(fixture.id to fileName), router.diffs)
        verify(tracker).trackMoreDetails()
        verify(tracker).trackFileDiff()
    }

    @Test fun invalidArgumentsCloseWithoutBindingBrowser() {
        val router = Router(); val tracker = mock(ChangeDetailsTracker::class.java)
        val model = ChangeDetailsViewModel(SavedStateHandle(), tracker)
        compose.setContent { TeamCityTheme { ChangeDetailsRoute(router, model) } }
        compose.waitForIdle()
        assertEquals(1, router.closes); assertEquals(0, router.starts)
        verifyNoInteractions(tracker)
    }
}
