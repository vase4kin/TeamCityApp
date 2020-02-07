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

package com.github.vase4kin.teamcityapp.changes.dagger

import android.os.Bundle
import com.github.vase4kin.teamcityapp.R
import com.github.vase4kin.teamcityapp.api.Repository
import com.github.vase4kin.teamcityapp.base.list.view.BaseListView
import com.github.vase4kin.teamcityapp.base.list.view.ViewHolderFactory
import com.github.vase4kin.teamcityapp.base.tracker.ViewTracker
import com.github.vase4kin.teamcityapp.changes.data.ChangesDataManager
import com.github.vase4kin.teamcityapp.changes.data.ChangesDataManagerImpl
import com.github.vase4kin.teamcityapp.changes.data.ChangesDataModel
import com.github.vase4kin.teamcityapp.changes.extractor.ChangesValueExtractor
import com.github.vase4kin.teamcityapp.changes.extractor.ChangesValueExtractorImpl
import com.github.vase4kin.teamcityapp.changes.view.ChangesAdapter
import com.github.vase4kin.teamcityapp.changes.view.ChangesFragment
import com.github.vase4kin.teamcityapp.changes.view.ChangesView
import com.github.vase4kin.teamcityapp.changes.view.ChangesViewHolderFactory
import com.github.vase4kin.teamcityapp.changes.view.ChangesViewImpl
import com.github.vase4kin.teamcityapp.changes.view.LoadMoreViewHolderFactory
import dagger.Module
import dagger.Provides
import dagger.multibindings.IntKey
import dagger.multibindings.IntoMap
import org.greenrobot.eventbus.EventBus

@Module
object ChangesModule {

    @JvmStatic
    @Provides
    fun providesChangesDataManager(repository: Repository, eventBus: EventBus): ChangesDataManager {
        return ChangesDataManagerImpl(repository, eventBus)
    }

    @JvmStatic
    @Provides
    fun providesChangesView(fragment: ChangesFragment, changesAdapter: ChangesAdapter): ChangesView {
        return ChangesViewImpl(fragment.view!!, fragment.requireActivity(), R.string.empty_list_message_changes, changesAdapter)
    }

    @JvmStatic
    @Provides
    fun providesChangesValueExtractor(fragment: ChangesFragment): ChangesValueExtractor {
        return ChangesValueExtractorImpl(fragment.arguments ?: Bundle.EMPTY)
    }

    @JvmStatic
    @Provides
    fun providesViewTracker(): ViewTracker {
        return ViewTracker.STUB
    }

    @JvmStatic
    @Provides
    fun providesChangesAdapter(viewHolderFactories: Map<Int, @JvmSuppressWildcards ViewHolderFactory<ChangesDataModel>>): ChangesAdapter {
        return ChangesAdapter(viewHolderFactories)
    }

    @JvmStatic
    @IntoMap
    @IntKey(BaseListView.TYPE_LOAD_MORE)
    @Provides
    fun providesLoadMoreViewHolderFactory(): ViewHolderFactory<ChangesDataModel> {
        return LoadMoreViewHolderFactory()
    }

    @JvmStatic
    @IntoMap
    @IntKey(BaseListView.TYPE_DEFAULT)
    @Provides
    fun providesChangesViewHolderFactory(): ViewHolderFactory<ChangesDataModel> {
        return ChangesViewHolderFactory()
    }
}
