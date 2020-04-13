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

package teamcityapp.features.change.viewmodel

import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.OnLifecycleEvent
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.GroupieViewHolder
import teamcityapp.features.change.router.ChangeRouter
import teamcityapp.features.change.view.ChangeActivity
import teamcityapp.features.change.view.ChangeItemsFactory

class ChangeViewModel(
    private val bundleData: ChangeActivity.Companion.BundleData?,
    private val router: ChangeRouter,
    private val itemsFactory: ChangeItemsFactory,
    val adapter: GroupAdapter<GroupieViewHolder>,
    val finish: () -> Unit
) : LifecycleObserver {

    @OnLifecycleEvent(Lifecycle.Event.ON_CREATE)
    fun onCreate() {
        val bundleData = bundleData
        if (bundleData == null) {
            finish()
            return
        }
        router.bind()
        bundleData.let {
            // Details card
            val commitDetailsItem =
                itemsFactory.createChangeDetailsCard(
                    comment = it.commitName.trim(),
                    version = it.version,
                    date = it.date,
                    userName = it.userName,
                    webUrl = it.webUrl
                )
            // Changes header
            val changeFilesHeader =
                itemsFactory.createChangeFilesHeader(it.changeFileNames.size)
            // Changes
            val changeFileItems = it.changeFileNames.map { changeFileName ->
                itemsFactory.createChangeFile(
                    id = it.id, changeFile = changeFileName
                )
            }
            adapter.add(commitDetailsItem)
            adapter.add(changeFilesHeader)
            adapter.addAll(changeFileItems)
        }
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_DESTROY)
    fun onDestroy() {
        router.unbind()
    }
}
