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
import com.github.vase4kin.teamcityapp.TeamCityApplicationBase
import com.github.vase4kin.teamcityapp.storage.SharedUserStorage
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import teamcityapp.libraries.accounts.AccountSessionManager
import teamcityapp.libraries.accounts.AccountSessionState
import teamcityapp.libraries.coroutines.MainDispatcher
import teamcityapp.libraries.session.homeBuildAccountKey
import teamcityapp.libraries.session.requireCurrentHomeBuildAccount

interface AccountSessionPreparation {
    fun currentKey(): String
    suspend fun prepare(expectedKey: String, resetSession: Boolean)
    fun clear()
}
class AppAccountSessionPreparation @Inject constructor(
    private val storage: SharedUserStorage,
    @ApplicationContext private val context: Context,
    @MainDispatcher private val main: CoroutineDispatcher,
    private val sessions: AccountSessionManager
) : AccountSessionPreparation {
    override fun currentKey() = storage.activeUser.takeIf { it.teamcityUrl.isNotBlank() }?.homeBuildAccountKey() ?: "signed-out"
    override suspend fun prepare(expectedKey: String, resetSession: Boolean) = withContext(main) {
        requireCurrentHomeBuildAccount(storage, expectedKey)
        sessions.restore()
        val account = checkNotNull(sessions.state.value as? AccountSessionState.Active).scope
        check(account.identity.key == expectedKey)
        val app = context.applicationContext as TeamCityApplicationBase
        if (resetSession) {
            app.appInjector.cacheManager().evictAllCache()
        }
        requireCurrentHomeBuildAccount(storage, expectedKey)
        app.buildRestApiInjectorWithBaseUrl(storage.activeUser.teamcityUrl)
        account.requireCurrent()
    }
    override fun clear() {
        sessions.close()
        (context.applicationContext as TeamCityApplicationBase).clearApiSession()
    }
}
