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

package com.github.vase4kin.teamcityapp.changes.view

import android.app.Activity
import android.view.View
import androidx.annotation.StringRes
import androidx.appcompat.app.AppCompatActivity

import com.github.vase4kin.teamcityapp.R
import com.github.vase4kin.teamcityapp.base.list.view.BaseListViewImpl
import com.github.vase4kin.teamcityapp.changes.api.Changes
import com.github.vase4kin.teamcityapp.changes.data.ChangesDataModel
import com.google.android.material.snackbar.Snackbar
import com.mugen.Mugen
import com.mugen.MugenCallbacks
import teamcityapp.features.change.view.ChangeActivity

/**
 * Impl of [ChangesView]
 */
class ChangesViewImpl(
    view: View,
    activity: Activity,
    @StringRes emptyMessage: Int,
    adapter: ChangesAdapter
) : BaseListViewImpl<ChangesDataModel, ChangesAdapter>(view, activity, emptyMessage, adapter),
    ChangesView {

    private var loadMoreCallbacks: MugenCallbacks? = null

    /**
     * {@inheritDoc}
     */
    override fun setLoadMoreListener(loadMoreCallbacks: MugenCallbacks) {
        this.loadMoreCallbacks = loadMoreCallbacks
    }

    /**
     * {@inheritDoc}
     */
    override fun showData(dataModel: ChangesDataModel) {
        Mugen.with(recyclerView, loadMoreCallbacks).start()
        adapter.onChangeClickListener = this
        adapter.dataModel = dataModel
        recyclerView.adapter = adapter
        adapter.notifyDataSetChanged()
    }

    /**
     * {@inheritDoc}
     */
    override fun addLoadMore() {
        adapter.addLoadMore()
        adapter.notifyDataSetChanged()
    }

    /**
     * {@inheritDoc}
     */
    override fun removeLoadMore() {
        adapter.removeLoadMore()
        adapter.notifyDataSetChanged()
    }

    /**
     * {@inheritDoc}
     */
    override fun addMoreBuilds(dataModel: ChangesDataModel) {
        adapter.addMoreBuilds(dataModel)
        adapter.notifyDataSetChanged()
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
    override fun onClick(change: Changes.Change) {
        ChangeActivity.start(
            activity = activity as AppCompatActivity,
            commitName = change.comment,
            userName = change.username,
            date = change.date,
            changeFileNames = change.files.file.map {
                Pair<String, String>(
                    first = it.file,
                    second = it.changeType
                )
            },
            version = change.version,
            webUrl = change.webUrl,
            changeId = change.getId()
        )
    }

    /**
     * {@inheritDoc}
     */
    override fun replaceSkeletonViewContent() {
        replaceSkeletonViewContent(R.layout.layout_skeleton_changes_list)
    }

    /**
     * {@inheritDoc}
     */
    override fun recyclerViewId(): Int {
        return R.id.changes_recycler_view
    }
}
