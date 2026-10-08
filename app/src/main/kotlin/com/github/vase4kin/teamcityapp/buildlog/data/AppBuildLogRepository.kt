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

package com.github.vase4kin.teamcityapp.buildlog.data

import android.content.Context
import android.os.Bundle
import com.github.vase4kin.teamcityapp.buildlog.urlprovider.BuildLogUrlProviderImpl
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Named
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import teamcityapp.features.build_log.api.*
import teamcityapp.libraries.coroutines.MainDispatcher
import teamcityapp.libraries.storage.Storage

/** The legacy consent preference is also reset by Home on account changes. Keep that boundary shared. */
class AppBuildLogRepository @Inject constructor(
    private val storage: Storage,
    @ApplicationContext private val context: Context,
    @Named("BuildLogOverrideUrl") private val overrideUrl: String,
    @MainDispatcher private val mainDispatcher: CoroutineDispatcher
) : BuildLogRepository {
    override suspend fun session(buildId: String): BuildLogSession = withContext(mainDispatcher) {
        val legacy = BuildLogInteractorImpl(storage, context, Bundle().apply { putString(BuildLogInteractor.BUILD_ID, buildId) })
        BuildLogSession(overrideUrl.ifEmpty { BuildLogUrlProviderImpl(legacy).provideUrl() }, legacy.isSslDisabled, !legacy.isGuestUser && !legacy.isAuthDialogShown)
    }
    override suspend fun acknowledgeAuthentication() = withContext(mainDispatcher) {
        BuildLogInteractorImpl(storage, context, null).setAuthDialogStatus(true)
    }
}
