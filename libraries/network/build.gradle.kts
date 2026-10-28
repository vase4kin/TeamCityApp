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
kotlin.sourceSets {
    named("commonMain") {
        dependencies {
            api(projects.libraries.accounts)
            api(projects.libraries.builds)
            api(projects.libraries.buildConfigurations)
            api(libs.ktor.client.core)
            api(libs.kotlinx.serialization.json)
        }
    }
    named("androidMain") { dependencies { implementation(libs.ktor.client.okhttp) } }
    matching { it.name == "iosMain" }.configureEach { dependencies { implementation(libs.ktor.client.darwin) } }
    named("commonTest") {
        dependencies {
            implementation(libs.coroutines.test)
            implementation(libs.ktor.client.mock)
        }
    }
}

if (providers.gradleProperty("teamcityPlatformFixture").isPresent) {
    kotlin.sourceSets.named("commonTest") { kotlin.srcDir("src/platformTest/kotlin") }
}

tasks.withType<org.gradle.api.tasks.testing.Test>().configureEach {
    providers.gradleProperty("teamcityLiveServer").orNull?.let { systemProperty("teamcity.liveServer", it) }
    providers.gradleProperty("teamcityLiveEvidence").orNull?.let { systemProperty("teamcity.liveEvidence", it) }
}

kotlin.sourceSets.named("androidHostTest") {
    dependencies {
        implementation(libs.robolectric)
    }
}
