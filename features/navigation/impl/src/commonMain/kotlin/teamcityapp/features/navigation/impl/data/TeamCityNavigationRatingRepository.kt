/*
 * Copyright 2026 Andrey Tolpeev
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

package teamcityapp.features.navigation.impl.data

import teamcityapp.features.navigation.api.NavigationRatingRepository
import teamcityapp.libraries.accounts.*
import teamcityapp.libraries.network.*

class TeamCityNavigationRatingRepository(private val account: AccountScope, private val preferences: AccountPreferenceState, private val appLaunchId: String) : NavigationRatingRepository {
    /** Count successful visible Overview loads once, bounded to the three-build milestone. */
    suspend fun buildViewed(id: String) {
        account.requireCurrent()
        require(id.isNotBlank())
        if (preferences.state.value.ratingHandled || preferences.state.value.ratingQualifiedLaunch != null || id in preferences.state.value.ratingViewedBuildIds) return
        preferences.update { current ->
            if (current.ratingHandled || current.ratingQualifiedLaunch != null) {
                current
            } else {
                val viewed = (current.ratingViewedBuildIds + id).take(3).toSet()
                current.copy(ratingViewedBuildIds = viewed, ratingQualifiedLaunch = if (viewed.size == 3) appLaunchId else null)
            }
        }
    }
    override suspend fun isEligible() = account.run {
        val saved = preferences.state.value
        !saved.ratingHandled && saved.ratingViewedBuildIds.size >= 3 && saved.ratingQualifiedLaunch != null && saved.ratingQualifiedLaunch != appLaunchId
    }
    override suspend fun markHandled() {
        preferences.update { it.copy(ratingHandled = true) }
    }
}
