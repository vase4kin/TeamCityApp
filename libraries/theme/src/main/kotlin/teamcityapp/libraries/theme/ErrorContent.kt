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

@Composable
fun ErrorContent(modifier: Modifier = Modifier, onRetry: () -> Unit, title: String? = null, message: String? = null, actionLabel: String? = null) = SharedErrorContent(modifier, onRetry, title, message, actionLabel)

@Composable
fun ErrorNotice(message: String, onRetry: (() -> Unit)? = null, modifier: Modifier = Modifier, actionLabel: String? = null, enabled: Boolean = true, onDismiss: (() -> Unit)? = null, tonal: Boolean = false, @DrawableRes leadingIcon: Int = R.drawable.ic_error_outline_24dp) = SharedErrorNotice(message, onRetry, modifier, actionLabel, enabled, onDismiss, tonal, painterResource(leadingIcon))
