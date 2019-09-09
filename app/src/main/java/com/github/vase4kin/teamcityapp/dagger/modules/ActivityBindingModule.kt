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

import com.github.vase4kin.teamcityapp.about.AboutActivity
import com.github.vase4kin.teamcityapp.about.AboutFragment
import com.github.vase4kin.teamcityapp.about.dagger.AboutActivityScope
import com.github.vase4kin.teamcityapp.account.create.dagger.CreateAccountActivityScope
import com.github.vase4kin.teamcityapp.account.create.dagger.CreateAccountModule
import com.github.vase4kin.teamcityapp.account.create.view.CreateAccountActivity
import com.github.vase4kin.teamcityapp.account.manage.dagger.AccountListActivityScope
import com.github.vase4kin.teamcityapp.account.manage.dagger.AccountsModule
import com.github.vase4kin.teamcityapp.account.manage.view.AccountListActivity
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
import com.github.vase4kin.teamcityapp.navigation.dagger.NavigationActivityScope
import com.github.vase4kin.teamcityapp.navigation.dagger.NavigationBaseModule
import com.github.vase4kin.teamcityapp.navigation.dagger.NavigationFragmentModule
import com.github.vase4kin.teamcityapp.navigation.dagger.NavigationFragmentScope
import com.github.vase4kin.teamcityapp.navigation.dagger.NavigationModule
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

    @AboutActivityScope
    @ContributesAndroidInjector
    abstract fun aboutActivity(): AboutActivity

    @AboutActivityScope
    @ContributesAndroidInjector
    abstract fun aboutFragment(): AboutFragment

    @TestDetailsActivityScope
    @ContributesAndroidInjector(modules = [TestDetailsModule::class])
    abstract fun testDetailsActivity(): TestDetailsActivity

    @CreateAccountActivityScope
    @ContributesAndroidInjector(modules = [CreateAccountModule::class])
    abstract fun createAccountActivity(): CreateAccountActivity

    @AccountListActivityScope
    @ContributesAndroidInjector(modules = [AccountsModule::class])
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
    @ContributesAndroidInjector(modules = [NavigationModule::class, NavigationBaseModule::class])
    abstract fun navigationActivity(): NavigationActivity

    @NavigationFragmentScope
    @ContributesAndroidInjector(modules = [NavigationFragmentModule::class, NavigationBaseModule::class])
    abstract fun navigationListFragment(): NavigationListFragment
}
