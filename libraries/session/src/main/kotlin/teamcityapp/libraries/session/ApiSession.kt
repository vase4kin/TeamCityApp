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

package teamcityapp.libraries.session

import teamcityapp.libraries.storage.Storage

/** Keeps the API graph tied to the current account, including its authentication settings. */
class ApiSession<Graph : Any>(
    private val storage: () -> Storage,
    private var createGraph: (String) -> Graph
) {
    private data class AccountKey(
        val url: String,
        val userName: String,
        val guest: Boolean,
        val sslDisabled: Boolean,
        val password: List<Byte>
    )

    private var accountKey: AccountKey? = null
    private var graph: Graph? = null

    @Synchronized
    fun requireGraph(): Graph {
        val account = storage().activeUser
        if (account.teamcityUrl.isEmpty()) {
            clear()
            error("An active account is required to resolve TeamCity API dependencies")
        }
        val key = AccountKey(
            account.teamcityUrl,
            account.userName,
            account.isGuestUser,
            account.isSslDisabled,
            account.passwordAsBytes?.toList() ?: emptyList()
        )
        if (graph == null || accountKey != key) {
            clear()
            graph = createGraph(key.url)
            accountKey = key
        }
        return checkNotNull(graph)
    }

    @Synchronized
    fun rebuild(baseUrl: String) {
        require(baseUrl == storage().activeUser.teamcityUrl && baseUrl.isNotEmpty()) {
            "The API graph URL must match the active account"
        }
        clear()
        requireGraph()
    }

    @Synchronized
    fun clear() {
        graph = null
        accountKey = null
    }

    @Synchronized
    fun setFactoryForTesting(factory: (String) -> Graph) {
        clear()
        createGraph = factory
    }
}
