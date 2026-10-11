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

package com.github.vase4kin.teamcityapp.navigation

import android.content.Context
import android.webkit.CookieManager
import com.github.vase4kin.teamcityapp.TeamCityApplicationBase
import com.github.vase4kin.teamcityapp.builds.data.homeBuildAccountKey
import com.github.vase4kin.teamcityapp.builds.data.requireCurrentHomeBuildAccount
import com.github.vase4kin.teamcityapp.storage.SharedUserStorage
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import kotlin.coroutines.resume
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import teamcityapp.features.build_log.api.BuildLogConsentStorage
import teamcityapp.libraries.coroutines.MainDispatcher

interface AccountSessionPreparation {
    fun currentKey(): String
    suspend fun prepare(expectedKey: String, resetSession: Boolean)
    fun clear()
}
class AppAccountSessionPreparation @Inject constructor(
    private val storage: SharedUserStorage,
    @ApplicationContext private val context: Context,
    private val consent: BuildLogConsentStorage,
    @MainDispatcher private val main: CoroutineDispatcher
) : AccountSessionPreparation {
    override fun currentKey() = if (storage.hasUserAccounts()) storage.activeUser.homeBuildAccountKey() else "signed-out"
    override suspend fun prepare(expectedKey: String, resetSession: Boolean) = withContext(main) {
        requireCurrentHomeBuildAccount(storage, expectedKey)
        val app = context.applicationContext as TeamCityApplicationBase
        if (resetSession) {
            app.appInjector.cacheManager().evictAllCache()
            suspendCancellableCoroutine { continuation -> CookieManager.getInstance().removeAllCookies { if (continuation.isActive) continuation.resume(Unit) } }
            consent.setAcknowledged(false)
        }
        requireCurrentHomeBuildAccount(storage, expectedKey)
        app.buildRestApiInjectorWithBaseUrl(storage.activeUser.teamcityUrl)
    }
    override fun clear() {
        (context.applicationContext as TeamCityApplicationBase).clearApiSession()
    }
}
