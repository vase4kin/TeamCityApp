/*
 * Copyright 2020 Andrey Tolpeev
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

import com.github.vase4kin.teamcityapp.account.create.dagger.CreateAccountActivityScope
import com.github.vase4kin.teamcityapp.account.create.dagger.CreateAccountModule
import com.github.vase4kin.teamcityapp.account.create.view.CreateAccountActivity
import com.github.vase4kin.teamcityapp.artifact.dagger.ArtifactActivityScope
import com.github.vase4kin.teamcityapp.artifact.dagger.ArtifactsActivityModule
import com.github.vase4kin.teamcityapp.artifact.view.ArtifactListActivity
import com.github.vase4kin.teamcityapp.build_details.dagger.BuildDetailsActivityScope
import com.github.vase4kin.teamcityapp.build_details.dagger.BuildDetailsFragmentsBindingModule
import com.github.vase4kin.teamcityapp.build_details.dagger.BuildDetailsModule
import com.github.vase4kin.teamcityapp.build_details.view.BuildDetailsActivity
import com.github.vase4kin.teamcityapp.buildlist.dagger.BuildListActivityScope
import com.github.vase4kin.teamcityapp.buildlist.dagger.BuildListAdapterModule
import com.github.vase4kin.teamcityapp.buildlist.dagger.BuildListModule
import com.github.vase4kin.teamcityapp.buildlist.view.BuildListActivity
import com.github.vase4kin.teamcityapp.dagger.modules.about.AboutRepositoryModule
import com.github.vase4kin.teamcityapp.dagger.modules.manage_accounts.ManageAccountsRouterModule
import com.github.vase4kin.teamcityapp.dagger.modules.test_details.TestDetailsRepositoryModule
import com.github.vase4kin.teamcityapp.filter_builds.dagger.FilterBuildsActivityScope
import com.github.vase4kin.teamcityapp.filter_builds.dagger.FilterBuildsModule
import com.github.vase4kin.teamcityapp.filter_builds.view.FilterBuildsActivity
import com.github.vase4kin.teamcityapp.home.dagger.HomeActivityBindingModule
import com.github.vase4kin.teamcityapp.home.dagger.HomeActivityScope
import com.github.vase4kin.teamcityapp.home.dagger.HomeModule
import com.github.vase4kin.teamcityapp.home.view.HomeActivity
import com.github.vase4kin.teamcityapp.navigation.dagger.NavigationActivityScope
import com.github.vase4kin.teamcityapp.navigation.dagger.NavigationBaseModule
import com.github.vase4kin.teamcityapp.navigation.dagger.NavigationModule
import com.github.vase4kin.teamcityapp.navigation.view.NavigationActivity
import com.github.vase4kin.teamcityapp.runbuild.dagger.RunBuildActivityScope
import com.github.vase4kin.teamcityapp.runbuild.dagger.RunBuildModule
import com.github.vase4kin.teamcityapp.runbuild.view.RunBuildActivity
import dagger.Module
import dagger.android.ContributesAndroidInjector
import teamcityapp.features.about.AboutActivity
import teamcityapp.features.about.dagger.AboutActivityBindingModule
import teamcityapp.features.about.dagger.AboutActivityScope
import teamcityapp.features.change.dagger.ChangeActivityModule
import teamcityapp.features.change.dagger.ChangeActivityScope
import teamcityapp.features.change.view.ChangeActivity
import teamcityapp.features.manage_accounts.dagger.ManageAccountsActivityScope
import teamcityapp.features.manage_accounts.dagger.ManageAccountsModule
import teamcityapp.features.manage_accounts.view.ManageAccountsActivity
import teamcityapp.features.settings.dagger.SettingsActivityBindingModule
import teamcityapp.features.settings.dagger.SettingsActivityModule
import teamcityapp.features.settings.dagger.SettingsActivityScope
import teamcityapp.features.settings.view.SettingsActivity
import teamcityapp.features.test_details.dagger.TestDetailsActivityScope
import teamcityapp.features.test_details.dagger.TestDetailsModule
import teamcityapp.features.test_details.view.TestDetailsActivity

@Module
abstract class ActivityBindingModule {

    @AboutActivityScope
    @ContributesAndroidInjector(
        modules = [
            AboutActivityBindingModule::class, AboutRepositoryModule::class]
    )
    abstract fun aboutActivity(): AboutActivity

    @TestDetailsActivityScope
    @ContributesAndroidInjector(
        modules = [
            TestDetailsModule::class, TestDetailsRepositoryModule::class
        ]
    )
    abstract fun testDetailsActivity(): TestDetailsActivity

    @ChangeActivityScope
    @ContributesAndroidInjector(modules = [ChangeActivityModule::class])
    abstract fun changeActivity(): ChangeActivity

    @CreateAccountActivityScope
    @ContributesAndroidInjector(modules = [CreateAccountModule::class])
    abstract fun createAccountActivity(): CreateAccountActivity

    @ManageAccountsActivityScope
    @ContributesAndroidInjector(
        modules = [
            ManageAccountsModule::class, ManageAccountsRouterModule::class
        ]
    )
    abstract fun accountListActivity(): ManageAccountsActivity

    @BuildDetailsActivityScope
    @ContributesAndroidInjector(
        modules = [
            BuildDetailsModule::class, BuildDetailsFragmentsBindingModule::class
        ]
    )
    abstract fun buildDetailsActivity(): BuildDetailsActivity

    @BuildListActivityScope
    @ContributesAndroidInjector(modules = [BuildListModule::class, BuildListAdapterModule::class])
    abstract fun buildListActivity(): BuildListActivity

    @FilterBuildsActivityScope
    @ContributesAndroidInjector(modules = [FilterBuildsModule::class])
    abstract fun filterBuildsActivity(): FilterBuildsActivity

    @RunBuildActivityScope
    @ContributesAndroidInjector(modules = [RunBuildModule::class])
    abstract fun runBuildActivity(): RunBuildActivity

    @HomeActivityScope
    @ContributesAndroidInjector(modules = [HomeModule::class, HomeActivityBindingModule::class])
    abstract fun homeActivity(): HomeActivity

    @NavigationActivityScope
    @ContributesAndroidInjector(modules = [NavigationModule::class, NavigationBaseModule::class])
    abstract fun navigationActivity(): NavigationActivity

    @ArtifactActivityScope
    @ContributesAndroidInjector(modules = [ArtifactsActivityModule::class])
    abstract fun artifactListActivity(): ArtifactListActivity

    @SettingsActivityScope
    @ContributesAndroidInjector(
        modules = [
            SettingsActivityModule::class, SettingsActivityBindingModule::class
        ]
    )
    abstract fun settingsActivity(): SettingsActivity
}
