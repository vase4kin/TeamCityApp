/*
 * Copyright 2020 Andrey Tolpeev
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

package com.github.vase4kin.teamcityapp.navigation.data

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import teamcityapp.features.navigation.api.NavigationRatingRepository
import teamcityapp.libraries.coroutines.IoDispatcher
import teamcityapp.libraries.remote.RemoteService

/** Keeps the global choice and its persisted keys compatible with existing installs. */
class AppNavigationRatingRepository @Inject constructor(
    @ApplicationContext context: Context,
    private val remoteService: RemoteService,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher
) : NavigationRatingRepository {
    private val preferences = context.getSharedPreferences("rateTheAppPref", Context.MODE_PRIVATE)
    override suspend fun isEligible(): Boolean = withContext(ioDispatcher) {
        !preferences.getBoolean("rated", false) && remoteService.isNotChurn()
    }
    override suspend fun markHandled(): Unit = withContext(ioDispatcher) {
        preferences.edit().putBoolean("rated", true).apply()
    }
}
