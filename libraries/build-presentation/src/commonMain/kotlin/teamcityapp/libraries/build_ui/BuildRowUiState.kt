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
import org.jetbrains.compose.resources.StringResource
import teamcityapp.libraries.build_presentation.resources.*
import teamcityapp.libraries.builds.BuildLaunchData
import teamcityapp.libraries.design_system.SharedUiText

/** Shared row text is selected before rendering; the complete launch payload stays available. */
data class BuildRowUiState(val build: BuildLaunchData) {
    val statusLabel: StringResource = when {
        build.isRunning -> Res.string.build_running
        build.isQueued -> Res.string.build_queued
        build.isFailed -> Res.string.build_failed
        build.status == "ERROR" -> Res.string.build_error
        build.status == "UNKNOWN" -> Res.string.build_unknown
        else -> Res.string.build_success
    }
    val statusText: SharedUiText = if (build.isQueued) build.waitReason?.let(SharedUiText::Dynamic) ?: SharedUiText.Resource(Res.string.build_queued_fallback) else SharedUiText.Dynamic(build.statusText.orEmpty())
    val number: SharedUiText = build.number?.let(SharedUiText::Dynamic) ?: SharedUiText.Resource(Res.string.build_no_number)
}
