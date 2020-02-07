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

package com.github.vase4kin.teamcityapp.tests.dagger

import android.content.Context
import android.os.Bundle
import com.github.vase4kin.teamcityapp.R
import com.github.vase4kin.teamcityapp.api.Repository
import com.github.vase4kin.teamcityapp.base.list.view.BaseListView
import com.github.vase4kin.teamcityapp.base.list.view.SimpleSectionedRecyclerViewAdapter
import com.github.vase4kin.teamcityapp.base.list.view.ViewHolderFactory
import com.github.vase4kin.teamcityapp.base.tracker.ViewTracker
import com.github.vase4kin.teamcityapp.tests.data.TestsDataManager
import com.github.vase4kin.teamcityapp.tests.data.TestsDataManagerImpl
import com.github.vase4kin.teamcityapp.tests.data.TestsDataModel
import com.github.vase4kin.teamcityapp.tests.extractor.TestsValueExtractor
import com.github.vase4kin.teamcityapp.tests.extractor.TestsValueExtractorImpl
import com.github.vase4kin.teamcityapp.tests.router.TestsRouter
import com.github.vase4kin.teamcityapp.tests.router.TestsRouterImpl
import com.github.vase4kin.teamcityapp.tests.view.LoadMoreViewHolderFactory
import com.github.vase4kin.teamcityapp.tests.view.TestOccurrenceViewHolderFactory
import com.github.vase4kin.teamcityapp.tests.view.TestOccurrencesAdapter
import com.github.vase4kin.teamcityapp.tests.view.TestOccurrencesFragment
import com.github.vase4kin.teamcityapp.tests.view.TestsView
import com.github.vase4kin.teamcityapp.tests.view.TestsViewImpl
import dagger.Module
import dagger.Provides
import dagger.multibindings.IntKey
import dagger.multibindings.IntoMap
import org.greenrobot.eventbus.EventBus

@Module
object TestsModule {

    @JvmStatic
    @Provides
    internal fun providesTestsDataManager(repository: Repository, eventBus: EventBus): TestsDataManager {
        return TestsDataManagerImpl(repository, eventBus)
    }

    @JvmStatic
    @Provides
    internal fun providesTestsValueExtractor(fragment: TestOccurrencesFragment): TestsValueExtractor {
        return TestsValueExtractorImpl(fragment.arguments ?: Bundle.EMPTY)
    }

    @JvmStatic
    @Provides
    internal fun providesTestsView(
        testsValueExtractor: TestsValueExtractor,
        adapter: SimpleSectionedRecyclerViewAdapter<TestOccurrencesAdapter>,
        fragment: TestOccurrencesFragment
    ): TestsView {
        return TestsViewImpl(
            fragment.view!!,
            fragment.requireActivity(),
            testsValueExtractor,
            R.string.empty_passed_tests,
            adapter
        )
    }

    @JvmStatic
    @Provides
    internal fun providesTestsRouter(fragment: TestOccurrencesFragment): TestsRouter {
        return TestsRouterImpl(fragment.requireActivity())
    }

    @JvmStatic
    @Provides
    internal fun providesViewTracker(): ViewTracker {
        return ViewTracker.STUB
    }

    @JvmStatic
    @Provides
    internal fun providesTestOccurrencesAdapter(viewHolderFactories: Map<Int, @JvmSuppressWildcards ViewHolderFactory<TestsDataModel>>): TestOccurrencesAdapter {
        return TestOccurrencesAdapter(viewHolderFactories)
    }

    @JvmStatic
    @Provides
    internal fun providesSimpleSectionedRecyclerViewAdapter(
        context: Context,
        adapter: TestOccurrencesAdapter
    ): SimpleSectionedRecyclerViewAdapter<TestOccurrencesAdapter> {
        return SimpleSectionedRecyclerViewAdapter(context, adapter)
    }

    @JvmStatic
    @IntoMap
    @IntKey(BaseListView.TYPE_LOAD_MORE)
    @Provides
    internal fun providesLoadMoreViewHolderFactory(): ViewHolderFactory<TestsDataModel> {
        return LoadMoreViewHolderFactory()
    }

    @JvmStatic
    @IntoMap
    @IntKey(BaseListView.TYPE_DEFAULT)
    @Provides
    internal fun providesTestsViewHolderFactory(): ViewHolderFactory<TestsDataModel> {
        return TestOccurrenceViewHolderFactory()
    }
}
