package com.github.vase4kin.teamcityapp.filter_bottom_sheet_dialog.tracker

import android.os.Bundle
import com.github.vase4kin.teamcityapp.filter_bottom_sheet_dialog.filter.Filter
import com.google.firebase.analytics.FirebaseAnalytics
import javax.inject.Inject

interface FilterBottomSheetTracker {

    companion object {

        const val EVENT_FILTER_SELECTED = "builds_filter_selected"
        const val ARG_FILTER = "filter"
    }

    fun trackFilterSelected(filter: Filter)
}

class FilterBottomSheetTrackerImpl @Inject constructor(
        private val firebaseAnalytics: FirebaseAnalytics
) : FilterBottomSheetTracker {
    override fun trackFilterSelected(filter: Filter) {
        firebaseAnalytics.logEvent(
                FilterBottomSheetTracker.EVENT_FILTER_SELECTED,
                Bundle().apply { putString(FilterBottomSheetTracker.ARG_FILTER, filter.toString()) })
    }
}