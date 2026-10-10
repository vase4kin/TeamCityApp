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

package teamcityapp.features.running_builds.api

enum class RunningBuildsFilter { All, Favorites }

/** One account/filter snapshot; adapters copy favorite IDs and use an empty list for All. */
data class RunningBuildsQuery(
    val accountKey: String,
    val filter: RunningBuildsFilter = RunningBuildsFilter.Favorites,
    val favoriteConfigurationIds: List<String> = emptyList()
)
