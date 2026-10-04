package teamcityapp.libraries.settings

import android.content.Context
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.first
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import java.io.File
import java.util.UUID

@RunWith(AndroidJUnit4::class)
class ThemeMigrationTest {
    @Test fun migrationPreservesEachThemeAndOtherPreferences() = runBlocking {
        val context = ApplicationProvider.getApplicationContext<Context>()
        for (mode in ThemeMode.entries) {
            val id = UUID.randomUUID().toString()
            val prefs = context.getSharedPreferences(id, Context.MODE_PRIVATE)
            prefs.edit().putString(LEGACY_THEME_KEY, mode.storedValue).putString("unrelated", "keep").commit()
            val file = File(context.cacheDir, "$id.preferences_pb")
            val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
            try {
                val store = PreferenceDataStoreFactory.create(
                    migrations = listOf(themeMigration(context, id)), scope = scope, produceFile = { file }
                )
                val repository = DataStoreSettingsRepository(store, 36)
                assertEquals(mode, repository.theme.first())
                assertFalse(prefs.contains(LEGACY_THEME_KEY))
                assertEquals("keep", prefs.getString("unrelated", null))
                repository.setTheme(ThemeMode.DARK)
                assertEquals(ThemeMode.DARK, repository.theme.first())
            } finally {
                scope.coroutineContext[Job]!!.cancelAndJoin()
                file.delete()
                context.deleteSharedPreferences(id)
            }
        }
    }

    @Test fun dataStoreValueWinsIfMigrationIsRunAgain() = runBlocking {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val id = UUID.randomUUID().toString()
        val prefs = context.getSharedPreferences(id, Context.MODE_PRIVATE)
        val file = File(context.cacheDir, "$id.preferences_pb")
        suspend fun useStore(action: suspend (SettingsRepository) -> Unit) {
            val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
            try {
                val store = PreferenceDataStoreFactory.create(
                    migrations = listOf(themeMigration(context, id)), scope = scope, produceFile = { file }
                )
                action(DataStoreSettingsRepository(store, 36))
            } finally { scope.coroutineContext[Job]!!.cancelAndJoin() }
        }
        try {
            useStore { it.setTheme(ThemeMode.DARK) }
            prefs.edit().putString(LEGACY_THEME_KEY, "Light").commit()
            useStore { assertEquals(ThemeMode.DARK, it.theme.first()) }
        } finally {
            file.delete()
            context.deleteSharedPreferences(id)
        }
    }
}
