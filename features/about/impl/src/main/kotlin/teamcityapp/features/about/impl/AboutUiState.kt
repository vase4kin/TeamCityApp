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

package teamcityapp.features.about.impl

import teamcityapp.features.about.api.AboutServerInfo

sealed interface AboutUiState {
    data object Loading : AboutUiState
    data class Content(val serverDetails: ServerDetailsUiState) : AboutUiState
}

/** Failure is explicit in state while the rest of About stays usable. */
sealed interface ServerDetailsUiState {
    data class Available(val info: ServerDetailsUiModel) : ServerDetailsUiState
    data object Unavailable : ServerDetailsUiState
}

data class ServerDetailsUiModel(val version: String, val webUrl: String)

internal fun AboutServerInfo.toUiModel() = ServerDetailsUiModel(version, webUrl)
