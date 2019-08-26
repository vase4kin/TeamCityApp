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

package com.github.vase4kin.teamcityapp.navigation.view;

import android.app.Activity;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.StringRes;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import com.github.vase4kin.teamcityapp.R;
import com.github.vase4kin.teamcityapp.base.list.view.BaseListViewImpl;
import com.github.vase4kin.teamcityapp.navigation.data.NavigationDataModel;

/**
 * Impl of {@link NavigationView}
 */
public class NavigationViewImpl extends BaseListViewImpl<NavigationDataModel, NavigationAdapter> implements NavigationView {

    private OnNavigationItemClickListener mOnNavigationItemClickListener;

    public NavigationViewImpl(View view,
                              Activity activity,
                              @StringRes int emptyMessage,
                              NavigationAdapter adapter) {
        super(view, activity, emptyMessage, adapter);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setNavigationAdapterClickListener(OnNavigationItemClickListener clickListener) {
        mOnNavigationItemClickListener = clickListener;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setTitle(@NonNull String title) {
        ActionBar actionBar = ((AppCompatActivity) getActivity()).getSupportActionBar();
        if (actionBar != null) {
            actionBar.setTitle(title);
        }
    }

    @Override
    public void hideTheRateApp() {
        getAdapter().removeRateTheApp();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void showData(@NonNull NavigationDataModel dataModel) {
        getAdapter().setDataModel(dataModel);
        getAdapter().setOnClickListener(mOnNavigationItemClickListener);
        getRecyclerView().setAdapter(getAdapter());
        getRecyclerView().getAdapter().notifyDataSetChanged();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected int recyclerViewId() {
        return R.id.navigation_recycler_view;
    }
}
