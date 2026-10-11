/*
 * Copyright 2019 Andrey Tolpeev
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

package teamcityapp.features.snapshot_dependencies.impl.router

import teamcityapp.features.snapshot_dependencies.api.SnapshotDependenciesAppRouter
import teamcityapp.libraries.builds.BuildLaunchData

class SnapshotDependenciesRouterImpl(private val appRouter: SnapshotDependenciesAppRouter) : SnapshotDependenciesRouter {
    override fun openBuild(build: BuildLaunchData, buildTypeName: String?) = appRouter.openBuild(build, buildTypeName)
    override fun openBuildHistory(configurationId: String, configurationName: String) = appRouter.openBuildHistory(configurationId, configurationName)
}
