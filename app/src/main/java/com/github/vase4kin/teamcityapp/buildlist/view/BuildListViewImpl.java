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

package com.github.vase4kin.teamcityapp.buildlist.view;

import android.app.Activity;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.support.annotation.ColorInt;
import android.support.annotation.NonNull;
import android.support.annotation.StringRes;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.view.animation.FastOutSlowInInterpolator;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.TextView;

import com.afollestad.materialdialogs.MaterialDialog;
import com.github.vase4kin.teamcityapp.R;
import com.github.vase4kin.teamcityapp.base.list.view.BaseListViewImpl;
import com.github.vase4kin.teamcityapp.base.list.view.SimpleSectionedRecyclerViewAdapter;
import com.github.vase4kin.teamcityapp.buildlist.data.BuildListDataModel;
import com.github.vase4kin.teamcityapp.buildlist.data.OnBuildListPresenterListener;
import com.github.vase4kin.teamcityapp.onboarding.OnboardingManager;
import com.joanzapata.iconify.IconDrawable;
import com.joanzapata.iconify.fonts.MaterialIcons;
import com.mugen.Mugen;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindString;
import butterknife.BindView;
import tr.xip.errorview.ErrorView;
import uk.co.samuelwall.materialtaptargetprompt.MaterialTapTargetPrompt;

/**
 * Impl of {@link BuildListView}
 */
public class BuildListViewImpl extends BaseListViewImpl<BuildListDataModel, SimpleSectionedRecyclerViewAdapter<BuildListAdapter>> implements BuildListView {

    @BindString(R.string.text_queued_header)
    String mQueuedHeaderText;
    @BindView(R.id.floating_action_button)
    FloatingActionButton mFloatingActionButton;
    private MaterialDialog mProgressDialog;
    private List<SimpleSectionedRecyclerViewAdapter.Section> mSections;
    private BuildListDataModel mDataModel;

    protected OnBuildListPresenterListener mOnBuildListPresenterListener;

    public BuildListViewImpl(View mView,
                             Activity activity,
                             @StringRes int emptyMessage,
                             SimpleSectionedRecyclerViewAdapter<BuildListAdapter> adapter) {
        super(mView, activity, emptyMessage, adapter);
    }

    /**
     * {@inheritDoc}
     */
    public void setOnBuildListPresenterListener(OnBuildListPresenterListener onBuildListPresenterListener) {
        this.mOnBuildListPresenterListener = onBuildListPresenterListener;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void initViews(@NonNull ErrorView.RetryListener retryListener,
                          @NonNull SwipeRefreshLayout.OnRefreshListener refreshListener) {
        super.initViews(retryListener, refreshListener);
        mFloatingActionButton.setImageDrawable(new IconDrawable(mActivity, MaterialIcons.md_directions_run).color(Color.WHITE));
        mFloatingActionButton.hide();
        mFloatingActionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mOnBuildListPresenterListener.onRunBuildFabClick();
            }
        });
        mProgressDialog = new MaterialDialog.Builder(mActivity)
                .content(R.string.text_opening_build)
                .progress(true, 0)
                .autoDismiss(false)
                .build();
        mProgressDialog.setCancelable(false);
        mProgressDialog.setCanceledOnTouchOutside(false);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setTitle(String title) {
        ActionBar actionBar = ((AppCompatActivity) mActivity).getSupportActionBar();
        if (actionBar != null) {
            actionBar.setTitle(title);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void showRunBuildFloatActionButton() {
        mFloatingActionButton.show();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void hideRunBuildFloatActionButton() {
        mFloatingActionButton.hide();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void addLoadMore() {
        BuildListAdapter baseAdapter = mAdapter.getBaseAdapter();
        baseAdapter.addLoadMore();
        baseAdapter.notifyDataSetChanged();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void removeLoadMore() {
        BuildListAdapter baseAdapter = mAdapter.getBaseAdapter();
        baseAdapter.removeLoadMore();
        baseAdapter.notifyDataSetChanged();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void showRetryLoadMoreSnackBar() {
        Snackbar snackBar = Snackbar.make(
                mRecyclerView,
                R.string.load_more_retry_snack_bar_text,
                Snackbar.LENGTH_LONG)
                .setAction(R.string.download_artifact_retry_snack_bar_retry_button, new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        mOnBuildListPresenterListener.onLoadMore();
                    }
                });
        TextView textView = (TextView) snackBar.getView().findViewById(android.support.design.R.id.snackbar_text);
        textView.setTextColor(Color.WHITE);
        snackBar.show();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void addMoreBuilds(BuildListDataModel dataModel) {
        BuildListAdapter baseAdapter = mAdapter.getBaseAdapter();
        baseAdapter.addMoreBuilds(dataModel);
        reInitSections(mDataModel);
        setSections(mSections);
        mRecyclerView.getAdapter().notifyDataSetChanged();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void showData(BuildListDataModel dataModel) {
        this.mDataModel = dataModel;
        mSections = initSections(dataModel);
        BuildListAdapter baseAdapter = mAdapter.getBaseAdapter();
        baseAdapter.setDataModel(dataModel);
        baseAdapter.setOnBuildListPresenterListener(mOnBuildListPresenterListener);
        initSectionAdapter();
        Mugen.with(mRecyclerView, mOnBuildListPresenterListener).start();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected int recyclerViewId() {
        return R.id.build_recycler_view;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void showBuildQueuedSuccessSnackBar() {
        Snackbar snackBar = Snackbar.make(
                mRecyclerView,
                R.string.text_build_is_run,
                Snackbar.LENGTH_LONG)
                .setAction(R.string.text_show_build, new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        mOnBuildListPresenterListener.onShowQueuedBuildSnackBarClick();
                    }
                });
        TextView textView = (TextView) snackBar.getView().findViewById(android.support.design.R.id.snackbar_text);
        textView.setTextColor(Color.WHITE);
        snackBar.show();
    }

    @Override
    public void showBuildFilterAppliedSnackBar() {
        Snackbar snackBar = Snackbar.make(
                mRecyclerView,
                R.string.text_filters_applied,
                Snackbar.LENGTH_LONG);
        TextView textView = (TextView) snackBar.getView().findViewById(android.support.design.R.id.snackbar_text);
        textView.setTextColor(Color.WHITE);
        snackBar.show();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void showOpeningBuildErrorSnackBar() {
        Snackbar snackBar = Snackbar.make(
                mRecyclerView,
                R.string.error_opening_build,
                Snackbar.LENGTH_LONG)
                .setAction(R.string.download_artifact_retry_snack_bar_retry_button, new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        mOnBuildListPresenterListener.onShowQueuedBuildSnackBarClick();
                    }
                });
        TextView textView = (TextView) snackBar.getView().findViewById(android.support.design.R.id.snackbar_text);
        textView.setTextColor(Color.WHITE);
        snackBar.show();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void showBuildLoadingProgress() {
        mProgressDialog.show();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void hideBuildLoadingProgress() {
        mProgressDialog.dismiss();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void showFilterBuildsPrompt(final OnboardingManager.OnPromptShownListener listener) {
        int color = getToolbarColor();
        new MaterialTapTargetPrompt.Builder(mActivity)
                .setTarget(R.id.filter_builds)
                .setPrimaryText(R.string.title_onboarding_filter_builds)
                .setSecondaryText(R.string.text_onboarding_filter_builds)
                .setAnimationInterpolator(new FastOutSlowInInterpolator())
                .setIcon(R.drawable.ic_filter_list_white_24px)
                .setIconDrawableTintList(ColorStateList.valueOf(color))
                .setBackgroundColour(color)
                .setCaptureTouchEventOutsidePrompt(true)
                .setOnHidePromptListener(new MaterialTapTargetPrompt.OnHidePromptListener() {
                    @Override
                    public void onHidePrompt(MotionEvent event, boolean tappedTarget) {
                        listener.onPromptShown();
                    }

                    @Override
                    public void onHidePromptComplete() {
                    }
                })
                .show();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void showRunBuildPrompt(final OnboardingManager.OnPromptShownListener listener) {
        int color = getToolbarColor();
        new MaterialTapTargetPrompt.Builder(mActivity)
                .setTarget(mFloatingActionButton)
                .setPrimaryText(R.string.title_onboarding_run_build)
                .setSecondaryText(R.string.text_onboarding_run_build)
                .setAnimationInterpolator(new FastOutSlowInInterpolator())
                .setBackgroundColour(color)
                .setCaptureTouchEventOutsidePrompt(true)
                .setOnHidePromptListener(new MaterialTapTargetPrompt.OnHidePromptListener() {
                    @Override
                    public void onHidePrompt(MotionEvent event, boolean tappedTarget) {
                        listener.onPromptShown();
                    }

                    @Override
                    public void onHidePromptComplete() {
                    }
                })
                .show();
    }

    /**
     * @return color of toolbar
     */
    @ColorInt
    private int getToolbarColor() {
        return ((ColorDrawable) mActivity.findViewById(R.id.toolbar).getBackground()).getColor();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void createOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_filter_builds_activity, menu);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        mActivity.invalidateOptionsMenu();
        switch (item.getItemId()) {
            case R.id.filter_builds:
                mOnBuildListPresenterListener.onFilterBuildsOptionMenuClick();
                return true;
            default:
                return false;
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isBuildListOpen() {
        return mActivity instanceof BuildListActivity;
    }

    /**
     * Init sectionAdapter
     *
     * @param
     */
    private void initSectionAdapter() {
        setSections(mSections);
        mRecyclerView.setAdapter(mAdapter);
        mRecyclerView.getAdapter().notifyDataSetChanged();
    }

    /**
     * Set sections for mSectionAdapter
     *
     * @param sections
     */
    private void setSections(List<SimpleSectionedRecyclerViewAdapter.Section> sections) {
        SimpleSectionedRecyclerViewAdapter.Section[] userStates = new SimpleSectionedRecyclerViewAdapter.Section[sections.size()];
        mAdapter.setSections(sections.toArray(userStates));
    }

    /**
     * Return sections for build data
     *
     * @param dataModel
     * @return List<SimpleSectionedRecyclerViewAdapter.Section>
     */
    private List<SimpleSectionedRecyclerViewAdapter.Section> initSections(BuildListDataModel dataModel) {
        List<SimpleSectionedRecyclerViewAdapter.Section> sections = new ArrayList<>();

        if (dataModel.getItemCount() != 0) {
            for (int i = 0; i < dataModel.getItemCount(); i++) {
                String sectionTitle = dataModel.isQueued(i) ? mQueuedHeaderText
                        : dataModel.getStartDate(i);
                if (sections.size() != 0) {
                    SimpleSectionedRecyclerViewAdapter.Section prevSection = sections.get(sections.size() - 1);
                    if (!prevSection.getTitle().equals(sectionTitle)) {
                        sections.add(new SimpleSectionedRecyclerViewAdapter.Section(i, sectionTitle));
                    }
                } else {
                    sections.add(new SimpleSectionedRecyclerViewAdapter.Section(i, sectionTitle));
                }
            }
        }
        return sections;
    }

    /**
     * ReInit sections if more builds are loaded
     */
    private void reInitSections(BuildListDataModel dataModel) {
        mSections.clear();
        mSections.addAll(initSections(dataModel));
    }
}
