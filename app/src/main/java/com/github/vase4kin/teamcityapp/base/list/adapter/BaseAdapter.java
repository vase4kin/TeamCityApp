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

import android.support.v7.widget.RecyclerView;
import android.view.ViewGroup;

import com.github.vase4kin.teamcityapp.base.list.view.BaseDataModel;
import com.github.vase4kin.teamcityapp.base.list.view.BaseViewHolder;
import com.github.vase4kin.teamcityapp.base.list.view.ViewHolderFactory;

import java.util.Map;

/**
 * Base adapter class
 *
 * @param <DM> - Data model type
 */
public abstract class BaseAdapter<DM extends BaseDataModel> extends RecyclerView.Adapter<BaseViewHolder<DM>> {

    /**
     * Data model instance
     */
    protected DM mDataModel;
    /**
     * View holders map
     */
    private Map<Integer, ViewHolderFactory<DM>> mViewHolderFactories;

    /**
     * Constructor
     *
     * @param viewHolderFactories - view holder factories from DI
     */
    public BaseAdapter(Map<Integer, ViewHolderFactory<DM>> viewHolderFactories) {
        this.mViewHolderFactories = viewHolderFactories;
    }

    /**
     * Set data model
     *
     * @param dataModel - data model to set
     */
    public void setDataModel(DM dataModel) {
        this.mDataModel = dataModel;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getItemCount() {
        return mDataModel.getItemCount();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public BaseViewHolder<DM> onCreateViewHolder(ViewGroup parent, int viewType) {
        return mViewHolderFactories.get(viewType).createViewHolder(parent);
    }

    @Override
    public void onBindViewHolder(BaseViewHolder<DM> holder, int position) {
        holder.bind(mDataModel, position);
    }
}
