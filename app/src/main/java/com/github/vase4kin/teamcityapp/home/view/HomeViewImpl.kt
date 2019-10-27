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

package com.github.vase4kin.teamcityapp.home.view

import android.content.res.ColorStateList
import android.os.Handler
import android.os.Looper
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.interpolator.view.animation.FastOutSlowInInterpolator

import com.github.vase4kin.teamcityapp.R
import com.github.vase4kin.teamcityapp.drawer.view.DrawerViewImpl
import com.github.vase4kin.teamcityapp.drawer.view.OnDrawerPresenterListener
import com.github.vase4kin.teamcityapp.filter_bottom_sheet_dialog.filter.Filter
import com.github.vase4kin.teamcityapp.filter_bottom_sheet_dialog.view.FilterBottomSheetDialogFragment
import com.github.vase4kin.teamcityapp.new_drawer.view.DrawerBottomSheetDialogFragment
import com.github.vase4kin.teamcityapp.onboarding.OnboardingManager
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.snackbar.Snackbar

import uk.co.samuelwall.materialtaptargetprompt.MaterialTapTargetPrompt

private const val TAG_BOTTOM_SHEET = "Tag filter bottom sheet"
private const val TAG_DRAWER_BOTTOM_SHEET = "Tag drawer bottom sheet"
private const val TIME_PROMPT_DELAY = 500

/**
 * impl of [HomeView]
 */
class HomeViewImpl(
    activity: AppCompatActivity,
    drawerSelection: Int,
    isBackArrowEnabled: Boolean
) : DrawerViewImpl(activity, drawerSelection, isBackArrowEnabled), HomeView {

    private lateinit var snackBarAnchor: View
    private lateinit var fab: FloatingActionButton
    private var snackbar: Snackbar? = null
    private var listener: HomeView.ViewListener? = null

    override fun initViews(listener: OnDrawerPresenterListener) {
        super.initViews(listener)
        snackBarAnchor = activity.findViewById(R.id.snackbar_anchor)
        fab = activity.findViewById(R.id.home_floating_action_button)
    }

    override fun setListener(listener: HomeView.ViewListener?) {
        this.listener = listener
    }

    /**
     * {@inheritDoc}
     */
    override fun setDrawerSelection(selection: Int) {
        drawerResult.setSelection(selection.toLong(), false)
    }

    /**
     * {@inheritDoc}
     */
    override fun showFavoritesInfoSnackbar() {
        this.snackbar = Snackbar.make(
            snackBarAnchor,
            R.string.text_info_add,
            Snackbar.LENGTH_LONG
        )
            .setAction(R.string.text_info_add_action) {
                listener?.onFavoritesSnackBarActionClicked()
            }
        snackbar?.show()
    }

    /**
     * {@inheritDoc}
     */
    override fun dimissSnackbar() {
        val snackbar = this.snackbar
        if (snackbar != null && snackbar.isShown) {
            snackbar.dismiss()
        }
    }

    /**
     * {@inheritDoc}
     */
    override fun showFilterBottomSheet(filter: Filter) {
        FilterBottomSheetDialogFragment.createBottomSheetDialog(filter.code)
            .show(activity.supportFragmentManager, TAG_BOTTOM_SHEET)
    }

    /**
     * {@inheritDoc}
     */
    override fun showFilterAppliedSnackBar() {
        this.snackbar = Snackbar.make(
            snackBarAnchor,
            R.string.text_filters_applied,
            Snackbar.LENGTH_LONG
        )
        snackbar?.show()
    }

    /**
     * {@inheritDoc}
     */
    override fun showAgentsFilterAppliedSnackBar() {
        this.snackbar = Snackbar.make(
            snackBarAnchor,
            R.string.text_agents_filters_applied,
            Snackbar.LENGTH_LONG
        )
        snackbar?.show()
    }

    /**
     * {@inheritDoc}
     */
    override fun showNavigationDrawerPrompt(listener: OnboardingManager.OnPromptShownListener) {
        // Creating prompt
        val color = ContextCompat.getColor(activity, R.color.primary)
        val navigationDrawerPrompt = MaterialTapTargetPrompt.Builder(activity)
            .setPrimaryText(R.string.title_onboarding_navigation_drawer)
            .setSecondaryText(R.string.text_onboarding_navigation_drawer)
            .setAnimationInterpolator(FastOutSlowInInterpolator())
            .setIcon(R.drawable.ic_menu_black_24dp)
            .setIconDrawableTintList(ColorStateList.valueOf(color))
            .setBackgroundColour(color)
            .setCaptureTouchEventOutsidePrompt(true)
            .setPromptStateChangeListener { prompt, state -> listener.onPromptShown() }
        // Show prompt
        Handler(Looper.getMainLooper()).postDelayed({
            navigationDrawerPrompt.setTarget(toolbar.getChildAt(1))
            navigationDrawerPrompt.show()
        }, TIME_PROMPT_DELAY.toLong())
    }

    /**
     * {@inheritDoc}
     */
    override fun showAddFavPrompt(listener: OnboardingManager.OnPromptShownListener) {
        val color = ContextCompat.getColor(activity, R.color.primary)
        MaterialTapTargetPrompt.Builder(activity)
            .setTarget(fab)
            .setPrimaryText(R.string.title_onboarding_add_fav)
            .setSecondaryText(R.string.text_onboarding_add_fav)
            .setAnimationInterpolator(FastOutSlowInInterpolator())
            .setBackgroundColour(color)
            .setCaptureTouchEventOutsidePrompt(true)
            .setPromptStateChangeListener { _, state ->
                if (state == MaterialTapTargetPrompt.STATE_DISMISSED) {
                    listener.onPromptShown()
                }
            }.show()
    }

    /**
     * {@inheritDoc}
     */
    override fun showRunningBuildsFilterPrompt(listener: OnboardingManager.OnPromptShownListener) {
        val color = ContextCompat.getColor(activity, R.color.primary)
        MaterialTapTargetPrompt.Builder(activity)
            .setTarget(fab)
            .setPrimaryText(R.string.title_onboarding_filter)
            .setSecondaryText(R.string.text_onboarding_filter)
            .setAnimationInterpolator(FastOutSlowInInterpolator())
            .setBackgroundColour(color)
            .setCaptureTouchEventOutsidePrompt(true)
            .setPromptStateChangeListener { _, state ->
                if (state == MaterialTapTargetPrompt.STATE_DISMISSED) {
                    listener.onPromptShown()
                }
            }.show()
    }

    /**
     * {@inheritDoc}
     */
    override fun showBuildsQueueFilterPrompt(onPromptShown: () -> Unit) {
        val color = ContextCompat.getColor(activity, R.color.primary)
        MaterialTapTargetPrompt.Builder(activity)
            .setTarget(fab)
            .setPrimaryText(R.string.title_onboarding_filter_queued)
            .setSecondaryText(R.string.text_onboarding_filter_queued)
            .setAnimationInterpolator(FastOutSlowInInterpolator())
            .setBackgroundColour(color)
            .setCaptureTouchEventOutsidePrompt(true)
            .setPromptStateChangeListener { _, state ->
                if (state == MaterialTapTargetPrompt.STATE_DISMISSED) {
                    onPromptShown()
                }
            }.show()
    }

    /**
     * {@inheritDoc}
     */
    override fun showAgentsFilterPrompt(onPromptShown: () -> Unit) {
        val color = ContextCompat.getColor(activity, R.color.primary)
        MaterialTapTargetPrompt.Builder(activity)
            .setTarget(fab)
            .setPrimaryText(R.string.title_onboarding_filter_agents)
            .setSecondaryText(R.string.text_onboarding_filter_agents)
            .setAnimationInterpolator(FastOutSlowInInterpolator())
            .setBackgroundColour(color)
            .setCaptureTouchEventOutsidePrompt(true)
            .setPromptStateChangeListener { _, state ->
                if (state == MaterialTapTargetPrompt.STATE_DISMISSED) {
                    onPromptShown()
                }
            }.show()
    }

    /**
     * {@inheritDoc}
     */
    override fun showDrawer() {
        DrawerBottomSheetDialogFragment.createInstance()
            .show(activity.supportFragmentManager, TAG_DRAWER_BOTTOM_SHEET)
    }
}
