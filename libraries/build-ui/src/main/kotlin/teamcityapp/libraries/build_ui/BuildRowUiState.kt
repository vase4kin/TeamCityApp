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

package teamcityapp.libraries.build_ui

import androidx.annotation.StringRes
import teamcityapp.libraries.builds.BuildLaunchData
import teamcityapp.libraries.theme.UiText

/** Shared row text is selected before rendering; the complete launch payload stays available. */
data class BuildRowUiState(val build: BuildLaunchData) {
    @get:StringRes val statusLabelRes: Int = when {
        build.isRunning -> R.string.build_running
        build.isQueued -> R.string.build_queued
        build.isFailed -> R.string.build_failed
        build.status == "ERROR" -> R.string.build_error
        build.status == "UNKNOWN" -> R.string.build_unknown
        else -> R.string.build_success
    }
    val statusText: UiText = if (build.isQueued) build.waitReason?.let(UiText::Dynamic) ?: UiText.Resource(R.string.build_queued_fallback) else UiText.Dynamic(build.statusText.orEmpty())
    val number: UiText = build.number?.let(UiText::Dynamic) ?: UiText.Resource(R.string.build_no_number)
}
