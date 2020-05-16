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
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.GroupieViewHolder
import org.junit.Assert
import org.junit.Test
import teamcityapp.features.properties.feature.R
import teamcityapp.features.properties.feature.model.InternalProperty
import teamcityapp.features.properties.feature.router.PropertiesRouter
import teamcityapp.features.properties.feature.view.PropertyItem
import teamcityapp.features.properties.feature.view.PropertyItemFactory
import teamcityapp.libraries.utils.ResourcesManager

class PropertiesViewModelTest {

    private val adapter: GroupAdapter<GroupieViewHolder> = mock()
    private val factory: PropertyItemFactory = mock()
    private val resourceManager: ResourcesManager = mock()

    @Test
    fun testWhenDataIsEmpty() {
        val text = "text"
        whenever(resourceManager.getString(R.string.empty_list_message_parameters)).thenReturn(text)
        val viewModel = PropertiesViewModel(adapter, factory, emptyList(), resourceManager)
        viewModel.onCreate()
        Assert.assertEquals(View.GONE, viewModel.dataVisibility.get())
        Assert.assertEquals(View.VISIBLE, viewModel.emptyVisibility.get())
        Assert.assertEquals(text, viewModel.emptyText.get())
    }

    @Test
    fun testWhenDataIsNotEmpty() {
        val property = InternalProperty("name", "value")
        val router: PropertiesRouter = mock()
        val propertyItem = PropertyItem(property, router)
        whenever(factory.createPropertyItem(property)).thenReturn(propertyItem)
        val viewModel = PropertiesViewModel(adapter, factory, listOf(property), resourceManager)
        viewModel.onCreate()
        Assert.assertEquals(View.VISIBLE, viewModel.dataVisibility.get())
        Assert.assertEquals(View.GONE, viewModel.emptyVisibility.get())
        verify(adapter).update(listOf(propertyItem))
    }
}
