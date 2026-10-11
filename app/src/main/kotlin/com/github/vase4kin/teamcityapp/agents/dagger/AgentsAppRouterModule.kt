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

package com.github.vase4kin.teamcityapp.agents.dagger

import com.github.vase4kin.teamcityapp.agents.router.AgentsAppRouterImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.android.components.FragmentComponent
import teamcityapp.features.agents.api.AgentsAppRouter

@Module
@InstallIn(FragmentComponent::class)
abstract class AgentsAppRouterModule {
    @Binds abstract fun router(implementation: AgentsAppRouterImpl): AgentsAppRouter
}
