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

package com.github.vase4kin.teamcityapp.changes.view;

import android.app.Activity;
import android.text.TextUtils;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.StringRes;

import com.afollestad.materialdialogs.MaterialDialog;
import com.github.vase4kin.teamcityapp.R;
import com.github.vase4kin.teamcityapp.base.list.view.BaseListViewImpl;
import com.github.vase4kin.teamcityapp.changes.api.Changes;
import com.github.vase4kin.teamcityapp.changes.data.ChangesDataModel;
import com.google.android.material.snackbar.Snackbar;
import com.mugen.Mugen;
import com.mugen.MugenCallbacks;

/**
 * Impl of {@link ChangesView}
 */
public class ChangesViewImpl extends BaseListViewImpl<ChangesDataModel, ChangesAdapter> implements ChangesView {

    private MugenCallbacks mLoadMoreCallbacks;

    public ChangesViewImpl(View view,
                           Activity activity,
                           @StringRes int emptyMessage,
                           ChangesAdapter adapter) {
        super(view, activity, emptyMessage, adapter);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setLoadMoreListener(@NonNull MugenCallbacks loadMoreCallbacks) {
        this.mLoadMoreCallbacks = loadMoreCallbacks;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void showData(@NonNull ChangesDataModel dataModel) {
        Mugen.with(getRecyclerView(), mLoadMoreCallbacks).start();
        getAdapter().setOnChangeClickListener(this);
        getAdapter().setDataModel(dataModel);
        getRecyclerView().setAdapter(getAdapter());
        getAdapter().notifyDataSetChanged();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void addLoadMore() {
        getAdapter().addLoadMore();
        getAdapter().notifyDataSetChanged();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void removeLoadMore() {
        getAdapter().removeLoadMore();
        getAdapter().notifyDataSetChanged();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void addMoreBuilds(@NonNull ChangesDataModel dataModel) {
        getAdapter().addMoreBuilds(dataModel);
        getAdapter().notifyDataSetChanged();
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
                .setAction(R.string.download_artifact_retry_snack_bar_retry_button, v -> mLoadMoreCallbacks.onLoadMore());
        snackBar.show();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onClick(@NonNull Changes.Change change) {
        String content = change.getUsername() + " on " + change.getDate();
        MaterialDialog.Builder builder = new MaterialDialog.Builder(getActivity())
                .title(change.getComment())
                .content(content)
                .positiveText(R.string.dialog_ok_title);
        if (change.getFiles().getFiles().isEmpty()) {
            builder.items(new String[]{getActivity().getString(R.string.empty_list_files)});
        } else {
            builder.items(change.getFiles().getFiles());
        }
        MaterialDialog dialog = builder.build();
        dialog.getTitleView().setEllipsize(TextUtils.TruncateAt.END);
        dialog.getTitleView().setMaxLines(2);

        dialog.show();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void replaceSkeletonViewContent() {
        replaceSkeletonViewContent(R.layout.layout_skeleton_changes_list);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected int recyclerViewId() {
        return R.id.changes_recycler_view;
    }
}
