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

package com.github.vase4kin.teamcityapp.app_navigation

import android.os.Bundle
import android.os.Handler
import com.github.vase4kin.teamcityapp.R
import com.github.vase4kin.teamcityapp.home.view.HomeActivity
import com.google.android.material.floatingactionbutton.FloatingActionButton
import teamcityapp.libraries.utils.getThemeColor

private const val DELAY = 40L

interface BottomNavigationView {
    /**
     * Init bottom navigation
     */
    fun initViews(listener: ViewListener, savedInstanceState: Bundle?)

    fun showFavoritesFab()
    fun showFilterFab()
    fun hideFab()
    fun updateNotifications(tabPosition: Int, count: Int)
    fun selectTab(tabPosition: Int)

    interface ViewListener {
        fun onTabSelected(navItem: AppNavigationItem, wasSelected: Boolean)
        fun onFavoritesFabClicked()
        fun onFilterTabsClicked(navItem: AppNavigationItem)
    }
}

class BottomNavigationViewImpl(
    private val interactor: AppNavigationInteractor,
    private val activity: HomeActivity
) : BottomNavigationView {

    private lateinit var fab: FloatingActionButton
    private lateinit var navigation: com.google.android.material.bottomnavigation.BottomNavigationView

    private lateinit var listener: BottomNavigationView.ViewListener

    override fun initViews(
        listener: BottomNavigationView.ViewListener,
        savedInstanceState: Bundle?
    ) {
        this.listener = listener
        initViews()
        initBottomNavView()
        initFab()
        interactor.initNavigation(savedInstanceState)
    }

    private fun initViews() {
        navigation = activity.findViewById(R.id.navigation)
        fab = activity.findViewById(R.id.home_floating_action_button)
    }

    private fun initFab() {
        fab.setOnClickListener {
            when (val currentItem = navigation.selectedItemId) {
                R.id.favorites -> listener.onFavoritesFabClicked()
                R.id.build_queue, R.id.running_builds, R.id.agents -> {
                    AppNavigationItem.values().find { it.id == currentItem }?.let {
                        listener.onFilterTabsClicked(it)
                    }
                }
            }
        }
    }

    private fun initBottomNavView() {
        navigation.setOnNavigationItemSelectedListener { item ->
            AppNavigationItem.values().find { it.id == item.itemId }?.let {
                selectTabInternal(it, item.isChecked)
            }
            true
        }
    }

    private fun selectTabInternal(navItem: AppNavigationItem, isSelected: Boolean) {
        Handler(activity.mainLooper).postDelayed(
            {
                interactor.switchTab(navItem.ordinal)
                listener.onTabSelected(navItem, isSelected)
            },
            DELAY
        )
    }

    override fun showFavoritesFab() = fab.run {
        setImageResource(R.drawable.ic_add_black_24dp)
        show()
    }

    override fun showFilterFab() = fab.run {
        setImageResource(R.drawable.ic_filter_list_white_24px)
        show()
    }

    override fun hideFab() = fab.hide()

    override fun updateNotifications(tabPosition: Int, count: Int) {
        getAppNavItemByPosition(tabPosition)?.let {
            val menuItemId = it.id
            val badgeDrawable = navigation.getOrCreateBadge(menuItemId)
            badgeDrawable.backgroundColor = activity.getThemeColor(R.attr.colorSecondary)
            badgeDrawable.badgeTextColor = activity.getThemeColor(R.attr.colorOnPrimarySurface)
            badgeDrawable.isVisible = true
            badgeDrawable.number = count
            badgeDrawable.verticalOffset = activity.resources.getDimensionPixelOffset(R.dimen.dp_4)
        }
    }

    override fun selectTab(tabPosition: Int) {
        getAppNavItemByPosition(tabPosition)?.let {
            navigation.selectedItemId = it.id
        }
    }

    private fun getAppNavItemByPosition(position: Int): AppNavigationItem? =
        AppNavigationItem.values().getOrNull(position)
}
