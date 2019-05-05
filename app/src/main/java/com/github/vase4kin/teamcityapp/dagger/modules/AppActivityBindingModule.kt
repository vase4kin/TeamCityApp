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