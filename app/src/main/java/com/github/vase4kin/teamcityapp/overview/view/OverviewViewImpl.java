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

package com.github.vase4kin.teamcityapp.overview.view;

import android.app.Activity;
import android.support.annotation.StringRes;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;

import com.github.vase4kin.teamcityapp.R;
import com.github.vase4kin.teamcityapp.base.list.view.BaseListViewImpl;
import com.github.vase4kin.teamcityapp.overview.data.OverviewDataModel;

/**
 * View to manage {@link OverviewFragment}
 */
public class OverviewViewImpl extends BaseListViewImpl<OverviewDataModel, OverviewAdapter> implements OverviewView {

    private OverviewViewListener mListener;

    public OverviewViewImpl(View view,
                            Activity activity,
                            @StringRes int emptyMessage,
                            OverviewAdapter adapter) {
        super(view, activity, emptyMessage, adapter);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setOverViewListener(OverviewViewListener listener) {
        this.mListener = listener;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void showData(OverviewDataModel dataModel) {
        mAdapter.setDataModel(dataModel);
        mAdapter.setOnCopyActionClickListener(new OnCustomCopyActionClickListenerImpl(mActivity));
        mRecyclerView.setAdapter(mAdapter);
        mRecyclerView.getAdapter().notifyDataSetChanged();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected int recyclerViewId() {
        return R.id.overview_recycler_view;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void createStopBuildOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_stop_build_tabs_activity, menu);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void createRemoveBuildFromQueueOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_remove_from_queue_build_tabs_activity, menu);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void createDefaultOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_share_build_tabs_activity, menu);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        mActivity.invalidateOptionsMenu();
        switch (item.getItemId()) {
            case R.id.cancel_build:
                mListener.onCancelBuildContextMenuClick();
                return true;
            case R.id.share_build:
                mListener.onShareButtonClick();
                return true;
            case R.id.restart_build:
                mListener.onRestartBuildButtonClick();
                return true;
            default:
                return false;
        }
    }
}
