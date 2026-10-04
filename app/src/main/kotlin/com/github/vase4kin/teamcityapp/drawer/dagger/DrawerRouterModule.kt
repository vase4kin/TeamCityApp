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

package com.github.vase4kin.teamcityapp.drawer.dagger
import com.github.vase4kin.teamcityapp.drawer.data.DrawerRepositoryImpl
import com.github.vase4kin.teamcityapp.drawer.router.DrawerAppRouterImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import teamcityapp.features.drawer.api.DrawerRepository
import teamcityapp.features.drawer.api.router.DrawerAppRouter
@Module
@InstallIn(SingletonComponent::class)
abstract class DrawerRouterModule {
    @Binds abstract fun router(impl: DrawerAppRouterImpl): DrawerAppRouter

    @Binds abstract fun repository(impl: DrawerRepositoryImpl): DrawerRepository
}
