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

package com.github.vase4kin.teamcityapp.buildlist.view

import android.app.Activity
import android.content.res.ColorStateList
import android.graphics.drawable.ColorDrawable
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import androidx.annotation.ColorInt
import androidx.annotation.StringRes
import androidx.appcompat.app.AppCompatActivity
import androidx.interpolator.view.animation.FastOutSlowInInterpolator
import butterknife.BindString
import butterknife.BindView
import com.afollestad.materialdialogs.MaterialDialog
import com.github.vase4kin.teamcityapp.R
import com.github.vase4kin.teamcityapp.base.list.view.BaseListView
import com.github.vase4kin.teamcityapp.base.list.view.BaseListViewImpl
import com.github.vase4kin.teamcityapp.base.list.view.SimpleSectionedRecyclerViewAdapter
import com.github.vase4kin.teamcityapp.buildlist.data.BuildListDataModel
import com.github.vase4kin.teamcityapp.buildlist.data.OnBuildListPresenterListener
import com.google.android.material.elevation.ElevationOverlayProvider
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton
import com.google.android.material.snackbar.Snackbar
import com.mugen.Mugen
import teamcityapp.libraries.onboarding.OnboardingManager
import teamcityapp.libraries.utils.getThemeColor
import uk.co.samuelwall.materialtaptargetprompt.MaterialTapTargetPrompt
import uk.co.samuelwall.materialtaptargetprompt.extras.focals.RectanglePromptFocal
import java.util.ArrayList

/**
 * Impl of [BuildListView]
 */
open class BuildListViewImpl(
    mView: View,
    activity: Activity,
    @StringRes emptyMessage: Int,
    adapter: SimpleSectionedRecyclerViewAdapter<BuildListAdapter>
) : BaseListViewImpl<BuildListDataModel, SimpleSectionedRecyclerViewAdapter<BuildListAdapter>>(
    mView,
    activity,
    emptyMessage,
    adapter
), BuildListView {

    @BindString(R.string.text_queued_header)
    lateinit var queuedHeaderText: String
    @BindView(R.id.floating_action_button)
    lateinit var floatingActionButton: ExtendedFloatingActionButton
    private lateinit var progressDialog: MaterialDialog
    private lateinit var sections: ArrayList<SimpleSectionedRecyclerViewAdapter.Section>
    private lateinit var dataModel: BuildListDataModel
    private var filtersAppliedSnackBar: Snackbar? = null

    protected var listener: OnBuildListPresenterListener? = null

    /**
     * @return color of toolbar
     */
    private val toolbarColor: Int
        @ColorInt
        get() = (activity.findViewById<View>(R.id.toolbar).background as ColorDrawable).color

    /**
     * {@inheritDoc}
     */
    override val isBuildListOpen: Boolean
        get() = activity is BuildListActivity

    /**
     * {@inheritDoc}
     */
    override fun setOnBuildListPresenterListener(onBuildListPresenterListener: OnBuildListPresenterListener) {
        this.listener = onBuildListPresenterListener
    }

    /**
     * {@inheritDoc}
     */
    override fun initViews(listener: BaseListView.ViewListener) {
        super.initViews(listener)
        floatingActionButton.visibility = View.GONE
        floatingActionButton.setOnClickListener { this.listener?.onRunBuildFabClick() }
        progressDialog = MaterialDialog.Builder(activity)
            .content(R.string.text_opening_build)
            .progress(true, 0)
            .autoDismiss(false)
            .build()
        progressDialog.setCancelable(false)
        progressDialog.setCanceledOnTouchOutside(false)
    }

    /**
     * {@inheritDoc}
     */
    override fun setTitle(title: String) {
        val actionBar = (activity as AppCompatActivity).supportActionBar
        actionBar?.title = title
    }

    /**
     * {@inheritDoc}
     */
    override fun showRunBuildFloatActionButton() {
        floatingActionButton.visibility = View.VISIBLE
    }

    /**
     * {@inheritDoc}
     */
    override fun hideRunBuildFloatActionButton() {
        floatingActionButton.visibility = View.GONE
    }

    /**
     * {@inheritDoc}
     */
    override fun addLoadMore() {
        val baseAdapter = adapter.baseAdapter
        baseAdapter.addLoadMore()
        baseAdapter.notifyDataSetChanged()
    }

    /**
     * {@inheritDoc}
     */
    override fun removeLoadMore() {
        val baseAdapter = adapter.baseAdapter
        baseAdapter.removeLoadMore()
        baseAdapter.notifyDataSetChanged()
    }

    /**
     * {@inheritDoc}
     */
    override fun showRetryLoadMoreSnackBar() {
        val snackBar = Snackbar.make(
            recyclerView,
            R.string.load_more_retry_snack_bar_text,
            Snackbar.LENGTH_LONG
        )
            .setAction(R.string.download_artifact_retry_snack_bar_retry_button) { listener?.onLoadMore() }
            .setAnchorView(floatingActionButton)
        snackBar.show()
    }

    /**
     * {@inheritDoc}
     */
    override fun addMoreBuilds(dataModel: BuildListDataModel) {
        val baseAdapter = adapter.baseAdapter
        baseAdapter.addMoreBuilds(dataModel)
        reInitSections(this.dataModel)
        setAdapterSections(sections)
        recyclerView.adapter?.notifyDataSetChanged()
    }

    /**
     * {@inheritDoc}
     */
    override fun showData(dataModel: BuildListDataModel) {
        this.dataModel = dataModel
        sections = initSections(dataModel)
        val baseAdapter = adapter.baseAdapter
        baseAdapter.dataModel = dataModel
        baseAdapter.setOnBuildListPresenterListener(listener ?: OnBuildListPresenterListener.EMPTY)
        initSectionAdapter()
        Mugen.with(recyclerView, listener).start()
    }

    /**
     * {@inheritDoc}
     */
    override fun recyclerViewId(): Int {
        return R.id.build_recycler_view
    }

    /**
     * {@inheritDoc}
     */
    override fun showBuildQueuedSuccessSnackBar() {
        val snackBar = Snackbar.make(
            recyclerView,
            R.string.text_build_is_run,
            Snackbar.LENGTH_LONG
        )
            .setAction(R.string.text_show_build) { listener?.onShowQueuedBuildSnackBarClick() }
            .setAnchorView(floatingActionButton)
        snackBar.show()
    }

    /**
     * {@inheritDoc}
     */
    override fun showBuildFilterAppliedSnackBar() {
        filtersAppliedSnackBar = Snackbar.make(
            recyclerView,
            R.string.text_filters_applied,
            Snackbar.LENGTH_INDEFINITE
        )
            .setAction(R.string.text_snackbar_button_reset_filters) { listener?.onResetFiltersSnackBarActionClick() }
            .setAnchorView(floatingActionButton)
        filtersAppliedSnackBar?.show()
    }

    /**
     * {@inheritDoc}
     */
    override fun showOpeningBuildErrorSnackBar() {
        val snackBar = Snackbar.make(
            recyclerView,
            R.string.error_opening_build,
            Snackbar.LENGTH_LONG
        )
            .setAction(R.string.download_artifact_retry_snack_bar_retry_button) { listener?.onShowQueuedBuildSnackBarClick() }
            .setAnchorView(floatingActionButton)
        snackBar.show()
    }

    /**
     * {@inheritDoc}
     */
    override fun showAddToFavoritesSnackBar() {
        val snackBar = Snackbar.make(
            recyclerView,
            R.string.text_add_to_favorites,
            Snackbar.LENGTH_LONG
        )
            .setAction(R.string.text_view_favorites) { listener?.onNavigateToFavorites() }
            .setAnchorView(floatingActionButton)
        snackBar.show()
    }

    /**
     * {@inheritDoc}
     */
    override fun showRemoveFavoritesSnackBar() {
        val snackBar = Snackbar.make(
            recyclerView,
            R.string.text_remove_from_favorites,
            Snackbar.LENGTH_LONG
        )
            .setAnchorView(floatingActionButton)
        snackBar.show()
    }

    /**
     * {@inheritDoc}
     */
    override fun showBuildLoadingProgress() {
        progressDialog.show()
    }

    /**
     * {@inheritDoc}
     */
    override fun hideBuildLoadingProgress() {
        progressDialog.dismiss()
    }

    /**
     * {@inheritDoc}
     */
    override fun showFilterBuildsPrompt(listener: OnboardingManager.OnPromptShownListener) {
        val color = getBackgroundColor()
        MaterialTapTargetPrompt.Builder(activity)
            .setTarget(R.id.filter_builds)
            .setPrimaryText(R.string.title_onboarding_filter_builds)
            .setSecondaryText(R.string.text_onboarding_filter_builds)
            .setAnimationInterpolator(FastOutSlowInInterpolator())
            .setIcon(R.drawable.ic_filter_list_white_24px)
            .setIconDrawableTintList(ColorStateList.valueOf(color))
            .setBackgroundColour(color)
            .setCaptureTouchEventOutsidePrompt(true)
            .setPromptStateChangeListener { _, state ->
                if (state == MaterialTapTargetPrompt.STATE_DISMISSED) {
                    listener.onPromptShown()
                }
            }
            .show()
    }

    /**
     * {@inheritDoc}
     */
    override fun showRunBuildPrompt(listener: OnboardingManager.OnPromptShownListener) {
        val color = getBackgroundColor()
        MaterialTapTargetPrompt.Builder(activity)
            .setTarget(floatingActionButton)
            .setPrimaryText(R.string.title_onboarding_run_build)
            .setSecondaryText(R.string.text_onboarding_run_build)
            .setAnimationInterpolator(FastOutSlowInInterpolator())
            .setBackgroundColour(color)
            .setCaptureTouchEventOutsidePrompt(true)
            .setPromptFocal(
                RectanglePromptFocal().setCornerRadius(
                    activity.resources.getDimension(R.dimen.dp_38),
                    activity.resources.getDimension(R.dimen.dp_38)
                )
            )
            .setPromptStateChangeListener { _, state ->
                if (state == MaterialTapTargetPrompt.STATE_DISMISSED) {
                    listener.onPromptShown()
                }
            }
            .show()
    }

    /**
     * {@inheritDoc}
     */
    override fun showFavPrompt(listener: OnboardingManager.OnPromptShownListener) {
        val color = getBackgroundColor()
        MaterialTapTargetPrompt.Builder(activity)
            .setTarget(R.id.add_to_favorites)
            .setPrimaryText(R.string.title_onboarding_add_fav)
            .setSecondaryText(R.string.text_onboarding_add_fav)
            .setAnimationInterpolator(FastOutSlowInInterpolator())
            .setIcon(R.drawable.ic_favorite_border_wgite_24dp)
            .setIconDrawableTintList(ColorStateList.valueOf(color))
            .setBackgroundColour(color)
            .setCaptureTouchEventOutsidePrompt(true)
            .setPromptStateChangeListener { _, state ->
                if (state == MaterialTapTargetPrompt.STATE_DISMISSED) {
                    listener.onPromptShown()
                }
            }
            .show()
    }

    /**
     * {@inheritDoc}
     */
    override fun createFavOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.menu_not_fav_builds_activity, menu)
    }

    /**
     * {@inheritDoc}
     */
    override fun createNotFavOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.menu_fav_builds_activity, menu)
    }

    /**
     * {@inheritDoc}
     */
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        activity.invalidateOptionsMenu()
        return when (item.itemId) {
            R.id.filter_builds -> {
                listener?.onFilterBuildsOptionMenuClick()
                true
            }
            R.id.add_to_favorites -> {
                listener?.onAddToFavoritesOptionMenuClick()
                true
            }
            else -> false
        }
    }

    /**
     * {@inheritDoc}
     */
    override fun hideFiltersAppliedSnackBar() {
        val filtersAppliedSnackBar = this.filtersAppliedSnackBar
        if (filtersAppliedSnackBar != null && filtersAppliedSnackBar.isShown) {
            filtersAppliedSnackBar.dismiss()
        }
    }

    private fun getBackgroundColor(): Int {
        val elevation =
            activity.resources.getDimension(R.dimen.dp_4)
        return ElevationOverlayProvider(activity).compositeOverlayIfNeeded(
            activity.getThemeColor(R.attr.colorPrimarySurface), elevation
        )
    }

    /**
     * Init sectionAdapter
     *
     * @param
     */
    private fun initSectionAdapter() {
        setAdapterSections(sections)
        recyclerView.adapter = adapter
        recyclerView.adapter?.notifyDataSetChanged()
    }

    /**
     * Set sections for mSectionAdapter
     *
     * @param sections
     */
    private fun setAdapterSections(sections: ArrayList<SimpleSectionedRecyclerViewAdapter.Section>) {
        val userStates = arrayOfNulls<SimpleSectionedRecyclerViewAdapter.Section>(sections.size)
        adapter.setSections(sections.toArray(userStates))
    }

    /**
     * Return sections for build data
     *
     * @param dataModel
     * @return List<SimpleSectionedRecyclerViewAdapter.Section>
    </SimpleSectionedRecyclerViewAdapter.Section> */
    private fun initSections(dataModel: BuildListDataModel): ArrayList<SimpleSectionedRecyclerViewAdapter.Section> {
        val sections = ArrayList<SimpleSectionedRecyclerViewAdapter.Section>()

        if (dataModel.itemCount != 0) {
            for (i in 0 until dataModel.itemCount) {
                val sectionTitle = if (dataModel.isQueued(i))
                    queuedHeaderText
                else
                    dataModel.getStartDate(i)
                if (sections.size != 0) {
                    val prevSection = sections[sections.size - 1]
                    if (prevSection.title != sectionTitle) {
                        sections.add(SimpleSectionedRecyclerViewAdapter.Section(i, sectionTitle))
                    }
                } else {
                    sections.add(SimpleSectionedRecyclerViewAdapter.Section(i, sectionTitle))
                }
            }
        }
        return sections
    }

    /**
     * ReInit sections if more builds are loaded
     */
    private fun reInitSections(dataModel: BuildListDataModel) {
        sections.clear()
        sections.addAll(initSections(dataModel))
    }
}
