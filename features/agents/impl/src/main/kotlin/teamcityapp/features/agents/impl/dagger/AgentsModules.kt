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

package teamcityapp.features.agents.impl.dagger

import androidx.fragment.app.Fragment
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.FragmentComponent
import dagger.hilt.components.SingletonComponent
import teamcityapp.features.agents.api.AgentsAppRouter
import teamcityapp.features.agents.api.AgentsNavigation
import teamcityapp.features.agents.impl.AgentsFragment
import teamcityapp.features.agents.impl.navigation.AgentsNavigationImpl
import teamcityapp.features.agents.impl.router.AgentsRouter
import teamcityapp.features.agents.impl.router.AgentsRouterImpl
import teamcityapp.libraries.utils.requireScreenOwner

@Module
@InstallIn(SingletonComponent::class)
abstract class AgentsNavigationModule {
    @Binds abstract fun navigation(implementation: AgentsNavigationImpl): AgentsNavigation
}

@Module
@InstallIn(FragmentComponent::class)
object AgentsRouterModule {
    @Provides fun router(owner: Fragment, appRouter: AgentsAppRouter): AgentsRouter {
        owner.requireScreenOwner<AgentsFragment>()
        return AgentsRouterImpl(appRouter)
    }
}
