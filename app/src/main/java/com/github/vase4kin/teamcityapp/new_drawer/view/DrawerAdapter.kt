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
        return when (DrawerType.values()[viewType]) {
            DrawerType.ACCOUNT -> AccountViewHolder(parent, router)
            DrawerType.NEW_ACCOUNT,
            DrawerType.MANAGE_ACCOUNTS,
            DrawerType.ABOUT -> MenuViewHolder(parent, router)
            DrawerType.ACCOUNTS_DIVIDER -> AccountsDividerViewHolder(parent)
            DrawerType.DIVIDER -> DividerViewHolder(parent)
            DrawerType.BOTTOM -> BottomViewHolder(parent, router)
        }
    }

    override fun getItemCount(): Int = list.count()

    override fun onBindViewHolder(holder: BaseDrawerItemViewHolder, position: Int) {
        val item = list[position]
        holder.bind(item)
    }

    override fun getItemViewType(position: Int): Int {
        return list[position].type.ordinal
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
    parent: ViewGroup,
    private val router: DrawerRouter
) : BaseDrawerItemViewHolder(
    LayoutInflater.from(parent.context).inflate(
        R.layout.item_drawer_bottom,
        parent,
        false
    )
) {
    override fun bind(drawerItem: BaseDrawerItem) {
        itemView.findViewById<View>(R.id.privacy).setOnClickListener {
            router.openPrivacy()
        }
        itemView.findViewById<View>(R.id.website).setOnClickListener {
            router.openWebsite()
        }
    }
}

class MenuViewHolder(
    parent: ViewGroup,
    private val router: DrawerRouter
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
        itemView.setOnClickListener {
            when (drawerItem.type) {
                DrawerType.ABOUT -> router.openAbout()
                DrawerType.NEW_ACCOUNT -> router.openAddNewAccount()
                DrawerType.MANAGE_ACCOUNTS -> router.openManageAccounts()
                else -> {
                    //Do nothing
                }
            }
        }
    }
}

class AccountViewHolder(
    parent: ViewGroup,
    private val router: DrawerRouter
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
        if (account.isActive) {
            // Control router behaviour
        }
    }
}