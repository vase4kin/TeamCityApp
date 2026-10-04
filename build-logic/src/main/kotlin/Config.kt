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

package teamcityapp.buildlogic

import org.gradle.api.JavaVersion

object Config {
    const val minSdk = 24
    const val compileSdk = 37
    const val targetSdk = 36
    const val versionName = "1.52.8" // x-release-please-version
    val versionCode = androidVersionCode(versionName)
    const val applicationId = "com.github.vase4kin.teamcityapp"
    val javaVersion = JavaVersion.VERSION_17

    object KotlinOptions {
        const val jvmTarget = "17"
    }
}

internal fun androidVersionCode(version: String): Int {
    val components = version.split('.').map { it.toIntOrNull() }
    require(components.size == 3 && components.all { it != null }) {
        "Release versions must contain numeric major, minor, and patch components"
    }
    val (major, minor, patch) = components.map { requireNotNull(it) }
    require(major in 1..9_999 && minor in 0..99 && patch in 0..99) {
        "Release versions must fit the Android version code scheme"
    }
    return major * 10_000 + minor * 100 + patch
}
