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

/** One account-selection policy over platform-owned encrypted records. No credentials are decoded. */
class AccountRecords<T>(val accounts: List<T>, val activeKey: String?, private val identity: (T) -> AccountIdentity) {
    fun saveAndActivate(replacement: T): AccountRecords<T> {
        val owner = identity(replacement)
        return AccountRecords(accounts.filterNot { identity(it).sameAccount(owner) } + replacement, owner.key, identity)
    }
    fun activate(key: String): AccountRecords<T> {
        require(accounts.any { identity(it).key == key }) { "Account no longer exists" }
        return AccountRecords(accounts, key, identity)
    }
    fun remove(key: String): AccountRecords<T> {
        val remaining = accounts.filterNot { identity(it).key == key }
        return AccountRecords(remaining, if (activeKey == key) remaining.firstOrNull()?.let(identity)?.key else activeKey, identity)
    }
    fun signOut(): AccountRecords<T> = AccountRecords(accounts, null, identity)
}
