package teamcityapp.features.settings.dagger

import com.google.firebase.analytics.FirebaseAnalytics
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import teamcityapp.features.settings.tracker.SettingsTracker
import teamcityapp.features.settings.tracker.SettingsTrackerImpl

@Module
@InstallIn(SingletonComponent::class)
object SettingsActivityModule {

    @Provides
    fun provideTracker(firebaseAnalytics: FirebaseAnalytics): SettingsTracker {
        return SettingsTrackerImpl(firebaseAnalytics)
    }
}
