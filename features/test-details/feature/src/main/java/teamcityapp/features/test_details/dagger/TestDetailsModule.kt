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

package teamcityapp.features.test_details.dagger

import dagger.Binds
import dagger.Module
import dagger.Provides
import teamcityapp.features.test_details.data.TestDetailsDataManager
import teamcityapp.features.test_details.data.TestDetailsDataManagerImpl
import teamcityapp.features.test_details.tracker.FirebaseTestDetailsTrackerImpl
import teamcityapp.features.test_details.tracker.TestDetailsTracker
import teamcityapp.features.test_details.view.ARG_TEST_URL
import teamcityapp.features.test_details.view.TestDetailsActivity
import teamcityapp.features.test_details.view.TestDetailsView
import teamcityapp.features.test_details.view.TestDetailsViewImpl

@Module
abstract class TestDetailsModule {

    @Binds
    abstract fun providesTestDetailsView(impl: TestDetailsViewImpl): TestDetailsView

    @Binds
    abstract fun providesTestDetailsDataManager(impl: TestDetailsDataManagerImpl): TestDetailsDataManager

    @Binds
    abstract fun providesFirebaseViewTracker(impl: FirebaseTestDetailsTrackerImpl): TestDetailsTracker
}

@Module
class TestDetailBundlesModule {

    @Provides
    fun providesUrl(activity: TestDetailsActivity): String {
        return activity.intent.getStringExtra(ARG_TEST_URL) ?: ""
    }
}
