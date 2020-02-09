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

package teamcityapp.features.manage_accounts.view

import teamcityapp.features.manage_accounts.tracker.ManageAccountsTracker
import teamcityapp.libraries.storage.models.UserAccount

interface AccountItemFactory {
    fun create(
        userAccount: UserAccount,
        onAccountRemove: () -> Unit
    ): AccountItem
}

class AccountItemFactoryImpl(
    private val tracker: ManageAccountsTracker,
    private val showSslDisabledInfoDialog: () -> Unit,
    private val showRemoveAccountDialog: (onAccountRemove: () -> Unit) -> Unit
) : AccountItemFactory {
    override fun create(
        userAccount: UserAccount,
        onAccountRemove: () -> Unit
    ): AccountItem {
        return AccountItem(
            userAccount = userAccount,
            showSslDisabledInfoDialog = {
                tracker.trackUserClicksOnSslDisabledWarning()
                showSslDisabledInfoDialog()
            },
            showRemoveAccountDialog = {
                showRemoveAccountDialog { onAccountRemove() }
            })
    }
}
