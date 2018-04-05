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

package com.github.vase4kin.teamcityapp.build_details.view;

import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.ColorRes;
import android.support.annotation.NonNull;
import android.support.annotation.StringRes;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TabLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.TextView;

import com.afollestad.materialdialogs.DialogAction;
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
import com.github.vase4kin.teamcityapp.storage.api.UserAccount;
import com.github.vase4kin.teamcityapp.tests.view.TestOccurrencesFragment;
import com.github.vase4kin.teamcityapp.utils.StatusBarUtils;
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
    FloatingActionButton mFloatingActionButton;
    @BindView(R.id.container)
    View mContainer;

    private BuildDetails mBuildDetails;
    private final UserAccount userAccount;

    private String overviewTabTitle;
    private String artifactsTabTitle;
    private String mTabTitle;

    private StatusBarUtils mStatusBarUtils;
    private OnBuildDetailsViewListener mOnBuildDetailsViewListener;
    private MaterialDialog mStoppingBuildProgressDialog;
    private MaterialDialog mRemovingBuildFromQueueProgressDialog;
    private MaterialDialog mRestartingBuildProgressDialog;
    private MaterialDialog mOpeningBuildProgressDialog;
    private MaterialDialog mYouAreAboutToStopBuildDialog;
    private MaterialDialog mYouAreAboutToRestartBuildDialog;
    private MaterialDialog mYouAreAboutToStopNotYoursBuildDialog;
    private MaterialDialog mYouAreAboutToRemoveBuildFromQueueDialog;
    private MaterialDialog mYouAreAboutToRemoveBuildFromQueueTriggeredByNotyouDialog;

    public BuildDetailsViewImpl(View mView,
                                AppCompatActivity mActivity,
                                StatusBarUtils statusBarUtils,
                                BaseValueExtractor valueExtractor,
                                UserAccount userAccount) {
        super(mView, mActivity);
        this.mStatusBarUtils = statusBarUtils;
        this.mBuildDetails = valueExtractor.getBuildDetails();
        this.userAccount = userAccount;
    }

    /**
     * {@inheritDoc}
     *
     * TODO: Move logic to presenter
     */
    @Override
    public void addFragments(FragmentAdapter fragmentAdapter) {
        fragmentAdapter.add(R.string.tab_overview, OverviewFragment.newInstance(mBuildDetails.toBuild()));
        fragmentAdapter.add(R.string.tab_changes, ChangesFragment.newInstance(mBuildDetails.getChangesHref()));
        if (mBuildDetails.hasTests()) {
            fragmentAdapter.add(R.string.tab_tests, TestOccurrencesFragment.newInstance(
                    mBuildDetails.getTestsHref(),
                    mBuildDetails.getPassedTestCount(),
                    mBuildDetails.getFailedTestCount(),
                    mBuildDetails.getIgnoredTestCount()));
        }
        if (!mBuildDetails.isQueued() && !userAccount.isSslDisabled()) {
            fragmentAdapter.add(R.string.tab_build_log, BuildLogFragment.newInstance(mBuildDetails.getId()));
        }
        fragmentAdapter.add(R.string.tab_parameters, PropertiesFragment.newInstance(mBuildDetails.toBuild()));
        if (!mBuildDetails.isQueued() && !mBuildDetails.isRunning()) {
            fragmentAdapter.add(R.string.tab_artifacts, ArtifactListFragment.newInstance(mBuildDetails.toBuild(), mBuildDetails.getArtifactsHref()));
        }
    }

    /**
     * {@inheritDoc}
     *
     * TODO: Move logic to presenter
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
            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {
            }
        });
        setTitle();
        setColorsByBuildType();

        mStoppingBuildProgressDialog = createProgressDialogWithContent(R.string.text_stopping_build);
        mRemovingBuildFromQueueProgressDialog = createProgressDialogWithContent(R.string.text_removing_build_from_queue);
        mRestartingBuildProgressDialog = createProgressDialogWithContent(R.string.text_restarting_build);
        mOpeningBuildProgressDialog = createProgressDialogWithContent(R.string.text_opening_build);
        mYouAreAboutToStopBuildDialog = createConfirmDialogWithReAddCheckbox(R.string.text_stop_the_build, R.string.text_stop_button);
        mYouAreAboutToStopNotYoursBuildDialog = createConfirmDialogWithReAddCheckbox(R.string.text_stop_the_build_2, R.string.text_stop_button);
        mYouAreAboutToRemoveBuildFromQueueDialog = createConfirmDialog(R.string.text_remove_build_from_queue, R.string.text_remove_from_queue_button);
        mYouAreAboutToRemoveBuildFromQueueTriggeredByNotyouDialog = createConfirmDialog(R.string.text_remove_build_from_queue_2, R.string.text_remove_from_queue_button);
        mYouAreAboutToRestartBuildDialog = createConfirmDialogBuilder(R.string.text_restart_the_build, R.string.text_restart_button)
                .onPositive(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                        mOnBuildDetailsViewListener.onConfirmRestartBuild();
                    }
                })
                .build();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setOnBuildTabsViewListener(OnBuildDetailsViewListener onBuildDetailsViewListener) {
        this.mOnBuildDetailsViewListener = onBuildDetailsViewListener;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void showYouAreAboutToRestartBuildDialog() {
        mYouAreAboutToRestartBuildDialog.show();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void showYouAreAboutToStopBuildDialog() {
        mYouAreAboutToStopBuildDialog.show();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void showYouAreAboutToStopNotYoursBuildDialog() {
        mYouAreAboutToStopNotYoursBuildDialog.show();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void showYouAreAboutToRemoveBuildFromQueueDialog() {
        mYouAreAboutToRemoveBuildFromQueueDialog.show();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void showYouAreAboutToRemoveBuildFromQueueTriggeredNotByYouDialog() {
        mYouAreAboutToRemoveBuildFromQueueTriggeredByNotyouDialog.show();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void showRestartingBuildProgressDialog() {
        mRestartingBuildProgressDialog.show();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void hideRestartingBuildProgressDialog() {
        mRestartingBuildProgressDialog.dismiss();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void showStoppingBuildProgressDialog() {
        mStoppingBuildProgressDialog.show();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void hideStoppingBuildProgressDialog() {
        mStoppingBuildProgressDialog.dismiss();
    }

    @Override
    public void showRemovingBuildFromQueueProgressDialog() {
        mRemovingBuildFromQueueProgressDialog.show();
    }

    @Override
    public void hideRemovingBuildFromQueueProgressDialog() {
        mRemovingBuildFromQueueProgressDialog.dismiss();
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
                .setAction(R.string.text_show_build, new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        mOnBuildDetailsViewListener.onShowQueuedBuild();
                    }
                }).show();
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
        mOpeningBuildProgressDialog.show();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void hideBuildLoadingProgress() {
        mOpeningBuildProgressDialog.dismiss();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void showOpeningBuildErrorSnackBar() {
        createSnackBarWithText(R.string.error_opening_build)
                .setAction(R.string.download_artifact_retry_snack_bar_retry_button, new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        mOnBuildDetailsViewListener.onShowQueuedBuild();
                    }
                }).show();
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public void showTextCopiedSnackBar() {
        new Handler(mActivity.getMainLooper()).postDelayed(new Runnable() {
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
        return new MaterialDialog.Builder(mActivity)
                .content(content)
                .positiveText(positiveText)
                .onPositive(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                        mOnBuildDetailsViewListener.onConfirmCancelingBuild(dialog.isPromptCheckBoxChecked());
                    }
                })
                .negativeText(R.string.text_cancel_button);
    }

    /**
     * Create progress dialog with custom content message
     *
     * @param content - resource id message
     * @return progress dialog
     */
    private MaterialDialog createProgressDialogWithContent(@StringRes int content) {
        MaterialDialog progressDialog = new MaterialDialog.Builder(mActivity)
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
        Snackbar snackBar = Snackbar.make(
                mContainer,
                text,
                Snackbar.LENGTH_LONG);
        TextView textView = (TextView) snackBar.getView().findViewById(android.support.design.R.id.snackbar_text);
        textView.setTextColor(Color.WHITE);
        return snackBar;
    }

    /**
     * Setting proper color for different build types
     */
    private void setColorsByBuildType() {
        if (mBuildDetails.isQueued()) {
            mStatusBarUtils.changeStatusBarColor(mActivity, R.color.queued_status_bar_color);
            setToolBarAndTabLayoutColor(R.color.queued_tool_bar_color);
            mTabLayout.setTabTextColors(
                    mActivity.getResources().getColor(R.color.tab_queued_unselected_color),
                    mActivity.getResources().getColor(R.color.md_white_1000));
        } else if (mBuildDetails.isRunning()) {
            mStatusBarUtils.changeStatusBarColor(mActivity, R.color.running_status_bar_color);
            setToolBarAndTabLayoutColor(R.color.running_tool_bar_color);
            mTabLayout.setTabTextColors(
                    mActivity.getResources().getColor(R.color.tab_running_unselected_color),
                    mActivity.getResources().getColor(R.color.md_white_1000));
        } else if (mBuildDetails.isFailed()) {
            mStatusBarUtils.changeStatusBarColor(mActivity, R.color.failed_status_bar_color);
            setToolBarAndTabLayoutColor(R.color.failed_tool_bar_color);
            mTabLayout.setTabTextColors(
                    mActivity.getResources().getColor(R.color.tab_failed_unselected_color),
                    mActivity.getResources().getColor(R.color.md_white_1000));
        } else if (mBuildDetails.isSuccess()) {
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
        if (mBuildDetails.isQueued()) {
            startDate = mBuildDetails.getQueuedDate();
        } else {
            startDate = mBuildDetails.getStartDate();
        }
        String title = String.format("#%s (%s)", mBuildDetails.getNumber(), startDate);
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
