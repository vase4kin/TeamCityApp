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
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.retryWhen
import kotlinx.coroutines.launch
import teamcityapp.libraries.coroutines.MainDispatcher

fun interface ThemeApplier {
    fun apply(mode: ThemeMode)
}

/** One application-owned subscription applies persisted choices to every legacy and Compose screen. */
@Singleton
class AppThemeController @Inject constructor(
    private val repository: ThemePreferencesRepository,
    private val applier: ThemeApplier,
    @MainDispatcher dispatcher: CoroutineDispatcher
) {
    private val scope = CoroutineScope(SupervisorJob() + dispatcher)
    private var observation: Job? = null
    fun start() {
        if (observation?.isActive == true) return
        observation = scope.launch {
            repository.theme.retryWhen { error, _ ->
                if (error is IOException) {
                    delay(1_000)
                    true
                } else {
                    false
                }
            }.distinctUntilChanged().collect(applier::apply)
        }
    }

    /** Releases the application observer when an isolated application/test lifecycle ends. */
    fun stop() {
        observation?.cancel()
        observation = null
    }
}
