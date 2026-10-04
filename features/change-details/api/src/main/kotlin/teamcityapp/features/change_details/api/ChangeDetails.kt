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

package teamcityapp.features.change_details.api

/** Plain change details passed by the changes list; no network or Android dependencies. */
data class ChangeDetails(
    val id: String,
    val comment: String,
    val userName: String,
    val date: String,
    val files: List<ChangedFile>,
    val revision: String,
    val webUrl: String,
)
data class ChangedFile(val name: String, val type: String)
