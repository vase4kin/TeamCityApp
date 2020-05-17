/*
 * Copyright 2020 Andrey Tolpeev
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

package teamcityapp.features.properties.feature.view

import androidx.core.content.ContextCompat
import com.xwray.groupie.databinding.BindableItem
import teamcityapp.features.properties.feature.R
import teamcityapp.features.properties.feature.databinding.ItemPropertyBinding
import teamcityapp.features.properties.feature.model.InternalProperty
import teamcityapp.features.properties.feature.router.PropertiesRouter

class PropertyItem(
    private val property: InternalProperty,
    private val router: PropertiesRouter
) : BindableItem<ItemPropertyBinding>() {

    override fun getLayout() = R.layout.item_property

    override fun bind(viewBinding: ItemPropertyBinding, position: Int) {
        viewBinding.apply {
            title.text = property.name
            val valueIsEmpty = property.value.isEmpty()
            if (valueIsEmpty) {
                subTitle.text = root.context.getString(R.string.text_property_value_empty)
                subTitle.setTextColor(
                    ContextCompat.getColor(
                        root.context,
                        R.color.material_on_background_disabled
                    )
                )
            } else {
                subTitle.text = property.value
                subTitle.setTextColor(
                    ContextCompat.getColor(
                        root.context,
                        R.color.material_on_background_emphasis_high_type
                    )
                )
                root.setOnClickListener {
                    router.showCopyValueBottomSheet(
                        property.name,
                        property.value
                    )
                }
            }
        }
    }
}
