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

package teamcityapp.shared

import teamcityapp.features.create_account.api.navigation.CreateAccountDestination
import teamcityapp.features.home.api.HomeDestination
import teamcityapp.features.home.api.HomeTabSelection
import teamcityapp.features.login.api.navigation.LoginDestination
import teamcityapp.features.manage_accounts.api.navigation.ManageAccountsDestination
import teamcityapp.libraries.navigation.Navigator

/** The same UI-owned launch policy is used by native deep links and installed Android aliases. */
internal fun Navigator.acceptLaunch(launch: SharedAppLaunch, accountKey: String, signedIn: Boolean): Boolean {
    if (launch.accountKey != accountKey) return false
    val target = launch.destination
    if (!signedIn && target !is LoginDestination && target != AccountsDestination && target != CreateAccountDestination && target != ManageAccountsDestination) return false
    if (target is HomeDestination) {
        val home = snapshot.entries.firstOrNull { it.destination is HomeDestination }
        if (home == null) reset(target, accountKey) else popTo(home.id, HomeTabSelection(target.tab))
    } else {
        push(target)
    }
    return true
}
