package com.github.vase4kin.teamcityapp.dagger

import com.github.vase4kin.teamcityapp.dagger.components.RestApiComponent
import teamcityapp.libraries.storage.Storage

/** Keeps the API graph tied to the current account, including its authentication settings. */
internal class ApiSession(
    private val storage: () -> Storage,
    private var createGraph: (String) -> RestApiComponent
) {
    private data class AccountKey(
        val url: String,
        val userName: String,
        val guest: Boolean,
        val sslDisabled: Boolean,
        val password: List<Byte>
    )

    private var accountKey: AccountKey? = null
    private var graph: RestApiComponent? = null

    @Synchronized
    fun requireGraph(): RestApiComponent {
        val account = storage().activeUser
        if (account.teamcityUrl.isEmpty()) {
            clear()
            error("An active account is required to resolve TeamCity API dependencies")
        }
        val key = AccountKey(account.teamcityUrl, account.userName, account.isGuestUser,
            account.isSslDisabled, account.passwordAsBytes?.toList() ?: emptyList())
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
    fun setFactoryForTesting(factory: (String) -> RestApiComponent) {
        clear()
        createGraph = factory
    }
}
