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

import android.text.Spanned
import android.text.style.URLSpan
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.autofill.ContentDataType
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalResources
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDataType
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import teamcityapp.libraries.theme.ErrorNotice
import teamcityapp.libraries.theme.TeamCityDimensions
import teamcityapp.libraries.theme.TeamCityLoadingIndicator
import teamcityapp.libraries.theme.TeamCitySwitch
import teamcityapp.libraries.theme.TeamCityTheme
import teamcityapp.libraries.theme.UiText
import teamcityapp.libraries.theme.resolve

@Composable
fun AuthenticationForm(state: AuthenticationFormState, onChange: (AuthenticationFormState) -> Unit, onSslChange: (Boolean) -> Unit, onSubmit: () -> Unit, modifier: Modifier = Modifier, spaced: Boolean = false, errorMessage: UiText? = state.errorMessage, horizontalPadding: Dp = TeamCityDimensions.noSpacing) = SharedAuthenticationForm(state, onChange, onSslChange, onSubmit, modifier, spaced, errorMessage?.resolve(), horizontalPadding)

@Composable
fun AuthenticationWarning(message: AuthenticationWarningText = AuthenticationWarningText.Ssl, onAccept: () -> Unit, onDecline: () -> Unit) = SharedAuthenticationWarning(message, onAccept, onDecline)

@Composable
fun styledAuthenticationText(text: CharSequence) = run {
    val color = MaterialTheme.colorScheme.primary
    remember(text, color) {
        buildAnnotatedString {
            append(text.toString())
            if (text is Spanned) {
                text.getSpans(0, text.length, URLSpan::class.java).forEach { span ->
                    addStyle(SpanStyle(color = color, textDecoration = TextDecoration.Underline), text.getSpanStart(span), text.getSpanEnd(span))
                }
            }
        }
    }
}
