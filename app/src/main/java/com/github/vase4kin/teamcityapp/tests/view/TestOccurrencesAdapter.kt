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

package com.github.vase4kin.teamcityapp.tests.view

import com.github.vase4kin.teamcityapp.base.list.adapter.BaseLoadMoreAdapter
import com.github.vase4kin.teamcityapp.base.list.view.BaseViewHolder
import com.github.vase4kin.teamcityapp.base.list.view.ViewHolderFactory
import com.github.vase4kin.teamcityapp.tests.data.TestsDataModel

/**
 * Tests adapter
 */
class TestOccurrencesAdapter(viewHolderFactories: Map<Int, ViewHolderFactory<TestsDataModel>>) :
    BaseLoadMoreAdapter<TestsDataModel>(viewHolderFactories) {

    var onClickListener: OnTestOccurrenceClickListener? = null

    /**
     * {@inheritDoc}
     */
    override fun onBindViewHolder(holder: BaseViewHolder<TestsDataModel>, position: Int) {
        super.onBindViewHolder(holder, position)
        // Find the way how to make it through DI
        if (holder is TestOccurrenceViewHolder && dataModel.isFailed(position)) {
            holder.container.setOnClickListener { onClickListener?.onFailedTestClick(dataModel.getHref(position)) }
        }
    }
}
