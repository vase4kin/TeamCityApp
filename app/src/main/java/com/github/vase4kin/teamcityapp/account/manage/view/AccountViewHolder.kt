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

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.TextView
import butterknife.BindView
import butterknife.ButterKnife
import com.github.vase4kin.teamcityapp.R
import com.github.vase4kin.teamcityapp.account.manage.data.AccountDataModel
import com.github.vase4kin.teamcityapp.base.list.view.BaseViewHolder
import com.joanzapata.iconify.widget.IconTextView

private const val DEFAULT_ACCOUNT_ICON = "{md-account-circle}"

class AccountViewHolder(parent: ViewGroup) : BaseViewHolder<AccountDataModel>(
    LayoutInflater.from(parent.context).inflate(
        R.layout.item_user_account_list,
        parent,
        false
    )
) {

    @BindView(R.id.itemSubTitle)
    lateinit var teamCityUrlTextView: TextView
    @BindView(R.id.itemTitle)
    lateinit var userNameTextView: TextView
    @BindView(R.id.itemIcon)
    lateinit var accountIcon: IconTextView
    @BindView(R.id.container)
    lateinit var container: FrameLayout

    init {
        ButterKnife.bind(this, itemView)
    }

    override fun bind(dataModel: AccountDataModel, position: Int) {
        teamCityUrlTextView.text = dataModel.getTeamcityUrl(position)
        userNameTextView.text = dataModel.getUserName(position)
        accountIcon.text = DEFAULT_ACCOUNT_ICON
    }
}
