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

package teamcityapp.features.tests.impl

import org.jetbrains.compose.resources.StringResource
import teamcityapp.features.tests.api.TestsCounts
import teamcityapp.features.tests.impl.resources.*
import teamcityapp.libraries.design_system.SharedUiText

sealed interface TestsCountState {
    val failureMessageRes: StringResource? get() = if (this == Unavailable) Res.string.tests_count_unavailable else null
    data object Loading : TestsCountState
    data class Available(val count: Int, val counts: TestsCounts? = null) : TestsCountState
    data object Unavailable : TestsCountState

    val totalLabel: SharedUiText get() = when (this) {
        Loading -> SharedUiText.Resource(Res.string.tests_total_loading)
        is Available -> SharedUiText.Resource(Res.string.tests_total, listOf(count))
        Unavailable -> SharedUiText.Resource(Res.string.tests_total_unavailable)
    }
}
