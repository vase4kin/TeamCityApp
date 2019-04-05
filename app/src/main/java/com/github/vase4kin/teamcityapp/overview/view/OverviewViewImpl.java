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

package com.github.vase4kin.teamcityapp.overview.view;

import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.StringRes;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.ActionMenuView;
import androidx.appcompat.widget.Toolbar;
import androidx.interpolator.view.animation.FastOutSlowInInterpolator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.github.vase4kin.teamcityapp.R;
import com.github.vase4kin.teamcityapp.bottomsheet_dialog.BottomSheetDialog;
import com.github.vase4kin.teamcityapp.bottomsheet_dialog.menu_items.MenuItemsFactory;
import com.github.vase4kin.teamcityapp.navigation.api.BuildElement;
import com.github.vase4kin.teamcityapp.onboarding.OnboardingManager;
import com.github.vase4kin.teamcityapp.overview.data.OverviewDataModelImpl;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import io.supercharge.shimmerlayout.ShimmerLayout;
import tr.xip.errorview.ErrorView;
import uk.co.samuelwall.materialtaptargetprompt.MaterialTapTargetPrompt;

/**
 * View to manage {@link OverviewFragment}
 */
public class OverviewViewImpl implements OverviewView {

    private static final int TIMEOUT_PROMPT = 500;

    private static final String ICON_TIME = "{mdi-clock}";
    private static final String ICON_BRANCH = "{mdi-git}";
    private static final String ICON_AGENT = "{md-directions-railway}";
    private static final String ICON_TRIGGER_BY = "{md-account-circle}";
    private static final String ICON_BUILD_TYPE = "{md-crop-din}";
    private static final String ICON_PROJECT = "{md-filter-none}";

    private static final String TAG_BOTTOM_SHEET = "BottomSheet Dialog";

    @BindView(R.id.swiperefresh)
    SwipeRefreshLayout mSwipeRefreshLayout;
    @BindView(R.id.my_recycler_view)
    RecyclerView mRecyclerView;
    @BindView(R.id.error_view)
    ErrorView mErrorView;
    @BindView(R.id.skeleton_view)
    ViewGroup skeletonView;

    private Unbinder mUnbinder;

    private ViewListener mListener;

    private AppCompatActivity mActivity;
    private View mView;
    private OverviewAdapter mAdapter;

    private final List<BuildElement> mElements = new ArrayList<>();

    public OverviewViewImpl(View view,
                            AppCompatActivity activity,
                            OverviewAdapter overviewAdapter) {
        this.mActivity = activity;
        this.mView = view;
        this.mAdapter = overviewAdapter;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void initViews(ViewListener listener) {
        this.mListener = listener;
        mUnbinder = ButterKnife.bind(this, mView);
        mErrorView.setImageTint(Color.LTGRAY);
        mErrorView.setRetryListener(listener);
        mSwipeRefreshLayout.setOnRefreshListener(listener);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(mActivity));
        // For testing purposes
        mRecyclerView.setId(R.id.overview_recycler_view);
        skeletonView.removeAllViewsInLayout();
        LayoutInflater.from(mActivity).inflate(R.layout.layout_skeleton_overview_list, skeletonView);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void showCards() {
        mAdapter.setDataModel(new OverviewDataModelImpl(mElements, mActivity.getApplicationContext()));
        mAdapter.setViewListener(mListener);
        mRecyclerView.setAdapter(mAdapter);
        mAdapter.notifyDataSetChanged();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void hideCards() {
        mElements.clear();
        mAdapter.setDataModel(new OverviewDataModelImpl(mElements, mActivity.getApplicationContext()));
        mAdapter.notifyDataSetChanged();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void showSkeletonView() {
        skeletonView.setVisibility(View.VISIBLE);
        ((ShimmerLayout) skeletonView.getChildAt(0)).startShimmerAnimation();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void hideSkeletonView() {
        skeletonView.setVisibility(View.GONE);
        ((ShimmerLayout) skeletonView.getChildAt(0)).startShimmerAnimation();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void showRefreshingProgress() {
        mSwipeRefreshLayout.setRefreshing(true);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void hideRefreshingProgress() {
        mSwipeRefreshLayout.setRefreshing(false);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void showErrorView() {
        mErrorView.setVisibility(View.VISIBLE);
        mErrorView.setSubtitle(R.string.error_view_error_text);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void hideErrorView() {
        mErrorView.setVisibility(View.GONE);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void unbindViews() {
        mUnbinder.unbind();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void addWaitReasonStatusCard(String icon, String waitReason) {
        addCard(R.string.build_wait_reason_section_text, icon, waitReason);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void addResultStatusCard(String icon, String result) {
        addCard(R.string.build_result_section_text, icon, result);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void addCancelledByCard(String icon, String userName) {
        addCard(R.string.build_canceled_by_text, icon, userName);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void addCancellationTimeCard(String time) {
        addCard(R.string.build_cancellation_time_text, ICON_TIME, time);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void addTimeCard(String time) {
        addCard(R.string.build_time_section_text, ICON_TIME, time);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void addQueuedTimeCard(String time) {
        addCard(R.string.build_queued_time_section_text, ICON_TIME, time);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void addEstimatedTimeToStartCard(String time) {
        addCard(R.string.build_time_to_start_section_text, ICON_TIME, time);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void addBranchCard(String branchName) {
        addCard(R.string.build_branch_section_text, ICON_BRANCH, branchName);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void addAgentCard(String agentName) {
        addCard(R.string.build_agent_section_text, ICON_AGENT, agentName);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void addTriggeredByCard(String triggeredBy) {
        addCard(R.string.build_triggered_by_section_text, ICON_TRIGGER_BY, triggeredBy);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void addRestartedByCard(String restartedBy) {
        addCard(R.string.build_restarted_by_section_text, ICON_TRIGGER_BY, restartedBy);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void addTriggeredByUnknownTriggerTypeCard() {
        String unknownTrigger = mActivity.getString(R.string.unknown_trigger_type_text);
        addTriggeredByCard(unknownTrigger);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void addBuildTypeNameCard(String buildTypeName) {
        addCard(R.string.build_type_by_section_text, ICON_BUILD_TYPE, buildTypeName);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void addBuildTypeProjectNameCard(String buildTypeProjectName) {
        addCard(R.string.build_project_by_section_text, ICON_PROJECT, buildTypeProjectName);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void addPersonalCard(String userName) {
        addCard(R.string.build_personal_text, ICON_TRIGGER_BY, userName);
    }

    /**
     * Add card
     *
     * @param header - Header
     * @param icon   - Icon
     * @param text   - Text
     */
    private void addCard(@StringRes int header, String icon, String text) {
        mElements.add(new BuildElement(icon, text, mActivity.getString(header)));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void createStopBuildOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_stop_build_tabs_activity, menu);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void createRemoveBuildFromQueueOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_remove_from_queue_build_tabs_activity, menu);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void createDefaultOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_share_build_tabs_activity, menu);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        mActivity.invalidateOptionsMenu();
        switch (item.getItemId()) {
            case R.id.cancel_build:
                mListener.onCancelBuildContextMenuClick();
                return true;
            case R.id.share_build:
                mListener.onShareButtonClick();
                return true;
            case R.id.restart_build:
                mListener.onRestartBuildButtonClick();
                return true;
            default:
                return false;
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void showDefaultCardBottomSheetDialog(String header, final String description) {
        BottomSheetDialog bottomSheetDialog = BottomSheetDialog.createBottomSheetDialog(header, description, MenuItemsFactory.TYPE_DEFAULT);
        bottomSheetDialog.show(mActivity.getSupportFragmentManager(), TAG_BOTTOM_SHEET);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void showBranchCardBottomSheetDialog(String description) {
        BottomSheetDialog bottomSheetDialog = BottomSheetDialog.createBottomSheetDialog(mActivity.getString(R.string.build_branch_section_text), description, MenuItemsFactory.TYPE_BRANCH);
        bottomSheetDialog.show(mActivity.getSupportFragmentManager(), TAG_BOTTOM_SHEET);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void showBuildTypeCardBottomSheetDialog(String description) {
        BottomSheetDialog bottomSheetDialog = BottomSheetDialog.createBottomSheetDialog(mActivity.getString(R.string.build_type_by_section_text), description, MenuItemsFactory.TYPE_BUILD_TYPE);
        bottomSheetDialog.show(mActivity.getSupportFragmentManager(), TAG_BOTTOM_SHEET);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void showProjectCardBottomSheetDialog(String description) {
        BottomSheetDialog bottomSheetDialog = BottomSheetDialog.createBottomSheetDialog(mActivity.getString(R.string.build_project_by_section_text), description, MenuItemsFactory.TYPE_PROJECT);
        bottomSheetDialog.show(mActivity.getSupportFragmentManager(), TAG_BOTTOM_SHEET);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void showStopBuildPrompt(OnboardingManager.OnPromptShownListener listener) {
        showPrompt(R.string.text_onboarding_stop_build, listener);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void showRestartBuildPrompt(OnboardingManager.OnPromptShownListener listener) {
        showPrompt(R.string.text_onboarding_restart_build, listener);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void showRemoveBuildFromQueuePrompt(OnboardingManager.OnPromptShownListener listener) {
        showPrompt(R.string.text_onboarding_remove_build_from_queue, listener);
    }

    /**
     * Show prompt
     *
     * @param secondaryText - secondary text
     * @param listener      - listener to receive callback when prompt is shown
     */
    private void showPrompt(@StringRes int secondaryText,
                            final OnboardingManager.OnPromptShownListener listener) {
        // Creating prompt
        final Toolbar toolbar = mActivity.findViewById(R.id.toolbar);
        int color = ((ColorDrawable) toolbar.getBackground()).getColor();
        final MaterialTapTargetPrompt.Builder promptBuilder = new MaterialTapTargetPrompt.Builder(mActivity)
                .setPrimaryText(R.string.title_onboarding_build_menu)
                .setSecondaryText(secondaryText)
                .setAnimationInterpolator(new FastOutSlowInInterpolator())
                .setIcon(R.drawable.ic_more_vert_black_24dp)
                .setIconDrawableTintList(ColorStateList.valueOf(color))
                .setBackgroundColour(color)
                .setCaptureTouchEventOutsidePrompt(true)
                .setPromptStateChangeListener((prompt, state) -> listener.onPromptShown());
        // Show prompt
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            final View child = toolbar.getChildAt(3);
            if (child instanceof ActionMenuView) {
                final ActionMenuView actionMenuView = ((ActionMenuView) child);
                promptBuilder.setTarget(actionMenuView.getChildAt(actionMenuView.getChildCount() - 1));
            }
            promptBuilder.show();
        }, TIMEOUT_PROMPT);
    }
}
