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

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.LinearWavyProgressIndicator
import androidx.compose.material3.LoadingIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview

/** Shared expressive feedback for indeterminate work, including running builds. */
@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun TeamCityLoadingIndicator(modifier: Modifier = Modifier, color: Color = MaterialTheme.colorScheme.primary) {
    LoadingIndicator(modifier.size(TeamCityDimensions.modalProgressIndicatorSize), color = color)
}

/** Horizontal expressive feedback for operations that retain their surrounding content. */
@Composable
fun TeamCityLinearLoadingIndicator(modifier: Modifier = Modifier) {
    LinearWavyProgressIndicator(modifier.fillMaxWidth())
}

@Preview
@Composable
private fun LoadingIndicatorsPreview() {
    TeamCityTheme {
        Column {
            TeamCityLoadingIndicator()
            TeamCityLoadingIndicator(Modifier.size(TeamCityDimensions.compactProgressIndicatorSize))
            TeamCityLinearLoadingIndicator()
        }
    }
}
