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

package com.github.vase4kin.teamcityapp.queue.dagger

import teamcityapp.libraries.utils.requireScreenOwner
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
import com.github.vase4kin.teamcityapp.queue.data.BuildQueueDataManagerImpl
import com.github.vase4kin.teamcityapp.queue.presenter.QueueBuildsListPresenterImpl
import com.github.vase4kin.teamcityapp.queue.view.BuildQueueFragment
import com.github.vase4kin.teamcityapp.queue.view.BuildQueueViewImpl
import com.github.vase4kin.teamcityapp.runningbuilds.data.RunningBuildsDataManager
import com.github.vase4kin.teamcityapp.runningbuilds.view.RunningBuildListView
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

@Module
@InstallIn(FragmentComponent::class)
object BuildQueueFragmentModule {

    @Provides
    fun provideOwner(owner: Fragment): BuildQueueFragment = owner.requireScreenOwner<BuildQueueFragment>()

    @Provides
    @Named("BuildQueueFragment")
    fun providesRunningBuildsDataManager(repository: Repository, storage: SharedUserStorage): RunningBuildsDataManager {
        return BuildQueueDataManagerImpl(repository, storage)
    }

    @Provides
    @Named("BuildQueueFragment")
    fun providesRunningBuildListView(
        fragment: BuildQueueFragment,
        @Named("BuildQueueFragment") adapter: SimpleSectionedRecyclerViewAdapter<BuildListAdapter>,
        filterProvider: FilterProvider
    ): RunningBuildListView {
        return BuildQueueViewImpl(
            fragment.requireView(),
            fragment.requireActivity(),
            R.string.empty_list_message_build_queue,
            adapter,
            filterProvider
        )
    }

    @Provides
    @Named("BuildQueueFragment")
    fun providesBuildListRouter(fragment: BuildQueueFragment): BuildListRouter {
        return BuildListRouterImpl(fragment.requireActivity())
    }

    @Provides
    @Named("BuildQueueFragment")
    fun providesBuildListValueExtractor(): BaseValueExtractor {
        return BaseValueExtractorImpl(Bundle.EMPTY)
    }

    @Provides
    @Named("BuildQueueFragment")
    fun providesBuildInteractor(teamCityService: TeamCityService): BuildInteractor {
        return BuildInteractorImpl(teamCityService)
    }

    @Provides
    @Named("BuildQueueFragment")
    fun providesFirebaseBuildListTracker(firebaseAnalytics: FirebaseAnalytics): BuildListTracker {
        return object : FirebaseBuildListTrackerImpl(firebaseAnalytics, "") {
            override fun trackView() {}
        }
    }

    @Provides
    @Named("BuildQueueFragment")
    fun providesSimpleSectionedRecyclerViewAdapter(
        fragment: BuildQueueFragment,
        @Named("BuildQueueFragment") adapter: BuildListAdapter
    ): SimpleSectionedRecyclerViewAdapter<BuildListAdapter> {
        return SimpleSectionedRecyclerViewAdapter(fragment.requireContext(), adapter)
    }

    @Provides
    @Named("BuildQueueFragment")
    fun providesBuildListAdapter(@Named("BuildQueueFragment") viewHolderFactories: Map<Int, @JvmSuppressWildcards ViewHolderFactory<BuildListDataModel>>): BuildListAdapter {
        return BuildListAdapter(viewHolderFactories)
    }

    @Provides
    @Named("BuildQueueFragment")
    @IntoMap
    @IntKey(BaseListView.TYPE_LOAD_MORE)
    fun providesLoadMoreViewHolderFactory(): ViewHolderFactory<BuildListDataModel> {
        return LoadMoreViewHolderFactory()
    }

    @Provides
    @Named("BuildQueueFragment")
    @IntoMap
    @IntKey(BaseListView.TYPE_DEFAULT)
    fun providesBuildViewHolderFactory(): ViewHolderFactory<BuildListDataModel> {
        return BuildsViewHolderFactory()
    }

    @Provides
    fun provideQueueBuildsListPresenterImpl(
        @Named("BuildQueueFragment") view: RunningBuildListView,
        @Named("BuildQueueFragment") dataManager: RunningBuildsDataManager,
        @Named("BuildQueueFragment") tracker: BuildListTracker,
        @Named("BuildQueueFragment") router: BuildListRouter,
        @Named("BuildQueueFragment") valueExtractor: BaseValueExtractor,
        @Named("BuildQueueFragment") buildInteractor: BuildInteractor,
        onboardingManager: OnboardingManager,
        filterProvider: FilterProvider,
        eventBus: EventBus
    ): QueueBuildsListPresenterImpl =
        QueueBuildsListPresenterImpl(
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
