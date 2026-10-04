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

package teamcityapp.features.drawer.impl
import teamcityapp.features.drawer.api.*
internal val active = DrawerAccount(DrawerAccountId("https://teamcity.example/primary", "Guest user"), true, false)
internal val inactive = DrawerAccount(DrawerAccountId("https://teamcity.example/secondary", "Alice"), false, true)
internal fun drawerFixture(name: String): DrawerUiState {
    val accounts = when (name) {
        "one" -> listOf(active)
        "long", "scrolled" -> (1..20).map { DrawerAccount(DrawerAccountId("https://a-very-long-teamcity-server.example/projects/production/$it", "Developer with a very long full name $it"), it == 1, it % 2 == 0) }
        "no_active" -> listOf(active.copy(isActive = false), inactive)
        else -> listOf(inactive, active)
    }
    val rows = when (name) {
        "loading" -> DrawerAccountsUiState.Loading
        "empty" -> DrawerAccountsUiState.Empty
        "error" -> DrawerAccountsUiState.Error
        else -> DrawerAccountsUiState.Content(accounts)
    }
    val selection = when (name) {
        "switching" -> AccountSwitchUiState.Switching(inactive.id)
        "switch_error" -> AccountSwitchUiState.Error(inactive.id)
        "missing" -> AccountSwitchUiState.Missing
        else -> AccountSwitchUiState.Idle
    }
    return DrawerUiState(rows, selection)
}
