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

package com.github.vase4kin.teamcityapp.runbuild.dagger

import com.github.vase4kin.teamcityapp.api.Repository
import com.github.vase4kin.teamcityapp.runbuild.interactor.BranchesInteractor
import com.github.vase4kin.teamcityapp.runbuild.interactor.BranchesInteractorImpl
import com.github.vase4kin.teamcityapp.runbuild.interactor.EXTRA_BUILD_TYPE_ID
import com.github.vase4kin.teamcityapp.runbuild.interactor.RunBuildInteractor
import com.github.vase4kin.teamcityapp.runbuild.interactor.RunBuildInteractorImpl
import com.github.vase4kin.teamcityapp.runbuild.router.RunBuildRouter
import com.github.vase4kin.teamcityapp.runbuild.router.RunBuildRouterImpl
import com.github.vase4kin.teamcityapp.runbuild.tracker.RunBuildTracker
import com.github.vase4kin.teamcityapp.runbuild.tracker.RunBuildTrackerImpl
import com.github.vase4kin.teamcityapp.runbuild.view.BranchesComponentView
import com.github.vase4kin.teamcityapp.runbuild.view.BranchesComponentViewImpl
import com.github.vase4kin.teamcityapp.runbuild.view.RunBuildActivity
import com.github.vase4kin.teamcityapp.runbuild.view.RunBuildView
import com.github.vase4kin.teamcityapp.runbuild.view.RunBuildViewImpl
import com.google.firebase.analytics.FirebaseAnalytics
import dagger.Module
import dagger.Provides

@Module
class RunBuildModule {

    @Provides
    internal fun providesRunBuildView(activity: RunBuildActivity): RunBuildView {
        return RunBuildViewImpl(activity)
    }

    @Provides
    internal fun providesBranchesComponentView(activity: RunBuildActivity): BranchesComponentView {
        return BranchesComponentViewImpl(activity)
    }

    @Provides
    internal fun providesRunBuildInteractor(
        activity: RunBuildActivity,
        repository: Repository
    ): RunBuildInteractor {
        return RunBuildInteractorImpl(
            repository,
            activity.intent.getStringExtra(EXTRA_BUILD_TYPE_ID) ?: ""
        )
    }

    @Provides
    internal fun providesBranchesInteractor(
        activity: RunBuildActivity,
        repository: Repository
    ): BranchesInteractor {
        return BranchesInteractorImpl(
            repository,
            activity.intent.getStringExtra(EXTRA_BUILD_TYPE_ID) ?: ""
        )
    }

    @Provides
    internal fun providesRunBuildRouter(activity: RunBuildActivity): RunBuildRouter {
        return RunBuildRouterImpl(activity)
    }

    @Provides
    internal fun providesFirebaseRunBuildTracker(firebaseAnalytics: FirebaseAnalytics): RunBuildTracker {
        return RunBuildTrackerImpl(firebaseAnalytics)
    }
}
