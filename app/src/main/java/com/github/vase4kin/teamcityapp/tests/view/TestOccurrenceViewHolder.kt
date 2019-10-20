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

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import butterknife.BindView
import butterknife.ButterKnife
import com.github.vase4kin.teamcityapp.R
import com.github.vase4kin.teamcityapp.base.list.view.BaseViewHolder
import com.github.vase4kin.teamcityapp.tests.data.TestsDataModel

/**
 * Test single item view holder
 */
class TestOccurrenceViewHolder(parent: ViewGroup) : BaseViewHolder<TestsDataModel>(
    LayoutInflater.from(parent.context).inflate(
        R.layout.item_test_occurence_list,
        parent,
        false
    )
) {
    @BindView(R.id.title)
    lateinit var testName: TextView
    @BindView(R.id.image)
    lateinit var testStatusImage: ImageView

    init {
        ButterKnife.bind(this, itemView)
    }

    /**
     * {@inheritDoc}
     */
    override fun bind(dataModel: TestsDataModel, position: Int) {
        testName.text = dataModel.getName(position)
        val statusIconRes = dataModel.getStatusIcon(position)
        testStatusImage.setImageResource(statusIconRes)
    }
}
