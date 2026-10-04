/*
 * Copyright 2020 Andrey Tolpeev
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

package com.github.vase4kin.teamcityapp.drawer.router

import com.github.vase4kin.teamcityapp.account.create.view.CreateAccountActivity
import com.github.vase4kin.teamcityapp.home.view.HomeActivity
import teamcityapp.features.about.api.navigation.AboutNavigation
import teamcityapp.features.drawer.drawer.DrawerAppRouter
import teamcityapp.features.drawer.view.DrawerBottomSheetDialogFragment
import teamcityapp.features.manage_accounts.view.ManageAccountsActivity
import teamcityapp.features.settings.api.navigation.SettingsNavigation

class DrawerAppRouterImpl(
    private val fragment: DrawerBottomSheetDialogFragment,
    private val aboutNavigation: AboutNavigation,
    private val settingsNavigation: SettingsNavigation
) : DrawerAppRouter {

    override fun openAboutScreen() {
        aboutNavigation.open(fragment.requireActivity())
    }

    override fun openNewAccount() {
        CreateAccountActivity.start(
            fragment.requireActivity()
        )
    }

    override fun openManageAccounts() {
        ManageAccountsActivity.start(fragment.requireActivity())
    }

    override fun openHomeActivity() {
        HomeActivity.startWhenSwitchingAccountsFromDrawer(
            fragment.requireActivity()
        )
    }

    override fun openSettingsActivity() {
        settingsNavigation.open(fragment.requireActivity())
    }

    override fun openAgentsActivity() {
        // Open agents activity
    }
}
