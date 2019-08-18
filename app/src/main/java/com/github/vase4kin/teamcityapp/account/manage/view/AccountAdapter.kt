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

package com.github.vase4kin.teamcityapp.account.manage.view

import com.github.vase4kin.teamcityapp.account.manage.data.AccountDataModel
import com.github.vase4kin.teamcityapp.base.list.adapter.BaseAdapter
import com.github.vase4kin.teamcityapp.base.list.view.BaseViewHolder
import com.github.vase4kin.teamcityapp.base.list.view.ViewHolderFactory

/**
 * Adapter to handle data for [AccountListActivity]
 */
class AccountAdapter(viewHolderFactories: Map<Int, ViewHolderFactory<AccountDataModel>>) :
    BaseAdapter<AccountDataModel>(viewHolderFactories) {

    private var listener: AccountsView.ViewListener? = null

    fun setListener(listener: AccountsView.ViewListener) {
        this.listener = listener
    }

    override fun onBindViewHolder(holder: BaseViewHolder<AccountDataModel>, position: Int) {
        super.onBindViewHolder(holder, position)
        val userAccount = dataModel.get(position)
        (holder as AccountViewHolder).mContainer.setOnClickListener { listener!!.onAccountClick(userAccount) }
        holder.mContainer.setOnLongClickListener {
            listener!!.onAccountClick(userAccount)
            true
        }
    }
}
