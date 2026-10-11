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

package com.github.vase4kin.teamcityapp.navigation.dagger

import com.github.vase4kin.teamcityapp.navigation.data.AppNavigationRatingRepository
import com.github.vase4kin.teamcityapp.navigation.data.AppNavigationRepository
import com.github.vase4kin.teamcityapp.navigation.router.NavigationAppRouterImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import teamcityapp.features.navigation.api.NavigationAppRouter
import teamcityapp.features.navigation.api.NavigationRatingRepository
import teamcityapp.features.navigation.api.NavigationRepository

@Module
@InstallIn(SingletonComponent::class)
abstract class NavigationAppModule {
    @Binds abstract fun repository(implementation: AppNavigationRepository): NavigationRepository

    @Binds abstract fun rating(implementation: AppNavigationRatingRepository): NavigationRatingRepository

    @Binds abstract fun router(implementation: NavigationAppRouterImpl): NavigationAppRouter
}
