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

package teamcityapp.features.drawer.api

/** Account identity is the server/user tuple; no credentials cross this boundary. */
data class DrawerAccountId(val serverUrl: String, val userName: String)
data class DrawerAccount(val id: DrawerAccountId, val isActive: Boolean, val isSslDisabled: Boolean)
data class AccountSelection(val accounts: List<DrawerAccount>, val outcome: SelectionOutcome)
enum class SelectionOutcome { Changed, Current, Missing }
interface DrawerRepository {
    suspend fun accounts(): List<DrawerAccount>
    suspend fun select(id: DrawerAccountId): AccountSelection
}
