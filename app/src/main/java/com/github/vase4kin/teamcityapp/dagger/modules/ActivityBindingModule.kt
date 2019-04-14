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
import com.github.vase4kin.teamcityapp.buildlist.dagger.BuildListAdapterModule
import com.github.vase4kin.teamcityapp.favorites.dagger.FavoritesActivityScope
import com.github.vase4kin.teamcityapp.favorites.dagger.FavoritesDrawerModule
import com.github.vase4kin.teamcityapp.favorites.dagger.FavoritesModule
import com.github.vase4kin.teamcityapp.favorites.view.FavoritesActivity
import com.github.vase4kin.teamcityapp.runningbuilds.dagger.RunningBuildsActivityScope
import com.github.vase4kin.teamcityapp.runningbuilds.dagger.RunningBuildsDrawerModule
import com.github.vase4kin.teamcityapp.runningbuilds.dagger.RunningBuildsModule
import com.github.vase4kin.teamcityapp.runningbuilds.view.RunningBuildsListActivity
import com.github.vase4kin.teamcityapp.testdetails.dagger.TestDetailsActivityScope
import com.github.vase4kin.teamcityapp.testdetails.dagger.TestDetailsModule
import com.github.vase4kin.teamcityapp.testdetails.view.TestDetailsActivity
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

    @TestDetailsActivityScope
    @ContributesAndroidInjector(modules = [TestDetailsModule::class])
    abstract fun testDetailsActivity(): TestDetailsActivity

    @RunningBuildsActivityScope
    @ContributesAndroidInjector(modules = [RunningBuildsModule::class, RunningBuildsDrawerModule::class, BuildListAdapterModule::class])
    abstract fun runningBuildsListActivity(): RunningBuildsListActivity
}