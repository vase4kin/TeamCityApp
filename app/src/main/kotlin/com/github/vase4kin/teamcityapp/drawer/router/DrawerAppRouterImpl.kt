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

import android.app.Activity
import com.github.vase4kin.teamcityapp.account.create.view.CreateAccountActivity
import com.github.vase4kin.teamcityapp.home.view.HomeActivity
import javax.inject.Inject
import teamcityapp.features.about.api.navigation.AboutNavigation
import teamcityapp.features.drawer.api.router.DrawerAppRouter
import teamcityapp.features.manage_accounts.api.navigation.ManageAccountsNavigation
import teamcityapp.features.settings.api.navigation.SettingsNavigation

class DrawerAppRouterImpl @Inject constructor(
    private val aboutNavigation: AboutNavigation,
    private val settingsNavigation: SettingsNavigation,
    private val manageAccountsNavigation: ManageAccountsNavigation
) : DrawerAppRouter {

    override fun openAbout(activity: Activity) {
        aboutNavigation.open(activity)
    }

    override fun openAddAccount(activity: Activity) {
        CreateAccountActivity.start(
            activity
        )
    }

    override fun openManageAccounts(activity: Activity) {
        manageAccountsNavigation.open(activity)
    }

    override fun openHome(activity: Activity) {
        HomeActivity.startWhenSwitchingAccountsFromDrawer(
            activity
        )
    }

    override fun openSettings(activity: Activity) {
        settingsNavigation.open(activity)
    }
}
