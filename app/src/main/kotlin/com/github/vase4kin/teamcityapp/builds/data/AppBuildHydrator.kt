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

package com.github.vase4kin.teamcityapp.builds.data

import com.github.vase4kin.teamcityapp.api.Repository
import com.github.vase4kin.teamcityapp.buildlist.api.Build
import javax.inject.Inject
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.rx2.await
import teamcityapp.libraries.builds.BuildLaunchData

/** One captured account session hydrates parallel requests without reordering server rows. */
class AppBuildHydrator @Inject constructor(private val mapper: AppBuildLaunchMapper) {
    suspend fun hydrate(session: Repository, summaries: List<Build>): List<BuildLaunchData> = coroutineScope {
        summaries.map { summary ->
            async {
                currentCoroutineContext().ensureActive()
                val detail = if (summary.state == "running") {
                    session.build(summary.href, true).await()
                } else {
                    val cached = session.build(summary.href, false).await()
                    currentCoroutineContext().ensureActive()
                    if ((summary.state == "finished") != (cached.state == "finished")) {
                        session.build(cached.href, true).await()
                    } else {
                        cached
                    }
                }
                currentCoroutineContext().ensureActive()
                mapper.toLaunchData(detail)
            }
        }.awaitAll().also { currentCoroutineContext().ensureActive() }
    }
}
