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

package teamcityapp.features.running_builds.impl.dagger

import androidx.fragment.app.Fragment
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.FragmentComponent
import dagger.hilt.components.SingletonComponent
import teamcityapp.features.running_builds.api.RunningBuildsAppRouter
import teamcityapp.features.running_builds.api.RunningBuildsNavigation
import teamcityapp.features.running_builds.impl.RunningBuildsFragment
import teamcityapp.features.running_builds.impl.navigation.RunningBuildsNavigationImpl
import teamcityapp.features.running_builds.impl.router.RunningBuildsRouter
import teamcityapp.features.running_builds.impl.router.RunningBuildsRouterImpl
import teamcityapp.libraries.utils.requireScreenOwner

@Module
@InstallIn(SingletonComponent::class)
abstract class RunningBuildsNavigationModule {
    @Binds abstract fun navigation(implementation: RunningBuildsNavigationImpl): RunningBuildsNavigation
}

@Module
@InstallIn(FragmentComponent::class)
object RunningBuildsRouterModule {
    @Provides fun router(owner: Fragment, appRouter: RunningBuildsAppRouter): RunningBuildsRouter {
        owner.requireScreenOwner<RunningBuildsFragment>()
        return RunningBuildsRouterImpl(appRouter)
    }
}
