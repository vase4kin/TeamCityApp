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

plugins { id("teamcityapp.kmp.library") }
kotlin.sourceSets.named("commonMain") {
    dependencies {
        api(projects.features.runBuild.api)
        api(projects.features.filterBuilds.api)
        api(projects.libraries.network)
        implementation(projects.libraries.time)
        api(projects.features.buildDetails.api)
        api(projects.features.home.api)
        api(projects.features.filterBottomSheet.api)
        api(projects.features.favorites.api)
        api(projects.features.runningBuilds.api)
        api(projects.features.buildQueue.api)
        api(projects.features.agents.api)
        api(projects.features.changes.api)
        api(projects.features.tests.api)
        api(projects.features.testDetails.api)
        api(projects.features.snapshotDependencies.api)
        api(projects.libraries.authenticationShared)
        api(projects.features.navigation.api)
        api(projects.features.buildHistory.api)
        api(projects.features.buildOverview.api)
    }
}
kotlin.sourceSets.named("commonTest") {
    dependencies {
        implementation(libs.ktor.client.mock)
        implementation(libs.coroutines.test)
    }
}
