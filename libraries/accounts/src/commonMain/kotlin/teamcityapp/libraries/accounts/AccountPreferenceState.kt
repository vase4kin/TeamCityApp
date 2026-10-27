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

package teamcityapp.libraries.accounts

import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext

/** Serializes durable writes and publishes only snapshots belonging to the current account activation. */
class AccountPreferenceState(
    private val account: AccountScope,
    initial: AccountPreferences,
    private val persist: suspend ((AccountPreferences) -> AccountPreferences) -> AccountPreferences
) {
    private val preferences = MutableStateFlow(initial)
    val state = preferences.asStateFlow()
    private val writes = Mutex()
    suspend fun update(transform: (AccountPreferences) -> AccountPreferences) = writes.withLock {
        account.run {
            withContext(NonCancellable) {
                account.requireCurrent()
                persist(transform).also {
                    account.requireCurrent()
                    preferences.value = it
                }
            }
        }
    }
}
