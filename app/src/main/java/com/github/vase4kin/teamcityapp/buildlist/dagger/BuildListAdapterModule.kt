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

package com.github.vase4kin.teamcityapp.buildlist.dagger

import android.content.Context

import com.github.vase4kin.teamcityapp.base.list.view.BaseListView
import com.github.vase4kin.teamcityapp.base.list.view.SimpleSectionedRecyclerViewAdapter
import com.github.vase4kin.teamcityapp.base.list.view.ViewHolderFactory
import com.github.vase4kin.teamcityapp.buildlist.data.BuildListDataModel
import com.github.vase4kin.teamcityapp.buildlist.view.BuildListAdapter
import com.github.vase4kin.teamcityapp.buildlist.view.BuildsViewHolderFactory
import com.github.vase4kin.teamcityapp.buildlist.view.LoadMoreViewHolderFactory

import dagger.Module
import dagger.Provides
import dagger.multibindings.IntKey
import dagger.multibindings.IntoMap

@Module
class BuildListAdapterModule {

    @Provides
    fun providesBuildListAdapter(viewHolderFactories: Map<Int, @JvmSuppressWildcards ViewHolderFactory<BuildListDataModel>>): BuildListAdapter {
        return BuildListAdapter(viewHolderFactories)
    }

    @Provides
    fun providesSimpleSectionedRecyclerViewAdapter(context: Context, adapter: BuildListAdapter): SimpleSectionedRecyclerViewAdapter<BuildListAdapter> {
        return SimpleSectionedRecyclerViewAdapter(context, adapter)
    }

    @IntoMap
    @IntKey(BaseListView.TYPE_LOAD_MORE)
    @Provides
    fun providesLoadMoreViewHolderFactory(): ViewHolderFactory<BuildListDataModel> {
        return LoadMoreViewHolderFactory()
    }

    @IntoMap
    @IntKey(BaseListView.TYPE_DEFAULT)
    @Provides
    fun providesBuildViewHolderFactory(): ViewHolderFactory<BuildListDataModel> {
        return BuildsViewHolderFactory()
    }
}
