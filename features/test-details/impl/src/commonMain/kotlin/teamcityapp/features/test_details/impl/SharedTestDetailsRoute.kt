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

package teamcityapp.features.test_details.impl

import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.lifecycle.compose.*

@Composable
fun SharedTestDetailsRoute(vm: TestDetailsViewModel, onBack: () -> Unit, onCopy: (String) -> Unit) {
    LifecycleResumeEffect(vm) {
        vm.onScreenViewed()
        onPauseOrDispose {}
    }
    val state by vm.state.collectAsStateWithLifecycle()
    var query by rememberSaveable { mutableStateOf("") }
    var wrap by rememberSaveable { mutableStateOf(true) }
    var match by rememberSaveable { mutableIntStateOf(0) }
    TestDetailsScreen(state, vm::retry, onBack, query, wrap, match, {
        query = it
        match = 0
    }, { wrap = it }, { match = it }, onCopy = onCopy)
}
