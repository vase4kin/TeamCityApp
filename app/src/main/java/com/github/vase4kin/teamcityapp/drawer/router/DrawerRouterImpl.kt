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

package com.github.vase4kin.teamcityapp.drawer.router

import androidx.appcompat.app.AppCompatActivity

import com.github.vase4kin.teamcityapp.about.AboutActivity
import com.github.vase4kin.teamcityapp.account.manage.view.AccountListActivity
import com.github.vase4kin.teamcityapp.agenttabs.view.AgentTabsActivity
import com.github.vase4kin.teamcityapp.home.view.HomeActivity

/**
 * Impl of [DrawerRouter]
 */
open class DrawerRouterImpl(protected var activity: AppCompatActivity) : DrawerRouter {

    /**
     * {@inheritDoc}
     */
    override fun startHomeActivity() {
        HomeActivity.startWhenNavigateToRootFromDrawer(activity)
    }

    /**
     * {@inheritDoc}
     */
    override fun startRootProjectsActivityWhenSwitchingAccounts() {
        HomeActivity.startWhenSwitchingAccountsFromDrawer(activity)
    }

    /**
     * {@inheritDoc}
     */
    override fun startAccountListActivity() {
        AccountListActivity.start(activity)
    }

    /**
     * {@inheritDoc}
     */
    override fun startAgentActivity() {
        AgentTabsActivity.start(activity)
    }

    /**
     * {@inheritDoc}
     */
    override fun startAboutActivity() {
        AboutActivity.start(activity)
    }
}
