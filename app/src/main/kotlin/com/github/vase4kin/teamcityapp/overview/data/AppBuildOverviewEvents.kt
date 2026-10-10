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

package com.github.vase4kin.teamcityapp.overview.data

import com.github.vase4kin.teamcityapp.build_details.data.OnOverviewRefreshDataEvent
import javax.inject.Inject
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import teamcityapp.features.build_overview.api.BuildOverviewEvents
import teamcityapp.features.build_overview.api.BuildOverviewRequest

class AppBuildOverviewEvents @Inject constructor(private val eventBus: EventBus) : BuildOverviewEvents {
    override val requests: Flow<BuildOverviewRequest> = callbackFlow {
        val subscriber = Subscriber { trySend(it) }
        eventBus.register(subscriber)
        awaitClose { eventBus.unregister(subscriber) }
    }
    class Subscriber internal constructor(private val request: (BuildOverviewRequest) -> Unit) {
        @Subscribe fun refresh(@Suppress("UNUSED_PARAMETER") event: OnOverviewRefreshDataEvent) = request(BuildOverviewRequest.Refresh)

        @Subscribe fun branch(event: NavigateToBuildListFilteredByBranchEvent) = request(BuildOverviewRequest.Branch(event.branchName))

        @Subscribe fun configuration(@Suppress("UNUSED_PARAMETER") event: NavigateToBuildListEvent) = request(BuildOverviewRequest.Configuration)

        @Subscribe fun project(@Suppress("UNUSED_PARAMETER") event: NavigateToProjectEvent) = request(BuildOverviewRequest.Project)
    }
}
