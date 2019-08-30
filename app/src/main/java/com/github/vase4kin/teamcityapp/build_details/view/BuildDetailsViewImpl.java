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

package com.github.vase4kin.teamcityapp.build_details.view;

import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;

import androidx.annotation.ColorRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.StringRes;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import com.afollestad.materialdialogs.MaterialDialog;
import com.github.vase4kin.teamcityapp.R;
import com.github.vase4kin.teamcityapp.artifact.view.ArtifactListFragment;
import com.github.vase4kin.teamcityapp.base.list.extractor.BaseValueExtractor;
import com.github.vase4kin.teamcityapp.base.tabs.view.BaseTabsViewModelImpl;
import com.github.vase4kin.teamcityapp.base.tabs.view.FragmentAdapter;
import com.github.vase4kin.teamcityapp.buildlog.view.BuildLogFragment;
import com.github.vase4kin.teamcityapp.changes.view.ChangesFragment;
import com.github.vase4kin.teamcityapp.overview.data.BuildDetails;
import com.github.vase4kin.teamcityapp.overview.view.OverviewFragment;
import com.github.vase4kin.teamcityapp.properties.view.PropertiesFragment;
import com.github.vase4kin.teamcityapp.snapshot_dependencies.view.SnapshotDependenciesFragment;
import com.github.vase4kin.teamcityapp.tests.view.TestOccurrencesFragment;
import com.github.vase4kin.teamcityapp.utils.StatusBarUtils;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.tabs.TabLayout;
import com.joanzapata.iconify.IconDrawable;
import com.joanzapata.iconify.fonts.MaterialIcons;

import butterknife.BindView;

import static com.github.vase4kin.teamcityapp.build_details.view.BuildDetailsViewTimeout.TIMEOUT_TEXT_COPIED_SNACKBAR;

/**
 * Impl of {@link BuildDetailsView}
 */
public class BuildDetailsViewImpl extends BaseTabsViewModelImpl implements BuildDetailsView {

    private static final String TAB_TITLE = "tabTitle";

    @BindView(R.id.floating_action_button)
    FloatingActionButton floatingActionButton;
    @BindView(R.id.container)
    View container;

    private BuildDetails buildDetails;

    private String overviewTabTitle;
    private String tabTitle;

    private StatusBarUtils statusBarUtils;
    private OnBuildDetailsViewListener onBuildDetailsViewListener;
    private MaterialDialog stoppingBuildProgressDialog;
    private MaterialDialog removingBuildFromQueueProgressDialog;
    private MaterialDialog restartingBuildProgressDialog;
    private MaterialDialog openingBuildProgressDialog;
    private MaterialDialog youAreAboutToStopBuildDialog;
    private MaterialDialog youAreAboutToRestartBuildDialog;
    private MaterialDialog youAreAboutToStopNotYoursBuildDialog;
    private MaterialDialog youAreAboutToRemoveBuildFromQueueDialog;
    private MaterialDialog youAreAboutToRemoveBuildFromQueueTriggeredByNotyouDialog;

    public BuildDetailsViewImpl(View mView,
                                AppCompatActivity mActivity,
                                StatusBarUtils statusBarUtils,
                                BaseValueExtractor valueExtractor) {
        super(mView, mActivity);
        this.statusBarUtils = statusBarUtils;
        this.buildDetails = valueExtractor.getBuildDetails();
    }

    /**
     * {@inheritDoc}
     * <p>
     * TODO: Move logic to presenter
     */
    @Override
    public void addFragments(FragmentAdapter fragmentAdapter) {
        fragmentAdapter.add(R.string.tab_overview, OverviewFragment.Companion.newInstance(buildDetails.toBuild()));
        fragmentAdapter.add(R.string.tab_changes, ChangesFragment.Companion.newInstance(buildDetails.getChangesHref()));
        if (buildDetails.hasTests()) {
            fragmentAdapter.add(R.string.tab_tests, TestOccurrencesFragment.Companion.newInstance(
                    buildDetails.getTestsHref(),
                    buildDetails.getPassedTestCount(),
                    buildDetails.getFailedTestCount(),
                    buildDetails.getIgnoredTestCount()));
        }
        if (!buildDetails.isQueued()) {
            fragmentAdapter.add(R.string.tab_build_log, BuildLogFragment.Companion.newInstance(buildDetails.getId()));
        }
        fragmentAdapter.add(R.string.tab_parameters, PropertiesFragment.Companion.newInstance(buildDetails.toBuild()));
        if (!buildDetails.isQueued() && !buildDetails.isRunning()) {
            fragmentAdapter.add(R.string.tab_artifacts, ArtifactListFragment.Companion.newInstance(buildDetails.toBuild(), buildDetails.getArtifactsHref()));
        }
        if (buildDetails.hasSnapshotDependencies()) {
            fragmentAdapter.add(R.string.tab_snapshot_dependencies, SnapshotDependenciesFragment.Companion.newInstance(buildDetails.getId()));
        }
    }

    /**
     * {@inheritDoc}
     * <p>
     * TODO: Move logic to presenter
     */
    @Override
    public void initViews() {
        super.initViews();
        floatingActionButton.setImageDrawable(new IconDrawable(getActivity(), MaterialIcons.md_directions_run).color(Color.WHITE));
        int offScreenPageLimit = getViewPager().getAdapter().getCount();
        getViewPager().setOffscreenPageLimit(offScreenPageLimit);
        overviewTabTitle = getActivity().getString(R.string.tab_overview);
        getTabLayout().setOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                getViewPager().setCurrentItem(tab.getPosition());
                tabTitle = tab.getText().toString();
                if (tabTitle.equals(overviewTabTitle)) {
                    showRunBuildFloatActionButton();
                } else {
                    hideRunBuildFloatActionButton();
                }
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {
            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {
            }
        });
        setTitle();
        setColorsByBuildType();

        stoppingBuildProgressDialog = createProgressDialogWithContent(R.string.text_stopping_build);
        removingBuildFromQueueProgressDialog = createProgressDialogWithContent(R.string.text_removing_build_from_queue);
        restartingBuildProgressDialog = createProgressDialogWithContent(R.string.text_restarting_build);
        openingBuildProgressDialog = createProgressDialogWithContent(R.string.text_opening_build);
        youAreAboutToStopBuildDialog = createConfirmDialogWithReAddCheckbox(R.string.text_stop_the_build, R.string.text_stop_button);
        youAreAboutToStopNotYoursBuildDialog = createConfirmDialogWithReAddCheckbox(R.string.text_stop_the_build_2, R.string.text_stop_button);
        youAreAboutToRemoveBuildFromQueueDialog = createConfirmDialog(R.string.text_remove_build_from_queue, R.string.text_remove_from_queue_button);
        youAreAboutToRemoveBuildFromQueueTriggeredByNotyouDialog = createConfirmDialog(R.string.text_remove_build_from_queue_2, R.string.text_remove_from_queue_button);
        youAreAboutToRestartBuildDialog = createConfirmDialogBuilder(R.string.text_restart_the_build, R.string.text_restart_button)
                .onPositive((dialog, which) -> onBuildDetailsViewListener.onConfirmRestartBuild())
                .build();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setOnBuildTabsViewListener(@NonNull OnBuildDetailsViewListener onBuildDetailsViewListener) {
        this.onBuildDetailsViewListener = onBuildDetailsViewListener;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void showYouAreAboutToRestartBuildDialog() {
        youAreAboutToRestartBuildDialog.show();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void showYouAreAboutToStopBuildDialog() {
        youAreAboutToStopBuildDialog.show();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void showYouAreAboutToStopNotYoursBuildDialog() {
        youAreAboutToStopNotYoursBuildDialog.show();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void showYouAreAboutToRemoveBuildFromQueueDialog() {
        youAreAboutToRemoveBuildFromQueueDialog.show();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void showYouAreAboutToRemoveBuildFromQueueTriggeredNotByYouDialog() {
        youAreAboutToRemoveBuildFromQueueTriggeredByNotyouDialog.show();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void showRestartingBuildProgressDialog() {
        restartingBuildProgressDialog.show();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void hideRestartingBuildProgressDialog() {
        restartingBuildProgressDialog.dismiss();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void showStoppingBuildProgressDialog() {
        stoppingBuildProgressDialog.show();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void hideStoppingBuildProgressDialog() {
        stoppingBuildProgressDialog.dismiss();
    }

    @Override
    public void showRemovingBuildFromQueueProgressDialog() {
        removingBuildFromQueueProgressDialog.show();
    }

    @Override
    public void hideRemovingBuildFromQueueProgressDialog() {
        removingBuildFromQueueProgressDialog.dismiss();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void showBuildIsStoppedSnackBar() {
        showSnackBarWithText(R.string.text_build_is_stopped);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void showBuildIsStoppedErrorSnackBar() {
        showSnackBarWithText(R.string.error_base_stop_build_error);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void showForbiddenToStopBuildSnackBar() {
        showSnackBarWithText(R.string.error_stop_build_forbidden_error);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void showBuildIsRemovedFromQueueErrorSnackBar() {
        showSnackBarWithText(R.string.error_base_remove_build_from_queue_error);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void showBuildIsRemovedFromQueueSnackBar() {
        showSnackBarWithText(R.string.text_build_is_removed_from_queue);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void showBuildRestartSuccessSnackBar() {
        createSnackBarWithText(R.string.text_build_is_restarted)
                .setAction(R.string.text_show_build, view -> onBuildDetailsViewListener.onShowQueuedBuild()).show();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void showForbiddenToRemoveBuildFromQueueSnackBar() {
        showSnackBarWithText(R.string.error_remove_build_from_queue_forbidden_error);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void showBuildRestartErrorSnackBar() {
        showSnackBarWithText(R.string.error_base_restart_build_error);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void showForbiddenToRestartBuildSnackBar() {
        showSnackBarWithText(R.string.error_restart_build_forbidden_error);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void showBuildLoadingProgress() {
        openingBuildProgressDialog.show();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void hideBuildLoadingProgress() {
        openingBuildProgressDialog.dismiss();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void showOpeningBuildErrorSnackBar() {
        createSnackBarWithText(R.string.error_opening_build)
                .setAction(R.string.download_artifact_retry_snack_bar_retry_button, view -> onBuildDetailsViewListener.onShowQueuedBuild()).show();
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public void showTextCopiedSnackBar() {
        new Handler(getActivity().getMainLooper()).postDelayed(new Runnable() {
            @Override
            public void run() {
                showSnackBarWithText(R.string.build_element_copy_text);
            }
        }, TIMEOUT_TEXT_COPIED_SNACKBAR);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void showErrorDownloadingArtifactSnackBar() {
        showSnackBarWithText(R.string.download_artifact_retry_snack_bar_text);
    }

    /**
     * Create cancel/remove build from queue confirm dialog
     *
     * @param content      - Resource id content message
     * @param positiveText - Resource id positive dialog text
     * @return confirm dialog
     */
    private MaterialDialog createConfirmDialog(@StringRes int content, @StringRes int positiveText) {
        return createConfirmDialogBuilder(content, positiveText)
                .build();
    }

    /**
     * Create stopping build confirm dialog with re-add build to queue checkbox
     *
     * @param content      - Resource id content message
     * @param positiveText - Resource id positive dialog text
     * @return confirm dialog
     */
    private MaterialDialog createConfirmDialogWithReAddCheckbox(@StringRes int content, @StringRes int positiveText) {
        return createConfirmDialogBuilder(content, positiveText)
                .checkBoxPromptRes(R.string.text_re_add_build, false, null)
                .build();
    }

    /**
     * Create cancel build confirm dialog builder
     *
     * @param content      - Resource id content message
     * @param positiveText - Resource id positive dialog text
     * @return confirm dialog builder
     */
    private MaterialDialog.Builder createConfirmDialogBuilder(@StringRes int content, @StringRes int positiveText) {
        return new MaterialDialog.Builder(getActivity())
                .content(content)
                .positiveText(positiveText)
                .onPositive((dialog, which) -> onBuildDetailsViewListener.onConfirmCancelingBuild(dialog.isPromptCheckBoxChecked()))
                .negativeText(R.string.text_cancel_button);
    }

    /**
     * Create progress dialog with custom content message
     *
     * @param content - resource id message
     * @return progress dialog
     */
    private MaterialDialog createProgressDialogWithContent(@StringRes int content) {
        MaterialDialog progressDialog = new MaterialDialog.Builder(getActivity())
                .content(content)
                .progress(true, 0)
                .autoDismiss(false)
                .build();
        progressDialog.setCancelable(false);
        progressDialog.setCanceledOnTouchOutside(false);
        return progressDialog;
    }

    /**
     * Show snack bar with text message
     *
     * @param text - Text message resource id
     */
    private void showSnackBarWithText(@StringRes int text) {
        createSnackBarWithText(text).show();
    }

    /**
     * Create snack bar with text message
     *
     * @param text - Text message resource id
     */
    private Snackbar createSnackBarWithText(@StringRes int text) {
        return Snackbar.make(
                container,
                text,
                Snackbar.LENGTH_LONG);
    }

    /**
     * Setting proper color for different build types
     */
    private void setColorsByBuildType() {
        if (buildDetails.isQueued()) {
            statusBarUtils.changeStatusBarColor(getActivity(), R.color.queued_tool_bar_color);
            setToolBarAndTabLayoutColor(R.color.queued_tool_bar_color);
            getTabLayout().setTabTextColors(
                    getActivity().getResources().getColor(R.color.tab_queued_unselected_color),
                    getActivity().getResources().getColor(R.color.md_white_1000));
        } else if (buildDetails.isRunning()) {
            statusBarUtils.changeStatusBarColor(getActivity(), R.color.running_tool_bar_color);
            setToolBarAndTabLayoutColor(R.color.running_tool_bar_color);
            getTabLayout().setTabTextColors(
                    getActivity().getResources().getColor(R.color.tab_running_unselected_color),
                    getActivity().getResources().getColor(R.color.md_white_1000));
        } else if (buildDetails.isFailed()) {
            statusBarUtils.changeStatusBarColor(getActivity(), R.color.failed_tool_bar_color);
            setToolBarAndTabLayoutColor(R.color.failed_tool_bar_color);
            getTabLayout().setTabTextColors(
                    getActivity().getResources().getColor(R.color.tab_failed_unselected_color),
                    getActivity().getResources().getColor(R.color.md_white_1000));
        } else if (buildDetails.isSuccess()) {
            statusBarUtils.changeStatusBarColor(getActivity(), R.color.success_tool_bar_color);
            setToolBarAndTabLayoutColor(R.color.success_tool_bar_color);
            getTabLayout().setTabTextColors(
                    getActivity().getResources().getColor(R.color.tab_success_unselected_color),
                    getActivity().getResources().getColor(R.color.md_white_1000));
        } else {
            statusBarUtils.changeStatusBarColor(getActivity(), R.color.queued_tool_bar_color);
            setToolBarAndTabLayoutColor(R.color.queued_tool_bar_color);
            getTabLayout().setTabTextColors(
                    getActivity().getResources().getColor(R.color.tab_queued_unselected_color),
                    getActivity().getResources().getColor(R.color.md_white_1000));
        }
    }

    /**
     * Setting toolbar color
     *
     * @param color - Color to set
     */
    private void setToolBarAndTabLayoutColor(@ColorRes int color) {
        ActionBar actionBar = getActivity().getSupportActionBar();
        if (actionBar != null) {
            actionBar.setBackgroundDrawable(new ColorDrawable(getActivity().getResources().getColor(color)));
        }
        getTabLayout().setBackgroundColor(getActivity().getResources().getColor(color));
    }

    /**
     * Set toolbar title
     */
    private void setTitle() {
        ActionBar actionBar = getActivity().getSupportActionBar();
        if (actionBar != null) {
            actionBar.setTitle("#" + buildDetails.getNumber());
            actionBar.setSubtitle(buildDetails.getBuildTypeName());
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onSave(@Nullable Bundle bundle) {
        if (bundle != null) {
            bundle.putString(TAB_TITLE, tabTitle);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onRestore(@Nullable Bundle bundle) {
        if (bundle != null) {
            tabTitle = bundle.getString(TAB_TITLE);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void showRunBuildFloatActionButton() {
        // <!---------------------------------------!>
        // UNCOMMENT THIS LINE WHEN RUNNING BUILD FEATURE IS IMPLEMENTED
        // floatingActionButton.show();
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
        // floatingActionButton.hide();
        // UNCOMMENT THIS LINE WHEN RUNNING BUILD FEATURE IS IMPLEMENTED
        // <!---------------------------------------!>
    }
}
