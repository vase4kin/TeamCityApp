package com.github.vase4kin.teamcityapp.dagger.modules

import com.github.vase4kin.teamcityapp.about.AboutActivity
import com.github.vase4kin.teamcityapp.about.AboutLibrariesActivity
import com.github.vase4kin.teamcityapp.about.dagger.AboutActivityScope
import com.github.vase4kin.teamcityapp.about.dagger.AboutDrawerModule
import com.github.vase4kin.teamcityapp.about.dagger.AboutLibrariesDrawerModule
import com.github.vase4kin.teamcityapp.account.create.dagger.CreateAccountActivityScope
import com.github.vase4kin.teamcityapp.account.create.dagger.CreateAccountModule
import com.github.vase4kin.teamcityapp.account.create.view.CreateAccountActivity
import com.github.vase4kin.teamcityapp.account.manage.dagger.AccountListActivityScope
import com.github.vase4kin.teamcityapp.account.manage.dagger.AccountListDrawerModule
import com.github.vase4kin.teamcityapp.account.manage.dagger.AccountsModule
import com.github.vase4kin.teamcityapp.account.manage.view.AccountListActivity
import com.github.vase4kin.teamcityapp.agenttabs.dagger.AgentsTabActivityScope
import com.github.vase4kin.teamcityapp.agenttabs.dagger.AgentsTabDrawerModule
import com.github.vase4kin.teamcityapp.agenttabs.dagger.AgentsTabFragmentsBindingModule
import com.github.vase4kin.teamcityapp.agenttabs.dagger.AgentsTabsModule
import com.github.vase4kin.teamcityapp.agenttabs.view.AgentTabsActivity
import com.github.vase4kin.teamcityapp.build_details.dagger.BuildDetailsActivityScope
import com.github.vase4kin.teamcityapp.build_details.dagger.BuildDetailsDrawerModule
import com.github.vase4kin.teamcityapp.build_details.dagger.BuildDetailsFragmentsBindingModule
import com.github.vase4kin.teamcityapp.build_details.dagger.BuildDetailsModule
import com.github.vase4kin.teamcityapp.build_details.view.BuildDetailsActivity
import com.github.vase4kin.teamcityapp.buildlist.dagger.BuildListActivityScope
import com.github.vase4kin.teamcityapp.buildlist.dagger.BuildListAdapterModule
import com.github.vase4kin.teamcityapp.buildlist.dagger.BuildListDrawerModule
import com.github.vase4kin.teamcityapp.buildlist.dagger.BuildListModule
import com.github.vase4kin.teamcityapp.buildlist.view.BuildListActivity
import com.github.vase4kin.teamcityapp.favorites.dagger.FavoritesActivityScope
import com.github.vase4kin.teamcityapp.favorites.dagger.FavoritesDrawerModule
import com.github.vase4kin.teamcityapp.favorites.dagger.FavoritesModule
import com.github.vase4kin.teamcityapp.favorites.view.FavoritesActivity
import com.github.vase4kin.teamcityapp.queue.dagger.BuildQueueActivityScope
import com.github.vase4kin.teamcityapp.queue.dagger.BuildQueueDrawerModule
import com.github.vase4kin.teamcityapp.queue.dagger.BuildQueueModule
import com.github.vase4kin.teamcityapp.queue.view.BuildQueueActivity
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
    @ContributesAndroidInjector(modules = [AgentsTabsModule::class, AgentsTabDrawerModule::class, AgentsTabFragmentsBindingModule::class])
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

    @BuildQueueActivityScope
    @ContributesAndroidInjector(modules = [BuildQueueModule::class, BuildQueueDrawerModule::class, BuildListAdapterModule::class])
    abstract fun buildQueueActivity(): BuildQueueActivity

    @CreateAccountActivityScope
    @ContributesAndroidInjector(modules = [CreateAccountModule::class])
    abstract fun createAccountActivity(): CreateAccountActivity

    @AccountListActivityScope
    @ContributesAndroidInjector(modules = [AccountsModule::class, AccountListDrawerModule::class])
    abstract fun accountListActivity(): AccountListActivity

    @BuildDetailsActivityScope
    @ContributesAndroidInjector(modules = [BuildDetailsModule::class, BuildDetailsDrawerModule::class, BuildDetailsFragmentsBindingModule::class])
    abstract fun buildDetailsActivity(): BuildDetailsActivity

    @BuildListActivityScope
    @ContributesAndroidInjector(modules = [BuildListModule::class, BuildListDrawerModule::class, BuildListAdapterModule::class, BuildDetailsFragmentsBindingModule::class])
    abstract fun buildListActivity(): BuildListActivity
}