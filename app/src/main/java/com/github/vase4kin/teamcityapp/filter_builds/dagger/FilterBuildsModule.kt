/*
 * Copyright 2016 Andrey Tolpeev
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.github.vase4kin.teamcityapp.filter_builds.dagger

import com.github.vase4kin.teamcityapp.api.Repository
import com.github.vase4kin.teamcityapp.filter_builds.router.FilterBuildsRouter
import com.github.vase4kin.teamcityapp.filter_builds.router.FilterBuildsRouterImpl
import com.github.vase4kin.teamcityapp.filter_builds.tracker.FilterBuildsTracker
import com.github.vase4kin.teamcityapp.filter_builds.tracker.FirebaseFilterBuildsTrackerImpl
import com.github.vase4kin.teamcityapp.filter_builds.view.FilterBuildsActivity
import com.github.vase4kin.teamcityapp.filter_builds.view.FilterBuildsView
import com.github.vase4kin.teamcityapp.filter_builds.view.FilterBuildsViewImpl
import com.github.vase4kin.teamcityapp.runbuild.interactor.BranchesInteractor
import com.github.vase4kin.teamcityapp.runbuild.interactor.BranchesInteractorImpl
import com.github.vase4kin.teamcityapp.runbuild.interactor.EXTRA_BUILD_TYPE_ID
import com.github.vase4kin.teamcityapp.runbuild.view.BranchesComponentView
import com.github.vase4kin.teamcityapp.runbuild.view.BranchesComponentViewImpl
import com.google.firebase.analytics.FirebaseAnalytics
import dagger.Module
import dagger.Provides

@Module
class FilterBuildsModule {

    @Provides
    fun providesFilterBuildsView(activity: FilterBuildsActivity): FilterBuildsView {
        return FilterBuildsViewImpl(activity)
    }

    @Provides
    fun providesBranchesComponentView(activity: FilterBuildsActivity): BranchesComponentView {
        return BranchesComponentViewImpl(activity)
    }

    @Provides
    fun providesBranchesInteractor(repository: Repository, activity: FilterBuildsActivity): BranchesInteractor {
        return BranchesInteractorImpl(repository, activity.intent.getStringExtra(EXTRA_BUILD_TYPE_ID))
    }

    @Provides
    fun providesFilterBuildsRouter(activity: FilterBuildsActivity): FilterBuildsRouter {
        return FilterBuildsRouterImpl(activity)
    }

    @Provides
    fun providesFirebaseFilterBuildsTracker(firebaseAnalytics: FirebaseAnalytics): FilterBuildsTracker {
        return FirebaseFilterBuildsTrackerImpl(firebaseAnalytics)
    }
}
