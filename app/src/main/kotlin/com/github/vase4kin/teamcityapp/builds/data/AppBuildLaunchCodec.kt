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

package com.github.vase4kin.teamcityapp.builds.data

import com.github.vase4kin.teamcityapp.buildlist.api.Build
import java.io.Serializable
import javax.inject.Inject
import teamcityapp.libraries.builds.BuildLaunchData
import teamcityapp.libraries.builds.compatibility.BuildLaunchCodec

/** Keeps the installed BuildDetails extra and persisted DTO class name compatible. */
class AppBuildLaunchCodec @Inject constructor(private val mapper: AppBuildLaunchMapper) : BuildLaunchCodec {
    override fun encode(build: BuildLaunchData): Serializable = mapper.toLegacyBuild(build)

    override fun decode(payload: Serializable): BuildLaunchData {
        require(payload is Build) { "Expected a legacy Build launch payload" }
        return mapper.toLaunchData(payload)
    }
}
