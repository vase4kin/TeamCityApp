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
import teamcityapp.features.change.databinding.ItemCommitCardBinding
import teamcityapp.features.change.router.ChangeRouter

class ChangeDetailsItem(
    private val comment: String,
    private val version: String,
    private val userName: String,
    private val date: String,
    private val webUrl: String,
    private val router: ChangeRouter
) : BindableItem<ItemCommitCardBinding>() {

    override fun getLayout() = R.layout.item_commit_card

    override fun bind(viewBinding: ItemCommitCardBinding, position: Int) {
        viewBinding.apply {
            title.text = this@ChangeDetailsItem.comment
            version.text = this@ChangeDetailsItem.version
            userName.text = root.context.getString(
                R.string.text_description,
                this@ChangeDetailsItem.userName, this@ChangeDetailsItem.date
            )
            button.setOnClickListener {
                router.openUrl(webUrl)
            }
        }
    }
}
