/*
 * Copyright 2020 Andrey Tolpeev
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

package com.github.vase4kin.teamcityapp.build_details.view

import android.os.Bundle
import android.os.Handler
import android.view.View
import androidx.annotation.StringRes
import androidx.appcompat.app.AppCompatActivity
import butterknife.BindView
import com.afollestad.materialdialogs.MaterialDialog
import com.github.vase4kin.teamcityapp.R
import com.github.vase4kin.teamcityapp.artifact.view.ArtifactListFragment
import com.github.vase4kin.teamcityapp.base.list.extractor.BaseValueExtractor
import com.github.vase4kin.teamcityapp.base.tabs.view.BaseTabsViewModelImpl
import com.github.vase4kin.teamcityapp.base.tabs.view.FragmentAdapter
import com.github.vase4kin.teamcityapp.build_details.view.BuildDetailsViewTimeout.Companion.TIMEOUT_TEXT_COPIED_SNACKBAR
import com.github.vase4kin.teamcityapp.buildlog.view.BuildLogFragment
import com.github.vase4kin.teamcityapp.changes.view.ChangesFragment
import com.github.vase4kin.teamcityapp.overview.data.BuildDetails
import com.github.vase4kin.teamcityapp.overview.view.OverviewFragment
import com.github.vase4kin.teamcityapp.snapshot_dependencies.view.SnapshotDependenciesFragment
import com.github.vase4kin.teamcityapp.tests.view.TestOccurrencesFragment
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.tabs.TabLayout
import teamcityapp.features.properties.feature.view.PropertiesFragment

private const val TAB_TITLE = "tabTitle"

/**
 * Impl of [BuildDetailsView]
 */
class BuildDetailsViewImpl(
    view: View,
    activity: AppCompatActivity,
    valueExtractor: BaseValueExtractor
) : BaseTabsViewModelImpl(view, activity), BuildDetailsView {

    @BindView(R.id.floating_action_button)
    lateinit var floatingActionButton: FloatingActionButton

    @BindView(R.id.container)
    lateinit var container: View

    private val buildDetails: BuildDetails = valueExtractor.buildDetails
    private var onBuildDetailsViewListener: OnBuildDetailsViewListener? = null

    private var tabTitle: String? = null
    private lateinit var overviewTabTitle: String
    private lateinit var stoppingBuildProgressDialog: MaterialDialog
    private lateinit var removingBuildFromQueueProgressDialog: MaterialDialog
    private lateinit var restartingBuildProgressDialog: MaterialDialog
    private lateinit var openingBuildProgressDialog: MaterialDialog
    private lateinit var youAreAboutToStopBuildDialog: MaterialDialog
    private lateinit var youAreAboutToRestartBuildDialog: MaterialDialog
    private lateinit var youAreAboutToStopNotYoursBuildDialog: MaterialDialog
    private lateinit var youAreAboutToRemoveBuildFromQueueDialog: MaterialDialog
    private lateinit var youAreAboutToRemoveBuildFromQueueTriggeredByNotyouDialog: MaterialDialog

    /**
     * {@inheritDoc}
     *
     *
     * TODO: Move logic to presenter
     */
    override fun addFragments(fragmentAdapter: FragmentAdapter) {
        fragmentAdapter.add(
            R.string.tab_overview,
            OverviewFragment.newInstance(buildDetails.toBuild())
        )
        val changesHref = buildDetails.changesHref
        if (changesHref != null) {
            fragmentAdapter.add(
                R.string.tab_changes,
                ChangesFragment.newInstance(changesHref)
            )
        }
        val testsHref = buildDetails.testsHref
        if (testsHref != null) {
            fragmentAdapter.add(
                R.string.tab_tests,
                TestOccurrencesFragment.newInstance(
                    testsHref,
                    buildDetails.passedTestCount,
                    buildDetails.failedTestCount,
                    buildDetails.ignoredTestCount
                )
            )
        }
        if (!buildDetails.isQueued) {
            fragmentAdapter.add(
                R.string.tab_build_log,
                BuildLogFragment.newInstance(buildDetails.id)
            )
        }
        fragmentAdapter.add(
            R.string.tab_parameters,
            PropertiesFragment.create(buildDetails.toBuild().properties?.properties ?: emptyList())
        )
        val artifactsHref = buildDetails.artifactsHref
        if (artifactsHref != null) {
            if (!buildDetails.isQueued && !buildDetails.isRunning) {
                fragmentAdapter.add(
                    R.string.tab_artifacts,
                    ArtifactListFragment.newInstance(buildDetails.toBuild(), artifactsHref)
                )
            }
        }
        if (buildDetails.hasSnapshotDependencies()) {
            fragmentAdapter.add(
                R.string.tab_snapshot_dependencies,
                SnapshotDependenciesFragment.newInstance(buildDetails.id)
            )
        }
    }

    /**
     * {@inheritDoc}
     *
     *
     * TODO: Move logic to presenter
     */
    override fun initViews() {
        super.initViews()
        floatingActionButton.setImageResource(R.drawable.ic_directions_run_white_24px)
        val offScreenPageLimit = viewPager.adapter?.count ?: 0
        viewPager.offscreenPageLimit = offScreenPageLimit
        overviewTabTitle = activity.getString(R.string.tab_overview)
        tabLayout.setOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab) {
                viewPager.currentItem = tab.position
                tabTitle = tab.text.toString()
                if (tabTitle == overviewTabTitle) {
                    showRunBuildFloatActionButton()
                } else {
                    hideRunBuildFloatActionButton()
                }
            }

            override fun onTabUnselected(tab: TabLayout.Tab) {}

            override fun onTabReselected(tab: TabLayout.Tab) {}
        })
        setTitle()

        stoppingBuildProgressDialog = createProgressDialogWithContent(R.string.text_stopping_build)
        removingBuildFromQueueProgressDialog =
            createProgressDialogWithContent(R.string.text_removing_build_from_queue)
        restartingBuildProgressDialog =
            createProgressDialogWithContent(R.string.text_restarting_build)
        openingBuildProgressDialog = createProgressDialogWithContent(R.string.text_opening_build)
        youAreAboutToStopBuildDialog = createConfirmDialogWithReAddCheckbox(
            R.string.text_stop_the_build,
            R.string.text_stop_button
        )
        youAreAboutToStopNotYoursBuildDialog = createConfirmDialogWithReAddCheckbox(
            R.string.text_stop_the_build_2,
            R.string.text_stop_button
        )
        youAreAboutToRemoveBuildFromQueueDialog = createConfirmDialog(
            R.string.text_remove_build_from_queue,
            R.string.text_remove_from_queue_button
        )
        youAreAboutToRemoveBuildFromQueueTriggeredByNotyouDialog = createConfirmDialog(
            R.string.text_remove_build_from_queue_2,
            R.string.text_remove_from_queue_button
        )
        youAreAboutToRestartBuildDialog = createConfirmDialogBuilder(
            R.string.text_restart_the_build,
            R.string.text_restart_button
        )
            .onPositive { _, _ -> onBuildDetailsViewListener?.onConfirmRestartBuild() }
            .build()
    }

    /**
     * {@inheritDoc}
     */
    override fun setOnBuildTabsViewListener(onBuildDetailsViewListener: OnBuildDetailsViewListener) {
        this.onBuildDetailsViewListener = onBuildDetailsViewListener
    }

    /**
     * {@inheritDoc}
     */
    override fun showYouAreAboutToRestartBuildDialog() {
        youAreAboutToRestartBuildDialog.show()
    }

    /**
     * {@inheritDoc}
     */
    override fun showYouAreAboutToStopBuildDialog() {
        youAreAboutToStopBuildDialog.show()
    }

    /**
     * {@inheritDoc}
     */
    override fun showYouAreAboutToStopNotYoursBuildDialog() {
        youAreAboutToStopNotYoursBuildDialog.show()
    }

    /**
     * {@inheritDoc}
     */
    override fun showYouAreAboutToRemoveBuildFromQueueDialog() {
        youAreAboutToRemoveBuildFromQueueDialog.show()
    }

    /**
     * {@inheritDoc}
     */
    override fun showYouAreAboutToRemoveBuildFromQueueTriggeredNotByYouDialog() {
        youAreAboutToRemoveBuildFromQueueTriggeredByNotyouDialog.show()
    }

    /**
     * {@inheritDoc}
     */
    override fun showRestartingBuildProgressDialog() {
        restartingBuildProgressDialog.show()
    }

    /**
     * {@inheritDoc}
     */
    override fun hideRestartingBuildProgressDialog() {
        restartingBuildProgressDialog.dismiss()
    }

    /**
     * {@inheritDoc}
     */
    override fun showStoppingBuildProgressDialog() {
        stoppingBuildProgressDialog.show()
    }

    /**
     * {@inheritDoc}
     */
    override fun hideStoppingBuildProgressDialog() {
        stoppingBuildProgressDialog.dismiss()
    }

    override fun showRemovingBuildFromQueueProgressDialog() {
        removingBuildFromQueueProgressDialog.show()
    }

    override fun hideRemovingBuildFromQueueProgressDialog() {
        removingBuildFromQueueProgressDialog.dismiss()
    }

    /**
     * {@inheritDoc}
     */
    override fun showBuildIsStoppedSnackBar() {
        showSnackBarWithText(R.string.text_build_is_stopped)
    }

    /**
     * {@inheritDoc}
     */
    override fun showBuildIsStoppedErrorSnackBar() {
        showSnackBarWithText(R.string.error_base_stop_build_error)
    }

    /**
     * {@inheritDoc}
     */
    override fun showForbiddenToStopBuildSnackBar() {
        showSnackBarWithText(R.string.error_stop_build_forbidden_error)
    }

    /**
     * {@inheritDoc}
     */
    override fun showBuildIsRemovedFromQueueErrorSnackBar() {
        showSnackBarWithText(R.string.error_base_remove_build_from_queue_error)
    }

    /**
     * {@inheritDoc}
     */
    override fun showBuildIsRemovedFromQueueSnackBar() {
        showSnackBarWithText(R.string.text_build_is_removed_from_queue)
    }

    /**
     * {@inheritDoc}
     */
    override fun showBuildRestartSuccessSnackBar() {
        createSnackBarWithText(R.string.text_build_is_restarted)
            .setAction(R.string.text_show_build) { onBuildDetailsViewListener?.onShowQueuedBuild() }
            .show()
    }

    /**
     * {@inheritDoc}
     */
    override fun showForbiddenToRemoveBuildFromQueueSnackBar() {
        showSnackBarWithText(R.string.error_remove_build_from_queue_forbidden_error)
    }

    /**
     * {@inheritDoc}
     */
    override fun showBuildRestartErrorSnackBar() {
        showSnackBarWithText(R.string.error_base_restart_build_error)
    }

    /**
     * {@inheritDoc}
     */
    override fun showForbiddenToRestartBuildSnackBar() {
        showSnackBarWithText(R.string.error_restart_build_forbidden_error)
    }

    /**
     * {@inheritDoc}
     */
    override fun showBuildLoadingProgress() {
        openingBuildProgressDialog.show()
    }

    /**
     * {@inheritDoc}
     */
    override fun hideBuildLoadingProgress() {
        openingBuildProgressDialog.dismiss()
    }

    /**
     * {@inheritDoc}
     */
    override fun showOpeningBuildErrorSnackBar() {
        createSnackBarWithText(R.string.error_opening_build)
            .setAction(R.string.download_artifact_retry_snack_bar_retry_button) { onBuildDetailsViewListener?.onShowQueuedBuild() }
            .show()
    }

    /**
     * {@inheritDoc}
     */
    override fun showTextCopiedSnackBar() {
        Handler(activity.mainLooper).postDelayed(
            { showSnackBarWithText(R.string.build_element_copy_text) },
            TIMEOUT_TEXT_COPIED_SNACKBAR.toLong()
        )
    }

    /**
     * {@inheritDoc}
     */
    override fun showErrorDownloadingArtifactSnackBar() {
        showSnackBarWithText(R.string.download_artifact_retry_snack_bar_text)
    }

    /**
     * Create cancel/remove build from queue confirm dialog
     *
     * @param content - Resource id content message
     * @param positiveText - Resource id positive dialog text
     * @return confirm dialog
     */
    private fun createConfirmDialog(
        @StringRes content: Int,
        @StringRes positiveText: Int
    ): MaterialDialog {
        return createConfirmDialogBuilder(content, positiveText)
            .build()
    }

    /**
     * Create stopping build confirm dialog with re-add build to queue checkbox
     *
     * @param content - Resource id content message
     * @param positiveText - Resource id positive dialog text
     * @return confirm dialog
     */
    private fun createConfirmDialogWithReAddCheckbox(
        @StringRes content: Int,
        @StringRes positiveText: Int
    ): MaterialDialog {
        return createConfirmDialogBuilder(content, positiveText)
            .checkBoxPromptRes(R.string.text_re_add_build, false, null)
            .build()
    }

    /**
     * Create cancel build confirm dialog builder
     *
     * @param content - Resource id content message
     * @param positiveText - Resource id positive dialog text
     * @return confirm dialog builder
     */
    private fun createConfirmDialogBuilder(
        @StringRes content: Int,
        @StringRes positiveText: Int
    ): MaterialDialog.Builder {
        return MaterialDialog.Builder(activity)
            .content(content)
            .positiveText(positiveText)
            .onPositive { dialog, _ ->
                onBuildDetailsViewListener?.onConfirmCancelingBuild(
                    dialog.isPromptCheckBoxChecked
                )
            }
            .negativeText(R.string.text_cancel_button)
    }

    /**
     * Create progress dialog with custom content message
     *
     * @param content - resource id message
     * @return progress dialog
     */
    private fun createProgressDialogWithContent(@StringRes content: Int): MaterialDialog {
        val progressDialog = MaterialDialog.Builder(activity)
            .content(content)
            .progress(true, 0)
            .autoDismiss(false)
            .build()
        progressDialog.setCancelable(false)
        progressDialog.setCanceledOnTouchOutside(false)
        return progressDialog
    }

    /**
     * Show snack bar with text message
     *
     * @param text - Text message resource id
     */
    private fun showSnackBarWithText(@StringRes text: Int) {
        createSnackBarWithText(text).show()
    }

    /**
     * Create snack bar with text message
     *
     * @param text - Text message resource id
     */
    private fun createSnackBarWithText(@StringRes text: Int): Snackbar {
        return Snackbar.make(
            container,
            text,
            Snackbar.LENGTH_LONG
        )
    }

    /**
     * Set toolbar title
     */
    private fun setTitle() {
        val actionBar = activity.supportActionBar ?: return
        actionBar.title = "#" + buildDetails.number
        actionBar.subtitle = buildDetails.buildTypeName
    }

    /**
     * {@inheritDoc}
     */
    override fun onSave(bundle: Bundle?) {
        bundle?.putString(TAB_TITLE, tabTitle)
    }

    /**
     * {@inheritDoc}
     */
    override fun onRestore(bundle: Bundle?) {
        bundle ?: return
        tabTitle = bundle.getString(TAB_TITLE, "")
    }

    /**
     * {@inheritDoc}
     */
    override fun showRunBuildFloatActionButton() {
        // <!---------------------------------------!>
        // UNCOMMENT THIS LINE WHEN RUNNING BUILD FEATURE IS IMPLEMENTED
        // floatingActionButton.show();
        // UNCOMMENT THIS LINE WHEN RUNNING BUILD FEATURE IS IMPLEMENTED
        // <!---------------------------------------!>
    }

    /**
     * {@inheritDoc}
     */
    override fun hideRunBuildFloatActionButton() {
        // <!---------------------------------------!>
        // UNCOMMENT THIS LINE WHEN RUNNING BUILD FEATURE IS IMPLEMENTED
        // floatingActionButton.hide();
        // UNCOMMENT THIS LINE WHEN RUNNING BUILD FEATURE IS IMPLEMENTED
        // <!---------------------------------------!>
    }

    /**
     * {@inheritDoc}
     */
    override fun unBindViews() {
        dismissAllDialogsOnDestroy()
        super.unBindViews()
    }

    /**
     * Dismiss all dialogs on destroy
     */
    private fun dismissAllDialogsOnDestroy() {
        if (::stoppingBuildProgressDialog.isInitialized && stoppingBuildProgressDialog.isShowing) {
            stoppingBuildProgressDialog.dismiss()
        }
        if (::removingBuildFromQueueProgressDialog.isInitialized && removingBuildFromQueueProgressDialog.isShowing) {
            removingBuildFromQueueProgressDialog.dismiss()
        }
        if (::restartingBuildProgressDialog.isInitialized && restartingBuildProgressDialog.isShowing) {
            restartingBuildProgressDialog.dismiss()
        }
        if (::openingBuildProgressDialog.isInitialized && openingBuildProgressDialog.isShowing) {
            openingBuildProgressDialog.dismiss()
        }
        if (::youAreAboutToStopBuildDialog.isInitialized && youAreAboutToStopBuildDialog.isShowing) {
            youAreAboutToStopBuildDialog.dismiss()
        }
        if (::youAreAboutToRestartBuildDialog.isInitialized && youAreAboutToRestartBuildDialog.isShowing) {
            youAreAboutToRestartBuildDialog.dismiss()
        }
        if (::youAreAboutToStopNotYoursBuildDialog.isInitialized && youAreAboutToStopNotYoursBuildDialog.isShowing) {
            youAreAboutToStopNotYoursBuildDialog.dismiss()
        }
        if (::youAreAboutToRemoveBuildFromQueueDialog.isInitialized && youAreAboutToRemoveBuildFromQueueDialog.isShowing) {
            youAreAboutToRemoveBuildFromQueueDialog.dismiss()
        }
        if (::youAreAboutToRemoveBuildFromQueueTriggeredByNotyouDialog.isInitialized && youAreAboutToRemoveBuildFromQueueTriggeredByNotyouDialog.isShowing) {
            youAreAboutToRemoveBuildFromQueueTriggeredByNotyouDialog.dismiss()
        }
    }
}
