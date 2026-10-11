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

package com.github.vase4kin.teamcityapp.favorites.dagger

import com.github.vase4kin.teamcityapp.favorites.data.AppFavoritesRepository
import com.github.vase4kin.teamcityapp.favorites.router.FavoritesAppRouterImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import teamcityapp.features.favorites.api.FavoritesAppRouter
import teamcityapp.features.favorites.api.FavoritesRepository

@Module
@InstallIn(SingletonComponent::class)
abstract class FavoritesAppModule {
    @Binds abstract fun repository(implementation: AppFavoritesRepository): FavoritesRepository

    @Binds abstract fun router(implementation: FavoritesAppRouterImpl): FavoritesAppRouter
}
