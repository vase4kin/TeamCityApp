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

package com.github.vase4kin.teamcityapp.new_drawer.view

import com.github.vase4kin.teamcityapp.R
import com.github.vase4kin.teamcityapp.storage.api.UserAccount

abstract class BaseDrawerItem(
    open val id: Int,
    open val type: DrawerType
)

enum class DrawerType {
    ACCOUNT, ABOUT, NEW_ACCOUNT, MANAGE_ACCOUNTS, ACCOUNTS_DIVIDER, DIVIDER, BOTTOM
}

class AccountDrawerItem(
    val account: UserAccount
) : BaseDrawerItem(account.hashCode(),
    DrawerType.ACCOUNT
)

abstract class MenuDrawerItem(
    override val id: Int,
    override val type: DrawerType,
    val imageRes: Int,
    val stringRes: Int
) : BaseDrawerItem(id, type)

private const val ID_NEW_ACCOUNT = "ID_NEW_ACCOUNT"
private const val ID_MANAGE_ACCOUNTS = "ID_MANAGE_ACCOUNTS"
private const val ID_ABOUT = "ID_ABOUT"
private const val ID_ACCOUNTS_DIVIDER = "ID_ACCOUNTS_DIVIDER"
private const val ID_DIVIDER = "ID_DIVIDER"
private const val ID_BOTTOM = "ID_BOTTOM"

class AccountsDividerDrawerItem : BaseDrawerItem(
    ID_ACCOUNTS_DIVIDER.hashCode(),
    DrawerType.ACCOUNTS_DIVIDER
)

class DividerDrawerItem : BaseDrawerItem(
    ID_DIVIDER.hashCode(),
    DrawerType.DIVIDER
)

class BottomDrawerItem : BaseDrawerItem(
    ID_BOTTOM.hashCode(),
    DrawerType.BOTTOM
)

class NewAccountDrawerItem : MenuDrawerItem(
    id = ID_NEW_ACCOUNT.hashCode(),
    type = DrawerType.NEW_ACCOUNT,
    imageRes = R.drawable.ic_accounts_add,
    stringRes = R.string.text_add_account)

class ManageAccountsDrawerItem : MenuDrawerItem(
    id = ID_MANAGE_ACCOUNTS.hashCode(),
    type = DrawerType.MANAGE_ACCOUNTS,
    imageRes = R.drawable.ic_accounts,
    stringRes = R.string.text_manage_accounts)

class AboutDrawerItem : MenuDrawerItem(
    ID_ABOUT.hashCode(),
    DrawerType.ABOUT,
    imageRes = R.drawable.ic_info_outline_black_24dp,
    stringRes = R.string.about_drawer_item)
