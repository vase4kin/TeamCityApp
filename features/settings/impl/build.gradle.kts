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
    id("teamcityapp.android.screenshot")
}

kotlin { android { namespace = "teamcityapp.features.settings.impl" } }

compose.resources {
    publicResClass = true
    packageOfResClass = "teamcityapp.features.settings.impl.resources"
}
kotlin.sourceSets.named("commonMain") {
    dependencies {
        implementation(projects.libraries.analytics)
        api(projects.features.settings.api)
        api(projects.libraries.themePreferences)
        api(projects.libraries.designSystem)
        implementation(libs.multiplatform.lifecycle.viewmodel)
    }
}
kotlin.sourceSets.named("commonTest") {
    dependencies {
        implementation(libs.coroutines.test)
        implementation(libs.turbine)
    }
}

kotlin.sourceSets.named("androidMain") {
    dependencies {
        implementation(projects.libraries.navigation)
        implementation(projects.features.settings.api)
        implementation(projects.libraries.appTheme)
        implementation(projects.libraries.theme)
        implementation(projects.libraries.resources)
        implementation(libs.androidx.activity.compose)
        implementation(libs.androidx.lifecycle.lifeCycleViewModel)
        implementation(libs.androidx.lifecycle.runtimeCompose)
    }
}
kotlin.sourceSets.named("androidHostTest") {
    dependencies {
        implementation(libs.mockito.mockitoCore)
        implementation(libs.coroutines.test)
        implementation(libs.turbine)
    }
}

roborazzi {
    outputDir.set(layout.projectDirectory.dir("src/test/screenshots/debug"))
    separateOutputDirs.set(false)
}
