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
    alias(libs.plugins.kotlin.serialization)
}

kotlin { android { namespace = "teamcityapp.features.filter_bottom_sheet.impl" } }

compose.resources {
    publicResClass = true
    packageOfResClass = "teamcityapp.features.filter_bottom_sheet.impl.resources"
}
kotlin.sourceSets.named("commonMain") {
    dependencies {
        implementation(projects.libraries.accounts)
        implementation(projects.libraries.analytics)
        api(projects.features.filterBottomSheet.api)
        api(projects.libraries.designSystem)
        implementation(libs.multiplatform.compose.backhandler)
        implementation(libs.multiplatform.lifecycle.runtime)
        implementation(libs.multiplatform.lifecycle.viewmodel)
    }
}
kotlin.sourceSets.named("commonTest") { dependencies { implementation(libs.coroutines.test) } }

kotlin.sourceSets.named("androidMain") {
    dependencies {
        implementation(projects.libraries.navigation)

        implementation(projects.features.filterBottomSheet.api)
        implementation(projects.libraries.theme)
        implementation(projects.libraries.resources)
        implementation(libs.androidx.lifecycle.lifeCycleViewModel)
        implementation(libs.androidx.lifecycle.runtimeCompose)
        implementation(libs.coroutines.android)
    }
}
kotlin.sourceSets.named("androidHostTest") {
    dependencies {
        implementation(libs.coroutines.test)
        implementation(libs.turbine)
        implementation(libs.mockito.mockitoCore)
    }
}

roborazzi {
    outputDir.set(layout.projectDirectory.dir("src/test/screenshots/debug"))
    separateOutputDirs.set(false)
}
