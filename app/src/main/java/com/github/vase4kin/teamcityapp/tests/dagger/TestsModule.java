/*
 * Copyright 2016 Andrey Tolpeev
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.github.vase4kin.teamcityapp.tests.dagger;

import android.content.Context;
import android.support.v4.app.Fragment;
import android.view.View;

import com.github.vase4kin.teamcityapp.R;
import com.github.vase4kin.teamcityapp.api.Repository;
import com.github.vase4kin.teamcityapp.base.list.view.BaseListView;
import com.github.vase4kin.teamcityapp.base.list.view.SimpleSectionedRecyclerViewAdapter;
import com.github.vase4kin.teamcityapp.base.list.view.ViewHolderFactory;
import com.github.vase4kin.teamcityapp.base.tracker.ViewTracker;
import com.github.vase4kin.teamcityapp.tests.data.TestsDataManager;
import com.github.vase4kin.teamcityapp.tests.data.TestsDataManagerImpl;
import com.github.vase4kin.teamcityapp.tests.data.TestsDataModel;
import com.github.vase4kin.teamcityapp.tests.extractor.TestsValueExtractor;
import com.github.vase4kin.teamcityapp.tests.extractor.TestsValueExtractorImpl;
import com.github.vase4kin.teamcityapp.tests.router.TestsRouter;
import com.github.vase4kin.teamcityapp.tests.router.TestsRouterImpl;
import com.github.vase4kin.teamcityapp.tests.view.LoadMoreViewHolderFactory;
import com.github.vase4kin.teamcityapp.tests.view.TestOccurrenceViewHolderFactory;
import com.github.vase4kin.teamcityapp.tests.view.TestOccurrencesAdapter;
import com.github.vase4kin.teamcityapp.tests.view.TestsView;
import com.github.vase4kin.teamcityapp.tests.view.TestsViewImpl;

import java.util.Map;

import dagger.Module;
import dagger.Provides;
import dagger.multibindings.IntKey;
import dagger.multibindings.IntoMap;
import de.greenrobot.event.EventBus;

@Module
public class TestsModule {

    private View mView;
    private Fragment mFragment;

    public TestsModule(View mView, Fragment fragment) {
        this.mView = mView;
        this.mFragment = fragment;
    }

    @Provides
    TestsDataManager providesTestsDataManager(Repository repository, EventBus eventBus) {
        return new TestsDataManagerImpl(repository, eventBus);
    }

    @Provides
    TestsValueExtractor providesTestsValueExtractor() {
        return new TestsValueExtractorImpl(mFragment.getArguments());
    }

    @Provides
    TestsView providesTestsView(TestsValueExtractor testsValueExtractor,
                                SimpleSectionedRecyclerViewAdapter<TestOccurrencesAdapter> adapter) {
        return new TestsViewImpl(
                mView,
                mFragment.getActivity(),
                testsValueExtractor,
                R.string.empty_passed_tests,
                adapter);
    }

    @Provides
    TestsRouter providesTestsRouter() {
        return new TestsRouterImpl(mFragment.getActivity());
    }

    @Provides
    ViewTracker providesViewTracker() {
        return ViewTracker.STUB;
    }

    @Provides
    TestOccurrencesAdapter providesTestOccurrencesAdapter(Map<Integer, ViewHolderFactory<TestsDataModel>> viewHolderFactories) {
        return new TestOccurrencesAdapter(viewHolderFactories);
    }

    @Provides
    SimpleSectionedRecyclerViewAdapter<TestOccurrencesAdapter> providesSimpleSectionedRecyclerViewAdapter(Context context, TestOccurrencesAdapter adapter) {
        return new SimpleSectionedRecyclerViewAdapter<>(context, adapter);
    }

    @IntoMap
    @IntKey(BaseListView.TYPE_LOAD_MORE)
    @Provides
    ViewHolderFactory<TestsDataModel> providesLoadMoreViewHolderFactory() {
        return new LoadMoreViewHolderFactory();
    }

    @IntoMap
    @IntKey(BaseListView.TYPE_DEFAULT)
    @Provides
    ViewHolderFactory<TestsDataModel> providesTestsViewHolderFactory() {
        return new TestOccurrenceViewHolderFactory();
    }
}
