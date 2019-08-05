package com.github.vase4kin.teamcityapp.home.dagger

import com.github.vase4kin.teamcityapp.buildlist.dagger.BuildListAdapterModule
import com.github.vase4kin.teamcityapp.filter_bottom_sheet_dialog.dagger.FilterBottomSheetDialogScope
import com.github.vase4kin.teamcityapp.filter_bottom_sheet_dialog.view.FilterBottomSheetDialogFragment
import com.github.vase4kin.teamcityapp.queue.dagger.BuildQueueFragmentModule
import com.github.vase4kin.teamcityapp.queue.dagger.BuildQueueFragmentScope
import com.github.vase4kin.teamcityapp.queue.view.BuildQueueFragment
import com.github.vase4kin.teamcityapp.runningbuilds.dagger.RunningBuildsFragmentModule
import com.github.vase4kin.teamcityapp.runningbuilds.dagger.RunningBuildsFragmentScope
import com.github.vase4kin.teamcityapp.runningbuilds.view.RunningBuildsFragment
import dagger.Module
import dagger.android.ContributesAndroidInjector

@Module
abstract class HomeActivityBindingModule {

    @RunningBuildsFragmentScope
    @ContributesAndroidInjector(modules = [RunningBuildsFragmentModule::class, BuildListAdapterModule::class])
    abstract fun runningBuildsFragment(): RunningBuildsFragment

    @BuildQueueFragmentScope
    @ContributesAndroidInjector(modules = [BuildQueueFragmentModule::class, BuildListAdapterModule::class])
    abstract fun buildQueueFragment(): BuildQueueFragment

    @FilterBottomSheetDialogScope
    @ContributesAndroidInjector
    abstract fun filterBottomSheetDialog(): FilterBottomSheetDialogFragment
}
