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