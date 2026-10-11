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

package teamcityapp.libraries.theme

import androidx.annotation.StringRes
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource

/** Unresolved UI text. ViewModels select content; the current composition localizes it. */
sealed interface UiText {
    data class Resource(@get:StringRes val id: Int, val arguments: List<Any> = emptyList()) : UiText
    data class Dynamic(val value: String) : UiText
}

@Composable
fun UiText.resolve(): String = when (this) {
    is UiText.Resource -> stringResource(id, *arguments.toTypedArray())
    is UiText.Dynamic -> value
}
