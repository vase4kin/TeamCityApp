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

import android.content.Context
import androidx.datastore.preferences.SharedPreferencesMigration
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.preferencesDataStoreFile
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancelAndJoin
import teamcityapp.libraries.coroutines.IoDispatcher

/** Owns the single DataStore and its IO work for an application component's lifetime. */
@Singleton
class ThemePreferencesStore @Inject constructor(
    @ApplicationContext context: Context,
    options: ThemeOptions,
    @IoDispatcher dispatcher: CoroutineDispatcher
) {
    private val job = SupervisorJob()
    val repository: ThemePreferencesRepository = DataStoreThemePreferencesRepository(
        PreferenceDataStoreFactory.create(
            migrations = listOf(
                SharedPreferencesMigration(
                    context,
                    "${context.packageName}_preferences",
                    keysToMigrate = setOf(DataStoreThemePreferencesRepository.LEGACY_KEY)
                )
            ),
            scope = CoroutineScope(job + dispatcher),
            produceFile = { context.preferencesDataStoreFile("app_theme") }
        ),
        options.default
    )

    /** Allows isolated application components to finish all file work before releasing the store. */
    suspend fun close() {
        job.cancelAndJoin()
    }
}
