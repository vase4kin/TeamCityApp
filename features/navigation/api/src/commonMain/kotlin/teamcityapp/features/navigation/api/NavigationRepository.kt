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

package teamcityapp.features.navigation.api

interface NavigationRepository {
    /** Returns projects before configurations, retaining each section's server order. */
    suspend fun entries(projectId: String, forceRefresh: Boolean): List<NavigationEntry>
}

/** Preserves the existing global rating choice through an app storage adapter. */
interface NavigationRatingRepository {
    suspend fun isEligible(): Boolean

    /** Both Cancel and Rate permanently dismiss the prompt using the existing preference. */
    suspend fun markHandled()
}
