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

import com.github.vase4kin.teamcityapp.base.list.view.BaseDataModel
import com.github.vase4kin.teamcityapp.base.list.view.BaseListView
import com.github.vase4kin.teamcityapp.base.list.view.ViewHolderFactory

/**
 * Base load more adapter class
 *
 * @param <DM> - Data model type
</DM> */
abstract class BaseLoadMoreAdapter<DM>(
    viewHolderFactories: Map<Int, ViewHolderFactory<DM>>
) : BaseAdapter<DM>(viewHolderFactories), ViewLoadMore<DM> where DM : BaseDataModel, DM : ModelLoadMore<DM> {

    /**
     * {@inheritDoc}
     */
    override fun getItemViewType(position: Int): Int {
        return if (dataModel.isLoadMore(position)) {
            BaseListView.TYPE_LOAD_MORE
        } else {
            BaseListView.TYPE_DEFAULT
        }
    }

    /**
     * {@inheritDoc}
     */
    override fun addLoadMore() {
        dataModel.addLoadMore()
    }

    /**
     * {@inheritDoc}
     */
    override fun removeLoadMore() {
        dataModel.removeLoadMore()
    }

    /**
     * {@inheritDoc}
     */
    override fun addMoreBuilds(dataModel: DM) {
        this.dataModel.addMoreBuilds(dataModel)
    }
}
