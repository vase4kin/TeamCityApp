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

package teamcityapp.libraries.authentication

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import teamcityapp.libraries.theme.TeamCityDimensions
import teamcityapp.libraries.theme.UiText
import teamcityapp.libraries.theme.resolve

@Composable
fun AuthenticationForm(state: AuthenticationFormState, onChange: (AuthenticationFormState) -> Unit, onSslChange: (Boolean) -> Unit, onSubmit: () -> Unit, modifier: Modifier = Modifier, spaced: Boolean = false, errorMessage: UiText? = state.errorMessage, horizontalPadding: Dp = TeamCityDimensions.noSpacing) = SharedAuthenticationForm(state, onChange, onSslChange, onSubmit, modifier, spaced, errorMessage?.resolve(), horizontalPadding)
