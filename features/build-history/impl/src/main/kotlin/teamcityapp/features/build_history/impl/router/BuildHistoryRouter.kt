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

package teamcityapp.features.build_history.impl.router

import android.app.Activity
import dagger.hilt.android.scopes.ActivityScoped
import javax.inject.Inject
import teamcityapp.features.build_history.api.BuildHistoryAppRouter
import teamcityapp.features.build_history.impl.BuildHistoryActivity
import teamcityapp.features.filter_builds.api.navigation.FilterBuildsNavigation
import teamcityapp.features.run_build.api.navigation.RunBuildNavigation
import teamcityapp.libraries.builds.BuildLaunchData
import teamcityapp.libraries.utils.requireScreenOwner

interface BuildHistoryRouter {
    fun navigateUp()
    fun openBuild(build: BuildLaunchData, configurationName: String)
    fun openRunBuild(configurationId: String)
    fun openFilterBuilds(configurationId: String)
    fun openFavorites()
}

@ActivityScoped
class BuildHistoryActivityRouter @Inject constructor(
    owner: Activity,
    private val appRouter: BuildHistoryAppRouter,
    private val runBuild: RunBuildNavigation,
    private val filterBuilds: FilterBuildsNavigation
) : BuildHistoryRouter {
    private val activity = owner.requireScreenOwner<BuildHistoryActivity>()
    override fun navigateUp() = activity.finish()
    override fun openBuild(build: BuildLaunchData, configurationName: String) = appRouter.openBuild(activity, build, configurationName)
    override fun openRunBuild(configurationId: String) = runBuild.openForResult(activity, configurationId)
    override fun openFilterBuilds(configurationId: String) = filterBuilds.openForResult(activity, configurationId)
    override fun openFavorites() = appRouter.openFavorites(activity)
}
