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
import com.github.vase4kin.teamcityapp.buildlist.api.Builds
import com.google.gson.Gson
import com.google.gson.JsonArray
import com.google.gson.JsonObject

internal fun draftBuild(id: String, state: String? = "finished", href: String = "/builds/$id", extra: String = ""): Build {
    val gson = Gson()
    return gson.fromJson("""{"id":"$id","href":"$href","state":${gson.toJson(state)}$extra}""", Build::class.java)
}

internal fun draftPage(rows: List<Build>, nextHref: String? = null, count: Int = rows.size): Builds {
    val gson = Gson()
    val json = JsonObject().apply {
        addProperty("count", count)
        if (nextHref != null) addProperty("nextHref", nextHref)
        add("build", JsonArray().apply { rows.forEach { add(gson.toJsonTree(it)) } })
    }
    return gson.fromJson(json, Builds::class.java)
}
