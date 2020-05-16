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

package teamcityapp.features.properties.feature.dagger

import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.GroupieViewHolder
import dagger.Module
import dagger.Provides
import teamcityapp.features.properties.feature.model.InternalProperty
import teamcityapp.features.properties.feature.router.PropertiesRouter
import teamcityapp.features.properties.feature.view.PropertiesFragment
import teamcityapp.features.properties.feature.view.PropertyItemFactory
import teamcityapp.features.properties.feature.view.PropertyItemFactoryImpl
import teamcityapp.features.properties.feature.viewmodel.PropertiesViewModel

@Module
object PropertiesFragmentModule {

    @JvmStatic
    @Provides
    fun providesViewModel(
        fragment: PropertiesFragment,
        adapter: GroupAdapter<GroupieViewHolder>,
        factory: PropertyItemFactory
    ): PropertiesViewModel {
        return PropertiesViewModel(
            adapter = adapter,
            properties = (fragment.arguments?.getParcelableArray(PropertiesFragment.ARG_PROPERTIES) as Array<InternalProperty>?
                ?: emptyArray<InternalProperty>()).toList(),
            factory = factory
        )
    }

    @JvmStatic
    @Provides
    fun provideFactory(router: PropertiesRouter): PropertyItemFactory {
        return PropertyItemFactoryImpl(router)
    }

    @JvmStatic
    @Provides
    fun providesAdapter() = GroupAdapter<GroupieViewHolder>()
}
