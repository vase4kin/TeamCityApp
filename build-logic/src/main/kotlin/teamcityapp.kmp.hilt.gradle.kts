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

import com.android.build.api.dsl.KotlinMultiplatformAndroidLibraryTarget
import org.gradle.api.artifacts.VersionCatalogsExtension

plugins {
    id("teamcityapp.kmp.library")
    id("com.google.devtools.ksp")
}

// These features own Hilt ViewModels/modules, not AndroidEntryPoint framework hosts.
// The Android app retains the Hilt Gradle plugin and component aggregation.
(kotlin.targets.getByName("android") as KotlinMultiplatformAndroidLibraryTarget).withJava()
val libs = extensions.getByType<VersionCatalogsExtension>().named("libs")
dependencies {
    add("androidMainImplementation", libs.findLibrary("hilt-android").get())
    add("kspAndroid", libs.findLibrary("hilt-compiler").get())
    add("kspAndroid", libs.findLibrary("dagger-compiler").get())
}
