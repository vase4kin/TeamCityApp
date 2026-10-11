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

package teamcityapp.features.changes.api

import teamcityapp.features.change_details.api.ChangeDetails

/** Fully hydrated changes and the server's opaque continuation token. */
data class ChangesPage(val items: List<ChangeDetails>, val nextHref: String?)

interface ChangesRepository {
    suspend fun changes(url: String, nextHref: String?, forceRefresh: Boolean): ChangesPage
    suspend fun count(url: String): Int
}
