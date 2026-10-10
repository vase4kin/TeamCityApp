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

import androidx.compose.foundation.background
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun BranchField(branches: List<String>?, failed: Boolean, value: String, onChange: (String) -> Unit, title: String, loading: String, empty: String, hint: String, filter: Boolean = false, enabled: Boolean = true) {
    var expanded by remember { mutableStateOf(false) }
    Column(Modifier.fillMaxWidth().padding(12.dp)) {
        Text(title, Modifier.padding(start = 4.dp), style = MaterialTheme.typography.bodyLarge)
        when {
            branches == null -> Row(Modifier.padding(start = 4.dp, top = 4.dp), verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
                Text(loading, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                CircularProgressIndicator(Modifier.padding(start = 16.dp).size(20.dp), strokeWidth = 2.dp)
            }

            failed || branches.isEmpty() || (filter && branches.size <= 1) -> Text(empty, Modifier.padding(start = 4.dp, top = 4.dp), style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)

            else -> Box {
                val focus = LocalFocusManager.current
                val active = enabled && branches.size > 1
                OutlinedTextField(
                    value = value,
                    onValueChange = {
                        onChange(it)
                        expanded = it.length >= 2
                    },
                    modifier = Modifier.fillMaxWidth().testTag("branches:input"),
                    enabled = active,
                    singleLine = true,
                    placeholder = { Text(hint) },
                    shape = MaterialTheme.shapes.medium,
                    keyboardOptions = KeyboardOptions(autoCorrectEnabled = false, imeAction = ImeAction.Done),
                    keyboardActions = KeyboardActions(onDone = {
                        focus.clearFocus()
                        expanded = false
                    })
                )
                DropdownMenu(expanded && enabled, { expanded = false }) {
                    branches.filter { it.contains(value, ignoreCase = true) }.forEach { branch ->
                        DropdownMenuItem(text = { Text(branch) }, onClick = {
                            onChange(branch)
                            focus.clearFocus()
                            expanded = false
                        })
                    }
                }
            }
        }
    }
}

@Preview @Composable
private fun BranchPreview() {
    TeamCityTheme { BranchField(listOf("main", "release"), false, "", {}, "Build branch", "Loading branches…", "No branches available", "Default branch") }
}
