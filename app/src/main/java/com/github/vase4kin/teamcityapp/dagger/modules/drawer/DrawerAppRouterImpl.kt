/*
 * Copyright 2019 Andrey Tolpeev
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

package com.github.vase4kin.teamcityapp.dagger.modules.drawer

import com.github.vase4kin.teamcityapp.account.create.view.CreateAccountActivity
import com.github.vase4kin.teamcityapp.home.view.HomeActivity
import teamcityapp.features.about.AboutActivity
import teamcityapp.features.drawer.drawer.DrawerAppRouter
import teamcityapp.features.drawer.view.DrawerBottomSheetDialogFragment
import teamcityapp.features.manage_accounts.view.ManageAccountsActivity

class DrawerAppRouterImpl(
    private val fragment: DrawerBottomSheetDialogFragment
) : DrawerAppRouter {

    override fun openAboutScreen() {
        AboutActivity.start(fragment.requireActivity())
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
}
