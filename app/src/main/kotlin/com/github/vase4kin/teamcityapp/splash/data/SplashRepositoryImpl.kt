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

package com.github.vase4kin.teamcityapp.splash.data

import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import teamcityapp.features.splash.api.SplashRepository
import teamcityapp.libraries.coroutines.IoDispatcher
import teamcityapp.libraries.storage.Storage

/** Adapts the existing account store without changing its persisted records or authentication. */
@Singleton
class SplashRepositoryImpl @Inject constructor(
    private val storage: Storage,
    @IoDispatcher private val dispatcher: CoroutineDispatcher
) : SplashRepository {
    override suspend fun hasAccounts(): Boolean = withContext(dispatcher) { storage.hasUserAccounts() }
}
