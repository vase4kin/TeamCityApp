/*
 * Copyright 2019 Andrey Tolpeev
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

package com.github.vase4kin.teamcityapp.builds.data

import com.github.vase4kin.teamcityapp.storage.SharedUserStorage
import com.google.gson.JsonArray
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.ensureActive
import teamcityapp.libraries.storage.models.UserAccount

/** Identifies accounts without putting passwords into UI queries; encoding avoids separator collisions. */
internal fun UserAccount.homeBuildAccountKey(): String = JsonArray().apply {
    add(teamcityUrl)
    add(userName)
    add(isGuestUser)
    add(isSslDisabled)
}.toString()

internal suspend fun requireCurrentHomeBuildAccount(storage: SharedUserStorage, expectedKey: String) {
    currentCoroutineContext().ensureActive()
    val account = storage.activeUser
    if (account.teamcityUrl.isBlank() || account.homeBuildAccountKey() != expectedKey) {
        throw CancellationException("The Home build query belongs to a different account")
    }
}
