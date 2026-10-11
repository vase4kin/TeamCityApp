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

package com.github.vase4kin.teamcityapp.snapshot_dependencies.router

import android.app.Activity
import com.github.vase4kin.teamcityapp.build_details.view.BuildDetailsActivity
import com.github.vase4kin.teamcityapp.builds.data.AppBuildLaunchMapper
import javax.inject.Inject
import teamcityapp.features.build_history.api.BuildHistoryNavigation
import teamcityapp.features.snapshot_dependencies.api.SnapshotDependenciesAppRouter
import teamcityapp.libraries.builds.BuildLaunchData

/** Bound in FragmentComponent so the bridge belongs to its visible UI owner. */
class AppSnapshotDependenciesRouter @Inject constructor(
    private val activity: Activity,
    private val mapper: AppBuildLaunchMapper,
    private val history: BuildHistoryNavigation
) : SnapshotDependenciesAppRouter {
    override fun openBuild(build: BuildLaunchData, buildTypeName: String?) {
        BuildDetailsActivity.startNotAsNewTask(activity, mapper.toLegacyBuild(build), buildTypeName)
    }

    override fun openBuildHistory(configurationId: String, configurationName: String) {
        history.open(activity, configurationId, configurationName)
    }
}
