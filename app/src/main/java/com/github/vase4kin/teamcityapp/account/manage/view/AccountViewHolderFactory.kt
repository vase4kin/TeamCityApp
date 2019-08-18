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

import android.view.ViewGroup
import com.github.vase4kin.teamcityapp.account.manage.data.AccountDataModel
import com.github.vase4kin.teamcityapp.base.list.view.BaseViewHolder
import com.github.vase4kin.teamcityapp.base.list.view.ViewHolderFactory

/**
 * Accounts single item view factory
 */
class AccountViewHolderFactory : ViewHolderFactory<AccountDataModel> {

    /**
     * {@inheritDoc}
     */
    override fun createViewHolder(parent: ViewGroup): BaseViewHolder<AccountDataModel> {
        return AccountViewHolder(parent)
    }
}
