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

import com.android.build.api.dsl.KotlinMultiplatformAndroidHostTestCompilation

plugins {
    id("teamcityapp.kmp.compose")
    alias(libs.plugins.kotlin.serialization)
}
kotlin {
    android {
        compilations.named("hostTest") {
            (this as KotlinMultiplatformAndroidHostTestCompilation).isIncludeAndroidResources = true
        }
    }
    sourceSets {
        named("commonMain") {
            dependencies {
                api(libs.androidx.navigation3.runtime)
                api(libs.kotlinx.serialization.json)
                implementation(libs.multiplatform.navigation3.ui)
                implementation(libs.multiplatform.lifecycle.viewModelNavigation3)
                implementation(libs.multiplatform.lifecycle.viewmodel)
                implementation(libs.multiplatform.lifecycle.runtime)
            }
        }
        named("androidHostTest") {
            dependencies {
                implementation(libs.robolectric)
                implementation(libs.compose.ui.testJunit4)
                implementation(libs.compose.ui.testManifest)
            }
        }
    }
}
