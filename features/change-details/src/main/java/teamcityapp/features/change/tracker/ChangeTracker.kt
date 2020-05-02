/*
 * Copyright 2020 Andrey Tolpeev
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

package teamcityapp.features.change.tracker

import com.google.firebase.analytics.FirebaseAnalytics
import teamcityapp.features.change.tracker.ChangeTracker.Companion.EVENT_CHANGE_CLICK_FILE_DIFF
import teamcityapp.features.change.tracker.ChangeTracker.Companion.EVENT_CHANGE_CLICK_MORE_DETAILS
import teamcityapp.features.change.tracker.ChangeTracker.Companion.SCREEN_NAME

interface ChangeTracker {

    fun trackView()
    fun trackMoreDetailsClicked()
    fun trackViewFileDiffClicked()

    companion object {

        /**
         * Screen name to track
         */
        const val SCREEN_NAME = "screen_open_change"

        /**
         * Event when user clicks on more details
         */
        const val EVENT_CHANGE_CLICK_MORE_DETAILS = "change_click_more_details"

        /**
         * Event when user clicks on file diff
         */
        const val EVENT_CHANGE_CLICK_FILE_DIFF = "change_click_view_file_diff"
    }
}

class ChangeTrackerImpl(
    private val firebaseAnalytics: FirebaseAnalytics
) : ChangeTracker {

    override fun trackView() {
        firebaseAnalytics.logEvent(SCREEN_NAME, null)
    }

    override fun trackMoreDetailsClicked() {
        firebaseAnalytics.logEvent(EVENT_CHANGE_CLICK_MORE_DETAILS, null)
    }

    override fun trackViewFileDiffClicked() {
        firebaseAnalytics.logEvent(EVENT_CHANGE_CLICK_FILE_DIFF, null)
    }
}
