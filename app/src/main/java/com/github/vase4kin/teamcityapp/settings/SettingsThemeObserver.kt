package com.github.vase4kin.teamcityapp.settings

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import teamcityapp.libraries.settings.SettingsRepository
import teamcityapp.libraries.utils.applyTheme
import javax.inject.Inject
import javax.inject.Singleton

/** One application-lifetime observer applies persisted settings to all Android screens. */
@Singleton
class SettingsThemeObserver @Inject constructor(
    private val repository: SettingsRepository,
    dispatchers: SettingsThemeDispatchers
) {
    private val scope = CoroutineScope(SupervisorJob() + dispatchers.main)
    private var observation: Job? = null

    fun stop() {
        observation?.cancel()
        observation = null
    }

    fun start() {
        if (observation?.isActive == true) return
        observation = scope.launch { repository.theme.collect(::applyTheme) }
    }
}

class SettingsThemeDispatchers @Inject constructor() {
    val main get() = Dispatchers.Main.immediate
}
