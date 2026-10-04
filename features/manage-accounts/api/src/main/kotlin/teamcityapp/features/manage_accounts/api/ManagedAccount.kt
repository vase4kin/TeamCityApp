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

package teamcityapp.features.manage_accounts.api

/** Account identity includes the user name: several users can share a TeamCity server. */
data class ManagedAccountId(val serverUrl: String, val userName: String)

data class ManagedAccount(val id: ManagedAccountId, val isActive: Boolean, val isSslDisabled: Boolean)

enum class AccountDestination { Home, Login }
enum class AccountRemovalOutcome { Stay, Home, Login, CleanupFailed }

data class AccountRemoval(
    val remainingAccounts: List<ManagedAccount>,
    val outcome: AccountRemovalOutcome,
    val removed: Boolean
)
