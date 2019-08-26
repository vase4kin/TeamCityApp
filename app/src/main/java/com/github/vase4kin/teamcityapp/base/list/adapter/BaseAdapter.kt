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

package com.github.vase4kin.teamcityapp.base.list.adapter

import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView

import com.github.vase4kin.teamcityapp.base.list.view.BaseDataModel
import com.github.vase4kin.teamcityapp.base.list.view.BaseListView
import com.github.vase4kin.teamcityapp.base.list.view.BaseViewHolder
import com.github.vase4kin.teamcityapp.base.list.view.ViewHolderFactory

/**
 * Base adapter class
 *
 * @param <DM> - Data model type
</DM> */
abstract class BaseAdapter<DM : BaseDataModel>(
    private val viewHolderFactories: Map<Int, ViewHolderFactory<DM>>
) : RecyclerView.Adapter<BaseViewHolder<DM>>() {

    /**
     * Data model instance
     */
    lateinit var dataModel: DM

    /**
     * {@inheritDoc}
     */
    override fun getItemCount(): Int {
        return dataModel.itemCount
    }

    /**
     * {@inheritDoc}
     */
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BaseViewHolder<DM> {
        return viewHolderFactories[viewType]!!.createViewHolder(parent)
    }

    /**
     * {@inheritDoc}
     */
    override fun onBindViewHolder(holder: BaseViewHolder<DM>, position: Int) {
        holder.bind(dataModel, position)
    }

    /**
     * {@inheritDoc}
     */
    override fun getItemViewType(position: Int): Int {
        return BaseListView.TYPE_DEFAULT
    }
}
