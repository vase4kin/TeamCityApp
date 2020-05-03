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

package teamcityapp.features.drawer.view

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import teamcityapp.features.drawer.R
import teamcityapp.features.drawer.drawer.DrawerRouter
import teamcityapp.features.drawer.tracker.DrawerTracker

class DrawerAdapter(
    val list: MutableList<BaseDrawerItem>,
    private val viewHolderFactories: Map<Int, BaseDrawerViewHolderFactory>
) : RecyclerView.Adapter<BaseDrawerItemViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BaseDrawerItemViewHolder {
        val type = DrawerType.values()[viewType].type
        return viewHolderFactories[type]?.create(parent) ?: DividerViewHolder(
            parent
        )
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
    private val router: DrawerRouter,
    private val tracker: DrawerTracker
) : BaseDrawerItemViewHolder(
    LayoutInflater.from(parent.context).inflate(
        R.layout.item_drawer_bottom,
        parent,
        false
    )
) {
    override fun bind(drawerItem: BaseDrawerItem) {
        itemView.findViewById<View>(R.id.privacy).setOnClickListener {
            tracker.trackOpenPrivacy()
            router.openPrivacy()
        }
        itemView.findViewById<View>(R.id.rate_the_app).setOnClickListener {
            tracker.trackRateTheApp()
            router.openRateTheApp()
        }
    }
}

class MenuViewHolder(
    parent: ViewGroup,
    private val router: DrawerRouter,
    private val tracker: DrawerTracker
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
                DrawerType.ABOUT -> {
                    tracker.trackOpenAbout()
                    router.openAbout()
                }
                DrawerType.NEW_ACCOUNT -> {
                    tracker.trackOpenAddNewAccount()
                    router.openAddNewAccount()
                }
                DrawerType.MANAGE_ACCOUNTS -> {
                    tracker.trackOpenManageAccounts()
                    router.openManageAccounts()
                }
                DrawerType.SETTINGS -> {
                    tracker.trackOpenSettings()
                    router.openSettings()
                }
                DrawerType.AGENTS -> {
                    // tracker
                    // open agents
                }
                else -> {
                    // Do nothing
                }
            }
        }
    }
}

class AccountViewHolder(
    parent: ViewGroup,
    private val router: DrawerRouter,
    private val tracker: DrawerTracker
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
        if (account.isActive) {
            imageView.setImageResource(R.drawable.ic_account_check_outline)
        } else {
            if (account.isSslDisabled) {
                imageView.setImageResource(R.drawable.ic_account_alert_outline)
            } else {
                imageView.setImageResource(R.drawable.ic_account_outline)
            }
        }
        if (account.isActive.not()) {
            itemView.setOnClickListener {
                tracker.trackChangeAccount()
                router.switchToAccount(account)
            }
        }
    }
}
