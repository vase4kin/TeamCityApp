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

package com.github.vase4kin.teamcityapp.runningbuilds.view;

import android.app.Activity;
import android.view.View;

import androidx.annotation.StringRes;

import com.github.vase4kin.teamcityapp.R;
import com.github.vase4kin.teamcityapp.base.list.view.SimpleSectionedRecyclerViewAdapter;
import com.github.vase4kin.teamcityapp.buildlist.data.BuildListDataModel;
import com.github.vase4kin.teamcityapp.buildlist.view.BuildListActivity;
import com.github.vase4kin.teamcityapp.buildlist.view.BuildListAdapter;
import com.github.vase4kin.teamcityapp.buildlist.view.BuildListViewImpl;

import java.util.ArrayList;
import java.util.List;

/**
 * impl of {@link RunningBuildListView}
 */
public class RunningBuildsListViewImpl extends BuildListViewImpl implements RunningBuildListView {

    public RunningBuildsListViewImpl(View mView,
                                     Activity activity,
                                     @StringRes int emptyMessage,
                                     SimpleSectionedRecyclerViewAdapter<BuildListAdapter> adapter) {
        super(mView, activity, emptyMessage, adapter);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void showData(final BuildListDataModel dataModel) {

        BuildListAdapter baseAdapter = mAdapter.getBaseAdapter();
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
            mAdapter.setListener(new SimpleSectionedRecyclerViewAdapter.OnSectionClickListener() {
                @Override
                public void onSectionClick(int position) {
                    final String buildTypeName = dataModel.getBuildTypeName(position);
                    final String buildTypeId = dataModel.getBuildTypeId(position);
                    BuildListActivity.start(buildTypeName, buildTypeId, null, mActivity);
                }
            });
        }
        SimpleSectionedRecyclerViewAdapter.Section[] userStates = new SimpleSectionedRecyclerViewAdapter.Section[sections.size()];
        mAdapter.setSections(sections.toArray(userStates));

        mRecyclerView.setAdapter(mAdapter);
        mRecyclerView.getAdapter().notifyDataSetChanged();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void updateTitle(int count) {
        String title = String.format("%s (%s)",
                getTitle(),
                count);
        setTitle(title);
    }

    /**
     * @return default tool bar title
     */
    protected String getTitle() {
        return mActivity.getString(R.string.running_builds_drawer_item);
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
}
