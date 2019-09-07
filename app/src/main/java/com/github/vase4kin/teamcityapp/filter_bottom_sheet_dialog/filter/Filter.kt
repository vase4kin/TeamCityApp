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

package com.github.vase4kin.teamcityapp.filter_bottom_sheet_dialog.filter

enum class Filter(val code: Int) {
    RUNNING_ALL(0), RUNNING_FAVORITES(1),
    QUEUE_ALL(2), QUEUE_FAVORITES(3),
    AGENTS_CONNECTED(4), AGENTS_DISCONNECTED(5);

    val isRunning: Boolean
        get() = this == RUNNING_ALL || this == RUNNING_FAVORITES

    val isQueued: Boolean
        get() = this == QUEUE_ALL || this == QUEUE_FAVORITES

    val isAgents: Boolean
        get() = this == AGENTS_CONNECTED || this == AGENTS_DISCONNECTED

    fun opposite(): Filter {
        return when (this) {
            RUNNING_ALL -> RUNNING_FAVORITES
            RUNNING_FAVORITES -> RUNNING_ALL
            QUEUE_ALL -> QUEUE_FAVORITES
            QUEUE_FAVORITES -> QUEUE_ALL
            AGENTS_CONNECTED -> AGENTS_DISCONNECTED
            AGENTS_DISCONNECTED -> AGENTS_CONNECTED
        }
    }
}
