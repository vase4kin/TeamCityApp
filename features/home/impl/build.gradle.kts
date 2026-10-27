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
    id("teamcityapp.android.screenshot")
}

kotlin { android { namespace = "teamcityapp.features.home.impl" } }

compose.resources {
    publicResClass = true
    packageOfResClass = "teamcityapp.features.home.impl.resources"
}
kotlin.sourceSets.named("commonMain") {
    dependencies {
        implementation(projects.libraries.network)
        implementation(projects.libraries.analytics)
        implementation(libs.multiplatform.lifecycle.runtime)
        api(projects.features.home.api)
        api(projects.libraries.designSystem)
        implementation(libs.multiplatform.lifecycle.viewmodel)
    }
}
kotlin.sourceSets.named("commonTest") { dependencies { implementation(libs.coroutines.test) } }

kotlin.sourceSets.named("androidHostTest") {
    dependencies {
        implementation(libs.coroutines.test)
        implementation(projects.features.home.api)
        implementation(projects.features.navigation.api)
        implementation(projects.features.favorites.api)
        implementation(projects.features.runningBuilds.api)
        implementation(projects.features.buildQueue.api)
        implementation(projects.features.agents.api)
        implementation(projects.features.drawer.api)
        implementation(projects.libraries.navigation)
        implementation(projects.libraries.theme)
        implementation(projects.libraries.resources)
        implementation(libs.androidx.activity.compose)
        implementation(libs.androidx.lifecycle.runtimeCompose)
        implementation(libs.androidx.lifecycle.lifeCycleViewModel)
        implementation(libs.compose.material3.windowSizeClass)
        implementation(libs.coroutines.android)
    }
}

roborazzi {
    outputDir.set(layout.projectDirectory.dir("src/test/screenshots/debug"))
    separateOutputDirs.set(false)
}
