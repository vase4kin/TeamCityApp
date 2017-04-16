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

package com.github.vase4kin.teamcityapp.testdetails.dagger;

import android.app.Activity;

import com.github.vase4kin.teamcityapp.api.Repository;
import com.github.vase4kin.teamcityapp.base.tracker.ViewTracker;
import com.github.vase4kin.teamcityapp.testdetails.data.TestDetailsDataManager;
import com.github.vase4kin.teamcityapp.testdetails.data.TestDetailsDataManagerImpl;
import com.github.vase4kin.teamcityapp.testdetails.extractor.TestDetailsValueExtractor;
import com.github.vase4kin.teamcityapp.testdetails.extractor.TestDetailsValueExtractorImpl;
import com.github.vase4kin.teamcityapp.testdetails.tracker.TestDetailsViewTrackerImpl;
import com.github.vase4kin.teamcityapp.testdetails.view.TestDetailsView;
import com.github.vase4kin.teamcityapp.testdetails.view.TestDetailsViewImpl;
import com.github.vase4kin.teamcityapp.utils.StatusBarUtils;

import dagger.Module;
import dagger.Provides;

@Module
public class TestDetailsModule {

    private Activity mActivity;

    public TestDetailsModule(Activity mActivity) {
        this.mActivity = mActivity;
    }

    @Provides
    TestDetailsView providesTestDetailsView(StatusBarUtils statusBarUtils) {
        return new TestDetailsViewImpl(mActivity, statusBarUtils);
    }

    @Provides
    TestDetailsDataManager providesTestDetailsDataManager(Repository repository) {
        return new TestDetailsDataManagerImpl(repository);
    }

    @Provides
    ViewTracker providesViewTracker() {
        return new TestDetailsViewTrackerImpl();
    }

    @Provides
    TestDetailsValueExtractor providesTestDetailsValueExtractor() {
        return new TestDetailsValueExtractorImpl(mActivity.getIntent().getExtras());
    }

    @Provides
    StatusBarUtils providesStatusBarUtils() {
        return new StatusBarUtils();
    }
}
