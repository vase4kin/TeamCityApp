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

import androidx.lifecycle.ViewModelStore
import app.cash.turbine.test
import java.io.IOException
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.test.*
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import teamcityapp.features.settings.impl.tracker.SettingsTracker
import teamcityapp.libraries.app_theme.*

@OptIn(ExperimentalCoroutinesApi::class)
class SettingsViewModelTest {
    private val dispatcher = StandardTestDispatcher()
    private val store = ViewModelStore()
    private val options = ThemeOptions.forSdk(35)
    private class Repo : ThemePreferencesRepository {
        val choice = MutableStateFlow(ThemeMode.System)
        var load: Flow<ThemeMode> = choice
        override val theme get() = load
        val writes = mutableListOf<ThemeMode>()
        var save: suspend (ThemeMode) -> Unit = { choice.value = it }
        override suspend fun setTheme(mode: ThemeMode) {
            writes += mode
            save(mode)
        }
    }
    private class Tracker : SettingsTracker {
        val modes = mutableListOf<ThemeMode>()
        var views = 0
        override fun trackView() {
            views++
        }
        override fun trackLightThemeSet() {
            modes += ThemeMode.Light
        }
        override fun trackDarkThemeSet() {
            modes += ThemeMode.Dark
        }
        override fun trackAutoBatteryThemeSet() {
            modes += ThemeMode.AutoBattery
        }
        override fun trackSystemThemeSet() {
            modes += ThemeMode.System
        }
    }

    @Before fun before() {
        Dispatchers.setMain(dispatcher)
    }

    @After fun after() {
        store.clear()
        Dispatchers.resetMain()
    }
    private fun vm(repo: Repo, tracker: Tracker = Tracker(), options: ThemeOptions = this.options) = SettingsViewModel(repo, options, tracker).also { store.put("settings", it) }

    @Test fun screenResumeTracksThroughTheViewModelWithoutLoadingPreferences() = runTest(dispatcher) {
        val r = Repo()
        var reads = 0
        r.load = r.choice.onStart { reads++ }
        val tracker = Tracker()
        val model = vm(r, tracker)
        runCurrent()
        assertEquals(0, tracker.views)
        model.onScreenViewed()
        model.onScreenViewed()
        assertEquals(2, tracker.views)
        assertEquals(0, reads)
        assertTrue(tracker.modes.isEmpty())
    }

    @Test fun loadingIsCollectionDrivenAndTracksExternalPreferenceUpdates() = runTest(dispatcher) {
        val r = Repo()
        var subscriptions = 0
        r.load = r.choice.onStart { subscriptions++ }
        val vm = vm(r)
        runCurrent()
        assertEquals(0, subscriptions)
        vm.state.test {
            assertEquals(SettingsUiState.Loading, awaitItem())
            assertEquals(
                SettingsUiState.Content(ThemeMode.System, options.modes),
                awaitItem().also {
                    assertEquals(teamcityapp.libraries.resources.R.string.name_follow_system, (it as SettingsUiState.Content).selectedThemeLabelRes)
                }
            )
            r.choice.value = ThemeMode.Dark
            assertEquals(
                SettingsUiState.Content(ThemeMode.Dark, options.modes),
                awaitItem().also {
                    assertEquals(teamcityapp.libraries.resources.R.string.name_dark_theme, (it as SettingsUiState.Content).selectedThemeLabelRes)
                }
            )
        }
    }

    @Test fun noCollectorsCancelsAndReturnReadsLatestPreference() = runTest(dispatcher) {
        val r = Repo()
        var cancelled = false
        r.load = r.choice.onCompletion { cancelled = true }
        val vm = vm(r)
        vm.state.test {
            awaitItem()
            awaitItem()
        }
        runCurrent()
        assertTrue(cancelled)
        r.choice.value = ThemeMode.Light
        vm.state.test {
            assertEquals(ThemeMode.System, (awaitItem() as SettingsUiState.Content).selected)
            assertEquals(ThemeMode.Light, (awaitItem() as SettingsUiState.Content).selected)
        }
    }

    @Test fun pendingReadCancellationDoesNotBecomeAnError() = runTest(dispatcher) {
        val r = Repo()
        var cancelled = false
        r.load = flow {
            try {
                awaitCancellation()
            } finally {
                cancelled = true
            }
        }
        val vm = vm(r)
        vm.state.test {
            assertEquals(SettingsUiState.Loading, awaitItem())
            runCurrent()
        }
        runCurrent()
        assertTrue(cancelled)
        assertEquals(SettingsUiState.Loading, vm.state.value)
    }

    @Test fun readFailureCanBeRetried() = runTest(dispatcher) {
        val r = Repo()
        r.load = flow { throw IOException() }
        val vm = vm(r)
        vm.state.test {
            awaitItem()
            assertEquals(SettingsUiState.Error, awaitItem())
            r.load = r.choice
            vm.retry()
            assertEquals(SettingsUiState.Loading, awaitItem())
            assertTrue(awaitItem() is SettingsUiState.Content)
        }
    }

    @Test fun selectionSavesAndTracksOnlySuccessfulChoice() = runTest(dispatcher) {
        val r = Repo()
        val t = Tracker()
        val vm = vm(r, t)
        vm.state.test {
            awaitItem()
            awaitItem()
            vm.select(ThemeMode.Dark)
            runCurrent()
            cancelAndIgnoreRemainingEvents()
        }
        assertEquals(listOf(ThemeMode.Dark), r.writes)
        assertEquals(listOf(ThemeMode.Dark), t.modes)
        assertEquals(ThemeMode.Dark, (vm.state.value as SettingsUiState.Content).selected)
    }

    @Test fun sameOrUnavailableChoiceAndLoadingEventsDoNotWrite() = runTest(dispatcher) {
        val r = Repo()
        val vm = vm(r)
        vm.select(ThemeMode.Dark)
        vm.retrySave()
        runCurrent()
        assertTrue(r.writes.isEmpty())
        vm.state.test {
            awaitItem()
            awaitItem()
            vm.select(ThemeMode.System)
            vm.select(ThemeMode.AutoBattery)
            vm.retry()
            runCurrent()
            expectNoEvents()
        }
        assertTrue(r.writes.isEmpty())
    }

    @Test fun failedSaveKeepsCurrentChoiceAndRetrySavesIntendedChoice() = runTest(dispatcher) {
        val r = Repo()
        r.save = { throw IOException() }
        val t = Tracker()
        val vm = vm(r, t)
        vm.state.test {
            awaitItem()
            awaitItem()
            vm.select(ThemeMode.Dark)
            runCurrent()
            cancelAndIgnoreRemainingEvents()
        }
        assertEquals(SettingsUiState.Content(ThemeMode.System, options.modes, saveFailed = true), vm.state.value)
        assertTrue(t.modes.isEmpty())
        vm.state.test {
            awaitItem()
            r.save = { r.choice.value = it }
            vm.retrySave()
            runCurrent()
            cancelAndIgnoreRemainingEvents()
        }
        assertEquals(listOf(ThemeMode.Dark, ThemeMode.Dark), r.writes)
        assertEquals(listOf(ThemeMode.Dark), t.modes)
    }

    @Test fun savingRejectsDuplicateAndConcurrentSelections() = runTest(dispatcher) {
        val r = Repo()
        val pending = CompletableDeferred<Unit>()
        r.save = {
            pending.await()
            r.choice.value = it
        }
        val vm = vm(r)
        vm.state.test {
            awaitItem()
            awaitItem()
            vm.select(ThemeMode.Dark)
            vm.select(ThemeMode.Light)
            runCurrent()
            assertEquals(1, r.writes.size)
            pending.complete(Unit)
            runCurrent()
            cancelAndIgnoreRemainingEvents()
        }
        assertEquals(listOf(ThemeMode.Dark), r.writes)
    }

    @Test fun clearingViewModelCancelsPendingSaveWithoutTrackingIt() = runTest(dispatcher) {
        val r = Repo()
        var cancelled = false
        r.save = {
            try {
                awaitCancellation()
            } finally {
                cancelled = true
            }
        }
        val t = Tracker()
        val vm = vm(r, t)
        vm.state.test {
            awaitItem()
            awaitItem()
            vm.select(ThemeMode.Dark)
            runCurrent()
            store.clear()
            runCurrent()
            cancelAndIgnoreRemainingEvents()
        }
        assertTrue(cancelled)
        assertTrue(t.modes.isEmpty())
    }

    @Test fun olderAndroidOptionsArePreservedAndBatterySelectionIsTracked() = runTest(dispatcher) {
        val r = Repo()
        val t = Tracker()
        val old = ThemeOptions.forSdk(28)
        val vm = vm(r, t, old)
        vm.state.test {
            awaitItem()
            assertEquals(old.modes, (awaitItem() as SettingsUiState.Content).options)
            vm.select(ThemeMode.AutoBattery)
            runCurrent()
            cancelAndIgnoreRemainingEvents()
        }
        assertEquals(listOf(ThemeMode.AutoBattery), t.modes)
    }

    @Test fun configurationChangeRetainsContentWhileTheNewCollectionWaits() = runTest(dispatcher) {
        val r = Repo()
        val vm = vm(r)
        vm.state.test {
            awaitItem()
            awaitItem()
        }
        runCurrent()
        r.load = flow { awaitCancellation() }
        vm.state.test {
            assertEquals(
                SettingsUiState.Content(ThemeMode.System, options.modes),
                awaitItem().also {
                    assertEquals(teamcityapp.libraries.resources.R.string.name_follow_system, (it as SettingsUiState.Content).selectedThemeLabelRes)
                }
            )
            runCurrent()
            expectNoEvents()
        }
    }
}
