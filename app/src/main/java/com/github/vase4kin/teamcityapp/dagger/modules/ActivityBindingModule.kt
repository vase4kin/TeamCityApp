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
import com.github.vase4kin.teamcityapp.buildlog.dagger.BuildLogInteractorModule
import com.github.vase4kin.teamcityapp.favorites.dagger.FavoritesFragmentModule
import com.github.vase4kin.teamcityapp.favorites.dagger.FavoritesFragmentScope
import com.github.vase4kin.teamcityapp.favorites.view.FavoritesFragment
import com.github.vase4kin.teamcityapp.filter_builds.dagger.FilterBuildsActivityScope
import com.github.vase4kin.teamcityapp.filter_builds.dagger.FilterBuildsModule
import com.github.vase4kin.teamcityapp.filter_builds.view.FilterBuildsActivity
import com.github.vase4kin.teamcityapp.home.dagger.HomeActivityBindingModule
import com.github.vase4kin.teamcityapp.home.dagger.HomeActivityScope
import com.github.vase4kin.teamcityapp.home.dagger.HomeModule
import com.github.vase4kin.teamcityapp.home.view.HomeActivity
import com.github.vase4kin.teamcityapp.navigation.dagger.*
import com.github.vase4kin.teamcityapp.navigation.view.NavigationActivity
import com.github.vase4kin.teamcityapp.navigation.view.NavigationListFragment
import com.github.vase4kin.teamcityapp.runbuild.dagger.RunBuildActivityScope
import com.github.vase4kin.teamcityapp.runbuild.dagger.RunBuildModule
import com.github.vase4kin.teamcityapp.runbuild.view.RunBuildActivity
import com.github.vase4kin.teamcityapp.testdetails.dagger.TestDetailsActivityScope
import com.github.vase4kin.teamcityapp.testdetails.dagger.TestDetailsModule
import com.github.vase4kin.teamcityapp.testdetails.view.TestDetailsActivity
import dagger.Module
import dagger.android.ContributesAndroidInjector

@Module
abstract class ActivityBindingModule {

    @FavoritesFragmentScope
    @ContributesAndroidInjector(modules = [FavoritesFragmentModule::class])
    abstract fun favoritesFragment(): FavoritesFragment

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

    @FilterBuildsActivityScope
    @ContributesAndroidInjector(modules = [FilterBuildsModule::class])
    abstract fun filterBuildsActivity(): FilterBuildsActivity

    @RunBuildActivityScope
    @ContributesAndroidInjector(modules = [RunBuildModule::class])
    abstract fun runBuildActivity(): RunBuildActivity

    @HomeActivityScope
    @ContributesAndroidInjector(modules = [HomeModule::class, BuildLogInteractorModule::class, HomeActivityBindingModule::class])
    abstract fun rootProjectsActivity(): HomeActivity

    @NavigationActivityScope
    @ContributesAndroidInjector(modules = [NavigationModule::class, NavigationBaseModule::class, NavigationDrawerModule::class])
    abstract fun navigationActivity(): NavigationActivity

    @NavigationFragmentScope
    @ContributesAndroidInjector(modules = [NavigationFragmentModule::class, NavigationBaseModule::class])
    abstract fun navigationListFragment(): NavigationListFragment
}
