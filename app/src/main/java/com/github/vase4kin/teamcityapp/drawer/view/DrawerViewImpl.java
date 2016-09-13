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
import android.support.annotation.ColorRes;
import android.support.annotation.DrawableRes;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;

import com.afollestad.materialdialogs.MaterialDialog;
import com.github.vase4kin.teamcityapp.R;
import com.github.vase4kin.teamcityapp.about.AboutActivity;
import com.github.vase4kin.teamcityapp.agenttabs.view.AgentTabsActivity;
import com.github.vase4kin.teamcityapp.drawer.data.DrawerDataModel;
import com.github.vase4kin.teamcityapp.queue.view.BuildQueueActivity;
import com.github.vase4kin.teamcityapp.root.view.RootProjectsActivity;
import com.github.vase4kin.teamcityapp.runningbuilds.view.RunningBuildsListActivity;
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
import com.mikepenz.materialdrawer.model.interfaces.IDrawerItem;
import com.mikepenz.materialdrawer.model.interfaces.IProfile;

import java.util.ArrayList;

/**
 * Impl of {@link DrawerView}
 */
public class DrawerViewImpl implements DrawerView {

    public static final int DELAY_ON_CLOSE = 350;

    protected AppCompatActivity mActivity;
    protected Drawer mDrawerResult;
    ArrayList<IProfile> mProfileList = new ArrayList<>();
    private Toolbar mToolbar;
    private AccountHeader mHeaderResult;
    protected DrawerDataModel mDrawerDataModel;

    private OnDrawerPresenterListener mOnDrawerPresenterListener;

    private final int mDrawerSelection;
    private final boolean mIsBackArrowEnabled;

    @ColorRes
    private int mDefaultColor = R.color.default_color;

    public DrawerViewImpl(AppCompatActivity activity, int drawerSelection, boolean isBackArrowEnabled) {
        this.mActivity = activity;
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
        mDrawerDataModel = dataModel;
        initProfilesDrawerItems();
        initAccountHeader();
        initDrawer();
        setActiveProfile();
        if (mIsBackArrowEnabled) {
            mDrawerResult.getActionBarDrawerToggle().setDrawerIndicatorEnabled(false);
            if (mActivity.getSupportActionBar() != null) {
                ActionBar actionBar = mActivity.getSupportActionBar();
                actionBar.setDisplayHomeAsUpEnabled(true);
            }
        }
        mDrawerResult.setSelection(mDrawerSelection, false);
    }

    /**
     * Every Activity should have ToolBar in order to handle navigation drawer
     */
    private void setActionBar() {
        Toolbar toolbar = (Toolbar) mActivity.findViewById(R.id.toolbar);
        mToolbar = toolbar;
        mActivity.setSupportActionBar(toolbar);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isModelEmpty() {
        return mDrawerDataModel == null || mDrawerDataModel.isEmpty();
    }

    /**
     * Set active profile
     */
    private void setActiveProfile() {
        for (IProfile iProfile : getUserProfiles(mDrawerDataModel)) {
            if (mOnDrawerPresenterListener.isActiveProfile(iProfile.getEmail().toString())) {
                mHeaderResult.setActiveProfile(iProfile);
                mHeaderResult.updateProfile(iProfile);
                break;
            }
        }
    }

    /**
     * Init profiles drawer items
     */
    private void initProfilesDrawerItems() {
        mProfileList.clear();
        for (IProfile iProfile : getUserProfiles(mDrawerDataModel)) {
            // Not show profile in the profile list, which is enabled
            if (!mOnDrawerPresenterListener.isActiveProfile(iProfile.getEmail().toString())) {
                mProfileList.add(iProfile);
            }
        }
        mProfileList.add(new ProfileSettingDrawerItem()
                .withName("Manage Accounts")
                .withIcon(new IconDrawable(mActivity, MaterialIcons.md_settings).colorRes(mDefaultColor))
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
                    .withIcon(new IconDrawable(mActivity, MaterialIcons.md_account_circle).colorRes(mDefaultColor));
            profiles.add(iProfile);
        }
        return profiles;
    }

    /**
     * Init account header
     */
    private void initAccountHeader() {
        mHeaderResult = new AccountHeaderBuilder()
                .withActivity(mActivity)
                .withProfiles(mProfileList)
                .withProfileImagesVisible(true)
                .withHeaderBackground(mDefaultColor)
                .withTextColorRes(R.color.md_white_1000)
                .withOnAccountHeaderListener(new AccountHeader.OnAccountHeaderListener() {
                    @Override
                    public boolean onProfileChanged(View view, IProfile iProfile, boolean b) {
                        if (iProfile != null) {
                            switch (iProfile.getIdentifier()) {
                                case PROFILES_MANAGING:
                                    mOnDrawerPresenterListener.startAccountListActivity();
                                    break;
                                default:
                                    if (!mOnDrawerPresenterListener.isActiveProfile(iProfile.getEmail().toString())) {
                                        mOnDrawerPresenterListener.setActiveUser(iProfile.getEmail().toString());
                                        mOnDrawerPresenterListener.startRootProjectsActivityWhenSwitchingAccounts();
                                        mOnDrawerPresenterListener.onUserChange();
                                    }
                                    break;
                            }
                        }
                        return false;
                    }
                })
                .withProfileImagesVisible(false)
                .build();
    }

    /**
     * Init drawer
     */
    private void initDrawer() {
        BadgeStyle badgeStyle = new BadgeStyle().withTextColor(Color.WHITE).withColorRes(mDefaultColor);
        mDrawerResult = new DrawerBuilder()
                .withActivity(mActivity)
                .withToolbar(mToolbar)
                .withAccountHeader(mHeaderResult)
                .addDrawerItems(
                        new PrimaryDrawerItem()
                                .withName(R.string.projects_drawer_item)
                                .withIcon(new IconDrawable(mActivity, MaterialIcons.md_home).colorRes(mDefaultColor))
                                .withSelectedTextColorRes(mDefaultColor)
                                .withIdentifier(PROJECTS),
                        new PrimaryDrawerItem()
                                .withName(R.string.running_builds_drawer_item)
                                .withIcon(new IconDrawable(mActivity, MaterialIcons.md_directions_run).colorRes(mDefaultColor))
                                .withSelectedTextColorRes(mDefaultColor)
                                .withBadgeStyle(badgeStyle)
                                .withIdentifier(RUNNING_BUILDS),
                        new PrimaryDrawerItem()
                                .withName(R.string.build_queue_drawer_item)
                                .withIcon(new IconDrawable(mActivity, MaterialIcons.md_layers).colorRes(mDefaultColor))
                                .withSelectedTextColorRes(mDefaultColor)
                                .withBadgeStyle(badgeStyle)
                                .withIdentifier(BUILD_QUEUE),
                        new PrimaryDrawerItem()
                                .withName(R.string.agents_drawer_item)
                                .withIcon(new IconDrawable(mActivity, MaterialIcons.md_directions_railway).colorRes(mDefaultColor))
                                .withSelectedTextColorRes(mDefaultColor)
                                .withBadgeStyle(badgeStyle)
                                .withIdentifier(AGENTS),
                        new DividerDrawerItem(),
                        new PrimaryDrawerItem()
                                .withName("About")
                                .withIcon(new IconDrawable(mActivity, MaterialIcons.md_help).colorRes(mDefaultColor))
                                .withSelectedTextColorRes(mDefaultColor)
                                .withIdentifier(ABOUT)
                )
                .withOnDrawerItemClickListener(new Drawer.OnDrawerItemClickListener() {
                    @Override
                    public boolean onItemClick(View view, int position, IDrawerItem drawerItem) {
                        if (drawerItem != null) {

                            switch (drawerItem.getIdentifier()) {
                                case PROJECTS:
                                    if (mActivity instanceof RootProjectsActivity) {
                                        break;
                                    }
                                    mOnDrawerPresenterListener.startRootProjectsActivity();
                                    break;
                                case AGENTS:
                                    if (mActivity instanceof AgentTabsActivity) {
                                        break;
                                    }
                                    mOnDrawerPresenterListener.startAgentActivity();
                                    break;
                                case RUNNING_BUILDS:
                                    if (mActivity instanceof RunningBuildsListActivity) {
                                        break;
                                    }
                                    mOnDrawerPresenterListener.startBuildRunningActivity();
                                    break;
                                case BUILD_QUEUE:
                                    if (mActivity instanceof BuildQueueActivity) {
                                        break;
                                    }
                                    mOnDrawerPresenterListener.startQueuedBuildsActivity();
                                    break;
                                case ABOUT:
                                    if (mActivity instanceof AboutActivity) {
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
                    }
                })
                .withOnDrawerNavigationListener(new Drawer.OnDrawerNavigationListener() {
                    @Override
                    public boolean onNavigationClickListener(View clickedView) {
                        //this method is only called if the Arrow icon is shown. The hamburger is automatically managed by the MaterialDrawer
                        //if the back arrow is shown. close the activity
                        backButtonPressed();
                        //return true if we have consumed the event
                        return true;
                    }
                })
                .withOnDrawerListener(new OnDrawerListenerImpl(mOnDrawerPresenterListener))
                .withShowDrawerOnFirstLaunch(false)
                .build();
    }

    /**
     * Show dialog for default drawer click
     */
    private void showDialogWithAdvice() {
        new MaterialDialog.Builder(mActivity)
                .title("Good advice")
                .content("These aren't the features you're looking for")
                .positiveText("Ok")
                .positiveColor(mActivity.getResources().getColor(R.color.md_blue_A100))
                .build().show();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void backButtonPressed() {
        //handle the back press :D close the drawer first and if the drawer is closed close the activity
        if (!isDrawerWasClosed()) {
            mActivity.finish();
            if (!mActivity.isTaskRoot()) {
                overridePendingTransition();
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void updateRunningBuildsBadge(int count) {
        mDrawerResult.updateBadge(RUNNING_BUILDS, new StringHolder(String.valueOf(count)));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void updateAgentsBadge(int count) {
        mDrawerResult.updateBadge(AGENTS, new StringHolder(String.valueOf(count)));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void updateBuildQueueBadge(int count) {
        mDrawerResult.updateBadge(BUILD_QUEUE, new StringHolder(String.valueOf(count)));
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
    public boolean isDrawerWasClosed() {
        if (mDrawerResult != null && mDrawerResult.isDrawerOpen()) {
            mDrawerResult.closeDrawer();
            return true;
        } else {
            return false;
        }
    }

    /**
     * {@inheritDoc}
     */
    protected void overridePendingTransition() {
        mActivity.overridePendingTransition(R.anim.abc_fade_in, R.anim.abc_fade_out);
    }
}
