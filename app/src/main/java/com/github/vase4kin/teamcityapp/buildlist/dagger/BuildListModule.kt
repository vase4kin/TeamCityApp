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

package com.github.vase4kin.teamcityapp.buildlist.dagger

import android.os.Bundle
import com.github.vase4kin.teamcityapp.R
import com.github.vase4kin.teamcityapp.api.Repository
import com.github.vase4kin.teamcityapp.api.TeamCityService
import com.github.vase4kin.teamcityapp.base.list.extractor.BaseValueExtractor
import com.github.vase4kin.teamcityapp.base.list.extractor.BaseValueExtractorImpl
import com.github.vase4kin.teamcityapp.base.list.view.SimpleSectionedRecyclerViewAdapter
import com.github.vase4kin.teamcityapp.buildlist.data.BuildInteractor
import com.github.vase4kin.teamcityapp.buildlist.data.BuildInteractorImpl
import com.github.vase4kin.teamcityapp.buildlist.data.BuildListDataManager
import com.github.vase4kin.teamcityapp.buildlist.data.BuildListDataManagerImpl
import com.github.vase4kin.teamcityapp.buildlist.router.BuildListRouter
import com.github.vase4kin.teamcityapp.buildlist.router.BuildListRouterImpl
import com.github.vase4kin.teamcityapp.buildlist.tracker.BuildListTracker
import com.github.vase4kin.teamcityapp.buildlist.tracker.FirebaseBuildListTrackerImpl
import com.github.vase4kin.teamcityapp.buildlist.view.BuildListActivity
import com.github.vase4kin.teamcityapp.buildlist.view.BuildListAdapter
import com.github.vase4kin.teamcityapp.buildlist.view.BuildListView
import com.github.vase4kin.teamcityapp.buildlist.view.BuildListViewImpl
import com.github.vase4kin.teamcityapp.storage.SharedUserStorage
import com.google.firebase.analytics.FirebaseAnalytics

import dagger.Module
import dagger.Provides

@Module
class BuildListModule {

    @Provides
    fun providesBuildListDataManager(repository: Repository,
                                     storage: SharedUserStorage): BuildListDataManager {
        return BuildListDataManagerImpl(repository, storage)
    }

    @Provides
    fun providesBuildListView(activity: BuildListActivity,
                              adapter: SimpleSectionedRecyclerViewAdapter<BuildListAdapter>): BuildListView {
        return BuildListViewImpl(activity.findViewById(android.R.id.content), activity, R.string.empty_list_message_builds, adapter)
    }

    @Provides
    fun providesBuildListRouter(activity: BuildListActivity): BuildListRouter {
        return BuildListRouterImpl(activity)
    }

    @Provides
    fun providesBuildListValueExtractor(activity: BuildListActivity): BaseValueExtractor {
        return BaseValueExtractorImpl(activity.intent.extras ?: Bundle.EMPTY)
    }

    @Provides
    fun providesBuildInteractor(teamCityService: TeamCityService): BuildInteractor {
        return BuildInteractorImpl(teamCityService)
    }

    @Provides
    fun providesFirebaseBuildListTracker(firebaseAnalytics: FirebaseAnalytics): BuildListTracker {
        return FirebaseBuildListTrackerImpl(firebaseAnalytics, BuildListTracker.SCREEN_NAME_BUILD_LIST)
    }
}
