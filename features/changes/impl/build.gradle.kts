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
}

kotlin { android { namespace = "teamcityapp.features.changes.impl" } }

compose.resources {
    publicResClass = true
    packageOfResClass = "teamcityapp.features.changes.impl.resources"
}
kotlin.sourceSets.named("commonMain") {
    dependencies {
        api(projects.features.changes.api)
        api(projects.libraries.designSystem)
        api(projects.libraries.listPresentation)
        implementation(libs.multiplatform.lifecycle.viewmodel)
    }
}
kotlin.sourceSets.named("commonTest") {
    dependencies {
        implementation(libs.coroutines.test)
        implementation(libs.androidx.paging.testing)
    }
}

kotlin.sourceSets.named("commonMain") { dependencies { api(projects.features.changeDetails.api) } }

kotlin.sourceSets.named("androidMain") {
    dependencies {
        implementation(projects.libraries.data)
        implementation(projects.libraries.coroutines)
        implementation(libs.coroutines.rx2)
        implementation(projects.features.changeDetails.api)
        implementation(projects.libraries.navigation)
        implementation(projects.features.buildDetails.api)
        implementation(projects.features.changes.api)
        implementation(projects.libraries.listUi)
        implementation(projects.libraries.theme)
        implementation(libs.androidx.lifecycle.lifeCycleViewModel)
        implementation(libs.androidx.hilt.viewModelCompose)
        implementation(libs.androidx.lifecycle.runtimeCompose)
        implementation(libs.androidx.paging.common)
        implementation(libs.androidx.paging.compose)
        implementation(libs.coroutines.android)
    }
}
kotlin.sourceSets.named("androidHostTest") {
    dependencies {
        implementation(libs.mockito.mockitoCore)
        implementation(libs.mockito.mockitoKotlin)
        implementation(libs.retrofit.gsonConverter)
        implementation(libs.coroutines.test)
        implementation(libs.androidx.paging.testing)
    }
}

roborazzi {
    outputDir.set(layout.projectDirectory.dir("src/test/screenshots/debug"))
    separateOutputDirs.set(false)
}
