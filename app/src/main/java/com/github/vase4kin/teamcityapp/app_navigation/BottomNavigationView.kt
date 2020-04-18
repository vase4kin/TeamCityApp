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
import android.widget.TextView
import androidx.annotation.StringRes
import androidx.core.content.ContextCompat
import com.aurelhubert.ahbottomnavigation.AHBottomNavigation
import com.aurelhubert.ahbottomnavigation.AHBottomNavigationItem
import com.github.vase4kin.teamcityapp.R
import com.github.vase4kin.teamcityapp.home.view.HomeActivity
import com.google.android.material.appbar.AppBarLayout
import com.google.android.material.elevation.ElevationOverlayProvider
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
    fun setTitle(@StringRes title: Int)
    fun updateNotifications(tabPosition: Int, count: Int)
    fun expandToolBar()
    fun selectTab(tabPosition: Int)

    interface ViewListener {
        fun onTabSelected(position: Int, wasSelected: Boolean)
        fun onFavoritesFabClicked()
        fun onFilterTabsClicked(position: Int)
    }
}

class BottomNavigationViewImpl(
    private val interactor: AppNavigationInteractor,
    private val activity: HomeActivity
) : BottomNavigationView {

    private lateinit var fab: FloatingActionButton
    private lateinit var bottomNavigation: AHBottomNavigation

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
        bottomNavigation = activity.findViewById(R.id.bottom_navigation)
        fab = activity.findViewById(R.id.home_floating_action_button)
    }

    private fun initFab() {
        fab.setOnClickListener {
            when (val currentItem = bottomNavigation.currentItem) {
                AppNavigationItem.FAVORITES.ordinal -> listener.onFavoritesFabClicked()
                AppNavigationItem.BUILD_QUEUE.ordinal, AppNavigationItem.RUNNING_BUILDS.ordinal, AppNavigationItem.AGENTS.ordinal -> listener.onFilterTabsClicked(
                    currentItem
                )
            }
        }
    }

    private fun initBottomNavView() {
        bottomNavigation.removeAllItems()

        // Add bottom nav items
        for (itemApp: AppNavigationItem in AppNavigationItem.values()) {
            val bottomNavItem =
                AHBottomNavigationItem(
                    itemApp.title,
                    itemApp.icon,
                    R.color.material_on_primary_emphasis_high_type
                )
            bottomNavigation.addItem(bottomNavItem)
        }

        val elevation =
            activity.resources.getDimension(R.dimen.dp_8)
        val backgroundColor = ElevationOverlayProvider(activity).compositeOverlayIfNeeded(
            activity.getThemeColor(R.attr.colorPrimarySurface), elevation
        )
        // Set bottom nav settings
        bottomNavigation.defaultBackgroundColor = backgroundColor
        bottomNavigation.accentColor =
            activity.getThemeColor(R.attr.colorOnPrimary)
        bottomNavigation.inactiveColor =
            ContextCompat.getColor(activity, R.color.material_on_primary_emphasis_medium)
        bottomNavigation.setNotificationBackgroundColor(
            activity.getThemeColor(R.attr.colorSecondary)
        )
        bottomNavigation.isBehaviorTranslationEnabled = false

        bottomNavigation.setOnTabSelectedListener { position, wasSelected ->
            Handler(activity.mainLooper).postDelayed({
                interactor.switchTab(position)
                listener.onTabSelected(position, wasSelected)
            }, DELAY)
            true
        }
    }

    override fun showFavoritesFab() = fab.run {
        hide()
        setImageResource(R.drawable.ic_add_black_24dp)
        show()
    }

    override fun showFilterFab() = fab.run {
        hide()
        setImageResource(R.drawable.ic_filter_list_white_24px)
        show()
    }

    override fun hideFab() = fab.hide()

    override fun setTitle(title: Int) {
        activity.findViewById<TextView>(R.id.toolbar_title).text = activity.getString(title)
    }

    override fun updateNotifications(tabPosition: Int, count: Int) {
        bottomNavigation.setNotification(count.toString(), tabPosition)
    }

    override fun expandToolBar() {
        activity.findViewById<AppBarLayout>(R.id.toolbar_container).setExpanded(true, true)
    }

    override fun selectTab(tabPosition: Int) {
        bottomNavigation.currentItem = tabPosition
    }
}
