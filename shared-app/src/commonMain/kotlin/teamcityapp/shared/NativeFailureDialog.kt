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

import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import org.jetbrains.compose.resources.stringResource
import teamcityapp.shared.resources.*

@Composable
internal fun NativeFailureDialog(failure: NativePlatformFailure, onDismiss: () -> Unit) {
    AlertDialog(onDismissRequest = onDismiss, title = { Text(stringResource(Res.string.platform_action_failed)) }, text = { Text(stringResource(failure.message())) }, confirmButton = { TextButton(onDismiss) { Text(stringResource(Res.string.platform_ok)) } })
}
