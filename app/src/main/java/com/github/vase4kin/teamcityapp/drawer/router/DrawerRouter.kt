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

import com.github.vase4kin.teamcityapp.about.AboutLibrariesActivity

/**
 * Drawer router
 */
interface DrawerRouter {

    /**
     * Start [com.github.vase4kin.teamcityapp.home.view.HomeActivity]
     */
    fun startHomeActivity()

    /**
     * Start [com.github.vase4kin.teamcityapp.home.view.HomeActivity] when accounts are switched
     */
    fun startRootProjectsActivityWhenSwitchingAccounts()

    /**
     * Start [com.github.vase4kin.teamcityapp.account.manage.view.AccountListActivity]
     */
    fun startAccountListActivity()

    /**
     * Start [AboutLibrariesActivity]
     */
    fun startAboutActivity()
}
