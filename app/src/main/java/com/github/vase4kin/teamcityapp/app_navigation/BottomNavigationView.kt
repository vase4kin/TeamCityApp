package com.github.vase4kin.teamcityapp.app_navigation

import android.os.Handler
import androidx.annotation.StringRes
import androidx.core.content.ContextCompat
import com.aurelhubert.ahbottomnavigation.AHBottomNavigation
import com.aurelhubert.ahbottomnavigation.AHBottomNavigationItem
import com.github.vase4kin.teamcityapp.R
import com.github.vase4kin.teamcityapp.home.view.HomeActivity
import com.google.android.material.appbar.AppBarLayout
import com.google.android.material.floatingactionbutton.FloatingActionButton

private const val DELAY = 40L

interface BottomNavigationView {
    /**
     * Init bottom navigation
     */
    fun initViews(listener: ViewListener)

    fun showFab()
    fun hideFab()
    fun setTitle(@StringRes title: Int)
    fun updateNotifications(tabPosition: Int, count: Int)
    fun expandToolBar()

    interface ViewListener {
        fun onTabSelected(position: Int, wasSelected: Boolean)
    }
}

class BottomNavigationViewImpl(
        private val interactor: AppNavigationInteractor,
        private val activity: HomeActivity
) : BottomNavigationView {

    private lateinit var favoriteFab: FloatingActionButton
    private lateinit var bottomNavigation: AHBottomNavigation

    private lateinit var listener: BottomNavigationView.ViewListener

    override fun initViews(listener: BottomNavigationView.ViewListener) {
        this.listener = listener
        initBottomNavView()
        initSimpleViews()
        interactor.initNavigation()
    }

    private fun initSimpleViews() {
        favoriteFab = activity.findViewById(R.id.floating_action_button)
    }

    private fun initBottomNavView() {
        bottomNavigation = activity.findViewById(R.id.bottom_navigation)
        bottomNavigation.removeAllItems()

        // Add bottom nav items
        for (itemApp: AppNavigationItem in AppNavigationItem.values()) {
            val bottomNavItem = AHBottomNavigationItem(itemApp.title, itemApp.icon, R.color.colorWhite)
            bottomNavigation.addItem(bottomNavItem)
        }

        // Set bottom nav settings
        bottomNavigation.defaultBackgroundColor = ContextCompat.getColor(activity, R.color.primary)
        bottomNavigation.accentColor = ContextCompat.getColor(activity, R.color.colorWhite)
        bottomNavigation.inactiveColor = ContextCompat.getColor(activity, R.color.colorWhiteWithOpacity)
        bottomNavigation.setNotificationBackgroundColor(ContextCompat.getColor(activity, R.color.accent))
        bottomNavigation.isBehaviorTranslationEnabled = false

        bottomNavigation.setOnTabSelectedListener { position, wasSelected ->
            Handler(activity.mainLooper).postDelayed({
                interactor.switchTab(position)
                listener.onTabSelected(position, wasSelected)
            }, DELAY)
            true
        }
    }

    override fun showFab() {
        favoriteFab.show()
    }

    override fun hideFab() {
        favoriteFab.hide()
    }

    override fun setTitle(title: Int) {
        activity.supportActionBar?.apply {
            this@apply.title = activity.getString(title)
        }
    }

    override fun updateNotifications(tabPosition: Int, count: Int) {
        bottomNavigation.setNotification(count.toString(), tabPosition)
    }

    override fun expandToolBar() {
        activity.findViewById<AppBarLayout>(R.id.toolbar_container).setExpanded(true, true)
    }
}