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

import android.content.Context
import android.os.Bundle
import com.github.vase4kin.teamcityapp.buildlog.data.BuildLogInteractor
import com.github.vase4kin.teamcityapp.buildlog.data.BuildLogInteractorImpl
import com.github.vase4kin.teamcityapp.buildlog.extractor.BuildLogValueExtractor
import com.github.vase4kin.teamcityapp.buildlog.extractor.BuildLogValueExtractorImpl
import com.github.vase4kin.teamcityapp.buildlog.router.BuildLogRouter
import com.github.vase4kin.teamcityapp.buildlog.router.BuildLogRouterImpl
import com.github.vase4kin.teamcityapp.buildlog.urlprovider.BuildLogUrlProvider
import com.github.vase4kin.teamcityapp.buildlog.view.BuildLogFragment
import com.github.vase4kin.teamcityapp.buildlog.view.BuildLogView
import com.github.vase4kin.teamcityapp.buildlog.view.BuildLogViewImpl
import com.github.vase4kin.teamcityapp.buildlog.view.BuildLogWebViewClient
import com.github.vase4kin.teamcityapp.storage.SharedUserStorage
import dagger.Module
import dagger.Provides

/**
 * Build log module with testing support
 */
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
    fun providesBuildLogValueExtractor(fragment: BuildLogFragment): BuildLogValueExtractor {
        return BuildLogValueExtractorImpl(fragment.arguments ?: Bundle.EMPTY)
    }

    /**
     * Provides fake html to make build log testable
     */
    @JvmStatic
    @Provides
    fun providesUrlProvider(
        buildLogValueExtractor: BuildLogValueExtractor,
        sharedUserStorage: SharedUserStorage
    ): BuildLogUrlProvider {
        return object : BuildLogUrlProvider {
            override fun provideUrl(): String {
                return "file:///android_asset/fake_build_log.html"
            }
        }
    }

    @JvmStatic
    @Provides
    fun providesBuildLogRouter(fragment: BuildLogFragment): BuildLogRouter {
        return BuildLogRouterImpl(fragment.requireActivity())
    }

    @JvmStatic
    @Provides
    fun providesBuildLogInteractor(
        fragment: BuildLogFragment,
        sharedUserStorage: SharedUserStorage
    ): BuildLogInteractor {
        return BuildLogInteractorImpl(
            sharedUserStorage.activeUser,
            fragment.requireContext().getSharedPreferences(
                BuildLogInteractorImpl.PREF_NAME,
                Context.MODE_PRIVATE
            )
        )
    }

    @JvmStatic
    @Provides
    fun providesBuildLogWebViewClient(): BuildLogWebViewClient {
        return BuildLogWebViewClient()
    }
}
