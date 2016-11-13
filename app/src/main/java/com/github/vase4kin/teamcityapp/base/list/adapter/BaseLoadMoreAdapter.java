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

package com.github.vase4kin.teamcityapp.base.list.adapter;

import com.github.vase4kin.teamcityapp.base.list.view.BaseDataModel;
import com.github.vase4kin.teamcityapp.base.list.view.BaseListView;
import com.github.vase4kin.teamcityapp.base.list.view.ViewHolderFactory;

import java.util.Map;

/**
 * Base load more adapter class
 *
 * @param <DM> - Data model type
 */
public abstract class BaseLoadMoreAdapter<DM extends BaseDataModel & ModelLoadMore<DM>> extends BaseAdapter<DM> implements ViewLoadMore<DM> {

    /**
     * Constructor
     *
     * @param viewHolderFactories - view holder factories from DI
     */
    public BaseLoadMoreAdapter(Map<Integer, ViewHolderFactory<DM>> viewHolderFactories) {
        super(viewHolderFactories);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getItemViewType(int position) {
        // some method to check
        if (mDataModel.isLoadMore(position)) {
            return BaseListView.TYPE_LOAD_MORE;
        } else {
            return BaseListView.TYPE_DEFAULT;
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void addLoadMore() {
        mDataModel.addLoadMore();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void removeLoadMore() {
        mDataModel.removeLoadMore();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void addMoreBuilds(DM dataModel) {
        mDataModel.addMoreBuilds(dataModel);
    }
}
