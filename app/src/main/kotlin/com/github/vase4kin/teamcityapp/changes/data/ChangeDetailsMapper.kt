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

package com.github.vase4kin.teamcityapp.changes.data

import com.github.vase4kin.teamcityapp.changes.api.Changes
import teamcityapp.features.change_details.api.ChangeDetails
import teamcityapp.features.change_details.api.ChangedFile

/** Keep the legacy network DTO and its date formatting behind the feature's plain model. */
internal fun Changes.Change.toChangeDetails() = ChangeDetails(
    id = getId().orEmpty(),
    comment = comment.orEmpty(),
    userName = username.orEmpty(),
    date = date.orEmpty(),
    files = files?.file.orEmpty().map { ChangedFile(it.file.orEmpty(), it.changeType.orEmpty()) },
    revision = version.orEmpty(),
    webUrl = webUrl
)
