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

package com.github.vase4kin.teamcityapp.navigation

import com.github.vase4kin.teamcityapp.buildlog.data.AppBuildLogConsentStorage
import com.github.vase4kin.teamcityapp.home.data.AppHomeRepository
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ActivityRetainedComponent
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import teamcityapp.features.build_details.api.BuildDetailsNavigation
import teamcityapp.features.build_log.api.BuildLogConsentStorage
import teamcityapp.features.home.api.HomeRepository
import teamcityapp.libraries.coroutines.MainDispatcher
import teamcityapp.libraries.navigation.AccountFlowNavigation

@Module
@InstallIn(SingletonComponent::class)
abstract class ComposeShellModule {
    @Binds abstract fun preparation(implementation: AppAccountSessionPreparation): AccountSessionPreparation

    @Binds abstract fun accounts(implementation: AppAccountFlowNavigation): AccountFlowNavigation

    @Binds abstract fun consent(implementation: AppBuildLogConsentStorage): BuildLogConsentStorage

    @Binds abstract fun builds(implementation: LegacyBuildDetailsNavigation): BuildDetailsNavigation
    companion object {
        @Provides @Singleton @AccountNavigationScope
        fun accountScope(@MainDispatcher main: CoroutineDispatcher): CoroutineScope = CoroutineScope(SupervisorJob() + main)
    }
}

@Module
@InstallIn(ActivityRetainedComponent::class)
abstract class ComposeHomeDataModule {
    @Binds abstract fun home(implementation: AppHomeRepository): HomeRepository
}
