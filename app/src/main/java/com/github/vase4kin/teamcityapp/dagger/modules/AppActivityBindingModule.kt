/*
 * Copyright 2019 Andrey Tolpeev
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

package com.github.vase4kin.teamcityapp.dagger.modules

import com.github.vase4kin.teamcityapp.account.create.dagger.UrlFormatterModule
import com.github.vase4kin.teamcityapp.login.dagger.LoginActivityScope
import com.github.vase4kin.teamcityapp.login.dagger.LoginModule
import com.github.vase4kin.teamcityapp.login.view.LoginActivity
import com.github.vase4kin.teamcityapp.splash.dagger.SplashActivityScope
import com.github.vase4kin.teamcityapp.splash.dagger.SplashModule
import com.github.vase4kin.teamcityapp.splash.view.SplashActivity
import dagger.Module
import dagger.android.ContributesAndroidInjector

@Module
abstract class AppActivityBindingModule {

    @SplashActivityScope
    @ContributesAndroidInjector(modules = [SplashModule::class])
    abstract fun splashActivity(): SplashActivity

    @LoginActivityScope
    @ContributesAndroidInjector(modules = [LoginModule::class, UrlFormatterModule::class])
    abstract fun loginActivity(): LoginActivity
}
