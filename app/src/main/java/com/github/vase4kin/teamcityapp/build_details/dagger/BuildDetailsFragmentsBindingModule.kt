package com.github.vase4kin.teamcityapp.build_details.dagger

import com.github.vase4kin.teamcityapp.artifact.dagger.ArtifactFragmentScope
import com.github.vase4kin.teamcityapp.artifact.dagger.ArtifactsModule
import com.github.vase4kin.teamcityapp.artifact.view.ArtifactListFragment
import com.github.vase4kin.teamcityapp.buildlist.dagger.BuildListAdapterModule
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
import com.github.vase4kin.teamcityapp.properties.dagger.PropertiesFragmentScope
import com.github.vase4kin.teamcityapp.properties.dagger.PropertiesModule
import com.github.vase4kin.teamcityapp.properties.view.PropertiesFragment
import com.github.vase4kin.teamcityapp.snapshot_dependencies.dagger.SnapshotDependenciesFragmentScope
import com.github.vase4kin.teamcityapp.snapshot_dependencies.dagger.SnapshotDependenciesModule
import com.github.vase4kin.teamcityapp.snapshot_dependencies.view.SnapshotDependenciesFragment
import com.github.vase4kin.teamcityapp.tests.dagger.TestsFragmentScope
import com.github.vase4kin.teamcityapp.tests.dagger.TestsModule
import com.github.vase4kin.teamcityapp.tests.view.TestOccurrencesFragment
import dagger.Module
import dagger.android.ContributesAndroidInjector

@Module
abstract class BuildDetailsFragmentsBindingModule {

    @OverviewFragmentScope
    @ContributesAndroidInjector(modules = [OverviewModule::class])
    abstract fun overviewFragment(): OverviewFragment

    @ChangesFragmentScope
    @ContributesAndroidInjector(modules = [ChangesModule::class])
    abstract fun changesFragment(): ChangesFragment

    @TestsFragmentScope
    @ContributesAndroidInjector(modules = [TestsModule::class])
    abstract fun testOccurrencesFragment(): TestOccurrencesFragment

    @BuildLogFragmentScope
    @ContributesAndroidInjector(modules = [BuildLogModule::class, BuildLogInteractorModule::class])
    abstract fun buildLogFragment(): BuildLogFragment

    @PropertiesFragmentScope
    @ContributesAndroidInjector(modules = [PropertiesModule::class])
    abstract fun propertiesFragment(): PropertiesFragment

    @ArtifactFragmentScope
    @ContributesAndroidInjector(modules = [ArtifactsModule::class])
    abstract fun artifactListFragment(): ArtifactListFragment

    @SnapshotDependenciesFragmentScope
    @ContributesAndroidInjector(modules = [SnapshotDependenciesModule::class, BuildListAdapterModule::class])
    abstract fun snapshotDependenciesFragment(): SnapshotDependenciesFragment
}