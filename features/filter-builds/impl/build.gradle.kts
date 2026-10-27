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

plugins {
    id("teamcityapp.kmp.compose")
    id("teamcityapp.kmp.hilt")
    id("teamcityapp.android.screenshot")
    alias(libs.plugins.kotlin.serialization)
}

kotlin { android { namespace = "teamcityapp.features.filter_builds.impl" } }

compose.resources {
    publicResClass = true
    packageOfResClass = "teamcityapp.features.filter_builds.impl.resources"
}
kotlin.sourceSets.named("commonMain") {
    dependencies {
        implementation(projects.libraries.network)
        implementation(projects.libraries.analytics)
        api(projects.features.filterBuilds.api)
        api(projects.libraries.designSystem)
        implementation(libs.multiplatform.compose.backhandler)
        implementation(libs.multiplatform.lifecycle.runtime)
        implementation(libs.multiplatform.lifecycle.viewmodel)
    }
}
kotlin.sourceSets.named("commonTest") { dependencies { implementation(libs.coroutines.test) } }

kotlin.sourceSets.named("androidHostTest") {
    dependencies {
        implementation(libs.coroutines.test)
        implementation(libs.turbine)
        implementation(libs.mockito.mockitoCore)
        implementation(projects.libraries.data)
        implementation(projects.libraries.navigation)
        implementation(projects.features.filterBuilds.api)
        implementation(libs.androidx.activity.compose)
        implementation(projects.libraries.theme)
        implementation(libs.androidx.lifecycle.lifeCycleViewModel)
        implementation(libs.androidx.hilt.viewModelCompose)
        implementation(libs.androidx.lifecycle.runtimeCompose)
        implementation(libs.coroutines.android)
    }
}

roborazzi {
    outputDir.set(layout.projectDirectory.dir("src/test/screenshots/debug"))
    separateOutputDirs.set(false)
}
