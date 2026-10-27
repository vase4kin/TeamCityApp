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

kotlin { android { namespace = "teamcityapp.features.build_details.impl" } }

compose.resources {
    publicResClass = true
    packageOfResClass = "teamcityapp.features.build_details.impl.resources"
}
kotlin.sourceSets.named("commonMain") {
    dependencies {
        implementation(projects.features.changes.api)
        implementation(projects.features.tests.api)
        implementation(projects.libraries.network)
        implementation(projects.features.buildOverview.api)
        implementation(projects.libraries.analytics)
        api(projects.features.buildDetails.api)
        api(projects.libraries.designSystem)
        implementation(libs.multiplatform.compose.backhandler)
        implementation(libs.multiplatform.lifecycle.runtime)
        implementation(libs.multiplatform.lifecycle.viewmodel)
    }
}
kotlin.sourceSets.named("commonTest") {
    dependencies {
        implementation(libs.coroutines.test)
        implementation(projects.libraries.networkTesting)
    }
}

kotlin.sourceSets.named("androidHostTest") {
    dependencies {
        implementation(projects.features.bottomSheet.api)
        implementation(libs.coroutines.test)
        implementation(libs.mockito.mockitoCore)
        implementation(projects.libraries.onboarding)
        implementation(projects.libraries.session)
        implementation(projects.libraries.coroutines)
        implementation(projects.libraries.data)
        implementation(projects.libraries.navigation)
        implementation(projects.features.changes.api)
        implementation(projects.features.tests.api)
        implementation(projects.features.properties.api)
        implementation(projects.features.buildArtifacts.api)
        implementation(projects.features.snapshotDependencies.api)
        implementation(projects.features.buildHistory.api)
        implementation(projects.features.navigation.api)
        implementation(projects.features.filterBuilds.api)
        implementation(projects.features.buildDetails.api)
        implementation(projects.libraries.listState)
        implementation(projects.libraries.theme)
        implementation(libs.androidx.activity.compose)
        implementation(libs.androidx.lifecycle.runtimeCompose)
        implementation(libs.androidx.lifecycle.lifeCycleViewModel)
        implementation(libs.androidx.hilt.viewModelCompose)
        implementation(libs.compose.material3.windowSizeClass)
        implementation(libs.coroutines.android)
    }
}

roborazzi {
    outputDir.set(layout.projectDirectory.dir("src/test/screenshots/debug"))
    separateOutputDirs.set(false)
}
