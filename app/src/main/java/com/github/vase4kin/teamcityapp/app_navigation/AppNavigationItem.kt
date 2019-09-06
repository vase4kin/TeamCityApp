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

package com.github.vase4kin.teamcityapp.app_navigation

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import com.github.vase4kin.teamcityapp.R

enum class AppNavigationItem(@DrawableRes val icon: Int, @StringRes val title: Int) {
    PROJECTS(R.drawable.ic_home_white_24dp, R.string.projects_drawer_item),
    FAVORITES(R.drawable.ic_favorite_white_24dp, R.string.favorites_drawer_item),
    RUNNING_BUILDS(R.drawable.ic_directions_run_white_24px, R.string.running_builds_drawer_item),
    BUILD_QUEUE(R.drawable.ic_layers_white_24dp, R.string.build_queue_drawer_item),
    AGENTS(R.drawable.ic_directions_railway_black_24dp, R.string.agents_drawer_item)
}
