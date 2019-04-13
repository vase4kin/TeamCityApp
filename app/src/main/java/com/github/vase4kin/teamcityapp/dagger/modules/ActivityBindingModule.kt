package com.github.vase4kin.teamcityapp.dagger.modules

import com.github.vase4kin.teamcityapp.about.AboutActivity
import com.github.vase4kin.teamcityapp.about.AboutLibrariesActivity
import com.github.vase4kin.teamcityapp.about.dagger.AboutActivityScope
import com.github.vase4kin.teamcityapp.about.dagger.AboutDrawerModule
import com.github.vase4kin.teamcityapp.about.dagger.AboutLibrariesDrawerModule
import com.github.vase4kin.teamcityapp.agenttabs.dagger.AgentsTabActivityScope
import com.github.vase4kin.teamcityapp.agenttabs.dagger.AgentsTabDrawerModule
import com.github.vase4kin.teamcityapp.agenttabs.dagger.AgentsTabsModule
import com.github.vase4kin.teamcityapp.agenttabs.view.AgentTabsActivity
import com.github.vase4kin.teamcityapp.favorites.dagger.FavoritesActivityScope
import com.github.vase4kin.teamcityapp.favorites.dagger.FavoritesDrawerModule
import com.github.vase4kin.teamcityapp.favorites.dagger.FavoritesModule
import com.github.vase4kin.teamcityapp.favorites.view.FavoritesActivity
import dagger.Module
import dagger.android.ContributesAndroidInjector

@Module
abstract class ActivityBindingModule {

    @FavoritesActivityScope
    @ContributesAndroidInjector(modules = [FavoritesModule::class, FavoritesDrawerModule::class])
    abstract fun favoritesActivity(): FavoritesActivity

    @AgentsTabActivityScope
    @ContributesAndroidInjector(modules = [AgentsTabsModule::class, AgentsTabDrawerModule::class])
    abstract fun agentTabsActivity(): AgentTabsActivity

    @AboutActivityScope
    @ContributesAndroidInjector(modules = [AboutDrawerModule::class])
    abstract fun aboutActivity(): AboutActivity

    @AboutActivityScope
    @ContributesAndroidInjector(modules = [AboutLibrariesDrawerModule::class])
    abstract fun aboutLibrariesActivity(): AboutLibrariesActivity
}