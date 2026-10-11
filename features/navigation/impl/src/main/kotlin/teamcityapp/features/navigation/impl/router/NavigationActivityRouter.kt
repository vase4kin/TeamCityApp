/*
 * Copyright 2019 Andrey Tolpeev
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

package teamcityapp.features.navigation.impl.router

import android.app.Activity
import dagger.hilt.android.scopes.ActivityScoped
import javax.inject.Inject
import teamcityapp.features.navigation.api.NavigationAppRouter
import teamcityapp.features.navigation.api.NavigationEntry
import teamcityapp.features.navigation.api.NavigationNavigation
import teamcityapp.features.navigation.impl.NavigationActivity
import teamcityapp.libraries.app_rating.AppRating
import teamcityapp.libraries.utils.requireScreenOwner

@ActivityScoped
class NavigationActivityRouter @Inject constructor(
    owner: Activity,
    private val navigation: NavigationNavigation,
    private val appRouter: NavigationAppRouter,
    private val rating: AppRating
) : NavigationRouter {
    private val activity = owner.requireScreenOwner<NavigationActivity>()
    override fun navigateUp() = activity.finish()
    override fun open(entry: NavigationEntry) {
        when (entry) {
            is NavigationEntry.Project -> navigation.open(activity, entry.reference)
            is NavigationEntry.Configuration -> appRouter.openBuildConfiguration(activity, entry.configuration)
        }
    }
    override fun openRating() = rating.open(activity)
}
