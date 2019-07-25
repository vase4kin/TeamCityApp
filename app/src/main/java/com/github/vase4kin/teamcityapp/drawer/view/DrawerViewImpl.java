/*
 * Copyright 2016 Andrey Tolpeev
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.github.vase4kin.teamcityapp.drawer.view;

import android.graphics.Color;

import androidx.annotation.ColorRes;
import androidx.annotation.DrawableRes;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.afollestad.materialdialogs.MaterialDialog;
import com.github.vase4kin.teamcityapp.R;
import com.github.vase4kin.teamcityapp.about.AboutLibrariesActivity;
import com.github.vase4kin.teamcityapp.agenttabs.view.AgentTabsActivity;
import com.github.vase4kin.teamcityapp.drawer.data.DrawerDataModel;
import com.github.vase4kin.teamcityapp.home.view.HomeActivity;
import com.github.vase4kin.teamcityapp.storage.api.UserAccount;
import com.joanzapata.iconify.IconDrawable;
import com.joanzapata.iconify.fonts.MaterialIcons;
import com.mikepenz.materialdrawer.AccountHeader;
import com.mikepenz.materialdrawer.AccountHeaderBuilder;
import com.mikepenz.materialdrawer.Drawer;
import com.mikepenz.materialdrawer.DrawerBuilder;
import com.mikepenz.materialdrawer.holder.BadgeStyle;
import com.mikepenz.materialdrawer.holder.StringHolder;
import com.mikepenz.materialdrawer.model.DividerDrawerItem;
import com.mikepenz.materialdrawer.model.PrimaryDrawerItem;
import com.mikepenz.materialdrawer.model.ProfileDrawerItem;
import com.mikepenz.materialdrawer.model.ProfileSettingDrawerItem;
import com.mikepenz.materialdrawer.model.interfaces.IProfile;

import java.util.ArrayList;

/**
 * Impl of {@link DrawerView}
 */
public class DrawerViewImpl implements DrawerView {

    protected final AppCompatActivity activity;
    protected Drawer drawerResult;
    private ArrayList<IProfile> profileList = new ArrayList<>();
    protected Toolbar toolbar;
    private AccountHeader headerResult;
    private DrawerDataModel drawerDataModel;

    private OnDrawerPresenterListener mOnDrawerPresenterListener;

    private final int mDrawerSelection;
    private final boolean mIsBackArrowEnabled;

    @ColorRes
    protected int mDefaultColor = R.color.default_color;

    public DrawerViewImpl(AppCompatActivity activity, int drawerSelection, boolean isBackArrowEnabled) {
        this.activity = activity;
        this.mDrawerSelection = drawerSelection;
        this.mIsBackArrowEnabled = isBackArrowEnabled;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void initViews(OnDrawerPresenterListener listener) {
        mOnDrawerPresenterListener = listener;
        setActionBar();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void showData(DrawerDataModel dataModel) {
        drawerDataModel = dataModel;
        initProfilesDrawerItems();
        initAccountHeader();
        initDrawer();
        setActiveProfile();
        if (mIsBackArrowEnabled) {
            drawerResult.getActionBarDrawerToggle().setDrawerIndicatorEnabled(false);
            if (activity.getSupportActionBar() != null) {
                ActionBar actionBar = activity.getSupportActionBar();
                actionBar.setDisplayHomeAsUpEnabled(true);
            }
        }
        drawerResult.setSelection(mDrawerSelection, false);
    }

    /**
     * Every Activity should have ToolBar in order to handle navigation drawer
     */
    private void setActionBar() {
        Toolbar toolbar = activity.findViewById(R.id.toolbar);
        this.toolbar = toolbar;
        activity.setSupportActionBar(toolbar);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isModelEmpty() {
        return drawerDataModel == null || drawerDataModel.isEmpty();
    }

    /**
     * Set active profile
     */
    private void setActiveProfile() {
        for (IProfile iProfile : getUserProfiles(drawerDataModel)) {
            if (mOnDrawerPresenterListener.isActiveProfile(
                    iProfile.getEmail().toString(),
                    iProfile.getName().toString())) {
                headerResult.setActiveProfile(iProfile);
                headerResult.updateProfile(iProfile);
                break;
            }
        }
    }

    /**
     * Init profiles drawer items
     */
    private void initProfilesDrawerItems() {
        profileList.clear();
        for (IProfile iProfile : getUserProfiles(drawerDataModel)) {
            // Not show profile in the profile list, which is enabled
            if (!mOnDrawerPresenterListener.isActiveProfile(iProfile.getEmail().toString(), iProfile.getName().toString())) {
                profileList.add(iProfile);
            }
        }
        profileList.add(new ProfileSettingDrawerItem()
                .withName("Manage Accounts")
                .withIcon(new IconDrawable(activity, MaterialIcons.md_settings).colorRes(mDefaultColor))
                .withIdentifier(PROFILES_MANAGING));
    }

    /**
     * Get user profiles
     */
    private ArrayList<IProfile> getUserProfiles(DrawerDataModel dataModel) {
        ArrayList<IProfile> profiles = new ArrayList<>();
        for (UserAccount userAccount : dataModel) {
            IProfile iProfile = new ProfileDrawerItem()
                    .withName(userAccount.getUserName())
                    .withEmail(userAccount.getTeamcityUrl())
                    .withIcon(new IconDrawable(activity, MaterialIcons.md_account_circle).colorRes(mDefaultColor));
            profiles.add(iProfile);
        }
        return profiles;
    }

    /**
     * Init account header
     */
    private void initAccountHeader() {
        headerResult = new AccountHeaderBuilder()
                .withActivity(activity)
                .withCompactStyle(true)
                .withProfiles(profileList)
                .withProfileImagesVisible(true)
                .withHeaderBackground(mDefaultColor)
                .withTextColorRes(R.color.md_white_1000)
                .withOnAccountHeaderListener((view, iProfile, b) -> {
                    if (iProfile != null) {
                        switch ((int) iProfile.getIdentifier()) {
                            case PROFILES_MANAGING:
                                mOnDrawerPresenterListener.startAccountListActivity();
                                break;
                            default:
                                if (!mOnDrawerPresenterListener.isActiveProfile(
                                        iProfile.getEmail().toString(),
                                        iProfile.getName().toString())) {
                                    mOnDrawerPresenterListener.setActiveUser(
                                            iProfile.getEmail().toString(),
                                            iProfile.getName().toString());
                                    mOnDrawerPresenterListener.startRootProjectsActivityWhenSwitchingAccounts();
                                    mOnDrawerPresenterListener.onUserChange();
                                }
                                break;
                        }
                    }
                    return false;
                })
                .withProfileImagesVisible(false)
                .build();
    }

    /**
     * Init drawer
     */
    private void initDrawer() {
        BadgeStyle badgeStyle = new BadgeStyle().withTextColor(Color.WHITE).withColorRes(mDefaultColor);
        drawerResult = new DrawerBuilder()
                .withActivity(activity)
                .withToolbar(toolbar)
                .withAccountHeader(headerResult)
                .addDrawerItems(
                        new PrimaryDrawerItem()
                                .withName(R.string.home_drawer_item)
                                .withIcon(new IconDrawable(activity, MaterialIcons.md_home).colorRes(mDefaultColor))
                                .withSelectedTextColorRes(mDefaultColor)
                                .withIdentifier(HOME),
                        new PrimaryDrawerItem()
                                .withName(R.string.agents_drawer_item)
                                .withIcon(new IconDrawable(activity, MaterialIcons.md_directions_railway).colorRes(mDefaultColor))
                                .withSelectedTextColorRes(mDefaultColor)
                                .withBadgeStyle(badgeStyle)
                                .withIdentifier(AGENTS),
                        new DividerDrawerItem(),
                        new PrimaryDrawerItem()
                                .withName("About")
                                .withIcon(new IconDrawable(activity, MaterialIcons.md_help).colorRes(mDefaultColor))
                                .withSelectedTextColorRes(mDefaultColor)
                                .withIdentifier(ABOUT)
                )
                .withOnDrawerItemClickListener((view, position, drawerItem) -> {
                    if (drawerItem != null) {

                        switch ((int) drawerItem.getIdentifier()) {
                            case HOME:
                                if (activity instanceof HomeActivity) {
                                    break;
                                }
                                mOnDrawerPresenterListener.startHomeActivity();
                                break;
                            case AGENTS:
                                if (activity instanceof AgentTabsActivity) {
                                    break;
                                }
                                mOnDrawerPresenterListener.startAgentActivity();
                                break;
                            case ABOUT:
                                if (activity instanceof AboutLibrariesActivity) {
                                    break;
                                }
                                mOnDrawerPresenterListener.startAboutActivity();
                                break;
                            default:
                                showDialogWithAdvice();
                                break;
                        }
                    }
                    return false;
                })
                .withOnDrawerNavigationListener(clickedView -> {
                    //this method is only called if the Arrow icon is shown. The hamburger is automatically managed by the MaterialDrawer
                    //if the back arrow is shown. close the activity
                    backButtonPressed();
                    //return true if we have consumed the event
                    return true;
                })
                .withOnDrawerListener(new OnDrawerListenerImpl(mOnDrawerPresenterListener))
                .withShowDrawerOnFirstLaunch(false)
                .build();
    }

    /**
     * Show dialog for default drawer click
     */
    private void showDialogWithAdvice() {
        new MaterialDialog.Builder(activity)
                .title("Good advice")
                .content("These aren't the features you're looking for")
                .positiveText("Ok")
                .positiveColor(activity.getResources().getColor(R.color.md_blue_A100))
                .build().show();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void backButtonPressed() {
        //handle the back press :D close the drawer first and if the drawer is closed close the activity
        if (!isDrawerWasClosed()) {
            activity.finish();
            if (!activity.isTaskRoot()) {
                overridePendingTransition();
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void updateAgentsBadge(int count) {
        drawerResult.updateBadge(AGENTS, new StringHolder(String.valueOf(count)));
        drawerResult.setSelection(mDrawerSelection, false);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setDefaultColors(@DrawableRes int color) {
        mDefaultColor = color;
    }

    /**
     * Method to know if the drawer was closed or not
     *
     * @return
     */
    private boolean isDrawerWasClosed() {
        if (drawerResult != null && drawerResult.isDrawerOpen()) {
            drawerResult.closeDrawer();
            return true;
        } else {
            return false;
        }
    }

    /**
     * {@inheritDoc}
     */
    protected void overridePendingTransition() {
        activity.overridePendingTransition(R.anim.abc_fade_in, R.anim.abc_fade_out);
    }
}
