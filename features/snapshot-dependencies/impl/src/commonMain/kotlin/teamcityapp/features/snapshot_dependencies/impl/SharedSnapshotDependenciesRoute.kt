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

package teamcityapp.features.snapshot_dependencies.impl

import androidx.compose.runtime.*
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.*
import teamcityapp.features.snapshot_dependencies.impl.tracker.SnapshotDependenciesTracker
import teamcityapp.libraries.builds.BuildLaunchData

@Composable
fun SharedSnapshotDependenciesRoute(vm: SnapshotDependenciesViewModel, tracker: SnapshotDependenciesTracker, onBuild: (BuildLaunchData) -> Unit, onConfiguration: (String, String) -> Unit) {
    LifecycleResumeEffect(vm) {
        tracker.viewShown()
        vm.onResumed()
        onPauseOrDispose { vm.onPaused() }
    }
    val state by vm.state.collectAsStateWithLifecycle(minActiveState = Lifecycle.State.RESUMED)
    SnapshotDependenciesScreen(state, vm::refresh, vm::retry, onBuild, onConfiguration)
}
