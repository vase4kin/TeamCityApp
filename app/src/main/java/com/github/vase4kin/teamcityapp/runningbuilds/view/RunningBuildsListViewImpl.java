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

package com.github.vase4kin.teamcityapp.runningbuilds.view;

import android.app.Activity;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.StringRes;

import com.github.vase4kin.teamcityapp.R;
import com.github.vase4kin.teamcityapp.base.list.view.SimpleSectionedRecyclerViewAdapter;
import com.github.vase4kin.teamcityapp.buildlist.data.BuildListDataModel;
import com.github.vase4kin.teamcityapp.buildlist.view.BuildListActivity;
import com.github.vase4kin.teamcityapp.buildlist.view.BuildListAdapter;
import com.github.vase4kin.teamcityapp.buildlist.view.BuildListViewImpl;
import com.github.vase4kin.teamcityapp.filter_bottom_sheet_dialog.filter.Filter;
import com.github.vase4kin.teamcityapp.filter_bottom_sheet_dialog.filter.FilterProvider;

import java.util.ArrayList;
import java.util.List;

/**
 * impl of {@link RunningBuildListView}
 */
public class RunningBuildsListViewImpl extends BuildListViewImpl implements RunningBuildListView {

    protected final FilterProvider filterProvider;

    public RunningBuildsListViewImpl(View mView,
                                     Activity activity,
                                     @StringRes int emptyMessage,
                                     SimpleSectionedRecyclerViewAdapter<BuildListAdapter> adapter,
                                     FilterProvider filterProvider) {
        super(mView, activity, emptyMessage, adapter);
        this.filterProvider = filterProvider;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void showData(@NonNull final BuildListDataModel dataModel) {

        BuildListAdapter baseAdapter = getAdapter().getBaseAdapter();
        baseAdapter.setDataModel(dataModel);
        baseAdapter.setOnBuildListPresenterListener(mOnBuildListPresenterListener);

        List<SimpleSectionedRecyclerViewAdapter.Section> sections =
                new ArrayList<>();

        if (dataModel.getItemCount() != 0) {
            for (int i = 0; i < dataModel.getItemCount(); i++) {
                final String buildTypeTitle = dataModel.hasBuildTypeInfo(i)
                        ? dataModel.getBuildTypeFullName(i)
                        : dataModel.getBuildTypeId(i);
                if (sections.size() != 0) {
                    SimpleSectionedRecyclerViewAdapter.Section prevSection = sections.get(sections.size() - 1);
                    if (!prevSection.getTitle().equals(buildTypeTitle)) {
                        sections.add(new SimpleSectionedRecyclerViewAdapter.Section(i, buildTypeTitle));
                    }
                } else {
                    sections.add(new SimpleSectionedRecyclerViewAdapter.Section(i, buildTypeTitle));
                }
            }
            getAdapter().setListener(position -> {
                final String buildTypeName = dataModel.getBuildTypeName(position);
                final String buildTypeId = dataModel.getBuildTypeId(position);
                BuildListActivity.Companion.start(buildTypeName, buildTypeId, null, getActivity());
            });
        }
        SimpleSectionedRecyclerViewAdapter.Section[] userStates = new SimpleSectionedRecyclerViewAdapter.Section[sections.size()];
        getAdapter().setSections(sections.toArray(userStates));

        getRecyclerView().setAdapter(getAdapter());
        getRecyclerView().getAdapter().notifyDataSetChanged();
    }

    /**
     * @return default tool bar title
     */
    protected String getTitle() {
        return getActivity().getString(R.string.running_builds_drawer_item);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void showRunBuildFloatActionButton() {
        // Do not show running build float action button here
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void hideRunBuildFloatActionButton() {
        // Do not show running build float action button here
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected int getEmptyMessage() {
        if (filterProvider.getRunningBuildsFilter() == Filter.RUNNING_FAVORITES) {
            return R.string.empty_list_message_favorite_running_builds;
        } else {
            return R.string.empty_list_message_running_builds;
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected int emptyTitleId() {
        return R.id.running_empty_title_view;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected int recyclerViewId() {
        return R.id.running_builds_recycler_view;
    }
}
