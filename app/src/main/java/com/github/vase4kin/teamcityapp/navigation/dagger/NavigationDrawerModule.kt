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

package com.github.vase4kin.teamcityapp.navigation.dagger

import com.github.vase4kin.teamcityapp.drawer.dagger.BaseDrawerModule
import com.github.vase4kin.teamcityapp.drawer.router.DrawerRouter
import com.github.vase4kin.teamcityapp.drawer.router.DrawerRouterImpl
import com.github.vase4kin.teamcityapp.drawer.view.CustomAnimationDrawerViewImpl
import com.github.vase4kin.teamcityapp.drawer.view.DrawerView
import com.github.vase4kin.teamcityapp.navigation.view.NavigationActivity
import dagger.Module
import dagger.Provides

@Module(includes = [BaseDrawerModule::class])
class NavigationDrawerModule {

    @Provides
    fun providesDrawerView(activity: NavigationActivity): DrawerView {
        return CustomAnimationDrawerViewImpl(activity, DrawerView.NO_SELECTION, true)
    }

    @Provides
    fun providesDrawerRouter(activity: NavigationActivity): DrawerRouter {
        return DrawerRouterImpl(activity)
    }
}
