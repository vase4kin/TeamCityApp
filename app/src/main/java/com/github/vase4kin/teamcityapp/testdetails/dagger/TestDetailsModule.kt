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

package com.github.vase4kin.teamcityapp.testdetails.dagger

import com.github.vase4kin.teamcityapp.testdetails.data.TestDetailsDataManager
import com.github.vase4kin.teamcityapp.testdetails.data.TestDetailsDataManagerImpl
import com.github.vase4kin.teamcityapp.testdetails.extractor.TestDetailsValueExtractor
import com.github.vase4kin.teamcityapp.testdetails.extractor.TestDetailsValueExtractorImpl
import com.github.vase4kin.teamcityapp.testdetails.tracker.FirebaseTestDetailsTrackerImpl
import com.github.vase4kin.teamcityapp.testdetails.tracker.TestDetailsTracker
import com.github.vase4kin.teamcityapp.testdetails.view.TestDetailsView
import com.github.vase4kin.teamcityapp.testdetails.view.TestDetailsViewImpl
import dagger.Binds
import dagger.Module

@Module
abstract class TestDetailsModule {

    @Binds
    abstract fun providesTestDetailsView(impl: TestDetailsViewImpl): TestDetailsView

    @Binds
    abstract fun providesTestDetailsDataManager(impl: TestDetailsDataManagerImpl): TestDetailsDataManager

    @Binds
    abstract fun providesTestDetailsValueExtractor(impl: TestDetailsValueExtractorImpl): TestDetailsValueExtractor

    @Binds
    abstract fun providesFirebaseViewTracker(impl: FirebaseTestDetailsTrackerImpl): TestDetailsTracker
}
