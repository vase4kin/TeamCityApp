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

package com.github.vase4kin.teamcityapp.overview.view

import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.annotation.StringRes
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.ActionMenuView
import androidx.appcompat.widget.Toolbar
import androidx.interpolator.view.animation.FastOutSlowInInterpolator
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import butterknife.BindView
import butterknife.ButterKnife
import butterknife.Unbinder
import com.github.vase4kin.teamcityapp.R
import com.github.vase4kin.teamcityapp.bottomsheet_dialog.menu_items.MenuItemsFactory
import com.github.vase4kin.teamcityapp.bottomsheet_dialog.view.BottomSheetDialogFragment
import com.github.vase4kin.teamcityapp.navigation.api.BuildElement
import com.github.vase4kin.teamcityapp.onboarding.OnboardingManager
import com.github.vase4kin.teamcityapp.overview.data.OverviewDataModelImpl
import io.supercharge.shimmerlayout.ShimmerLayout
import tr.xip.errorview.ErrorView
import uk.co.samuelwall.materialtaptargetprompt.MaterialTapTargetPrompt
import java.util.ArrayList

private const val TIMEOUT_PROMPT = 500
private const val ICON_TIME = "{mdi-clock}"
private const val ICON_BRANCH = "{mdi-git}"
private const val ICON_AGENT = "{md-directions-railway}"
private const val ICON_TRIGGER_BY = "{md-account-circle}"
private const val ICON_BUILD_TYPE = "{md-crop-din}"
private const val ICON_PROJECT = "{md-filter-none}"
private const val TAG_BOTTOM_SHEET = "BottomSheet Dialog"

/**
 * View to manage [OverviewFragment]
 */
class OverviewViewImpl(
    private val view: View,
    private val activity: AppCompatActivity,
    private val adapter: OverviewAdapter
) : OverviewView {

    @BindView(R.id.swiperefresh)
    lateinit var swipeRefreshLayout: SwipeRefreshLayout
    @BindView(R.id.my_recycler_view)
    lateinit var recyclerView: RecyclerView
    @BindView(R.id.error_view)
    lateinit var errorView: ErrorView
    @BindView(R.id.skeleton_view)
    lateinit var skeletonView: ViewGroup

    lateinit var unbinder: Unbinder

    private var listener: OverviewView.ViewListener? = null

    private val elements = ArrayList<BuildElement>()

    /**
     * {@inheritDoc}
     */
    override fun initViews(listener: OverviewView.ViewListener) {
        this.listener = listener
        unbinder = ButterKnife.bind(this, view)
        errorView.setImageTint(Color.LTGRAY)
        errorView.setRetryListener(listener)
        swipeRefreshLayout.setOnRefreshListener(listener)
        recyclerView.layoutManager = LinearLayoutManager(activity)
        // For testing purposes
        recyclerView.id = R.id.overview_recycler_view
        skeletonView.removeAllViewsInLayout()
        LayoutInflater.from(activity).inflate(R.layout.layout_skeleton_overview_list, skeletonView)
    }

    /**
     * {@inheritDoc}
     */
    override fun showCards() {
        adapter.dataModel = OverviewDataModelImpl(elements, activity.applicationContext)
        adapter.viewListener = listener
        recyclerView.adapter = adapter
        adapter.notifyDataSetChanged()
    }

    /**
     * {@inheritDoc}
     */
    override fun hideCards() {
        elements.clear()
        adapter.dataModel = OverviewDataModelImpl(elements, activity.applicationContext)
        adapter.notifyDataSetChanged()
    }

    /**
     * {@inheritDoc}
     */
    override fun showSkeletonView() {
        skeletonView.visibility = View.VISIBLE
        (skeletonView.getChildAt(0) as ShimmerLayout).startShimmerAnimation()
    }

    /**
     * {@inheritDoc}
     */
    override fun hideSkeletonView() {
        skeletonView.visibility = View.GONE
        (skeletonView.getChildAt(0) as ShimmerLayout).startShimmerAnimation()
    }

    /**
     * {@inheritDoc}
     */
    override fun showRefreshingProgress() {
        swipeRefreshLayout.isRefreshing = true
    }

    /**
     * {@inheritDoc}
     */
    override fun hideRefreshingProgress() {
        swipeRefreshLayout.isRefreshing = false
    }

    /**
     * {@inheritDoc}
     */
    override fun showErrorView() {
        errorView.visibility = View.VISIBLE
        errorView.setSubtitle(R.string.error_view_error_text)
    }

    /**
     * {@inheritDoc}
     */
    override fun hideErrorView() {
        errorView.visibility = View.GONE
    }

    /**
     * {@inheritDoc}
     */
    override fun unbindViews() {
        unbinder.unbind()
    }

    /**
     * {@inheritDoc}
     */
    override fun addWaitReasonStatusCard(icon: String, waitReason: String) {
        addCard(R.string.build_wait_reason_section_text, icon, waitReason)
    }

    /**
     * {@inheritDoc}
     */
    override fun addResultStatusCard(icon: String, result: String) {
        addCard(R.string.build_result_section_text, icon, result)
    }

    /**
     * {@inheritDoc}
     */
    override fun addCancelledByCard(icon: String, userName: String) {
        addCard(R.string.build_canceled_by_text, icon, userName)
    }

    /**
     * {@inheritDoc}
     */
    override fun addCancellationTimeCard(time: String) {
        addCard(R.string.build_cancellation_time_text, ICON_TIME, time)
    }

    /**
     * {@inheritDoc}
     */
    override fun addTimeCard(time: String) {
        addCard(R.string.build_time_section_text, ICON_TIME, time)
    }

    /**
     * {@inheritDoc}
     */
    override fun addQueuedTimeCard(time: String) {
        addCard(R.string.build_queued_time_section_text, ICON_TIME, time)
    }

    /**
     * {@inheritDoc}
     */
    override fun addEstimatedTimeToStartCard(time: String) {
        addCard(R.string.build_time_to_start_section_text, ICON_TIME, time)
    }

    /**
     * {@inheritDoc}
     */
    override fun addBranchCard(branchName: String) {
        addCard(R.string.build_branch_section_text, ICON_BRANCH, branchName)
    }

    /**
     * {@inheritDoc}
     */
    override fun addAgentCard(agentName: String) {
        addCard(R.string.build_agent_section_text, ICON_AGENT, agentName)
    }

    /**
     * {@inheritDoc}
     */
    override fun addTriggeredByCard(triggeredBy: String) {
        addCard(R.string.build_triggered_by_section_text, ICON_TRIGGER_BY, triggeredBy)
    }

    /**
     * {@inheritDoc}
     */
    override fun addRestartedByCard(restartedBy: String) {
        addCard(R.string.build_restarted_by_section_text, ICON_TRIGGER_BY, restartedBy)
    }

    /**
     * {@inheritDoc}
     */
    override fun addTriggeredByUnknownTriggerTypeCard() {
        val unknownTrigger = activity.getString(R.string.unknown_trigger_type_text)
        addTriggeredByCard(unknownTrigger)
    }

    /**
     * {@inheritDoc}
     */
    override fun addBuildTypeNameCard(buildTypeName: String) {
        addCard(R.string.build_type_by_section_text, ICON_BUILD_TYPE, buildTypeName)
    }

    /**
     * {@inheritDoc}
     */
    override fun addBuildTypeProjectNameCard(buildTypeProjectName: String) {
        addCard(R.string.build_project_by_section_text, ICON_PROJECT, buildTypeProjectName)
    }

    /**
     * {@inheritDoc}
     */
    override fun addPersonalCard(userName: String) {
        addCard(R.string.build_personal_text, ICON_TRIGGER_BY, userName)
    }

    /**
     * Add card
     *
     * @param header - Header
     * @param icon   - Icon
     * @param text   - Text
     */
    private fun addCard(@StringRes header: Int, icon: String, text: String) {
        elements.add(BuildElement(icon, text, activity.getString(header)))
    }

    /**
     * {@inheritDoc}
     */
    override fun createStopBuildOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.menu_stop_build_tabs_activity, menu)
    }

    /**
     * {@inheritDoc}
     */
    override fun createRemoveBuildFromQueueOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.menu_remove_from_queue_build_tabs_activity, menu)
    }

    /**
     * {@inheritDoc}
     */
    override fun createDefaultOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.menu_share_build_tabs_activity, menu)
    }

    /**
     * {@inheritDoc}
     */
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        activity.invalidateOptionsMenu()
        when (item.itemId) {
            R.id.cancel_build -> {
                listener?.onCancelBuildContextMenuClick()
                return true
            }
            R.id.share_build -> {
                listener?.onShareButtonClick()
                return true
            }
            R.id.restart_build -> {
                listener?.onRestartBuildButtonClick()
                return true
            }
            R.id.open_in_a_browser -> {
                listener?.onOpenBrowser()
                return true
            }
            else -> return false
        }
    }

    /**
     * {@inheritDoc}
     */
    override fun showDefaultCardBottomSheetDialog(header: String, description: String) {
        val bottomSheetDialogFragment = BottomSheetDialogFragment.createBottomSheetDialog(
            header,
            description,
            MenuItemsFactory.TYPE_DEFAULT
        )
        bottomSheetDialogFragment.show(activity.supportFragmentManager, TAG_BOTTOM_SHEET)
    }

    /**
     * {@inheritDoc}
     */
    override fun showBranchCardBottomSheetDialog(description: String) {
        val bottomSheetDialogFragment = BottomSheetDialogFragment.createBottomSheetDialog(
            activity.getString(R.string.build_branch_section_text),
            description,
            MenuItemsFactory.TYPE_BRANCH
        )
        bottomSheetDialogFragment.show(activity.supportFragmentManager, TAG_BOTTOM_SHEET)
    }

    /**
     * {@inheritDoc}
     */
    override fun showBuildTypeCardBottomSheetDialog(description: String) {
        val bottomSheetDialogFragment = BottomSheetDialogFragment.createBottomSheetDialog(
            activity.getString(R.string.build_type_by_section_text),
            description,
            MenuItemsFactory.TYPE_BUILD_TYPE
        )
        bottomSheetDialogFragment.show(activity.supportFragmentManager, TAG_BOTTOM_SHEET)
    }

    /**
     * {@inheritDoc}
     */
    override fun showProjectCardBottomSheetDialog(description: String) {
        val bottomSheetDialogFragment = BottomSheetDialogFragment.createBottomSheetDialog(
            activity.getString(R.string.build_project_by_section_text),
            description,
            MenuItemsFactory.TYPE_PROJECT
        )
        bottomSheetDialogFragment.show(activity.supportFragmentManager, TAG_BOTTOM_SHEET)
    }

    /**
     * {@inheritDoc}
     */
    override fun showStopBuildPrompt(listener: OnboardingManager.OnPromptShownListener) {
        showPrompt(R.string.text_onboarding_stop_build, listener)
    }

    /**
     * {@inheritDoc}
     */
    override fun showRestartBuildPrompt(listener: OnboardingManager.OnPromptShownListener) {
        showPrompt(R.string.text_onboarding_restart_build, listener)
    }

    /**
     * {@inheritDoc}
     */
    override fun showRemoveBuildFromQueuePrompt(listener: OnboardingManager.OnPromptShownListener) {
        showPrompt(R.string.text_onboarding_remove_build_from_queue, listener)
    }

    /**
     * Show prompt
     *
     * @param secondaryText - secondary text
     * @param listener      - listener to receive callback when prompt is shown
     */
    private fun showPrompt(
        @StringRes secondaryText: Int,
        listener: OnboardingManager.OnPromptShownListener
    ) {
        // Creating prompt
        val toolbar = activity.findViewById<Toolbar>(R.id.toolbar)
        val color = (toolbar.background as ColorDrawable).color
        val promptBuilder = MaterialTapTargetPrompt.Builder(activity)
            .setPrimaryText(R.string.title_onboarding_build_menu)
            .setSecondaryText(secondaryText)
            .setAnimationInterpolator(FastOutSlowInInterpolator())
            .setIcon(R.drawable.ic_more_vert_black_24dp)
            .setIconDrawableTintList(ColorStateList.valueOf(color))
            .setBackgroundColour(color)
            .setCaptureTouchEventOutsidePrompt(true)
            .setPromptStateChangeListener { _, _ -> listener.onPromptShown() }
        // Show prompt
        Handler(Looper.getMainLooper()).postDelayed({
            val child = toolbar.getChildAt(3)
            if (child is ActionMenuView) {
                promptBuilder.setTarget(child.getChildAt(child.childCount - 1))
            }
            promptBuilder.show()
        }, TIMEOUT_PROMPT.toLong())
    }
}
