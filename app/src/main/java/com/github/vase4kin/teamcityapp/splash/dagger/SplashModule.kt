/*
 * Copyright 2016 Andrey Tolpeev
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.github.vase4kin.teamcityapp.splash.dagger

import com.github.vase4kin.teamcityapp.splash.data.SplashDataManager
import com.github.vase4kin.teamcityapp.splash.data.SplashDataManagerImpl
import com.github.vase4kin.teamcityapp.splash.router.SplashRouter
import com.github.vase4kin.teamcityapp.splash.router.SplashRouterImpl
import com.github.vase4kin.teamcityapp.splash.view.SplashView
import com.github.vase4kin.teamcityapp.splash.view.SplashViewImpl
import dagger.Binds
import dagger.Module

@Module
abstract class SplashModule {

    @Binds
    abstract fun providesSplashRouter(impl: SplashRouterImpl): SplashRouter

    @Binds
    abstract fun providesSplashDataManager(impl: SplashDataManagerImpl): SplashDataManager

    @Binds
    abstract fun providesSplashView(impl: SplashViewImpl): SplashView
}
