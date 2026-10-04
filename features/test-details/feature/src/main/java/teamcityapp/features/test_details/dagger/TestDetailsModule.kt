package teamcityapp.features.test_details.dagger

import com.google.firebase.analytics.FirebaseAnalytics
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ViewModelComponent
import teamcityapp.features.test_details.tracker.TestDetailsTracker
import teamcityapp.features.test_details.tracker.TestDetailsTrackerImpl

@Module
@InstallIn(ViewModelComponent::class)
object TestDetailsModule {
    @Provides
    fun providesTracker(firebaseAnalytics: FirebaseAnalytics): TestDetailsTracker =
        TestDetailsTrackerImpl(firebaseAnalytics)
}
