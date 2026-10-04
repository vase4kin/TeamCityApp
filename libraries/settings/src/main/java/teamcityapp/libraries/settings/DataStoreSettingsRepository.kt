package teamcityapp.libraries.settings

import android.content.Context
import android.os.Build
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.SharedPreferencesMigration
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import java.io.IOException
import javax.inject.Singleton

const val LEGACY_THEME_KEY = "preference_key_theme"
internal val THEME_KEY = stringPreferencesKey(LEGACY_THEME_KEY)

fun themeMigration(context: Context, preferencesName: String = "${context.packageName}_preferences") =
    SharedPreferencesMigration(context, preferencesName, keysToMigrate = setOf(LEGACY_THEME_KEY))

private val Context.settingsDataStore by preferencesDataStore(
    name = "settings",
    produceMigrations = { context -> listOf(themeMigration(context)) }
)

class DataStoreSettingsRepository(
    private val dataStore: DataStore<Preferences>,
    sdk: Int
) : SettingsRepository {
    private val defaultTheme = ThemeMode.defaultFor(sdk)
    override val availableThemes = ThemeMode.availableFor(sdk)
    override val theme = dataStore.data
        .catch { error ->
            if (error is IOException) emit(emptyPreferences()) else throw error
        }
        .map { preferences ->
            ThemeMode.entries.firstOrNull { it.storedValue == preferences[THEME_KEY] } ?: defaultTheme
        }
        .distinctUntilChanged()

    override suspend fun setTheme(theme: ThemeMode) {
        dataStore.edit { preferences -> preferences[THEME_KEY] = theme.storedValue }
    }
}

@Module
@InstallIn(SingletonComponent::class)
object SettingsRepositoryModule {
    @Provides
    @Singleton
    fun repository(@ApplicationContext context: Context): SettingsRepository =
        DataStoreSettingsRepository(context.settingsDataStore, Build.VERSION.SDK_INT)
}
