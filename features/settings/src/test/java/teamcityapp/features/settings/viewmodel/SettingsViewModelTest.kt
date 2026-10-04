package teamcityapp.features.settings.viewmodel

import androidx.lifecycle.ViewModelStore
import app.cash.turbine.test
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.test.*
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito.*
import teamcityapp.features.settings.tracker.SettingsTracker
import teamcityapp.libraries.settings.SettingsRepository
import teamcityapp.libraries.settings.ThemeMode
import java.io.IOException

@OptIn(ExperimentalCoroutinesApi::class)
class SettingsViewModelTest {
    private val dispatcher = StandardTestDispatcher()
    private val tracker = mock(SettingsTracker::class.java)
    private class FakeSettings : SettingsRepository {
        override val theme = MutableStateFlow(ThemeMode.SYSTEM)
        override val availableThemes = ThemeMode.availableFor(36)
        var failWrites = false
        var writes = 0
        override suspend fun setTheme(theme: ThemeMode) {
            writes++
            if (failWrites) throw IOException("disk full")
            this.theme.value = theme
        }
    }
    @Before fun setUp() = Dispatchers.setMain(dispatcher)
    @After fun tearDown() = Dispatchers.resetMain()

    @Test fun observesPersistedThemeAndTracksOnlySuccessfulChanges() = runTest(dispatcher) {
        val repository = FakeSettings()
        val vm = SettingsViewModel(repository, tracker)
        val store = ViewModelStore().apply { put("settings", vm) }
        try {
            vm.state.test {
                assertEquals(SettingsUiState.Loading, awaitItem())
                runCurrent()
                assertEquals(SettingsUiState.Content(ThemeMode.SYSTEM), awaitItem())
                vm.selectTheme(ThemeMode.DARK)
                assertEquals(SettingsUiState.Content(ThemeMode.SYSTEM, saving = true), awaitItem())
                runCurrent()
                assertEquals(SettingsUiState.Content(ThemeMode.DARK), awaitItem())
            }
            vm.selectTheme(ThemeMode.DARK)
            runCurrent()
            assertEquals(1, repository.writes)
            verify(tracker, times(1)).trackDarkThemeSet()
            verifyNoMoreInteractions(tracker)
        } finally { store.clear() }
    }

    @Test fun failedWriteKeepsPreviousThemeAndCanBeRetried() = runTest(dispatcher) {
        val repository = FakeSettings().apply { failWrites = true }
        val vm = SettingsViewModel(repository, tracker)
        val store = ViewModelStore().apply { put("settings", vm) }
        try {
            runCurrent()
            vm.selectTheme(ThemeMode.LIGHT)
            runCurrent()
            assertEquals(SettingsUiState.Content(ThemeMode.SYSTEM, saveError = true), vm.state.value)
            verifyNoInteractions(tracker)
            repository.failWrites = false
            vm.selectTheme(ThemeMode.LIGHT)
            runCurrent()
            assertEquals(SettingsUiState.Content(ThemeMode.LIGHT), vm.state.value)
            verify(tracker).trackLightThemeSet()
        } finally { store.clear() }
    }

    @Test fun unsupportedSelectionIsIgnored() = runTest(dispatcher) {
        val repository = FakeSettings()
        val vm = SettingsViewModel(repository, tracker)
        val store = ViewModelStore().apply { put("settings", vm) }
        try {
            runCurrent()
            vm.selectTheme(ThemeMode.AUTO_BATTERY)
            runCurrent()
            assertEquals(0, repository.writes)
            assertEquals(SettingsUiState.Content(ThemeMode.SYSTEM), vm.state.value)
        } finally { store.clear() }
    }

    @Test fun loadFailureIsVisibleAndRetryRestartsCollection() = runTest(dispatcher) {
        var fail = true
        val repository = object : SettingsRepository {
            override val theme: Flow<ThemeMode> get() = flow {
                if (fail) throw IOException()
                emit(ThemeMode.LIGHT)
            }
            override val availableThemes = ThemeMode.availableFor(36)
            override suspend fun setTheme(theme: ThemeMode) = Unit
        }
        val vm = SettingsViewModel(repository, tracker)
        runCurrent()
        assertEquals(SettingsUiState.LoadError, vm.state.value)
        fail = false
        vm.retry()
        runCurrent()
        assertEquals(SettingsUiState.Content(ThemeMode.LIGHT), vm.state.value)
    }
}
