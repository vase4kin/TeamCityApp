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

package com.github.vase4kin.teamcityapp.buildlist.view;

import android.app.Activity;
import android.content.res.ColorStateList;
import android.graphics.drawable.ColorDrawable;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;

import androidx.annotation.ColorInt;
import androidx.annotation.NonNull;
import androidx.annotation.StringRes;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.interpolator.view.animation.FastOutSlowInInterpolator;

import com.afollestad.materialdialogs.MaterialDialog;
import com.github.vase4kin.teamcityapp.R;
import com.github.vase4kin.teamcityapp.base.list.view.BaseListViewImpl;
import com.github.vase4kin.teamcityapp.base.list.view.SimpleSectionedRecyclerViewAdapter;
import com.github.vase4kin.teamcityapp.buildlist.data.BuildListDataModel;
import com.github.vase4kin.teamcityapp.buildlist.data.OnBuildListPresenterListener;
import com.github.vase4kin.teamcityapp.onboarding.OnboardingManager;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.snackbar.Snackbar;
import com.mugen.Mugen;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindString;
import butterknife.BindView;
import uk.co.samuelwall.materialtaptargetprompt.MaterialTapTargetPrompt;
import uk.co.samuelwall.materialtaptargetprompt.extras.focals.RectanglePromptFocal;

/**
 * Impl of {@link BuildListView}
 */
public class BuildListViewImpl extends BaseListViewImpl<BuildListDataModel, SimpleSectionedRecyclerViewAdapter<BuildListAdapter>> implements BuildListView {

    @BindString(R.string.text_queued_header)
    String mQueuedHeaderText;
    @BindView(R.id.floating_action_button)
    MaterialButton mFloatingActionButton;
    private MaterialDialog mProgressDialog;
    private List<SimpleSectionedRecyclerViewAdapter.Section> mSections;
    private BuildListDataModel mDataModel;
    private Snackbar mFiltersAppliedSnackBar;

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
    public void setOnBuildListPresenterListener(@NonNull OnBuildListPresenterListener onBuildListPresenterListener) {
        this.mOnBuildListPresenterListener = onBuildListPresenterListener;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void initViews(@NonNull ViewListener listener) {
        super.initViews(listener);
        mFloatingActionButton.setVisibility(View.GONE);
        mFloatingActionButton.setOnClickListener(v -> mOnBuildListPresenterListener.onRunBuildFabClick());
        mProgressDialog = new MaterialDialog.Builder(getActivity())
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
    public void setTitle(@NonNull String title) {
        ActionBar actionBar = ((AppCompatActivity) getActivity()).getSupportActionBar();
        if (actionBar != null) {
            actionBar.setTitle(title);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void showRunBuildFloatActionButton() {
        mFloatingActionButton.setVisibility(View.VISIBLE);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void hideRunBuildFloatActionButton() {
        mFloatingActionButton.setVisibility(View.GONE);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void addLoadMore() {
        BuildListAdapter baseAdapter = getAdapter().getBaseAdapter();
        baseAdapter.addLoadMore();
        baseAdapter.notifyDataSetChanged();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void removeLoadMore() {
        BuildListAdapter baseAdapter = getAdapter().getBaseAdapter();
        baseAdapter.removeLoadMore();
        baseAdapter.notifyDataSetChanged();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void showRetryLoadMoreSnackBar() {
        Snackbar snackBar = Snackbar.make(
                getRecyclerView(),
                R.string.load_more_retry_snack_bar_text,
                Snackbar.LENGTH_LONG)
                .setAction(R.string.download_artifact_retry_snack_bar_retry_button, v -> mOnBuildListPresenterListener.onLoadMore());
        snackBar.show();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void addMoreBuilds(BuildListDataModel dataModel) {
        BuildListAdapter baseAdapter = getAdapter().getBaseAdapter();
        baseAdapter.addMoreBuilds(dataModel);
        reInitSections(mDataModel);
        setSections(mSections);
        getRecyclerView().getAdapter().notifyDataSetChanged();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void showData(BuildListDataModel dataModel) {
        this.mDataModel = dataModel;
        mSections = initSections(dataModel);
        BuildListAdapter baseAdapter = getAdapter().getBaseAdapter();
        baseAdapter.setDataModel(dataModel);
        baseAdapter.setOnBuildListPresenterListener(mOnBuildListPresenterListener);
        initSectionAdapter();
        Mugen.with(getRecyclerView(), mOnBuildListPresenterListener).start();
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
                getRecyclerView(),
                R.string.text_build_is_run,
                Snackbar.LENGTH_LONG)
                .setAction(R.string.text_show_build, v -> mOnBuildListPresenterListener.onShowQueuedBuildSnackBarClick());
        snackBar.show();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void showBuildFilterAppliedSnackBar() {
        mFiltersAppliedSnackBar = Snackbar.make(
                getRecyclerView(),
                R.string.text_filters_applied,
                Snackbar.LENGTH_INDEFINITE)
                .setAction(R.string.text_snackbar_button_reset_filters, v -> mOnBuildListPresenterListener.onResetFiltersSnackBarActionClick());
        mFiltersAppliedSnackBar.show();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void showOpeningBuildErrorSnackBar() {
        Snackbar snackBar = Snackbar.make(
                getRecyclerView(),
                R.string.error_opening_build,
                Snackbar.LENGTH_LONG)
                .setAction(R.string.download_artifact_retry_snack_bar_retry_button, v -> mOnBuildListPresenterListener.onShowQueuedBuildSnackBarClick());
        snackBar.show();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void showAddToFavoritesSnackBar() {
        Snackbar snackBar = Snackbar.make(
                getRecyclerView(),
                R.string.text_add_to_favorites,
                Snackbar.LENGTH_LONG)
                .setAction(R.string.text_view_favorites, v -> mOnBuildListPresenterListener.onNavigateToFavorites());
        snackBar.show();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void showRemoveFavoritesSnackBar() {
        Snackbar snackBar = Snackbar.make(
                getRecyclerView(),
                R.string.text_remove_from_favorites,
                Snackbar.LENGTH_LONG);
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
    public void showFilterBuildsPrompt(@NonNull final OnboardingManager.OnPromptShownListener listener) {
        int color = getToolbarColor();
        new MaterialTapTargetPrompt.Builder(getActivity())
                .setTarget(R.id.filter_builds)
                .setPrimaryText(R.string.title_onboarding_filter_builds)
                .setSecondaryText(R.string.text_onboarding_filter_builds)
                .setAnimationInterpolator(new FastOutSlowInInterpolator())
                .setIcon(R.drawable.ic_filter_list_white_24px)
                .setIconDrawableTintList(ColorStateList.valueOf(color))
                .setBackgroundColour(color)
                .setCaptureTouchEventOutsidePrompt(true)
                .setPromptStateChangeListener((prompt, state) -> {
                    if (state == MaterialTapTargetPrompt.STATE_DISMISSED) {
                        listener.onPromptShown();
                    }
                })
                .show();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void showRunBuildPrompt(@NonNull final OnboardingManager.OnPromptShownListener listener) {
        int color = getToolbarColor();
        new MaterialTapTargetPrompt.Builder(getActivity())
                .setTarget(mFloatingActionButton)
                .setPrimaryText(R.string.title_onboarding_run_build)
                .setSecondaryText(R.string.text_onboarding_run_build)
                .setAnimationInterpolator(new FastOutSlowInInterpolator())
                .setBackgroundColour(color)
                .setCaptureTouchEventOutsidePrompt(true)
                .setPromptFocal(new RectanglePromptFocal().setCornerRadius(
                        getActivity().getResources().getDimension(R.dimen.default_prompt_fab_radius),
                        getActivity().getResources().getDimension(R.dimen.default_prompt_fab_radius)))
                .setPromptStateChangeListener((prompt, state) -> {
                    if (state == MaterialTapTargetPrompt.STATE_DISMISSED) {
                        listener.onPromptShown();
                    }
                })
                .show();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void showFavPrompt(@NonNull final OnboardingManager.OnPromptShownListener listener) {
        int color = getToolbarColor();
        new MaterialTapTargetPrompt.Builder(getActivity())
                .setTarget(R.id.add_to_favorites)
                .setPrimaryText(R.string.title_onboarding_add_fav)
                .setSecondaryText(R.string.text_onboarding_add_fav)
                .setAnimationInterpolator(new FastOutSlowInInterpolator())
                .setIcon(R.drawable.ic_favorite_border_wgite_24dp)
                .setIconDrawableTintList(ColorStateList.valueOf(color))
                .setBackgroundColour(color)
                .setCaptureTouchEventOutsidePrompt(true)
                .setPromptStateChangeListener((prompt, state) -> {
                    if (state == MaterialTapTargetPrompt.STATE_DISMISSED) {
                        listener.onPromptShown();
                    }
                })
                .show();
    }

    /**
     * @return color of toolbar
     */
    @ColorInt
    private int getToolbarColor() {
        return ((ColorDrawable) getActivity().findViewById(R.id.toolbar).getBackground()).getColor();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void createFavOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_not_fav_builds_activity, menu);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void createNotFavOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_fav_builds_activity, menu);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        getActivity().invalidateOptionsMenu();
        switch (item.getItemId()) {
            case R.id.filter_builds:
                mOnBuildListPresenterListener.onFilterBuildsOptionMenuClick();
                return true;
            case R.id.add_to_favorites:
                mOnBuildListPresenterListener.onAddToFavoritesOptionMenuClick();
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
        return getActivity() instanceof BuildListActivity;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void hideFiltersAppliedSnackBar() {
        if (mFiltersAppliedSnackBar != null && mFiltersAppliedSnackBar.isShown()) {
            mFiltersAppliedSnackBar.dismiss();
        }
    }

    /**
     * Init sectionAdapter
     *
     * @param
     */
    private void initSectionAdapter() {
        setSections(mSections);
        getRecyclerView().setAdapter(getAdapter());
        getRecyclerView().getAdapter().notifyDataSetChanged();
    }

    /**
     * Set sections for mSectionAdapter
     *
     * @param sections
     */
    private void setSections(List<SimpleSectionedRecyclerViewAdapter.Section> sections) {
        SimpleSectionedRecyclerViewAdapter.Section[] userStates = new SimpleSectionedRecyclerViewAdapter.Section[sections.size()];
        getAdapter().setSections(sections.toArray(userStates));
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
