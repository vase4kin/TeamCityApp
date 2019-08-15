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

package com.github.vase4kin.teamcityapp.navigation.dagger

import android.app.Activity
import android.os.Bundle
import com.github.vase4kin.teamcityapp.R
import com.github.vase4kin.teamcityapp.navigation.extractor.NavigationValueExtractor
import com.github.vase4kin.teamcityapp.navigation.extractor.NavigationValueExtractorImpl
import com.github.vase4kin.teamcityapp.navigation.router.NavigationRouter
import com.github.vase4kin.teamcityapp.navigation.router.NavigationRouterImpl
import com.github.vase4kin.teamcityapp.navigation.view.NavigationAdapter
import com.github.vase4kin.teamcityapp.navigation.view.NavigationListFragment
import com.github.vase4kin.teamcityapp.navigation.view.NavigationView
import com.github.vase4kin.teamcityapp.navigation.view.NavigationViewImpl

import dagger.Module
import dagger.Provides

@Module
class NavigationFragmentModule {

    @Provides
    fun providesNavigationView(fragment: NavigationListFragment, adapter: NavigationAdapter): NavigationView {
        return NavigationViewImpl(fragment.view, fragment.activity as Activity, R.string.empty_list_message_projects_or_build_types, adapter)
    }

    @Provides
    fun providesNavigationValueExtractor(fragment: NavigationListFragment): NavigationValueExtractor {
        return NavigationValueExtractorImpl(fragment.arguments ?: Bundle.EMPTY)
    }

    @Provides
    fun providesNavigationRouter(fragment: NavigationListFragment): NavigationRouter {
        return NavigationRouterImpl(fragment.activity as Activity)
    }
}
