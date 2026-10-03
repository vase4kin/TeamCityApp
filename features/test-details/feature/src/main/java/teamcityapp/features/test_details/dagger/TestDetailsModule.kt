package teamcityapp.features.test_details.dagger

import teamcityapp.libraries.utils.requireScreenOwner
import android.app.Activity
import com.google.firebase.analytics.FirebaseAnalytics
import dagger.Module
import dagger.Provides
import dagger.hilt.android.scopes.ActivityScoped
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ActivityComponent
import teamcityapp.features.test_details.data.TestDetailsDataManager
import teamcityapp.features.test_details.data.TestDetailsDataManagerImpl
import teamcityapp.features.test_details.repository.TestDetailsRepository
import teamcityapp.features.test_details.tracker.TestDetailsTracker
import teamcityapp.features.test_details.tracker.TestDetailsTrackerImpl
import teamcityapp.features.test_details.view.TestDetailsActivity
import teamcityapp.features.test_details.view.TestDetailsActivity.Companion.ARG_TEST_URL
import teamcityapp.features.test_details.stateholder.TestDetailsStateHolder

@Module
@InstallIn(ActivityComponent::class)
object TestDetailsModule {

    @Provides
    fun provideOwner(owner: Activity): TestDetailsActivity = owner.requireScreenOwner<TestDetailsActivity>()

    @Provides
    fun providesTestDetailsDataManager(repository: TestDetailsRepository): TestDetailsDataManager =
        TestDetailsDataManagerImpl(repository)

    @Provides
    fun providesTracker(firebaseAnalytics: FirebaseAnalytics): TestDetailsTracker =
        TestDetailsTrackerImpl(firebaseAnalytics)

    @Provides
    @ActivityScoped
    fun providesStateHolder(
        activity: TestDetailsActivity,
        dataManager: TestDetailsDataManager,
        tracker: TestDetailsTracker
    ): TestDetailsStateHolder {
        return TestDetailsStateHolder(
            dataManager = dataManager,
            tracker = tracker,
            url = activity.intent.getStringExtra(ARG_TEST_URL) ?: "",
            showErrorToast = { activity.showErrorToast() },
            finish = { activity.finish() }
        )
    }
}
