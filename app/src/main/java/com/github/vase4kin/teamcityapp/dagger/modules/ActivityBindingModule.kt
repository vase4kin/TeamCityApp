package com.github.vase4kin.teamcityapp.dagger.modules

import com.github.vase4kin.teamcityapp.dagger.scopes.PresenterScope
import com.github.vase4kin.teamcityapp.favorites.dagger.FavoritesDrawerModule
import com.github.vase4kin.teamcityapp.favorites.dagger.FavoritesModule
import com.github.vase4kin.teamcityapp.favorites.view.FavoritesActivity
import dagger.Module
import dagger.android.ContributesAndroidInjector

@Module
abstract class ActivityBindingModule {

    @PresenterScope
    @ContributesAndroidInjector(modules = [FavoritesModule::class, FavoritesDrawerModule::class])
    abstract fun favoritesActivity(): FavoritesActivity
}