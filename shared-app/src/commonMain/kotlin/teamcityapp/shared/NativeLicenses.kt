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

package teamcityapp.shared

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable
import org.jetbrains.compose.resources.stringResource
import teamcityapp.libraries.theme.*
import teamcityapp.shared.resources.*

@Serializable internal data class NativeLicenseDestination(val id: String) : NavKey
internal data class NativeLicenseEntry(val id: String, val name: String, val source: String)
internal val nativeLicenses = listOf(
    NativeLicenseEntry("teamcityapp", "TeamCityApp — Apache 2.0", "https://github.com/vase4kin/TeamCityApp/blob/dev/LICENSE.txt"),
    NativeLicenseEntry("kotlin", "Kotlin — Apache 2.0", "https://github.com/JetBrains/kotlin/blob/master/license/LICENSE.txt"),
    NativeLicenseEntry("coroutines", "kotlinx.coroutines — Apache 2.0", "https://github.com/Kotlin/kotlinx.coroutines/blob/master/LICENSE.txt"),
    NativeLicenseEntry("serialization", "kotlinx.serialization — Apache 2.0", "https://github.com/Kotlin/kotlinx.serialization/blob/master/LICENSE.txt"),
    NativeLicenseEntry("compose", "Compose Multiplatform — Apache 2.0", "https://github.com/JetBrains/compose-multiplatform/blob/master/LICENSE.txt"),
    NativeLicenseEntry("androidx", "AndroidX — Apache 2.0", "https://github.com/androidx/androidx/blob/androidx-main/LICENSE.txt"),
    NativeLicenseEntry("ktor", "Ktor — Apache 2.0", "https://github.com/ktorio/ktor/blob/main/LICENSE"),
    NativeLicenseEntry("skiko", "Skiko — Apache 2.0", "https://github.com/JetBrains/skiko/blob/master/LICENSE"),
    NativeLicenseEntry("skia", "Skia — BSD", "https://skia.googlesource.com/skia/+/main/LICENSE")
)
internal sealed interface NativeLicenseText {
    data object Loading : NativeLicenseText
    data object Error : NativeLicenseText
    data class Content(val value: String) : NativeLicenseText
}

@Composable
internal fun NativeLicensesList(onOpen: (String) -> Unit, onBack: () -> Unit) {
    SharedTeamCityScreen(stringResource(Res.string.platform_licenses), onBack) { modifier ->
        Box(modifier.fillMaxSize(), contentAlignment = Alignment.TopCenter) {
            LazyColumn(Modifier.widthIn(max = TeamCityDimensions.screenContentMaxWidth).fillMaxSize().testTag("licenses:list"), contentPadding = PaddingValues(TeamCityDimensions.contentPadding)) {
                item { Text(stringResource(Res.string.platform_licenses_description), style = MaterialTheme.typography.bodyLarge) }
                nativeLicenses.forEach { entry -> item { TextButton({ onOpen(entry.id) }, Modifier.fillMaxWidth().testTag("licenses:${entry.id}")) { Text(entry.name) } } }
            }
        }
    }
}

@Composable
internal fun NativeLicenseScreen(title: String, state: NativeLicenseText, onRetry: () -> Unit, onSource: () -> Unit, onBack: () -> Unit) {
    SharedTeamCityScreen(title, onBack) { modifier ->
        when (state) {
            NativeLicenseText.Loading -> LoadingContent(modifier)

            NativeLicenseText.Error -> SharedErrorContent(modifier, onRetry, message = stringResource(Res.string.platform_license_read_error))

            is NativeLicenseText.Content -> Box(modifier.fillMaxSize(), contentAlignment = Alignment.TopCenter) {
                Column(Modifier.widthIn(max = TeamCityDimensions.screenContentMaxWidth).fillMaxSize().padding(TeamCityDimensions.contentPadding)) {
                    SelectionContainer(Modifier.weight(1f).verticalScroll(rememberScrollState())) { Text(state.value, Modifier.testTag("license:text"), style = MaterialTheme.typography.bodyMedium) }
                    TextButton(onSource, Modifier.fillMaxWidth().testTag("license:source")) { Text(stringResource(Res.string.platform_license_source)) }
                }
            }
        }
    }
}

@Composable
internal fun NativeFailureDialog(failure: NativePlatformFailure, onDismiss: () -> Unit) {
    AlertDialog(onDismissRequest = onDismiss, title = { Text(stringResource(Res.string.platform_action_failed)) }, text = { Text(stringResource(failure.message())) }, confirmButton = { TextButton(onDismiss) { Text(stringResource(Res.string.platform_ok)) } })
}
