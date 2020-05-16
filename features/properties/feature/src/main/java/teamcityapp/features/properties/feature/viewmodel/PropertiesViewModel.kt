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

package teamcityapp.features.properties.feature.viewmodel

import android.view.View
import androidx.databinding.ObservableField
import androidx.databinding.ObservableInt
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.OnLifecycleEvent
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.GroupieViewHolder
import teamcityapp.features.properties.feature.model.InternalProperty
import teamcityapp.features.properties.feature.view.PropertyItemFactory
import teamcityapp.libraries.utils.EmptyViewModel

class PropertiesViewModel(
    val adapter: GroupAdapter<GroupieViewHolder>,
    private val factory: PropertyItemFactory,
    private val properties: List<InternalProperty>
) : LifecycleObserver, EmptyViewModel {

    override val emptyVisibility = ObservableInt(View.GONE)
    override val emptyText = ObservableField<String>()
    val dataVisibility = ObservableInt(View.GONE)

    @OnLifecycleEvent(Lifecycle.Event.ON_CREATE)
    fun onCreate() {
        if (properties.isEmpty()) {
            emptyVisibility.set(View.VISIBLE)
        } else {
            val items = properties.map {
                factory.createPropertyItem(it)
            }
            adapter.update(items)
            dataVisibility.set(View.VISIBLE)
        }
    }
}