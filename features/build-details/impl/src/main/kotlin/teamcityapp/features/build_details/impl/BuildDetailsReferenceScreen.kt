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

package teamcityapp.features.build_details.impl

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import teamcityapp.libraries.theme.*

@Composable
internal fun BuildDetailsReferenceScreen(state: BuildDetailsReferenceState, onUp: () -> Unit, onRetry: () -> Unit) {
    TeamCityScreen(stringResource(R.string.build_details_title), onUp, navigation = ScreenNavigation.Back) { modifier ->
        when (state) {
            BuildDetailsReferenceState.Loading -> LoadingContent(modifier.fillMaxSize().testTag("details:reference-loading"))
            BuildDetailsReferenceState.Error -> ErrorContent(modifier.fillMaxSize().testTag("details:reference-error"), onRetry, message = stringResource(BuildDetailsReferenceState.Error.messageRes), actionLabel = stringResource(BuildDetailsReferenceState.Error.retryLabelRes))
            is BuildDetailsReferenceState.Content -> LoadingContent(modifier.fillMaxSize())
        }
    }
}
