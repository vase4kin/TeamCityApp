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

package teamcityapp.features.properties.feature.stateholder

import android.view.View
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
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

class PropertiesStateHolderTest {

    private val adapter: GroupAdapter<GroupieViewHolder> = mock()
    private val factory: PropertyItemFactory = mock()
    private val resourceManager: ResourcesManager = mock()

    @Test
    fun testWhenDataIsEmpty() {
        val text = "text"
        whenever(resourceManager.getString(R.string.empty_list_message_parameters)).thenReturn(text)
        val stateHolder = PropertiesStateHolder(adapter, factory, emptyList(), resourceManager)
        stateHolder.onCreate()
        Assert.assertEquals(View.GONE, stateHolder.dataVisibility.get())
        Assert.assertEquals(View.VISIBLE, stateHolder.emptyVisibility.get())
        Assert.assertEquals(text, stateHolder.emptyText.get())
    }

    @Test
    fun testWhenDataIsNotEmpty() {
        val property = InternalProperty("name", "value")
        val router: PropertiesRouter = mock()
        val propertyItem = PropertyItem(property, router)
        whenever(factory.createPropertyItem(property)).thenReturn(propertyItem)
        val stateHolder = PropertiesStateHolder(adapter, factory, listOf(property), resourceManager)
        stateHolder.onCreate()
        Assert.assertEquals(View.VISIBLE, stateHolder.dataVisibility.get())
        Assert.assertEquals(View.GONE, stateHolder.emptyVisibility.get())
        verify(adapter).update(listOf(propertyItem))
    }
}
