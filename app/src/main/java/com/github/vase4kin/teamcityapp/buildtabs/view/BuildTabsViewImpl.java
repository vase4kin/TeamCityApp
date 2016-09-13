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

package com.github.vase4kin.teamcityapp.buildtabs.view;

import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.annotation.ColorRes;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.TabLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

import com.github.vase4kin.teamcityapp.R;
import com.github.vase4kin.teamcityapp.artifact.view.ArtifactListFragment;
import com.github.vase4kin.teamcityapp.base.list.extractor.BaseValueExtractor;
import com.github.vase4kin.teamcityapp.base.tabs.view.BaseTabsViewModelImpl;
import com.github.vase4kin.teamcityapp.base.tabs.view.FragmentAdapter;
import com.github.vase4kin.teamcityapp.buildlist.api.Build;
import com.github.vase4kin.teamcityapp.buildlog.view.BuildLogFragment;
import com.github.vase4kin.teamcityapp.changes.view.ChangesFragment;
import com.github.vase4kin.teamcityapp.overview.view.BuildOverviewElementsFragment;
import com.github.vase4kin.teamcityapp.properties.view.PropertiesFragment;
import com.github.vase4kin.teamcityapp.tests.view.TestOccurrencesFragment;
import com.github.vase4kin.teamcityapp.utils.DateUtils;
import com.github.vase4kin.teamcityapp.utils.StatusBarUtils;
import com.joanzapata.iconify.IconDrawable;
import com.joanzapata.iconify.fonts.MaterialIcons;

import butterknife.BindView;

/**
 * Impl of {@link BuildTabsView}
 */
public class BuildTabsViewImpl extends BaseTabsViewModelImpl implements BuildTabsView {

    private static final String TAB_TITLE = "tabTitle";

    @BindView(R.id.floating_action_button)
    FloatingActionButton mFloatingActionButton;

    private Build mBuild;

    private String overviewTabTitle;
    private String artifactsTabTitle;
    private String mTabTitle;

    private StatusBarUtils mStatusBarUtils;
    private OnTabUnSelectListener mOnTabUnSelectListener;

    public BuildTabsViewImpl(View mView,
                             AppCompatActivity mActivity,
                             StatusBarUtils statusBarUtils,
                             BaseValueExtractor valueExtractor) {
        super(mView, mActivity);
        this.mStatusBarUtils = statusBarUtils;
        this.mBuild = valueExtractor.getBuild();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void addFragments(FragmentAdapter fragmentAdapter) {
        fragmentAdapter.add(R.string.tab_overview, BuildOverviewElementsFragment.newInstance(mBuild));
        fragmentAdapter.add(R.string.tab_changes, ChangesFragment.newInstance(mBuild.getChanges().getHref()));
        if (mBuild.getTestOccurrences() != null) {
            fragmentAdapter.add(R.string.tab_tests, TestOccurrencesFragment.newInstance(
                    mBuild.getTestOccurrences().getHref(),
                    mBuild.getTestOccurrences().getPassed(),
                    mBuild.getTestOccurrences().getFailed(),
                    mBuild.getTestOccurrences().getIgnored()));
        }
        if (!mBuild.isQueued()) {
            fragmentAdapter.add(R.string.tab_build_log, BuildLogFragment.newInstance(mBuild.getId()));
        }
        fragmentAdapter.add(R.string.tab_parameters, PropertiesFragment.newInstance(mBuild));
        if (!mBuild.isQueued() && !mBuild.isRunning()) {
            fragmentAdapter.add(R.string.tab_artifacts, ArtifactListFragment.newInstance(mBuild, mBuild.getArtifacts().getHref()));
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void initViews() {
        super.initViews();
        mFloatingActionButton.setImageDrawable(new IconDrawable(mActivity, MaterialIcons.md_directions_run).color(Color.WHITE));
        int offScreenPageLimit = mViewPager.getAdapter().getCount();
        mViewPager.setOffscreenPageLimit(offScreenPageLimit);
        overviewTabTitle = mActivity.getString(R.string.tab_overview);
        artifactsTabTitle = mActivity.getString(R.string.tab_artifacts);
        mTabLayout.setOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                mViewPager.setCurrentItem(tab.getPosition());
                mTabTitle = tab.getText().toString();
                if (mTabTitle.equals(overviewTabTitle)) {
                    showRunBuildFloatActionButton();
                } else {
                    hideRunBuildFloatActionButton();
                }
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {
                if (tab.getText().toString().equals(artifactsTabTitle)) {
                    mOnTabUnSelectListener.onArtifactTabUnSelect();
                }
            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {
            }
        });
        setTitle();
        setColorsByBuildType();

        // <!---------------------------------------!>
        // REMOVE THIS LINE WHEN RUNNING BUILD FEATURE IS IMPLEMENTED
        mFloatingActionButton.hide();
        // REMOVE THIS LINE WHEN RUNNING BUILD FEATURE IS IMPLEMENTED
        // <!---------------------------------------!>
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setOnTabUnSelectListener(OnTabUnSelectListener onTabUnSelectListener) {
        this.mOnTabUnSelectListener = onTabUnSelectListener;
    }

    /**
     * Setting proper color for different build types
     */
    private void setColorsByBuildType() {
        if (mBuild.isQueued()) {
            mStatusBarUtils.changeStatusBarColor(mActivity, R.color.queued_status_bar_color);
            setToolBarAndTabLayoutColor(R.color.queued_tool_bar_color);
            mTabLayout.setTabTextColors(
                    mActivity.getResources().getColor(R.color.tab_queued_unselected_color),
                    mActivity.getResources().getColor(R.color.md_white_1000));
        } else if (mBuild.isRunning()) {
            mStatusBarUtils.changeStatusBarColor(mActivity, R.color.running_status_bar_color);
            setToolBarAndTabLayoutColor(R.color.running_tool_bar_color);
            mTabLayout.setTabTextColors(
                    mActivity.getResources().getColor(R.color.tab_running_unselected_color),
                    mActivity.getResources().getColor(R.color.md_white_1000));
        } else if (mBuild.isFailed()) {
            mStatusBarUtils.changeStatusBarColor(mActivity, R.color.failed_status_bar_color);
            setToolBarAndTabLayoutColor(R.color.failed_tool_bar_color);
            mTabLayout.setTabTextColors(
                    mActivity.getResources().getColor(R.color.tab_failed_unselected_color),
                    mActivity.getResources().getColor(R.color.md_white_1000));
        } else if (mBuild.isSuccess()) {
            mStatusBarUtils.changeStatusBarColor(mActivity, R.color.success_status_bar_color);
            setToolBarAndTabLayoutColor(R.color.success_tool_bar_color);
            mTabLayout.setTabTextColors(
                    mActivity.getResources().getColor(R.color.tab_success_unselected_color),
                    mActivity.getResources().getColor(R.color.md_white_1000));
        } else {
            mStatusBarUtils.changeStatusBarColor(mActivity, R.color.queued_status_bar_color);
            setToolBarAndTabLayoutColor(R.color.queued_tool_bar_color);
            mTabLayout.setTabTextColors(
                    mActivity.getResources().getColor(R.color.tab_queued_unselected_color),
                    mActivity.getResources().getColor(R.color.md_white_1000));
        }
    }

    /**
     * Setting toolbar color
     *
     * @param color - Color to set
     */
    private void setToolBarAndTabLayoutColor(@ColorRes int color) {
        ActionBar actionBar = mActivity.getSupportActionBar();
        if (actionBar != null) {
            actionBar.setBackgroundDrawable(new ColorDrawable(mActivity.getResources().getColor(color)));
        }
        mTabLayout.setBackgroundColor(mActivity.getResources().getColor(color));
    }

    /**
     * Set toolbar title
     */
    private void setTitle() {
        String startDate;
        if (mBuild.isQueued()) {
            startDate = DateUtils.initWithDate(mBuild.getQueuedDate()).formatStartDateToBuildTitle();
        } else {
            startDate = DateUtils.initWithDate(mBuild.getStartDate()).formatStartDateToBuildTitle();
        }
        String title = String.format("#%s (%s)", mBuild.getNumber() == null ? "No number" : mBuild.getNumber(), startDate);
        ActionBar actionBar = mActivity.getSupportActionBar();
        if (actionBar != null) {
            actionBar.setTitle(title);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onSave(Bundle bundle) {
        bundle.putString(TAB_TITLE, mTabTitle);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onRestore(Bundle bundle) {
        if (bundle != null) {
            mTabTitle = bundle.getString(TAB_TITLE);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void showRunBuildFloatActionButton() {
        // <!---------------------------------------!>
        // UNCOMMENT THIS LINE WHEN RUNNING BUILD FEATURE IS IMPLEMENTED
        // mFloatingActionButton.show();
        // UNCOMMENT THIS LINE WHEN RUNNING BUILD FEATURE IS IMPLEMENTED
        // <!---------------------------------------!>
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void hideRunBuildFloatActionButton() {
        // <!---------------------------------------!>
        // UNCOMMENT THIS LINE WHEN RUNNING BUILD FEATURE IS IMPLEMENTED
        // mFloatingActionButton.hide();
        // UNCOMMENT THIS LINE WHEN RUNNING BUILD FEATURE IS IMPLEMENTED
        // <!---------------------------------------!>
    }
}
