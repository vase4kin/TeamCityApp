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

package teamcityapp.features.run_build.impl.dagger
import android.app.Activity
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ActivityComponent
import dagger.hilt.components.SingletonComponent
import teamcityapp.features.run_build.api.navigation.RunBuildNavigation
import teamcityapp.features.run_build.impl.RunBuildActivity
import teamcityapp.features.run_build.impl.navigation.RunBuildNavigationImpl
import teamcityapp.features.run_build.impl.router.RunBuildRouter
import teamcityapp.features.run_build.impl.router.RunBuildRouterImpl
import teamcityapp.libraries.utils.requireScreenOwner

@Module
@InstallIn(SingletonComponent::class)
object RunBuildNavigationModule {
    @Provides fun navigation(): RunBuildNavigation = RunBuildNavigationImpl()
}

@Module
@InstallIn(ActivityComponent::class)
object RunBuildRouterModule {
    @Provides fun router(owner: Activity): RunBuildRouter = RunBuildRouterImpl(owner.requireScreenOwner<RunBuildActivity>())
}
