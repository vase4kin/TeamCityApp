package com.github.vase4kin.teamcityapp.build_details.dagger

import com.github.vase4kin.teamcityapp.artifact.dagger.ArtifactFragmentScope
import com.github.vase4kin.teamcityapp.artifact.dagger.ArtifactsModule
import com.github.vase4kin.teamcityapp.artifact.view.ArtifactListFragment
import dagger.Module
import dagger.android.ContributesAndroidInjector

@Module
abstract class BuildDetailsActivityBindingModule {

    @ArtifactFragmentScope
    @ContributesAndroidInjector(modules = [ArtifactsModule::class])
    abstract fun artifactListFragment(): ArtifactListFragment
}