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

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.github.vase4kin.teamcityapp.R
import com.github.vase4kin.teamcityapp.new_drawer.drawer.DrawerRouter

class DrawerAdapter(
    val list: MutableList<BaseDrawerItem>,
    private val router: DrawerRouter
) : RecyclerView.Adapter<BaseDrawerItemViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BaseDrawerItemViewHolder {
        return when (viewType) {
            DrawerType.ACCOUNT.hashCode() -> AccountViewHolder(
                parent
            )
            DrawerType.NEW_ACCOUNT.hashCode(),
            DrawerType.MANAGE_ACCOUNTS.hashCode(),
            DrawerType.ABOUT.hashCode() -> MenuViewHolder(
                parent
            )
            DrawerType.ACCOUNTS_DIVIDER.hashCode() -> AccountsDividerViewHolder(
                parent
            )
            DrawerType.DIVIDER.hashCode() -> DividerViewHolder(
                parent
            )
            DrawerType.BOTTOM.hashCode() -> BottomViewHolder(
                parent
            )
            else -> DividerViewHolder(parent)
        }
    }

    override fun getItemCount(): Int = list.count()

    override fun onBindViewHolder(holder: BaseDrawerItemViewHolder, position: Int) {
        val item = list[position]
        holder.bind(item)
        // Do not set click listener for active accounts
        if (item is AccountDrawerItem) {
            val account = item.account
            if (account.isActive) {
                return
            }
        }
        holder.itemView.setOnClickListener {
            when(holder.itemViewType) {
                DrawerType.ABOUT.hashCode() -> router.openAbout()
                DrawerType.NEW_ACCOUNT.hashCode() -> router.openAddNewAccount()
                DrawerType.MANAGE_ACCOUNTS.hashCode() -> router.openManageAccounts()
            }
        }
    }

    override fun getItemViewType(position: Int): Int {
        return list[position].type.hashCode()
    }
}

abstract class BaseDrawerItemViewHolder(
    itemView: View
) : RecyclerView.ViewHolder(itemView) {

    abstract fun bind(drawerItem: BaseDrawerItem)
}

class AccountsDividerViewHolder(
    parent: ViewGroup
) : BaseDrawerItemViewHolder(
    LayoutInflater.from(parent.context).inflate(
        R.layout.layout_drawer_accounts_divider,
        parent,
        false
    )
) {
    override fun bind(drawerItem: BaseDrawerItem) {}
}

class DividerViewHolder(
    parent: ViewGroup
) : BaseDrawerItemViewHolder(
    LayoutInflater.from(parent.context).inflate(
        R.layout.layout_divider,
        parent,
        false
    )
) {
    override fun bind(drawerItem: BaseDrawerItem) {}
}

class BottomViewHolder(
    parent: ViewGroup
) : BaseDrawerItemViewHolder(
    LayoutInflater.from(parent.context).inflate(
        R.layout.item_drawer_bottom,
        parent,
        false
    )
) {
    override fun bind(drawerItem: BaseDrawerItem) {}
}

class MenuViewHolder(
    parent: ViewGroup
) : BaseDrawerItemViewHolder(
    LayoutInflater.from(parent.context).inflate(
        R.layout.item_drawer_menu,
        parent,
        false
    )
) {
    override fun bind(drawerItem: BaseDrawerItem) {
        val item = drawerItem as MenuDrawerItem
        itemView.findViewById<TextView>(R.id.title).setText(item.stringRes)
        itemView.findViewById<ImageView>(R.id.image).setImageResource(drawerItem.imageRes)
    }
}

class AccountViewHolder(
    parent: ViewGroup
) : BaseDrawerItemViewHolder(
    LayoutInflater.from(parent.context).inflate(
        R.layout.item_drawer_user_account,
        parent,
        false
    )
) {

    override fun bind(drawerItem: BaseDrawerItem) {
        val item = drawerItem as AccountDrawerItem
        val account = item.account
        itemView.findViewById<TextView>(R.id.title).text = account.userName
        itemView.findViewById<TextView>(R.id.subTitle).text = account.teamcityUrl
        val imageView: ImageView = itemView.findViewById(R.id.image)
        if (account.isSslDisabled) {
            imageView.setImageResource(R.drawable.ic_account_alert_outline)
        } else {
            imageView.setImageResource(R.drawable.ic_account)
        }
    }
}