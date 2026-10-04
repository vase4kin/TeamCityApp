package teamcityapp.libraries.settings

import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import app.cash.turbine.test
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.*
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import java.io.File

@OptIn(ExperimentalCoroutinesApi::class)
class SettingsRepositoryTest {
    @get:Rule val folder = TemporaryFolder()

    @Test fun defaultsAndAvailableChoicesMatchEachPlatform() {
        assertEquals(ThemeMode.LIGHT, ThemeMode.defaultFor(24))
        assertEquals(ThemeMode.AUTO_BATTERY, ThemeMode.defaultFor(28))
        assertEquals(ThemeMode.SYSTEM, ThemeMode.defaultFor(29))
        assertEquals(listOf(ThemeMode.LIGHT, ThemeMode.DARK), ThemeMode.availableFor(24))
        assertEquals(listOf(ThemeMode.LIGHT, ThemeMode.DARK, ThemeMode.AUTO_BATTERY), ThemeMode.availableFor(28))
        assertEquals(listOf(ThemeMode.LIGHT, ThemeMode.DARK, ThemeMode.SYSTEM), ThemeMode.availableFor(36))
    }

    @Test fun writesEmitNewThemeAndPersistStableValues() = runTest {
        val store = PreferenceDataStoreFactory.create(scope = backgroundScope) {
            File(folder.root, "settings.preferences_pb")
        }
        val repository = DataStoreSettingsRepository(store, 36)
        repository.theme.test {
            assertEquals(ThemeMode.SYSTEM, awaitItem())
            repository.setTheme(ThemeMode.DARK)
            assertEquals(ThemeMode.DARK, awaitItem())
            assertEquals("Dark", store.data.first()[THEME_KEY])
            repository.setTheme(ThemeMode.LIGHT)
            assertEquals(ThemeMode.LIGHT, awaitItem())
        }
    }

    @Test fun understandsAllLegacyValuesAndFallsBackForUnknownValues() = runTest {
        val store = PreferenceDataStoreFactory.create(scope = backgroundScope) {
            File(folder.root, "legacy.preferences_pb")
        }
        val repository = DataStoreSettingsRepository(store, 36)
        ThemeMode.entries.forEach { mode ->
            store.edit { it[THEME_KEY] = mode.storedValue }
            assertEquals(mode, repository.theme.first())
        }
        store.edit { it[THEME_KEY] = "unknown" }
        assertEquals(ThemeMode.SYSTEM, repository.theme.first())
    }
}
