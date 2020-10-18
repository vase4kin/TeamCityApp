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

package com.github.vase4kin.teamcityapp.app_navigation

import androidx.annotation.IdRes
import com.github.vase4kin.teamcityapp.R

enum class AppNavigationItem(@IdRes val id: Int) {
    PROJECTS(R.id.projects),
    FAVORITES(R.id.favorites),
    RUNNING_BUILDS(R.id.running_builds),
    BUILD_QUEUE(R.id.build_queue),
    AGENTS(R.id.agents);

    fun getIdByPosition(position: Int): Int? = values().getOrNull(position)?.id
}
