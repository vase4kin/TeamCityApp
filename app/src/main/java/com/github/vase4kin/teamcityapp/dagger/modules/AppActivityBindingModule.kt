package com.github.vase4kin.teamcityapp.dagger.modules

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
}