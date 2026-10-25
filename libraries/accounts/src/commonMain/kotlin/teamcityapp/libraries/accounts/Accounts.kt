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

import kotlin.coroutines.CoroutineContext
import kotlin.uuid.Uuid
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.async
import kotlinx.coroutines.cancel
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.add
import kotlinx.serialization.json.buildJsonArray

/** Public account identity contains no credential. Encoding matches the existing Android key. */
@Serializable
data class AccountIdentity(val serverUrl: String, val userName: String, val guest: Boolean, val sslDisabled: Boolean = false) {
    init {
        require(serverUrl.isNotBlank())
        require(userName.isNotBlank())
    }
    fun sameAccount(other: AccountIdentity): Boolean = serverUrl == other.serverUrl && userName == other.userName && guest == other.guest
    val key: String get() = buildJsonArray {
        add(serverUrl)
        add(userName)
        add(guest)
        add(sslDisabled)
    }.toString().replace("\u2028", "\\u2028").replace("\u2029", "\\u2029")
}

/** This value belongs in memory or platform secure storage; never save it with navigation/UI state. */
class AccountSecret(val identity: AccountIdentity, val password: String = "") {
    override fun toString(): String = "AccountSecret(identity=$identity, password=<redacted>)"
}

/** Platforms preserve their storage formats. Writes must finish before the account becomes visible. */
interface AccountStorage {
    suspend fun accounts(): List<AccountIdentity>
    suspend fun active(): AccountSecret?
    suspend fun secret(key: String): AccountSecret?
    suspend fun saveAndActivate(account: AccountSecret)
    suspend fun activate(key: String)
    suspend fun remove(key: String)
    suspend fun signOut()
}

/** One activation owns all account work and resources, including requests awaiting a response. */
class AccountScope internal constructor(
    val identity: AccountIdentity,
    val generation: String,
    parentContext: CoroutineContext,
    private val isCurrent: () -> Boolean
) {
    private val job = SupervisorJob(parentContext[Job])
    val coroutineScope = CoroutineScope(parentContext + job)
    suspend fun requireCurrent() {
        currentCoroutineContext().ensureActive()
        if (!job.isActive || !isCurrent()) throw CancellationException("The account session has changed")
    }

    /** Both destination cancellation and account replacement cancel the underlying platform request. */
    suspend fun <T> run(block: suspend () -> T): T {
        requireCurrent()
        val request = coroutineScope.async {
            requireCurrent()
            block().also { requireCurrent() }
        }
        return try {
            request.await()
        } finally {
            request.cancel()
        }
    }
    internal fun close() {
        coroutineScope.cancel()
    }
}

sealed interface AccountSessionState {
    data object Loading : AccountSessionState
    data object SignedOut : AccountSessionState
    data class Active(val scope: AccountScope) : AccountSessionState
}

/** Serializes switches and invalidates old work before publishing a new account. */
class AccountSessionManager(private val storage: AccountStorage, private val context: CoroutineContext, private val onAccountReplaced: suspend () -> Unit = {}) {
    private val mutex = Mutex()
    private val mutableState = MutableStateFlow<AccountSessionState>(AccountSessionState.Loading)
    val state: StateFlow<AccountSessionState> = mutableState.asStateFlow()
    private var active: AccountScope? = null

    suspend fun restore(forceRefresh: Boolean = false) = mutex.withLock {
        val identity = storage.active()?.identity
        if (forceRefresh) withContext(NonCancellable) { replaceAfterMutation(identity) } else replace(identity)
    }
    suspend fun accounts(): List<AccountIdentity> = storage.accounts()
    suspend fun saveAndActivate(account: AccountSecret, checkDuplicate: Boolean = true): Boolean = mutex.withLock {
        if (checkDuplicate && storage.accounts().any { it.sameAccount(account.identity) }) return@withLock false
        currentCoroutineContext().ensureActive()
        withContext(NonCancellable) {
            storage.saveAndActivate(account)
            replaceAfterMutation(account.identity)
        }
        true
    }
    suspend fun activate(key: String) = mutex.withLock {
        val account = requireNotNull(storage.secret(key)) { "Account no longer exists" }
        currentCoroutineContext().ensureActive()
        withContext(NonCancellable) {
            storage.activate(key)
            replaceAfterMutation(account.identity)
        }
    }
    suspend fun remove(key: String) = mutex.withLock {
        val wasActive = active?.identity?.key == key
        currentCoroutineContext().ensureActive()
        withContext(NonCancellable) {
            storage.remove(key)
            if (wasActive) replaceAfterMutation(storage.active()?.identity)
        }
    }
    suspend fun signOut() = mutex.withLock {
        currentCoroutineContext().ensureActive()
        withContext(NonCancellable) {
            storage.signOut()
            replaceAfterMutation(null)
        }
    }
    suspend fun secret(scope: AccountScope): AccountSecret {
        scope.requireCurrent()
        val result = storage.secret(scope.identity.key) ?: throw CancellationException("Account was removed")
        scope.requireCurrent()
        return result
    }

    suspend fun preferences(scope: AccountScope): AccountPreferences = mutex.withLock {
        scope.requireCurrent()
        checkNotNull(storage as? AccountPreferencesStorage) { "This host does not provide portable preferences" }.preferences(scope.identity.key).also { scope.requireCurrent() }
    }
    suspend fun updatePreferences(scope: AccountScope, transform: (AccountPreferences) -> AccountPreferences): AccountPreferences = mutex.withLock {
        scope.requireCurrent()
        checkNotNull(storage as? AccountPreferencesStorage).updatePreferences(scope.identity.key, transform).also { scope.requireCurrent() }
    }
    fun close() {
        active?.close()
        active = null
        mutableState.value = AccountSessionState.SignedOut
    }
    private suspend fun replaceAfterMutation(identity: AccountIdentity?) {
        // Durable storage succeeded. Stop old work and invalidate its resources before publishing.
        active?.close()
        onAccountReplaced()
        replace(identity)
    }
    private fun replace(identity: AccountIdentity?) {
        active?.close()
        active = null
        if (identity == null) {
            mutableState.value = AccountSessionState.SignedOut
        } else {
            val generation = Uuid.random().toString()
            val scope = AccountScope(identity, generation, context) { active?.generation == generation }
            active = scope
            mutableState.value = AccountSessionState.Active(scope)
        }
    }
}
