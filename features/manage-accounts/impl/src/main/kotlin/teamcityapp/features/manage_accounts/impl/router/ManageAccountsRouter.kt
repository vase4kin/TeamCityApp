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

package teamcityapp.features.manage_accounts.impl.router

import android.app.Activity
import teamcityapp.features.manage_accounts.api.AccountDestination
import teamcityapp.features.manage_accounts.api.router.ManageAccountsAppRouter

interface ManageAccountsRouter {
    fun close()
    fun createAccount()
    fun navigate(destination: AccountDestination)
}
internal class ManageAccountsRouterImpl(
    private val activity: Activity,
    private val appRouter: ManageAccountsAppRouter
) : ManageAccountsRouter {
    override fun close() {
        activity.finish()
    }
    override fun createAccount() {
        appRouter.openCreateAccount(activity)
    }
    override fun navigate(destination: AccountDestination) {
        when (destination) {
            AccountDestination.Home -> appRouter.openHome(activity)
            AccountDestination.Login -> appRouter.openLogin(activity)
        }
    }
}
