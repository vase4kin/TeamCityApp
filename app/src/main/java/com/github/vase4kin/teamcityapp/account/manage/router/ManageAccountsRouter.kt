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

package com.github.vase4kin.teamcityapp.account.manage.router

import android.app.Activity
import com.github.vase4kin.teamcityapp.account.create.view.CreateAccountActivity
import com.github.vase4kin.teamcityapp.home.view.HomeActivity
import com.github.vase4kin.teamcityapp.login.view.LoginActivity

interface ManageAccountsRouter {

    fun openHome()

    fun openCreateNewAccount()

    fun openLogin()
}

class ManageAccountsRouterImpl(private val activity: Activity) : ManageAccountsRouter {

    override fun openHome() {
        HomeActivity.startWhenSwitchingAccountsFromDrawer(activity)
    }

    override fun openCreateNewAccount() {
        CreateAccountActivity.start(activity)
    }

    override fun openLogin() {
        LoginActivity.startWithClearStack(activity)
    }
}
