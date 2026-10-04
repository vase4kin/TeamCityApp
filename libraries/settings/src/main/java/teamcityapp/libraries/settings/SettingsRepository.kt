package teamcityapp.libraries.settings

import kotlinx.coroutines.flow.Flow

/** Persisted values match the previous ListPreference values for upgrade compatibility. */
enum class ThemeMode(val storedValue: String) {
    LIGHT("Light"), DARK("Dark"), AUTO_BATTERY("Auto-battery"), SYSTEM("System");

    companion object {
        fun defaultFor(sdk: Int): ThemeMode = when {
            sdk >= 29 -> SYSTEM
            sdk >= 28 -> AUTO_BATTERY
            else -> LIGHT
        }

        fun availableFor(sdk: Int): List<ThemeMode> = when {
            sdk >= 29 -> listOf(LIGHT, DARK, SYSTEM)
            sdk >= 28 -> listOf(LIGHT, DARK, AUTO_BATTERY)
            else -> listOf(LIGHT, DARK)
        }
    }
}

interface SettingsRepository {
    val theme: Flow<ThemeMode>
    val availableThemes: List<ThemeMode>
    suspend fun setTheme(theme: ThemeMode)
}
