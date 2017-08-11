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

package com.github.vase4kin.teamcityapp.properties.view;

import android.app.Activity;
import android.support.annotation.StringRes;
import android.view.MenuItem;
import android.view.View;

import com.cocosw.bottomsheet.BottomSheet;
import com.github.vase4kin.teamcityapp.R;
import com.github.vase4kin.teamcityapp.base.list.view.BaseListViewImpl;
import com.github.vase4kin.teamcityapp.properties.data.PropertiesDataModel;
import com.joanzapata.iconify.IconDrawable;
import com.joanzapata.iconify.fonts.MaterialIcons;

/**
 * View to manage properties interactions
 */
public class PropertiesViewImpl extends BaseListViewImpl<PropertiesDataModel, PropertiesAdapter> implements PropertiesView {

    private Listener mListener;

    public PropertiesViewImpl(View view,
                              Activity activity,
                              @StringRes int emptyMessage,
                              PropertiesAdapter adapter) {
        super(view, activity, emptyMessage, adapter);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void showData(PropertiesDataModel dataModel) {
        mAdapter.setDataModel(dataModel);
        mAdapter.setOnCopyActionClickListener(mListener);
        mRecyclerView.setAdapter(mAdapter);
        mRecyclerView.getAdapter().notifyDataSetChanged();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected int recyclerViewId() {
        return R.id.properties_recycler_view;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setListener(Listener listener) {
        this.mListener = listener;
    }

    @Override
    public void showCopyValueBottomSheet(String title, final String value) {
        BottomSheet bottomSheet = new BottomSheet.Builder(mActivity)
                .title(title)
                .sheet(R.menu.menu_build_element)
                .listener(new MenuItem.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem item) {
                        switch (item.getItemId()) {
                            case R.id.copy:
                                mListener.onCopyActionClick(value);
                                return true;
                            default:
                                return false;
                        }
                    }
                }).build();
        bottomSheet.getMenu().findItem(R.id.copy)
                .setIcon(new IconDrawable(mActivity, MaterialIcons.md_content_copy));
        bottomSheet.show();
    }
}
