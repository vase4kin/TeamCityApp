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

package teamcityapp.features.change.view

import android.view.View
import com.xwray.groupie.databinding.BindableItem
import teamcityapp.features.change.R
import teamcityapp.features.change.databinding.ItemFileNamesHeaderBinding

class ChangeFileItemsHeader(
    private val filesNumber: Int
) : BindableItem<ItemFileNamesHeaderBinding>() {

    override fun getLayout() = R.layout.item_file_names_header

    override fun bind(viewBinding: ItemFileNamesHeaderBinding, position: Int) {
        viewBinding.apply {
            title.text = this.root.context.getString(R.string.title_changed_files, filesNumber.toString())
            if (filesNumber > 0) {
                info.visibility = View.VISIBLE
            } else {
                info.visibility = View.GONE
            }
        }
    }
}
