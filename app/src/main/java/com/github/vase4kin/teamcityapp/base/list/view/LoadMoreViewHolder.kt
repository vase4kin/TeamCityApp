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

package com.github.vase4kin.teamcityapp.base.list.view

import android.view.LayoutInflater
import android.view.ViewGroup
import com.github.vase4kin.teamcityapp.R

/**
 * Base load more view holder
 *
 * @param <BM> - Base data model
</BM> */
class LoadMoreViewHolder<BM : BaseDataModel>(parent: ViewGroup) :
    BaseViewHolder<BM>(LayoutInflater.from(parent.context).inflate(R.layout.item_load_more, parent, false)) {

    /**
     * {@inheritDoc}
     */
    override fun bind(dataModel: BM, position: Int) {
    }
}
