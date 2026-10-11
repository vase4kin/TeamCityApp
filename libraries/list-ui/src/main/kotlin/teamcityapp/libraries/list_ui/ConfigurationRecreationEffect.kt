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

package teamcityapp.libraries.list_ui

import androidx.activity.compose.LocalActivity
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.rememberUpdatedState

/** Distinguishes a recreated visible route from a genuine return that should refresh its list. */
@Composable
fun ConfigurationRecreationEffect(owner: Any, onRecreation: () -> Unit) {
    val activity = LocalActivity.current
    val callback = rememberUpdatedState(onRecreation)
    DisposableEffect(owner, activity) {
        onDispose {
            if (activity?.isChangingConfigurations == true) callback.value()
        }
    }
}
