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

package com.github.vase4kin.teamcityapp.agents.data

import com.github.vase4kin.teamcityapp.base.loading.OnLoadingListener

/** Compatibility adapter for the legacy Home agent-tab count. */
interface AgentsDataManager {
    fun loadCount(loadingListener: OnLoadingListener<Int>, includeDisconnected: Boolean? = false)
    fun unsubscribe()
}
