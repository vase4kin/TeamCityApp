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

package com.github.vase4kin.teamcityapp.buildlog.dagger

import android.app.Activity
import com.github.vase4kin.teamcityapp.buildlog.data.BuildLogInteractor
import com.github.vase4kin.teamcityapp.buildlog.data.BuildLogInteractorImpl
import com.github.vase4kin.teamcityapp.buildlog.router.BuildLogRouter
import com.github.vase4kin.teamcityapp.buildlog.router.BuildLogRouterImpl
import com.github.vase4kin.teamcityapp.buildlog.view.BuildLogFragment
import com.github.vase4kin.teamcityapp.buildlog.view.BuildLogView
import com.github.vase4kin.teamcityapp.buildlog.view.BuildLogViewImpl
import com.github.vase4kin.teamcityapp.buildlog.view.BuildLogWebViewClient
import dagger.Module
import dagger.Provides
import teamcityapp.libraries.storage.Storage

@Module
object BuildLogModule {

    @JvmStatic
    @Provides
    fun providesBuildLogViewModel(
        fragment: BuildLogFragment,
        client: BuildLogWebViewClient
    ): BuildLogView {
        return BuildLogViewImpl(fragment.view!!, client)
    }

    @JvmStatic
    @Provides
    fun providesBuildLogRouter(fragment: BuildLogFragment): BuildLogRouter {
        return BuildLogRouterImpl(fragment.activity as Activity)
    }

    @JvmStatic
    @Provides
    fun providesBuildLogInteractor(
        fragment: BuildLogFragment,
        storage: Storage
    ): BuildLogInteractor {
        return BuildLogInteractorImpl(
            storage,
            fragment.requireContext(),
            fragment.arguments
        )
    }

    @JvmStatic
    @Provides
    fun providesBuildLogWebViewClient(): BuildLogWebViewClient {
        return BuildLogWebViewClient()
    }
}
