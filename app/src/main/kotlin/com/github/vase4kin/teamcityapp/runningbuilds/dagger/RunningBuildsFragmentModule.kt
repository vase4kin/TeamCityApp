/*
 * Copyright 2026 Andrey Tolpeev
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

package com.github.vase4kin.teamcityapp.runningbuilds.dagger

import android.app.Activity
import android.os.Bundle
import androidx.fragment.app.Fragment
import com.github.vase4kin.teamcityapp.R
import com.github.vase4kin.teamcityapp.api.Repository
import com.github.vase4kin.teamcityapp.api.TeamCityService
import com.github.vase4kin.teamcityapp.base.list.extractor.BaseValueExtractor
import com.github.vase4kin.teamcityapp.base.list.extractor.BaseValueExtractorImpl
import com.github.vase4kin.teamcityapp.base.list.view.BaseListView
import com.github.vase4kin.teamcityapp.base.list.view.SimpleSectionedRecyclerViewAdapter
import com.github.vase4kin.teamcityapp.base.list.view.ViewHolderFactory
import com.github.vase4kin.teamcityapp.buildlist.data.BuildInteractor
import com.github.vase4kin.teamcityapp.buildlist.data.BuildInteractorImpl
import com.github.vase4kin.teamcityapp.buildlist.data.BuildListDataModel
import com.github.vase4kin.teamcityapp.buildlist.router.BuildListRouter
import com.github.vase4kin.teamcityapp.buildlist.router.BuildListRouterImpl
import com.github.vase4kin.teamcityapp.buildlist.tracker.BuildListTracker
import com.github.vase4kin.teamcityapp.buildlist.tracker.FirebaseBuildListTrackerImpl
import com.github.vase4kin.teamcityapp.buildlist.view.BuildListAdapter
import com.github.vase4kin.teamcityapp.buildlist.view.BuildsViewHolderFactory
import com.github.vase4kin.teamcityapp.buildlist.view.LoadMoreViewHolderFactory
import com.github.vase4kin.teamcityapp.filter_bottom_sheet_dialog.filter.FilterProvider
import com.github.vase4kin.teamcityapp.runningbuilds.data.RunningBuildsDataManager
import com.github.vase4kin.teamcityapp.runningbuilds.data.RunningBuildsDataManagerImpl
import com.github.vase4kin.teamcityapp.runningbuilds.presenter.RunningBuildsListPresenterImpl
import com.github.vase4kin.teamcityapp.runningbuilds.view.RunningBuildListView
import com.github.vase4kin.teamcityapp.runningbuilds.view.RunningBuildsFragment
import com.github.vase4kin.teamcityapp.runningbuilds.view.RunningBuildsListViewImpl
import com.github.vase4kin.teamcityapp.storage.SharedUserStorage
import com.google.firebase.analytics.FirebaseAnalytics
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.FragmentComponent
import dagger.multibindings.IntKey
import dagger.multibindings.IntoMap
import javax.inject.Named
import org.greenrobot.eventbus.EventBus
import teamcityapp.libraries.onboarding.OnboardingManager
import teamcityapp.libraries.utils.requireScreenOwner

@Module
@InstallIn(FragmentComponent::class)
object RunningBuildsFragmentModule {

    @Provides
    fun provideOwner(owner: Fragment): RunningBuildsFragment = owner.requireScreenOwner<RunningBuildsFragment>()

    @Provides
    @Named("RunningBuildsFragment")
    fun providesRunningBuildsDataManager(repository: Repository, storage: SharedUserStorage): RunningBuildsDataManager = RunningBuildsDataManagerImpl(repository, storage)

    @Provides
    @Named("RunningBuildsFragment")
    fun providesRunningBuildListView(
        fragment: RunningBuildsFragment,
        @Named("RunningBuildsFragment") adapter: SimpleSectionedRecyclerViewAdapter<BuildListAdapter>,
        filterProvider: FilterProvider
    ): RunningBuildListView = RunningBuildsListViewImpl(
        fragment.requireView(),
        fragment.requireActivity(),
        R.string.empty_list_message_running_builds,
        adapter,
        filterProvider
    )

    @Provides
    @Named("RunningBuildsFragment")
    fun providesBuildListRouter(fragment: RunningBuildsFragment, runBuild: teamcityapp.features.run_build.api.navigation.RunBuildNavigation, filterBuilds: teamcityapp.features.filter_builds.api.navigation.FilterBuildsNavigation): BuildListRouter = BuildListRouterImpl(fragment.requireActivity(), runBuild, filterBuilds)

    @Provides
    @Named("RunningBuildsFragment")
    fun providesBuildListValueExtractor(): BaseValueExtractor = BaseValueExtractorImpl(Bundle.EMPTY)

    @Provides
    @Named("RunningBuildsFragment")
    fun providesBuildInteractor(teamCityService: TeamCityService): BuildInteractor = BuildInteractorImpl(teamCityService)

    @Provides
    @Named("RunningBuildsFragment")
    fun providesFirebaseBuildListTracker(firebaseAnalytics: FirebaseAnalytics): BuildListTracker = object : FirebaseBuildListTrackerImpl(firebaseAnalytics, "") {
        override fun trackView() {}
    }

    @Provides
    @Named("RunningBuildsFragment")
    fun providesSimpleSectionedRecyclerViewAdapter(
        fragment: RunningBuildsFragment,
        @Named("RunningBuildsFragment") adapter: BuildListAdapter
    ): SimpleSectionedRecyclerViewAdapter<BuildListAdapter> = SimpleSectionedRecyclerViewAdapter(fragment.requireContext(), adapter)

    @Provides
    @Named("RunningBuildsFragment")
    fun providesBuildListAdapter(@Named("RunningBuildsFragment") viewHolderFactories: Map<Int, @JvmSuppressWildcards ViewHolderFactory<BuildListDataModel>>): BuildListAdapter = BuildListAdapter(viewHolderFactories)

    @Provides
    @Named("RunningBuildsFragment")
    @IntoMap
    @IntKey(BaseListView.TYPE_LOAD_MORE)
    fun providesLoadMoreViewHolderFactory(): ViewHolderFactory<BuildListDataModel> = LoadMoreViewHolderFactory()

    @Provides
    @Named("RunningBuildsFragment")
    @IntoMap
    @IntKey(BaseListView.TYPE_DEFAULT)
    fun providesBuildViewHolderFactory(): ViewHolderFactory<BuildListDataModel> = BuildsViewHolderFactory()

    @Provides
    fun provideRunningBuildsListPresenterImpl(
        @Named("RunningBuildsFragment") view: RunningBuildListView,
        @Named("RunningBuildsFragment") dataManager: RunningBuildsDataManager,
        @Named("RunningBuildsFragment") tracker: BuildListTracker,
        @Named("RunningBuildsFragment") router: BuildListRouter,
        @Named("RunningBuildsFragment") valueExtractor: BaseValueExtractor,
        @Named("RunningBuildsFragment") buildInteractor: BuildInteractor,
        onboardingManager: OnboardingManager,
        filterProvider: FilterProvider,
        eventBus: EventBus
    ): RunningBuildsListPresenterImpl = RunningBuildsListPresenterImpl(
        view,
        dataManager,
        tracker,
        router,
        valueExtractor,
        buildInteractor,
        onboardingManager,
        filterProvider,
        eventBus
    )
}
