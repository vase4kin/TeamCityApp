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

package com.github.vase4kin.teamcityapp.properties.view

import android.graphics.Color
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.TextView
import butterknife.BindColor
import butterknife.BindView
import butterknife.ButterKnife
import com.github.vase4kin.teamcityapp.R
import com.github.vase4kin.teamcityapp.base.list.view.BaseViewHolder
import com.github.vase4kin.teamcityapp.properties.data.PropertiesDataModel

/**
 * Properties single item view holder
 */
class PropertyViewHolder
/**
 * Constructor
 *
 * @param parent group view
 */
    (parent: ViewGroup) : BaseViewHolder<PropertiesDataModel>(
    LayoutInflater.from(parent.context).inflate(
        R.layout.item_properties_list,
        parent,
        false
    )
) {
    @BindColor(R.color.main_text_color)
    @JvmField
    var emptyColor: Int = 0
    @BindView(R.id.title)
    lateinit var parameterName: TextView
    @BindView(R.id.subTitle)
    lateinit var parameterValue: TextView

    init {
        ButterKnife.bind(this, itemView)
    }

    override fun bind(dataModel: PropertiesDataModel, position: Int) {
        parameterName.text = dataModel.getName(position)
        parameterValue.text = dataModel.getValue(position)
        if (dataModel.isEmpty(position)) {
            parameterValue.setTextColor(Color.LTGRAY)
        } else {
            parameterValue.setTextColor(emptyColor)
        }
    }
}
