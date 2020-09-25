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

package com.github.vase4kin.teamcityapp.tests.view

import android.app.Activity
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import androidx.annotation.StringRes

import com.github.vase4kin.teamcityapp.R
import com.github.vase4kin.teamcityapp.base.list.view.BaseListViewImpl
import com.github.vase4kin.teamcityapp.base.list.view.SimpleSectionedRecyclerViewAdapter
import com.github.vase4kin.teamcityapp.tests.data.TestsDataModel
import com.github.vase4kin.teamcityapp.tests.extractor.TestsValueExtractor
import com.google.android.material.snackbar.Snackbar
import com.mugen.Mugen
import com.mugen.MugenCallbacks

import java.util.ArrayList

private const val SUCCESS = "SUCCESS"
private const val FAILURE = "FAILURE"

/**
 * Impl of [TestsView]
 */
class TestsViewImpl(
    mView: View,
    activity: Activity,
    valueExtractor: TestsValueExtractor,
    @StringRes emptyMessage: Int,
    adapter: SimpleSectionedRecyclerViewAdapter<TestOccurrencesAdapter>
) : BaseListViewImpl<TestsDataModel, SimpleSectionedRecyclerViewAdapter<TestOccurrencesAdapter>>(
    mView,
    activity,
    emptyMessage,
    adapter
),
    TestsView {

    private var loadMoreCallbacks: MugenCallbacks? = null
    private var selectedId = R.id.show_failed
    private val sections = ArrayList<SimpleSectionedRecyclerViewAdapter.Section>()
    private lateinit var testsDataModel: TestsDataModel
    private var listener: OnTestsPresenterListener? = null

    private var passed: Int = 0
    private var failed: Int = 0
    private var ignored: Int = 0

    init {
        passed = valueExtractor.passedCount
        failed = valueExtractor.failedCount
        ignored = valueExtractor.ignoredCount
    }

    /**
     * {@inheritDoc}
     */
    override fun setListener(listener: OnTestsPresenterListener) {
        this.listener = listener
    }

    /**
     * {@inheritDoc}
     */
    override fun showData(dataModel: TestsDataModel) {
        testsDataModel = dataModel
        val baseAdapter = adapter.baseAdapter
        baseAdapter.dataModel = dataModel
        baseAdapter.onClickListener = listener
        initSections()
        initSectionAdapter()
        Mugen.with(recyclerView, loadMoreCallbacks).start()
    }

    /**
     * Init sectionAdapter
     */
    private fun initSectionAdapter() {
        setSections()
        recyclerView.adapter = adapter
        recyclerView.adapter?.notifyDataSetChanged()
    }

    /**
     * Set sections for mSectionAdapter
     */
    private fun setSections() {
        val userStates = arrayOfNulls<SimpleSectionedRecyclerViewAdapter.Section>(sections.size)
        adapter.setSections(sections.toArray(userStates))
    }

    /**
     * Return sections for build data
     */
    private fun initSections() {
        sections.clear()
        if (testsDataModel.itemCount != 0) {
            for (i in 0 until testsDataModel.itemCount) {
                val title = getFormattedTitle(testsDataModel.getStatus(i))
                if (sections.size != 0) {
                    val prevSection = sections[sections.size - 1]
                    if (prevSection.title != title) {
                        sections.add(SimpleSectionedRecyclerViewAdapter.Section(i, title))
                    }
                } else {
                    sections.add(SimpleSectionedRecyclerViewAdapter.Section(i, title))
                }
            }
        }
    }

    /**
     * Get formatted title
     *
     * @param status - Status of the test
     * @return Title
     */
    private fun getFormattedTitle(status: String): String {
        return when (status) {
            SUCCESS -> activity.getString(R.string.text_passed, passed)
            FAILURE -> activity.getString(R.string.text_failed, failed)
            else -> activity.getString(R.string.text_ignored, ignored)
        }
    }

    /**
     * {@inheritDoc}
     */
    override fun invalidateOptionsMenu() {
        activity.invalidateOptionsMenu()
    }

    /**
     * {@inheritDoc}
     */
    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.menu_test_occurences_fragment, menu)
    }

    /**
     * {@inheritDoc}
     */
    override fun onPrepareOptionsMenu(menu: Menu) {
        if (ignored == 0 && failed == 0 && passed == 0) {
            menu.clear()
        } else {
            menu.findItem(R.id.show_failed).isVisible = true
            menu.findItem(R.id.show_ignored).isVisible = true
            menu.findItem(R.id.show_passed).isVisible = true
            if (ignored == 0) {
                menu.findItem(R.id.show_ignored).isVisible = false
            }
            if (failed == 0 || selectedId == R.id.show_failed) {
                menu.findItem(R.id.show_failed).isVisible = false
            }
            if (passed == 0) {
                menu.findItem(R.id.show_passed).isVisible = false
            }
            if (selectedId != 0) {
                menu.findItem(selectedId).isVisible = false
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        activity.invalidateOptionsMenu()
        when (item.itemId) {
            R.id.show_ignored -> {
                listener!!.loadIgnoredTests()
                selectedId = R.id.show_ignored
                return true
            }
            R.id.show_failed -> {
                listener!!.loadFailedTests()
                selectedId = R.id.show_failed
                return true
            }
            R.id.show_passed -> {
                listener!!.loadSuccessTests()
                selectedId = R.id.show_passed
                return true
            }
            else -> return false
        }
    }

    /**
     * {@inheritDoc}
     */
    override fun setOnLoadMoreListener(loadMoreCallbacks: MugenCallbacks) {
        this.loadMoreCallbacks = loadMoreCallbacks
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
    override fun addMoreBuilds(dataModel: TestsDataModel) {
        val baseAdapter = adapter.baseAdapter
        baseAdapter.addMoreBuilds(dataModel)
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
            .setAction(R.string.download_artifact_retry_snack_bar_retry_button) { loadMoreCallbacks!!.onLoadMore() }
        snackBar.show()
    }

    /**
     * {@inheritDoc}
     */
    override fun replaceSkeletonViewContent() {
        replaceSkeletonViewContent(R.layout.layout_skeleton_agent_list)
    }

    /**
     * {@inheritDoc}
     */
    override fun recyclerViewId(): Int {
        return R.id.tests_recycler_view
    }
}
