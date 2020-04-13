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

import com.xwray.groupie.databinding.BindableItem
import teamcityapp.features.change.R
import teamcityapp.features.change.databinding.ItemFileNameBinding
import teamcityapp.features.change.router.ChangeRouter

class ChangeFileItem(
    private val id: String,
    private val changeFile: ChangeActivity.ChangeFile,
    private val router: ChangeRouter
) : BindableItem<ItemFileNameBinding>() {

    override fun getLayout() = R.layout.item_file_name

    override fun bind(viewBinding: ItemFileNameBinding, position: Int) {
        viewBinding.apply {
            title.text = changeFile.fileName
            typeText.text = changeFile.type
            root.setOnClickListener {
                router.openDiffView(id, changeFile.fileName)
            }
        }
    }
}
