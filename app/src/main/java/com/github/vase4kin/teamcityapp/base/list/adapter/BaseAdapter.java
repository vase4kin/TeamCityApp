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

package com.github.vase4kin.teamcityapp.base.list.adapter;

import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.github.vase4kin.teamcityapp.base.list.view.BaseDataModel;
import com.github.vase4kin.teamcityapp.base.list.view.BaseListView;
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
    protected DM dataModel;
    /**
     * View holders map
     */
    private Map<Integer, ViewHolderFactory<DM>> viewHolderFactories;

    /**
     * Constructor
     *
     * @param viewHolderFactories - view holder factories from DI
     */
    public BaseAdapter(Map<Integer, ViewHolderFactory<DM>> viewHolderFactories) {
        this.viewHolderFactories = viewHolderFactories;
    }

    /**
     * Set data model
     *
     * @param dataModel - data model to set
     */
    public void setDataModel(DM dataModel) {
        this.dataModel = dataModel;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getItemCount() {
        return dataModel.getItemCount();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public BaseViewHolder<DM> onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return viewHolderFactories.get(viewType).createViewHolder(parent);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onBindViewHolder(@NonNull BaseViewHolder<DM> holder, int position) {
        holder.bind(dataModel, position);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getItemViewType(int position) {
        return BaseListView.TYPE_DEFAULT;
    }
}
