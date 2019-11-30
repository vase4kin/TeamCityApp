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

import com.google.firebase.analytics.FirebaseAnalytics
import dagger.Module
import dagger.Provides
import teamcityapp.features.test_details.data.TestDetailsDataManager
import teamcityapp.features.test_details.data.TestDetailsDataManagerImpl
import teamcityapp.features.test_details.repository.TestDetailsRepository
import teamcityapp.features.test_details.tracker.TestDetailsTracker
import teamcityapp.features.test_details.tracker.TestDetailsTrackerImpl
import teamcityapp.features.test_details.view.TestDetailsActivity
import teamcityapp.features.test_details.view.TestDetailsActivity.Companion.ARG_TEST_URL
import teamcityapp.features.test_details.viewmodel.TestDetailsViewModel

@Module
class TestDetailsModule {

    @Provides
    fun providesTestDetailsDataManager(repository: TestDetailsRepository): TestDetailsDataManager =
        TestDetailsDataManagerImpl(repository)

    @Provides
    fun providesTracker(firebaseAnalytics: FirebaseAnalytics): TestDetailsTracker =
        TestDetailsTrackerImpl(firebaseAnalytics)

    @Provides
    fun providesViewModel(
        activity: TestDetailsActivity,
        dataManager: TestDetailsDataManager,
        tracker: TestDetailsTracker
    ): TestDetailsViewModel {
        return TestDetailsViewModel(
            dataManager = dataManager,
            tracker = tracker,
            url = activity.intent.getStringExtra(ARG_TEST_URL) ?: "",
            finish = { activity.finish() }
        )
    }
}
