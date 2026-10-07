/*
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

package teamcityapp.features.run_build.api

data class BuildAgent(val id: String, val name: String)
data class BuildParameter(val name: String, val value: String)
data class BuildRequest(val buildTypeId: String, val branch: String = "", val agent: BuildAgent? = null, val personal: Boolean = false, val queueAtTop: Boolean = false, val cleanSources: Boolean = true, val parameters: List<BuildParameter> = emptyList())
sealed interface QueueBuildResult {
    data class Success(val href: String) : QueueBuildResult
    data object Forbidden : QueueBuildResult
    data object Error : QueueBuildResult
}
interface RunBuildRepository {
    suspend fun branches(buildTypeId: String): List<String>
    suspend fun agents(buildTypeId: String): List<BuildAgent>
    suspend fun queue(request: BuildRequest): QueueBuildResult
}
const val BUILD_TYPE_ID = "BuildTypeId"
