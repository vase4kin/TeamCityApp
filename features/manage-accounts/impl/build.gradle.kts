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

kotlin { android { namespace = "teamcityapp.features.manage_accounts.impl" } }

compose.resources {
    publicResClass = true
    packageOfResClass = "teamcityapp.features.manage_accounts.impl.resources"
}
kotlin.sourceSets {
    named("commonMain") {
        dependencies {
            implementation(projects.libraries.analytics)
            api(projects.features.manageAccounts.api)
            api(projects.libraries.authenticationShared)
            implementation(libs.multiplatform.lifecycle.viewmodel)
            implementation(libs.multiplatform.lifecycle.runtime)
            implementation(libs.coroutines.core)
            implementation(libs.multiplatform.compose.backhandler)
        }
    }
    named("commonTest") {
        dependencies {
            implementation(libs.coroutines.test)
            implementation(libs.turbine)
        }
    }
}

kotlin.sourceSets.named("androidMain") {
    dependencies {
        implementation(projects.libraries.coroutines)
        implementation(projects.libraries.session)
        implementation(projects.features.createAccount.api)
        implementation(projects.libraries.navigation)
        implementation(projects.features.manageAccounts.api)
        implementation(projects.libraries.theme)
        implementation(projects.libraries.resources)
        implementation(libs.androidx.activity.compose)
        implementation(libs.androidx.lifecycle.lifeCycleViewModel)
        implementation(libs.androidx.lifecycle.runtimeCompose)
        implementation(libs.coroutines.android)
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
