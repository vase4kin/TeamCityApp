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

package teamcityapp.libraries.shared_data

import io.ktor.client.HttpClient
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.ensureActive
import teamcityapp.libraries.accounts.*
import teamcityapp.libraries.authentication.*
import teamcityapp.libraries.network.*

/** Validate against the server before an atomic secure-storage/session change. No secret enters UI state. */
class TeamCityAuthentication(
    private val sessions: AccountSessionManager,
    private val client: (Boolean) -> HttpClient = ::platformTeamCityClient
) : AuthenticationRepository {
    override suspend fun signIn(credentials: AccountCredentials, checkSecureConnection: Boolean, checkDuplicate: Boolean): AuthenticationResult {
        val url = try {
            normalizeServerUrl(credentials.serverUrl)
        } catch (_: IllegalArgumentException) {
            return AuthenticationResult.Failure(0, "", AuthenticationError.InvalidUrl)
        }
        val identity = AccountIdentity(url, if (credentials.guest) "Guest user" else credentials.userName, credentials.guest, credentials.sslDisabled)
        if (checkDuplicate && sessions.accounts().any { it.sameAccount(identity) }) return AuthenticationResult.DuplicateAccount
        if (checkSecureConnection && url.startsWith("http://")) return AuthenticationResult.Failure(-1, "")
        val secret = AccountSecret(identity, if (credentials.guest) "" else credentials.password)
        val api = TeamCityApi(TeamCityTransport(client(identity.sslDisabled), secret, httpConsent = !checkSecureConnection))
        try {
            api.server()
        } catch (cancelled: CancellationException) {
            throw cancelled
        } catch (failure: TeamCityHttpFailure) {
            return AuthenticationResult.Failure(failure.status, "", AuthenticationError.Http(failure.status))
        } catch (_: Exception) {
            return AuthenticationResult.Failure(0, "", AuthenticationError.Connection)
        } finally {
            api.close()
        }
        currentCoroutineContext().ensureActive()
        return try {
            if (sessions.saveAndActivate(secret, checkDuplicate)) AuthenticationResult.Success else AuthenticationResult.DuplicateAccount
        } catch (cancelled: CancellationException) {
            throw cancelled
        } catch (_: Exception) {
            AuthenticationResult.SaveFailed
        }
    }
}
