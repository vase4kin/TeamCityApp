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
    const val versionName = "2.0.0-beta.3" // x-release-please-version
    val versionCode = androidVersionCode(versionName)
    const val applicationId = "com.github.vase4kin.teamcityapp"
    val javaVersion = JavaVersion.VERSION_17
}

internal fun androidVersionCode(version: String): Int {
    val match = Regex("([1-9][0-9]*)\\.([0-9]+)\\.([0-9]+)(?:-beta\\.(0|[1-9][0-9]*))?")
        .matchEntire(version)
    requireNotNull(match) {
        "Release versions must be major.minor.patch with an optional -beta.N suffix"
    }
    val (major, minor, patch) = match.groupValues.drop(1).take(3).map { it.toIntOrNull() }
    require(major != null && major in 1..209 && minor != null && minor in 0..99 && patch != null && patch in 0..99) {
        "Release versions must fit the Android version code scheme"
    }
    val base = major * 10_000 + minor * 100 + patch
    val beta = match.groupValues[4].takeIf { it.isNotEmpty() }
    // Preserve the existing 1.x release codes. Starting with 2.x, reserve 999
    // increasing beta codes before each stable release so promotion is an upgrade.
    if (major == 1) {
        require(beta == null) { "Beta releases are supported starting with 2.0.0" }
        return base
    }
    val release = if (beta == null) {
        999
    } else {
        requireNotNull(beta.toIntOrNull()?.takeIf { it in 0..998 }) {
            "Beta numbers must be between 0 and 998"
        }
    }
    return base * 1_000 + release
}
