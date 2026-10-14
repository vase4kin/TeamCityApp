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

package teamcityapp.features.changes.impl

import androidx.annotation.StringRes

internal sealed interface ChangesCountState {
    @get:StringRes val failureMessageRes: Int? get() = if (this == Unavailable) R.string.changes_count_unavailable else null
    data object Loading : ChangesCountState
    data class Available(val count: Int) : ChangesCountState
    data object Unavailable : ChangesCountState
}
