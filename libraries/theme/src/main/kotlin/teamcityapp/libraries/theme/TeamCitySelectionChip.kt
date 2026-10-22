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
import androidx.annotation.DrawableRes
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
@Composable
fun TeamCitySelectionChip(selected: Boolean, label: String, @DrawableRes iconRes: Int, onClick: () -> Unit, modifier: Modifier = Modifier, enabled: Boolean = true) = SharedTeamCitySelectionChip(selected, label, painterResource(iconRes), onClick, modifier, enabled)

@Preview
@Composable
private fun SelectionChipPreview() {
    TeamCityTheme { TeamCitySelectionChip(true, "Selected", R.drawable.ic_info_outline_black_24dp, {}) }
}
