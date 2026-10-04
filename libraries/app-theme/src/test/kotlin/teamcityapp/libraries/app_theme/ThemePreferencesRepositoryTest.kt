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

import android.app.Application
import android.content.Context
import androidx.datastore.preferences.SharedPreferencesMigration
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.edit
import androidx.test.core.app.ApplicationProvider
import java.io.File
import java.io.IOException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.*
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35], application = Application::class)
class ThemePreferencesRepositoryTest {
    private val context: Context get() = ApplicationProvider.getApplicationContext()
    private suspend fun TestScope.withStore(migrate: Boolean = false, block: suspend (DataStoreThemePreferencesRepository) -> Unit) {
        val file = File.createTempFile("theme-test", ".preferences_pb").also { it.delete() }
        val scope = CoroutineScope(SupervisorJob() + StandardTestDispatcher(testScheduler))
        val store = PreferenceDataStoreFactory.create(scope = scope,
            migrations = if(migrate) listOf(SharedPreferencesMigration(context, "migration-test", keysToMigrate = setOf(DataStoreThemePreferencesRepository.LEGACY_KEY))) else emptyList(),
            produceFile = { file })
        try { block(DataStoreThemePreferencesRepository(store, ThemeMode.System)) }
        finally { scope.cancel(); runCurrent(); file.delete() }
    }
    @Test fun noSavedChoiceUsesSdkDefault() = runTest {
        withStore { assertEquals(ThemeMode.System, it.theme.first()) }
    }
    @Test fun writesAllModesAndReadsThemBack() = runTest {
        withStore { repo -> ThemeMode.entries.forEach { mode -> repo.setTheme(mode); assertEquals(mode,repo.theme.first()) } }
    }
    @Test fun migrationPreservesEveryLegacyValueAndOtherPreferences() = runTest {
        ThemeMode.entries.forEach { mode ->
            val prefs = context.getSharedPreferences("migration-test", Context.MODE_PRIVATE)
            assertTrue(prefs.edit().clear().putString(DataStoreThemePreferencesRepository.LEGACY_KEY, mode.storedValue).putString("other", "preserved").commit())
            withStore(migrate = true) { repo ->
                assertEquals(mode,repo.theme.first())
                assertFalse(prefs.contains(DataStoreThemePreferencesRepository.LEGACY_KEY))
                assertEquals("preserved",prefs.getString("other",null))
            }
        }
    }
    @Test fun unknownLegacyValueFallsBackWithoutDeletingOtherPreferences() = runTest {
        val prefs = context.getSharedPreferences("migration-test", Context.MODE_PRIVATE)
        prefs.edit().clear().putString(DataStoreThemePreferencesRepository.LEGACY_KEY, "old-unknown").putInt("count",7).commit()
        withStore(migrate = true) { assertEquals(ThemeMode.System,it.theme.first()) }
        assertEquals(7,prefs.getInt("count",0))
    }
    @Test fun themeSurvivesStoreAndProcessRecreation() = runTest {
        val file=File.createTempFile("recreated-theme", ".preferences_pb").also { it.delete() }
        try {
            for(mode in listOf(ThemeMode.Dark, ThemeMode.Light)) {
                val scope=CoroutineScope(SupervisorJob()+StandardTestDispatcher(testScheduler))
                val store=PreferenceDataStoreFactory.create(scope=scope,produceFile={file})
                val repo=DataStoreThemePreferencesRepository(store,ThemeMode.System)
                if(mode==ThemeMode.Dark) repo.setTheme(mode) else assertEquals(ThemeMode.Dark,repo.theme.first())
                scope.cancel();runCurrent()
            }
        } finally { file.delete() }
    }
    @Test fun existingDataStoreChoiceWinsOverLegacyPreference() = runTest {
        val file=File.createTempFile("existing-theme", ".preferences_pb").also { it.delete() }
        val oldScope=CoroutineScope(SupervisorJob()+StandardTestDispatcher(testScheduler))
        val old=PreferenceDataStoreFactory.create(scope=oldScope,produceFile={file})
        old.edit { it[DataStoreThemePreferencesRepository.THEME]="Dark" }; oldScope.cancel();runCurrent()
        context.getSharedPreferences("migration-test",Context.MODE_PRIVATE).edit().clear().putString(DataStoreThemePreferencesRepository.LEGACY_KEY,"Light").commit()
        val scope=CoroutineScope(SupervisorJob()+StandardTestDispatcher(testScheduler))
        try {
            val migrated=PreferenceDataStoreFactory.create(scope=scope,produceFile={file},migrations=listOf(SharedPreferencesMigration(context,"migration-test",keysToMigrate=setOf(DataStoreThemePreferencesRepository.LEGACY_KEY))))
            assertEquals(ThemeMode.Dark,DataStoreThemePreferencesRepository(migrated,ThemeMode.System).theme.first())
        } finally { scope.cancel();runCurrent();file.delete() }
    }
    @Test fun corruptStoreIsReportedWithoutSilentlyResettingTheSavedFile() = runTest {
        val file = File.createTempFile("corrupt-theme", ".preferences_pb")
        val bytes = byteArrayOf(-1, -1, -1)
        file.writeBytes(bytes)
        val scope = CoroutineScope(SupervisorJob() + StandardTestDispatcher(testScheduler))
        try {
            val store = PreferenceDataStoreFactory.create(scope = scope, produceFile = { file })
            val repo = DataStoreThemePreferencesRepository(store, ThemeMode.System)
            assertTrue(runCatching { repo.theme.first() }.exceptionOrNull() is androidx.datastore.core.CorruptionException)
            assertArrayEquals(bytes, file.readBytes())
        } finally { scope.cancel(); runCurrent(); file.delete() }
    }
}
