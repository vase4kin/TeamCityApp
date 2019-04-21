package com.github.vase4kin.teamcityapp.agenttabs.dagger

import com.github.vase4kin.teamcityapp.agents.dagger.AgentFragmentScope
import com.github.vase4kin.teamcityapp.agents.dagger.AgentModule
import com.github.vase4kin.teamcityapp.agents.view.AgentListFragment
import dagger.Module
import dagger.android.ContributesAndroidInjector

@Module
abstract class AgentsTabFragmentsBindingModule {

    @AgentFragmentScope
    @ContributesAndroidInjector(modules = [AgentModule::class])
    abstract fun agentListFragment(): AgentListFragment
}