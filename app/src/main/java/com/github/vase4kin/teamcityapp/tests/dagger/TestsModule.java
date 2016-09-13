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

import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.View;

import com.github.vase4kin.teamcityapp.R;
import com.github.vase4kin.teamcityapp.api.TeamCityService;
import com.github.vase4kin.teamcityapp.navigation.tracker.ViewTracker;
import com.github.vase4kin.teamcityapp.tests.data.TestsDataManager;
import com.github.vase4kin.teamcityapp.tests.data.TestsDataManagerImpl;
import com.github.vase4kin.teamcityapp.tests.extractor.TestsValueExtractor;
import com.github.vase4kin.teamcityapp.tests.extractor.TestsValueExtractorImpl;
import com.github.vase4kin.teamcityapp.tests.router.TestsRouter;
import com.github.vase4kin.teamcityapp.tests.router.TestsRouterImpl;
import com.github.vase4kin.teamcityapp.tests.view.TestsView;
import com.github.vase4kin.teamcityapp.tests.view.TestsViewImpl;

import dagger.Module;
import dagger.Provides;
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
    TestsDataManager providesTestsDataManager(TeamCityService teamCityService, EventBus eventBus) {
        return new TestsDataManagerImpl(teamCityService, eventBus);
    }

    @Provides
    TestsValueExtractor providesTestsValueExtractor() {
        return new TestsValueExtractorImpl(mFragment.getArguments());
    }

    @Provides
    TestsView providesTestsView(TestsValueExtractor testsValueExtractor) {
        return new TestsViewImpl(mView, mFragment.getActivity(), testsValueExtractor, R.string.empty_passed_tests);
    }

    @Provides
    TestsRouter providesTestsRouter() {
        return new TestsRouterImpl(mFragment.getActivity());
    }

    @SuppressWarnings("SameReturnValue")
    @Provides
    @Nullable
    ViewTracker providesViewTracker() {
        return null;
    }
}
