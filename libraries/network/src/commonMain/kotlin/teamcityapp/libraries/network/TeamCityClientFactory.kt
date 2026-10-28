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

package teamcityapp.libraries.network

import io.ktor.client.HttpClient
import teamcityapp.libraries.accounts.AccountIdentity

/** Composition seam for deterministic fixtures; all authentication and REST rules remain common. */
fun interface TeamCityClientFactory {
    fun create(identity: AccountIdentity): HttpClient
}
object PlatformTeamCityClientFactory : TeamCityClientFactory {
    override fun create(identity: AccountIdentity) = platformTeamCityClient(identity.sslDisabled)
}
