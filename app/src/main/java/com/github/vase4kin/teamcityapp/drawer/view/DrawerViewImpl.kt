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

package com.github.vase4kin.teamcityapp.drawer.view

import android.graphics.drawable.Drawable
import androidx.annotation.DrawableRes
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import com.afollestad.materialdialogs.MaterialDialog
import com.github.vase4kin.teamcityapp.R
import com.github.vase4kin.teamcityapp.about.AboutLibrariesActivity
import com.github.vase4kin.teamcityapp.drawer.data.DrawerDataModel
import com.github.vase4kin.teamcityapp.home.view.HomeActivity
import com.github.vase4kin.teamcityapp.utils.getTintedDrawable
import com.mikepenz.materialdrawer.AccountHeader
import com.mikepenz.materialdrawer.AccountHeaderBuilder
import com.mikepenz.materialdrawer.Drawer
import com.mikepenz.materialdrawer.DrawerBuilder
import com.mikepenz.materialdrawer.holder.StringHolder
import com.mikepenz.materialdrawer.model.DividerDrawerItem
import com.mikepenz.materialdrawer.model.PrimaryDrawerItem
import com.mikepenz.materialdrawer.model.ProfileDrawerItem
import com.mikepenz.materialdrawer.model.ProfileSettingDrawerItem
import com.mikepenz.materialdrawer.model.interfaces.IProfile
import java.util.ArrayList

/**
 * Impl of [DrawerView]
 */
open class DrawerViewImpl(
    protected val activity: AppCompatActivity,
    private val drawerSelection: Int,
    private val isBackArrowEnabled: Boolean
) : DrawerView {
    private val profileList = ArrayList<IProfile<*>>()
    protected lateinit var toolbar: Toolbar
    protected lateinit var drawerResult: Drawer
    private lateinit var headerResult: AccountHeader
    private lateinit var drawerDataModel: DrawerDataModel

    private var onDrawerPresenterListener: OnDrawerPresenterListener? = null

    /**
     * Method to know if the drawer was closed or not
     *
     * @return
     */
    private val isDrawerWasClosed: Boolean
        get() {
            return if (drawerResult.isDrawerOpen) {
                drawerResult.closeDrawer()
                true
            } else {
                false
            }
        }

    /**
     * {@inheritDoc}
     */
    override fun initViews(listener: OnDrawerPresenterListener) {
        onDrawerPresenterListener = listener
        setActionBar()
    }

    /**
     * {@inheritDoc}
     */
    override fun showData(dataModel: DrawerDataModel) {
        drawerDataModel = dataModel
        initProfilesDrawerItems()
        initAccountHeader()
        initDrawer()
        setActiveProfile()
        if (isBackArrowEnabled) {
            drawerResult.actionBarDrawerToggle.isDrawerIndicatorEnabled = false
            val actionBar = activity.supportActionBar
            actionBar?.setDisplayHomeAsUpEnabled(true)
        }
        drawerResult.setSelection(drawerSelection.toLong(), false)
    }

    /**
     * Every Activity should have ToolBar in order to handle navigation drawer
     */
    private fun setActionBar() {
        val toolbar = activity.findViewById<Toolbar>(R.id.toolbar)
        this.toolbar = toolbar
        activity.setSupportActionBar(toolbar)
    }

    /**
     * {@inheritDoc}
     */
    override val isModelEmpty: Boolean
        get() = drawerDataModel.isEmpty

    /**
     * Set active profile
     */
    private fun setActiveProfile() {
        for (iProfile in headerResult.profiles) {
            if (onDrawerPresenterListener!!.isActiveProfile(
                    iProfile.email.toString(),
                    iProfile.name.toString()
                )
            ) {
                headerResult.activeProfile = iProfile
                break
            }
        }
    }

    /**
     * Init profiles drawer items
     */
    private fun initProfilesDrawerItems() {
        profileList.clear()
        profileList.addAll(getUserProfiles(drawerDataModel))
        profileList.add(
            ProfileSettingDrawerItem()
                .withName("Manage Accounts")
                .withIcon(getDrawable(R.drawable.ic_settings_black_24dp))
                .withIdentifier(DrawerView.PROFILES_MANAGING.toLong())
        )
    }

    /**
     * Get user profiles
     */
    private fun getUserProfiles(dataModel: DrawerDataModel): ArrayList<IProfile<*>> {
        val profiles = ArrayList<IProfile<*>>()
        for (userAccount in dataModel) {
            val id =
                String.format("%s+%s", userAccount.userName, userAccount.teamcityUrl).hashCode()
                    .toLong()
            val iProfile = ProfileDrawerItem()
                .withName(userAccount.userName)
                .withEmail(userAccount.teamcityUrl)
                .withIcon(getDrawable(R.drawable.ic_account_circle_black_24dp))
                .withNameShown(true)
                .withIdentifier(id)
            profiles.add(iProfile)
        }
        return profiles
    }

    /**
     * Init account header
     */
    private fun initAccountHeader() {
        headerResult = AccountHeaderBuilder()
            .withActivity(activity)
            .withCompactStyle(true)
            .withProfiles(profileList)
            .withProfileImagesVisible(true)
            .withHeaderBackground(R.color.md_white_1000)
            .withOnAccountHeaderListener { _, iProfile, _ ->
                if (iProfile != null) {
                    when (iProfile.identifier.toInt()) {
                        DrawerView.PROFILES_MANAGING -> onDrawerPresenterListener?.startAccountListActivity()
                        else -> if (!onDrawerPresenterListener!!.isActiveProfile(
                                iProfile.email.toString(),
                                iProfile.name.toString()
                            )
                        ) {
                            onDrawerPresenterListener?.setActiveUser(
                                iProfile.email.toString(),
                                iProfile.name.toString()
                            )
                            onDrawerPresenterListener?.startRootProjectsActivityWhenSwitchingAccounts()
                            onDrawerPresenterListener?.onUserChange()
                        }
                    }
                }
                false
            }
            .withProfileImagesVisible(false)
            .build()
    }

    /**
     * Init drawer
     */
    private fun initDrawer() {
        drawerResult = DrawerBuilder()
            .withActivity(activity)
            .withToolbar(toolbar)
            .withAccountHeader(headerResult)
            .addDrawerItems(
                PrimaryDrawerItem()
                    .withName(R.string.home_drawer_item)
                    .withIcon(getDrawable(R.drawable.ic_home_black_24dp))
                    .withSelectedTextColorRes(R.color.primary)
                    .withIdentifier(DrawerView.HOME.toLong()),
                DividerDrawerItem(),
                PrimaryDrawerItem()
                    .withName("About")
                    .withIcon(getDrawable(R.drawable.ic_help_black_24dp))
                    .withSelectedTextColorRes(R.color.primary)
                    .withIdentifier(DrawerView.ABOUT.toLong())
            )
            .withOnDrawerItemClickListener { _, _, drawerItem ->
                if (drawerItem != null) {

                    when (drawerItem.identifier.toInt()) {
                        DrawerView.HOME -> {
                            val isHomeActivity = activity is HomeActivity
                            if (!isHomeActivity) {
                                onDrawerPresenterListener?.startHomeActivity()
                            }
                        }
                        DrawerView.ABOUT -> {
                            val isAboutActivity = activity is AboutLibrariesActivity
                            if (!isAboutActivity) {
                                onDrawerPresenterListener?.startAboutActivity()
                            }
                        }
                        else -> showDialogWithAdvice()
                    }
                }
                false
            }
            .withOnDrawerNavigationListener {
                // this method is only called if the Arrow icon is shown. The hamburger is automatically managed by the MaterialDrawer
                // if the back arrow is shown. close the activity
                backButtonPressed()
                // return true if we have consumed the event
                true
            }
            .withOnDrawerListener(OnDrawerListenerImpl(onDrawerPresenterListener!!))
            .withShowDrawerOnFirstLaunch(false)
            .build()
    }

    /**
     * Show dialog for default drawer click
     */
    private fun showDialogWithAdvice() {
        MaterialDialog.Builder(activity)
            .title("Good advice")
            .content("These aren't the features you're looking for")
            .positiveText("Ok")
            .positiveColor(activity.resources.getColor(R.color.md_blue_A100))
            .build().show()
    }

    /**
     * {@inheritDoc}
     */
    override fun backButtonPressed() {
        // handle the back press :D close the drawer first and if the drawer is closed close the activity
        if (!isDrawerWasClosed) {
            activity.finish()
            if (!activity.isTaskRoot) {
                overridePendingTransition()
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    override fun updateAgentsBadge(count: Int) {
        drawerResult.updateBadge(DrawerView.AGENTS.toLong(), StringHolder(count.toString()))
        drawerResult.setSelection(drawerSelection.toLong(), false)
    }

    /**
     * {@inheritDoc}
     */
    protected open fun overridePendingTransition() {
        activity.overridePendingTransition(R.anim.abc_fade_in, R.anim.abc_fade_out)
    }

    private fun getDrawable(@DrawableRes intRes: Int): Drawable {
        return getTintedDrawable(activity, intRes, R.color.primary)
    }
}
