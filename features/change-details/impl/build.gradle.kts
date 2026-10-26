/*
 * Copyright 2020 Andrey Tolpeev
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
}

kotlin { android { namespace = "teamcityapp.features.change_details.impl" } }

compose.resources {
    publicResClass = true
    packageOfResClass = "teamcityapp.features.change_details.impl.resources"
}
kotlin.sourceSets.named("commonMain") {
    dependencies {
        api(projects.features.changeDetails.api)
        api(projects.libraries.designSystem)
        implementation(libs.multiplatform.lifecycle.viewmodel)
    }
}
kotlin.sourceSets.named("commonTest") { dependencies { implementation(libs.coroutines.test) } }

kotlin.sourceSets.named("androidMain") {
    dependencies {
        implementation(projects.libraries.navigation)
        implementation(projects.features.changeDetails.api)
        implementation(projects.libraries.resources)
        implementation(projects.libraries.theme)
        implementation(projects.libraries.chromeTabs)
        implementation(projects.libraries.storage)
        implementation(projects.libraries.storageModels)
        implementation(projects.libraries.api)
        implementation(libs.androidx.activity.compose)
        implementation(libs.androidx.lifecycle.lifeCycleViewModel)
        implementation(libs.androidx.hilt.viewModelCompose)
        implementation(libs.androidx.lifecycle.runtimeCompose)
        implementation(libs.coroutines.android)
        implementation(project.dependencies.platform(libs.google.firebaseBom))
        implementation(libs.google.analytics)
    }
}
kotlin.sourceSets.named("androidHostTest") {
    dependencies {
        implementation(libs.mockito.mockitoCore)
    }
}

roborazzi {
    outputDir.set(layout.projectDirectory.dir("src/test/screenshots/debug"))
    separateOutputDirs.set(false)
}
