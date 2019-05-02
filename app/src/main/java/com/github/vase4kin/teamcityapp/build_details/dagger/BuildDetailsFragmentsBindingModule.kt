package com.github.vase4kin.teamcityapp.build_details.dagger

import com.github.vase4kin.teamcityapp.artifact.dagger.ArtifactFragmentScope
import com.github.vase4kin.teamcityapp.artifact.dagger.ArtifactsModule
import com.github.vase4kin.teamcityapp.artifact.view.ArtifactListFragment
import com.github.vase4kin.teamcityapp.buildlog.dagger.BuildLogFragmentScope
import com.github.vase4kin.teamcityapp.buildlog.dagger.BuildLogInteractorModule
import com.github.vase4kin.teamcityapp.buildlog.dagger.BuildLogModule
import com.github.vase4kin.teamcityapp.buildlog.view.BuildLogFragment
import com.github.vase4kin.teamcityapp.changes.dagger.ChangesFragmentScope
import com.github.vase4kin.teamcityapp.changes.dagger.ChangesModule
import com.github.vase4kin.teamcityapp.changes.view.ChangesFragment
import com.github.vase4kin.teamcityapp.overview.dagger.OverviewFragmentScope
import com.github.vase4kin.teamcityapp.overview.dagger.OverviewModule
import com.github.vase4kin.teamcityapp.overview.view.OverviewFragment
import dagger.Module
import dagger.android.ContributesAndroidInjector

@Module
abstract class BuildDetailsFragmentsBindingModule {

    @ArtifactFragmentScope
    @ContributesAndroidInjector(modules = [ArtifactsModule::class])
    abstract fun artifactListFragment(): ArtifactListFragment

    @BuildLogFragmentScope
    @ContributesAndroidInjector(modules = [BuildLogModule::class, BuildLogInteractorModule::class])
    abstract fun buildLogFragment(): BuildLogFragment

    @ChangesFragmentScope
    @ContributesAndroidInjector(modules = [ChangesModule::class])
    abstract fun changesFragment(): ChangesFragment

    @OverviewFragmentScope
    @ContributesAndroidInjector(modules = [OverviewModule::class])
    abstract fun overviewFragment(): OverviewFragment
}