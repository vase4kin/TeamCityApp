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

package teamcityapp.features.snapshot_dependencies.impl.navigation

import android.os.Bundle
import androidx.fragment.app.Fragment
import javax.inject.Inject
import teamcityapp.features.snapshot_dependencies.api.SnapshotDependenciesNavigation
import teamcityapp.features.snapshot_dependencies.impl.SnapshotDependenciesFragment

class SnapshotDependenciesNavigationImpl @Inject constructor() : SnapshotDependenciesNavigation {
    override fun createFragment(buildId: String, buildTypeName: String?): Fragment = SnapshotDependenciesFragment().apply {
        require(buildId.isNotBlank()) { "Snapshot dependencies require a build ID" }
        arguments = Bundle().apply {
            putString(SnapshotDependenciesFragment.BUILD_ID, buildId)
            if (buildTypeName != null) putString(SnapshotDependenciesFragment.BUILD_TYPE_NAME, buildTypeName)
        }
    }
}
