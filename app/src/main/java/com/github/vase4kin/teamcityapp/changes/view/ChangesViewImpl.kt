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

package com.github.vase4kin.teamcityapp.changes.view

import android.app.Activity
import android.text.TextUtils
import android.view.View
import androidx.annotation.StringRes

import com.afollestad.materialdialogs.MaterialDialog
import com.github.vase4kin.teamcityapp.R
import com.github.vase4kin.teamcityapp.base.list.view.BaseListViewImpl
import com.github.vase4kin.teamcityapp.changes.api.Changes
import com.github.vase4kin.teamcityapp.changes.data.ChangesDataModel
import com.google.android.material.snackbar.Snackbar
import com.mugen.Mugen
import com.mugen.MugenCallbacks

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
        val content = change.username + " on " + change.date
        val builder = MaterialDialog.Builder(activity)
            .title(change.comment)
            .content(content)
            .positiveText(R.string.dialog_ok_title)
        if (change.files.files.isEmpty()) {
            builder.items(activity.getString(R.string.empty_list_files))
        } else {
            builder.items(change.files.files)
        }
        val dialog = builder.build()
        dialog.titleView.ellipsize = TextUtils.TruncateAt.END
        dialog.titleView.maxLines = 2

        dialog.show()
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
