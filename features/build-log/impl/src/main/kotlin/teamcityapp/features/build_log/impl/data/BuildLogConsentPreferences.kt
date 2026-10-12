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

package teamcityapp.features.build_log.impl.data

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import teamcityapp.features.build_log.api.BuildLogConsentStorage
import teamcityapp.features.build_log.impl.data.legacy.BuildLogInteractorImpl
import teamcityapp.libraries.coroutines.MainDispatcher
import teamcityapp.libraries.storage.Storage

class BuildLogConsentPreferences @Inject constructor(
    private val storage: Storage,
    @ApplicationContext private val context: Context,
    @MainDispatcher private val main: CoroutineDispatcher
) : BuildLogConsentStorage {
    override suspend fun acknowledged(): Boolean = withContext(main) { BuildLogInteractorImpl(storage, context, null).isAuthDialogShown }
    override suspend fun setAcknowledged(value: Boolean) = withContext(main) { BuildLogInteractorImpl(storage, context, null).setAuthDialogStatus(value) }
}
