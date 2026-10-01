package com.github.vase4kin.teamcityapp.filter_bottom_sheet_dialog.dagger

import com.github.vase4kin.teamcityapp.filter_bottom_sheet_dialog.tracker.FilterBottomSheetTracker
import com.github.vase4kin.teamcityapp.filter_bottom_sheet_dialog.tracker.FilterBottomSheetTrackerImpl
import com.google.firebase.analytics.FirebaseAnalytics
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.FragmentComponent

@Module
@InstallIn(FragmentComponent::class)
object FilterBottomSheetModule {

    @Provides
    fun provideTracker(firebaseAnalytics: FirebaseAnalytics): FilterBottomSheetTracker =
        FilterBottomSheetTrackerImpl(firebaseAnalytics)
}
