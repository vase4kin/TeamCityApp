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

package teamcityapp.features.manage_accounts.view

import android.view.View
import com.xwray.groupie.databinding.BindableItem
import teamcityapp.features.manage_accounts.R
import teamcityapp.features.manage_accounts.databinding.ItemUserAccountListBinding
import teamcityapp.libraries.storage.models.UserAccount

class AccountItem(
    private val userAccount: UserAccount,
    private val showSslDisabledInfoDialog: () -> Unit,
    private val showRemoveAccountDialog: () -> Unit
) : BindableItem<ItemUserAccountListBinding>() {

    override fun getLayout() = R.layout.item_user_account_list

    override fun bind(viewBinding: ItemUserAccountListBinding, position: Int) {
        viewBinding.apply {
            if (userAccount.isSslDisabled) {
                image.setImageResource(R.drawable.ic_account_alert)
                sslDisabled.visibility = View.VISIBLE
                sslDisabled.setOnClickListener {
                    showSslDisabledInfoDialog()
                }
            } else {
                image.setImageResource(R.drawable.ic_account)
                sslDisabled.visibility = View.GONE
            }
            title.text = userAccount.userName
            subTitle.text = userAccount.teamcityUrl
            root.setOnClickListener { showRemoveAccountDialog() }
        }
    }
}
