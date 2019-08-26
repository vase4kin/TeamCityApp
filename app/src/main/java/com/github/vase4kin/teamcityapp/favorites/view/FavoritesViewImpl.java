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

package com.github.vase4kin.teamcityapp.favorites.view;

import android.app.Activity;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.StringRes;

import com.github.vase4kin.teamcityapp.R;
import com.github.vase4kin.teamcityapp.base.list.view.BaseListViewImpl;
import com.github.vase4kin.teamcityapp.base.list.view.SimpleSectionedRecyclerViewAdapter;
import com.github.vase4kin.teamcityapp.navigation.api.NavigationItem;
import com.github.vase4kin.teamcityapp.navigation.data.NavigationDataModel;
import com.github.vase4kin.teamcityapp.navigation.view.NavigationActivity;
import com.github.vase4kin.teamcityapp.navigation.view.NavigationAdapter;
import com.github.vase4kin.teamcityapp.navigation.view.OnNavigationItemClickListener;

import java.util.ArrayList;
import java.util.List;

public class FavoritesViewImpl extends BaseListViewImpl<NavigationDataModel, SimpleSectionedRecyclerViewAdapter<NavigationAdapter>> implements FavoritesView {

    private FavoritesView.ViewListener listener;

    public FavoritesViewImpl(View view,
                             Activity activity,
                             @StringRes int emptyMessage,
                             SimpleSectionedRecyclerViewAdapter<NavigationAdapter> adapter) {
        super(view, activity, emptyMessage, adapter);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setViewListener(@NonNull FavoritesView.ViewListener listener) {
        this.listener = listener;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void showData(@NonNull final NavigationDataModel dataModel) {
        NavigationAdapter baseAdapter = getAdapter().getBaseAdapter();
        baseAdapter.setDataModel(dataModel);
        baseAdapter.setOnClickListener(new OnNavigationItemClickListener() {
            @Override
            public void onClick(@NonNull NavigationItem navigationItem) {
                listener.onClick(navigationItem);
            }

            @Override
            public void onRateCancelButtonClick() {

            }

            @Override
            public void onRateNowButtonClick() {

            }
        });

        List<SimpleSectionedRecyclerViewAdapter.Section> sections =
                new ArrayList<>();

        if (dataModel.getItemCount() != 0) {
            for (int i = 0; i < dataModel.getItemCount(); i++) {
                final String projectName = dataModel.getProjectName(i);
                if (sections.size() != 0) {
                    SimpleSectionedRecyclerViewAdapter.Section prevSection = sections.get(sections.size() - 1);
                    if (!prevSection.getTitle().equals(projectName)) {
                        sections.add(new SimpleSectionedRecyclerViewAdapter.Section(i, projectName));
                    }
                } else {
                    sections.add(new SimpleSectionedRecyclerViewAdapter.Section(i, projectName));
                }
            }
            getAdapter().setListener(position -> {
                final String projectName = dataModel.getProjectName(position);
                final String projectId = dataModel.getProjectId(position);
                NavigationActivity.Companion.start(projectName, projectId, getActivity());
            });
        }
        SimpleSectionedRecyclerViewAdapter.Section[] userStates = new SimpleSectionedRecyclerViewAdapter.Section[sections.size()];
        getAdapter().setSections(sections.toArray(userStates));

        getRecyclerView().setAdapter(getAdapter());
        getRecyclerView().getAdapter().notifyDataSetChanged();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected int recyclerViewId() {
        return R.id.favorites_recycler_view;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected int emptyTitleId() {
        return R.id.favorites_empty_title_view;
    }
}
