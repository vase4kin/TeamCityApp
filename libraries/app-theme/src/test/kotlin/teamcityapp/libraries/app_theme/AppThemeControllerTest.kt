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

package teamcityapp.libraries.app_theme

import java.io.IOException
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.awaitCancellation
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.test.*
import org.junit.Assert.*
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class AppThemeControllerTest {
    @Test fun startIsIdempotentAndAppliesChangesOnceOnMainDispatcher() = runTest {
        val theme=MutableStateFlow(ThemeMode.Dark); val applied=mutableListOf<ThemeMode>(); var subscriptions=0
        val repo=object: ThemePreferencesRepository { override val theme=theme.onStart { subscriptions++ }; override suspend fun setTheme(mode:ThemeMode) { theme.value=mode } }
        val controller=AppThemeController(repo,ThemeApplier { applied+=it },StandardTestDispatcher(testScheduler))
        try {
            controller.start();controller.start();runCurrent();assertEquals(1,subscriptions);assertEquals(listOf(ThemeMode.Dark),applied)
            repo.setTheme(ThemeMode.Light);runCurrent();assertEquals(listOf(ThemeMode.Dark,ThemeMode.Light),applied)
        } finally { controller.stop();runCurrent() }
    }
    @Test fun stopCancelsSubscriptionAndRestartReadsLatestChoice() = runTest {
        val theme=MutableStateFlow(ThemeMode.Dark);var cancelled=false
        val repo=object:ThemePreferencesRepository { override val theme=theme.onCompletion { cancelled=true };override suspend fun setTheme(mode:ThemeMode) { theme.value=mode } }
        val applied=mutableListOf<ThemeMode>();val c=AppThemeController(repo,ThemeApplier {applied+=it},StandardTestDispatcher(testScheduler))
        c.start();runCurrent();c.stop();runCurrent();assertTrue(cancelled)
        repo.setTheme(ThemeMode.System);runCurrent();assertEquals(listOf(ThemeMode.Dark),applied)
        c.start();runCurrent();assertEquals(listOf(ThemeMode.Dark,ThemeMode.System),applied);c.stop();runCurrent()
    }
    @Test fun transientReadFailureRetriesWithoutChangingCurrentTheme() = runTest {
        var attempts=0;val applied=mutableListOf<ThemeMode>()
        val repo=object:ThemePreferencesRepository {
            override val theme=flow { if(++attempts==1) throw IOException("offline storage");emit(ThemeMode.Dark);awaitCancellation() }
            override suspend fun setTheme(mode:ThemeMode) {}
        }
        val c=AppThemeController(repo,ThemeApplier {applied+=it},StandardTestDispatcher(testScheduler))
        c.start();runCurrent();assertTrue(applied.isEmpty());advanceTimeBy(1000);runCurrent();assertEquals(listOf(ThemeMode.Dark),applied);c.stop();runCurrent()
    }
}
