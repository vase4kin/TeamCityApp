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

package teamcityapp.features.build_log.impl.dagger
import androidx.fragment.app.Fragment
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.FragmentComponent
import dagger.hilt.components.SingletonComponent
import teamcityapp.features.build_log.api.*
import teamcityapp.features.build_log.impl.BuildLogFragment
import teamcityapp.features.build_log.impl.navigation.BuildLogNavigationImpl
import teamcityapp.features.build_log.impl.router.BuildLogRouter
import teamcityapp.features.build_log.impl.router.BuildLogRouterImpl
import teamcityapp.libraries.utils.requireScreenOwner
@Module
@InstallIn(SingletonComponent::class)
object BuildLogNavigationModule {
    @Provides fun navigation(): BuildLogNavigation = BuildLogNavigationImpl()
}

@Module
@InstallIn(FragmentComponent::class)
object BuildLogRouterModule {
    @Provides fun router(owner: Fragment): BuildLogRouter = BuildLogRouterImpl(owner.requireScreenOwner<BuildLogFragment>().requireActivity())
}
