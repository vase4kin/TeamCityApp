package teamcityapp.features.settings.dagger

import teamcityapp.libraries.utils.requireScreenOwner
import android.app.Activity
import com.google.firebase.analytics.FirebaseAnalytics
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ActivityComponent
import teamcityapp.features.settings.tracker.SettingsTracker
import teamcityapp.features.settings.tracker.SettingsTrackerImpl
import teamcityapp.features.settings.view.SettingsActivity

@Module
@InstallIn(ActivityComponent::class)
object SettingsActivityModule {

    @Provides
    fun provideOwner(owner: Activity): SettingsActivity = owner.requireScreenOwner<SettingsActivity>()

    @Provides
    fun provideTracker(firebaseAnalytics: FirebaseAnalytics): SettingsTracker {
        return SettingsTrackerImpl(firebaseAnalytics)
    }
}
